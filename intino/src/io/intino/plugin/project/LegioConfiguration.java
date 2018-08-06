package io.intino.plugin.project;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.intino.legio.graph.*;
import io.intino.legio.graph.Repository.Release;
import io.intino.legio.graph.level.LevelArtifact.Model;
import io.intino.plugin.dependencyresolution.DependencyPurger;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.tara.StashBuilder;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.io.Stash;
import io.intino.tara.io.StashDeserializer;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.plugin.codeinsight.languageinjection.imports.Imports;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.TaraElementFactory;
import io.intino.tara.plugin.lang.psi.TaraModel;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraNodeImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static io.intino.tara.compiler.shared.TaraBuildConstants.WORKING_PACKAGE;
import static java.util.stream.Collectors.toMap;

public class LegioConfiguration implements Configuration {
	private static final Logger LOG = Logger.getInstance(LegioConfiguration.class.getName());
	private final Module module;
	private VirtualFile legioFile;
	private LegioGraph graph;

	public LegioConfiguration(Module module) {
		this.module = module;
	}

	public Configuration init() {
		this.legioFile = new LegioFileCreator(module).getOrCreate();
		initConfiguration();
		return this;
	}

	public boolean inited() {
		return graph != null;
	}

	public LegioGraph graph() {
		return graph;
	}

	public Module module() {
		return module;
	}

	private void initConfiguration() {
		File stashFile = stashFile();
		this.graph = (!stashFile.exists()) ? newGraphFromLegio() : GraphLoader.loadGraph(StashDeserializer.stashFrom(stashFile), stashFile());
		if (graph == null && stashFile.exists()) stashFile.delete();
		final ConfigurationReloader reloader = new ConfigurationReloader(module, graph);
		reloader.reloadInterfaceBuilder();
		reloader.resolveLanguages();
		reloader.reloadArtifactoriesMetaData();
		if (graph != null && graph.serverList().isEmpty()) new CesarReloader(this.module.getProject()).reload();
		if (WebModuleType.isWebModule(module) && this.graph != null)
			new GulpExecutor(this.module, graph.artifact()).startGulpDev();
		if (graph != null && graph.artifact() != null) graph.artifact().save$();
	}

	public boolean isSuitable() {
		return new File(new File(module.getModuleFilePath()).getParentFile(), LegioFileType.LEGIO_FILE).exists();
	}

	public void purgeAndReload() {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
		withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Purging and loading Configuration", false, ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 if (legioFile == null) legioFile = new LegioFileCreator(module).getOrCreate();
						 graph = newGraphFromLegio();
						 new DependencyPurger(module, LegioConfiguration.this).execute();
						 final ConfigurationReloader reloader = new ConfigurationReloader(module, graph);
						 reloader.reloadInterfaceBuilder();
						 reloader.reloadDependencies();
						 if (graph != null && graph.artifact() != null) graph.artifact().save$();
					 }
				 }
		);
	}

	public void reload() {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
		if (module.isDisposed() || module.getProject().isDisposed()) return;
		withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Reloading Artifact", false, ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 if (legioFile == null) legioFile = new LegioFileCreator(module).getOrCreate();
						 graph = newGraphFromLegio();
						 final ConfigurationReloader reloader = new ConfigurationReloader(module, graph);
						 reloader.reloadInterfaceBuilder();
						 reloader.reloadDependencies();
						 reloader.reloadArtifactoriesMetaData();
						 reloader.reloadRunConfigurations();
						 if (graph != null && graph.serverList().isEmpty()) new CesarReloader(module.getProject()).reload();
						 if (graph != null && graph.artifact() != null) graph.artifact().save$();
					 }
				 }
		);
	}

	public static String parametersOf(io.intino.legio.graph.RunConfiguration runConfiguration) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> argument : runConfiguration.finalArguments().entrySet())
			builder.append("\"").append(argument.getKey()).append("=").append(argument.getValue()).append("\" ");
		return builder.toString();
	}

	public void addParameters(String... parameters) {
		if (parameters.length == 0) return;
		new WriteCommandAction(module.getProject(), legioFile()) {
			protected void run(@NotNull Result result) {
				final Node artifactNode = ((TaraModel) legioFile()).components().stream().filter(c -> c.type().equals(Artifact.class.getSimpleName())).findFirst().orElse(null);
				if (artifactNode == null) return;
				Arrays.stream(parameters).forEach(p -> addParameter((TaraNode) artifactNode, p));
			}
		}.execute();
	}

	public void addCompileDependencies(List<String> ids) {
		if (ids.isEmpty()) return;
		final FileDocumentManager documentManager = FileDocumentManager.getInstance();
		final Document document = Objects.requireNonNull(documentManager.getDocument(legioFile));
		documentManager.saveDocument(document);
		PsiDocumentManager.getInstance(module.getProject()).commitDocument(document);
		new WriteCommandAction(module.getProject(), legioFile()) {
			protected void run(@NotNull Result result) {
				final Node artifactNode = ((TaraModel) legioFile()).components().stream().filter(c -> c.type().equals(Artifact.class.getSimpleName())).findFirst().orElse(null);
				if (artifactNode == null) return;
				Node imports = artifactNode.components().stream().filter(c -> c.type().endsWith(Imports.class.getSimpleName())).findFirst().orElse(null);
				if (imports == null) imports = createImports();
				Node finalImports = imports;
				ids.forEach(i -> {
					final String[] split = i.split(":");
					addCompileDependency((TaraNode) finalImports, split[0], split[1], split[2]);
				});
			}
		}.execute();
		documentManager.saveDocument(document);
		PsiDocumentManager.getInstance(module.getProject()).commitDocument(document);
	}

	public void updateCompileDependencies(List<String> ids) {
		if (ids.isEmpty()) return;
		final FileDocumentManager documentManager = FileDocumentManager.getInstance();
		final Document document = Objects.requireNonNull(documentManager.getDocument(legioFile));
		documentManager.saveDocument(document);
		PsiDocumentManager.getInstance(module.getProject()).commitDocument(document);
		new WriteCommandAction(module.getProject(), legioFile()) {
			protected void run(@NotNull Result result) {
				final Node artifactNode = ((TaraModel) legioFile()).components().stream().filter(c -> c.type().equals(Artifact.class.getSimpleName())).findFirst().orElse(null);
				if (artifactNode == null) return;
				Node imports = artifactNode.components().stream().filter(c -> c.type().endsWith(Imports.class.getSimpleName())).findFirst().orElse(null);
				if (imports == null) return;
				ids.forEach(i -> {
					final String[] split = i.split(":");
					Node node = findDependency(imports, split);
					if (node == null) return;
					node.parameters().get(node.parameters().size() - 1).substituteValues(Collections.singletonList(split[2]));
				});
			}
		}.execute();
		documentManager.saveDocument(document);
		PsiDocumentManager.getInstance(module.getProject()).commitDocument(document);
	}

	private Node findDependency(Node imports, String[] ids) {
		for (Node node : imports.components()) {
			if (parameterValue("groupId", node.parameters()).equals(ids[0]) && parameterValue("artifactId", node.parameters()).equals(ids[1]))
				return node;
		}
		return null;
	}

	private String parameterValue(String parameterName, List<Parameter> parameters) {
		return parameters.stream().filter(parameter -> parameter.name().equals(parameterName)).findFirst().map(parameter -> parameter.values().get(0).toString()).orElse("");
	}

	private Node createImports() {
		return null;//TODO
	}

	private void addCompileDependency(TaraNode imports, String groupId, String artifactId, String version) {
		TaraElementFactory factory = TaraElementFactory.getInstance(module.getProject());
		Node node = factory.createFullNode("Compile(groupId = \"" + groupId + "\", artifactId = \"" + artifactId + "\", version = \"" + version + "\")");
		node.type("Artifact.Imports.Compile");
		((TaraNodeImpl) node).getSignature().getLastChild().getPrevSibling().delete();
		PsiElement last = null;
		if (imports.components().isEmpty()) {
			imports.add(factory.createBodyNewLine(2));
			last = imports;
		} else last = (PsiElement) imports.components().get(imports.components().size() - 1);
		PsiElement separator = imports.addAfter(factory.createBodyNewLine(2), last);
		imports.addAfter((PsiElement) node, separator);
	}

	private void addParameter(TaraNode artifactNode, String p) {
		TaraElementFactory factory = TaraElementFactory.getInstance(module.getProject());
		Node node = factory.createFullNode("Parameter(name = \"" + p + "\")");
		node.type("Artifact.Parameter");
		((TaraNodeImpl) node).getSignature().getLastChild().getPrevSibling().delete();
		final PsiElement last = (PsiElement) artifactNode.components().get(artifactNode.components().size() - 1);
		PsiElement separator = artifactNode.addAfter(factory.createBodyNewLine(), last);
		artifactNode.addAfter((PsiElement) node, separator);
	}

	private LegioGraph newGraphFromLegio() {
		Stash legioStash = loadNewConfiguration();
		return legioStash == null ? null : GraphLoader.loadGraph(legioStash, stashFile());
	}

	private Stash loadNewConfiguration() {
		try {
			return new StashBuilder(Collections.singletonMap(new File(legioFile.getPath()), legioFile.getCharset()), new tara.dsl.Legio(), module.getName(), System.out).build();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	@NotNull
	private File stashFile() {
		return new File(LanguageManager.getMiscDirectory(module.getProject()).getPath(), module.getName() + ".conf");
	}

	public List<Repository.Type> repositoryTypes() {
		List<Repository.Type> repos = new ArrayList<>();
		for (Repository r : safeList(() -> graph.repositoryList())) repos.addAll(r.typeList());
		return repos;
	}

	public Level level() {
		if (graph == null || graph.artifact() == null) return null;
		final Artifact artifact = graph().artifact();
		if (artifact == null) return null;
		final String level = artifact.core$().conceptList().stream().filter(c -> c.id().contains("#")).map(c -> c.id().split("#")[0]).findFirst().orElse(null);
		return level == null ? null : Level.valueOf(level);
	}

	public String artifactId() {
		return safe(() -> graph.artifact().name$());
	}

	public String groupId() {
		return safe(() -> graph.artifact().groupId());
	}

	public String version() {
		return safe(() -> graph.artifact().version());
	}

	public void version(String version) {
		reload();//TODO
	}

	public String workingPackage() {
		return safe(() -> graph.artifact().code().targetPackage(), groupId() + "." + artifactId());
	}

	public String nativeLanguage() {
		return "java";
	}

	public List<? extends LanguageLibrary> languages() {
		Model model = safe(() -> graph.artifact().asLevel().model());
		if (model == null) return Collections.emptyList();
		return Collections.singletonList(new LanguageLibrary() {
			public String name() {
				return model.language();
			}

			public String version() {
				return model.version();
			}

			public String effectiveVersion() {
				return model.effectiveVersion();
			}

			public void version(String version) {
				final Application application = ApplicationManager.getApplication();
				TaraModel psiFile = !application.isReadAccessAllowed() ?
						(TaraModel) application.runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(module.getProject()).findFile(legioFile)) :
						(TaraModel) PsiManager.getInstance(module.getProject()).findFile(legioFile);
				new WriteCommandAction(module.getProject(), psiFile) {
					protected void run(@NotNull Result result) {
						model.version(version);
						final Node model = psiFile.components().get(0).components().stream().filter(f -> f.type().equals("LevelArtifact.Model")).findFirst().orElse(null);
						if (model == null) return;
						final Parameter versionParameter = model.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
						versionParameter.substituteValues(Collections.singletonList(version));
					}
				}.execute();
//				reload();
			}

			public String generationPackage() {
				Attributes attributes = languageParameters();
				return attributes == null ? null : attributes.getValue(WORKING_PACKAGE.replace(".", "-"));
			}
		});
	}

	public Attributes languageParameters() {
		final Model model = safe(() -> graph.artifact().asLevel().model());
		if (model == null) return null;
		final File languageFile = LanguageManager.getLanguageFile(model.language(), model.effectiveVersion());
		if (!languageFile.exists()) return null;
		try {
			Manifest manifest = new JarFile(languageFile).getManifest();
			return manifest == null ? null : manifest.getAttributes("tara");
		} catch (IOException e) {
			return null;
		}
	}

	public Map<String, String> repositories() {
		Map<String, String> repositories = new HashMap<>();
		graph.repositoryList().forEach(r -> repositories.putAll(r.typeList().stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID))));
		return repositories;
	}

	public Map<String, String> releaseRepositories() {
		Map<String, String> repositories = new HashMap<>();
		safeList(() -> graph.repositoryList()).forEach(r -> repositories.putAll(r.typeList(t -> t.i$(Release.class)).stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID))));
		return repositories;
	}


	public Map<String, String> snapshotRepositories() {
		Map<String, String> repositories = new HashMap<>();
		safeList(() -> graph.repositoryList()).forEach(r -> repositories.putAll(r.typeList(t -> t.i$(Repository.Snapshot.class)).stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID))));
		return repositories;
	}

	public AbstractMap.SimpleEntry<String, String> distributionLanguageRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<>
				(graph.artifact().distribution().language().url(), graph.artifact().distribution().language().mavenID()));
	}

	public AbstractMap.SimpleEntry<String, String> distributionReleaseRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<>
				(graph.artifact().distribution().release().url(), graph.artifact().distribution().release().mavenID()));
	}

	public Map<String, String> languageRepositories() {
		if (graph == null) return new HashMap<>();
		Map<String, String> repositories = new HashMap<>();
		for (Repository r : graph.repositoryList()) {
			final List<Repository.Type> types = r.typeList(t -> t != null && t.i$(Repository.Language.class) && t.mavenID() != null && t.url() != null);
			if (types.isEmpty()) continue;
			repositories.putAll(types.stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID)));
		}
		return repositories;
	}

	public String outDSL() {
		return safe(() -> graph.artifact().name$());
	}

	public String boxPackage() {
		return safe(() -> graph.artifact().box().targetPackage());
	}

	@Deprecated
	public Model model() {
		return safe(() -> graph.artifact().asLevel().model());
	}

	public List<DeployConfiguration> deployConfigurations() {
		final List<Artifact.Deployment> deploys = safeList(() -> safe(() -> graph.artifact().deploymentList()));
		if (deploys == null || deploys.isEmpty()) return Collections.emptyList();
		return deploys.stream().flatMap(deploy -> deploy.destinations().stream()).map(this::createDeployConfiguration).collect(Collectors.toList());
	}

	@NotNull
	private DeployConfiguration createDeployConfiguration(final Destination deployment) {
		return new DeployConfiguration() {
			public String name() {
				return deployment.name$();
			}

			public boolean pro() {
				return deployment.i$(Artifact.Deployment.Pro.class);
			}

			public List<Parameter> parameters() {
				return deployment.runConfiguration().argumentList().stream().map(this::wrapParameter).collect(Collectors.toList()); //TODO merge with defaultValues
			}

			@NotNull
			private Parameter wrapParameter(final Argument p) {
				return new Parameter() {
					public String name() {
						return p.name();
					}

					public String value() {
						return p.value();
					}
				};
			}
		};
	}

	public String boxVersion() {
		return safe(() -> graph.artifact().box().sdk());
	}

	public PsiFile legioFile() {
		return PsiManager.getInstance(module.getProject()).findFile(legioFile);
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}
}
