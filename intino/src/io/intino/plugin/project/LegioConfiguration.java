package io.intino.plugin.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
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
import io.intino.cesar.box.schemas.ProjectInfo;
import io.intino.legio.graph.*;
import io.intino.legio.graph.Repository.Release;
import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.dependencyresolution.DependencyCatalog.DependencyScope;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.configuration.LegioBox;
import io.intino.plugin.project.configuration.LegioDeployConfiguration;
import io.intino.plugin.project.configuration.LegioModel;
import io.intino.tara.StashBuilder;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.io.Stash;
import io.intino.tara.io.StashDeserializer;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.plugin.codeinsight.languageinjection.imports.Imports;
import io.intino.tara.plugin.lang.psi.TaraElementFactory;
import io.intino.tara.plugin.lang.psi.TaraModel;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraNodeImpl;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.repository.RepositoryPolicy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;
import static io.intino.legio.graph.Artifact.Imports.Dependency;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static java.util.stream.Collectors.toMap;

public class LegioConfiguration implements Configuration {
	private static final Logger LOG = Logger.getInstance(LegioConfiguration.class.getName());
	private final Module module;
	private VirtualFile legioFile;
	private LegioGraph graph;
	private DependencyAuditor dependencyAuditor;
	private boolean reloading = false;

	public LegioConfiguration(Module module) {
		this.module = module;
		this.dependencyAuditor = new DependencyAuditor(module);
	}

	public static String parametersOf(io.intino.legio.graph.RunConfiguration runConfiguration) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> argument : runConfiguration.finalArguments().entrySet())
			builder.append("\"").append(argument.getKey()).append("=").append(argument.getValue()).append("\" ");
		return builder.toString();
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

	public boolean isSuitable() {
		return new File(new File(module.getModuleFilePath()).getParentFile(), LegioFileType.LEGIO_FILE).exists();
	}

	public void purgeAndReload() {
		if (reloading) return;
		reloading = true;
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
		withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Purging and loading Configuration", false, ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 reloading = true;
						 if (legioFile == null) legioFile = new LegioFileCreator(module).getOrCreate();
						 LegioConfiguration.this.graph = newGraphFromLegio();
//						 new DependencyPurger(module, LegioConfiguration.this).execute();
						 final ConfigurationReloader reloader = new ConfigurationReloader(module, dependencyAuditor, graph, RepositoryPolicy.UPDATE_POLICY_ALWAYS);
						 reloader.reloadInterfaceBuilder();
						 reloader.reloadDependencies();
						 if (graph != null && graph.artifact() != null) graph.artifact().save$();
						 reloading = false;
					 }
				 }
		);
		reloading = false;
	}

	public void reload() {
		if (reloading) return;
		reloading = true;
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
		if (module.isDisposed() || module.getProject().isDisposed()) return;
		withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Reloading Artifact", false, ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 reloading = true;
						 if (legioFile == null) legioFile = new LegioFileCreator(module).getOrCreate();
						 LegioConfiguration.this.graph = newGraphFromLegio();
						 final ConfigurationReloader reloader = new ConfigurationReloader(module, dependencyAuditor, graph, RepositoryPolicy.UPDATE_POLICY_DAILY);
						 reloader.reloadInterfaceBuilder();
						 reloader.reloadDependencies();
						 reloader.reloadArtifactoriesMetaData();
						 reloader.reloadRunConfigurations();
						 if (graph != null && !graph.serverList().isEmpty())
							 new CesarAccessor(module.getProject()).projectInfo();
						 if (graph != null && graph.artifact() != null) graph.artifact().save$();
						 reloading = false;
					 }
				 }
		);
		reloading = false;
	}

	public void addParameters(String... parameters) {
		if (parameters.length == 0) return;
		writeCommandAction(module.getProject(), legioFile()).run(() -> {
			final Node artifactNode = ((TaraModel) legioFile()).components().stream().filter(c -> c.type().equals(Artifact.class.getSimpleName())).findFirst().orElse(null);
			if (artifactNode == null) return;
			Arrays.stream(parameters).forEach(p -> addParameter((TaraNode) artifactNode, p));
		});
	}

	public void addDependency(DependencyScope scope, String id) {
		if (id == null || id.isEmpty() || !id.contains(":") || alreadyExists(scope, id)) return;
		final FileDocumentManager documentManager = FileDocumentManager.getInstance();
		final Document document = Objects.requireNonNull(documentManager.getDocument(legioFile));
		documentManager.saveDocument(document);
		PsiDocumentManager.getInstance(module.getProject()).commitDocument(document);
		writeCommandAction(module.getProject(), legioFile()).run(() -> {
			final Node artifactNode = ((TaraModel) legioFile()).components().stream().filter(c -> c.type().equals(Artifact.class.getSimpleName())).findFirst().orElse(null);
			if (artifactNode == null) return;
			Node imports = artifactNode.components().stream().filter(c -> c.type().endsWith(Imports.class.getSimpleName())).findFirst().orElse(null);
			if (imports == null) imports = createImports();
			Node finalImports = imports;
			if (finalImports == null) return;
			final String[] split = id.split(":");
			addDependency((TaraNode) finalImports, scope, split[0], split[1], split[2]);
		});
		documentManager.saveDocument(document);
		PsiDocumentManager.getInstance(module.getProject()).commitDocument(document);
	}

	private boolean alreadyExists(DependencyScope scope, String id) {
		for (Dependency d : safeList(() -> graph.artifact().imports().dependencyList()))
			if (d.identifier().equals(id) && scope.label().equals(d.getClass().getSimpleName())) return true;
		for (Artifact.Imports.Web d : safeList(() -> graph.artifact().imports().webList()))
			if (d.identifier().equals(id) && scope.label().equals(d.getClass().getSimpleName())) return true;
		return false;
	}

	public void updateCompileDependencies(List<String> ids) {
		if (ids.isEmpty()) return;
		final FileDocumentManager documentManager = FileDocumentManager.getInstance();
		final Document document = Objects.requireNonNull(documentManager.getDocument(legioFile));
		documentManager.saveDocument(document);
		PsiDocumentManager.getInstance(module.getProject()).commitDocument(document);
		writeCommandAction(module.getProject(), legioFile()).run(() -> {
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
		});
		documentManager.saveDocument(document);
		PsiDocumentManager.getInstance(module.getProject()).commitDocument(document);
	}

	private void initConfiguration() {
		File stashFile = stashFile();
		if (!stashFile.exists()) this.graph = newGraphFromLegio();
		else {
			Stash stash = StashDeserializer.stashFrom(stashFile);
			dependencyAuditor.reload(stash);
			this.graph = GraphLoader.loadGraph(stash, stashFile());
		}
		if (graph == null && stashFile.exists()) stashFile.delete();
		final ConfigurationReloader reloader = new ConfigurationReloader(module, dependencyAuditor, graph, RepositoryPolicy.UPDATE_POLICY_DAILY);
		reloader.reloadInterfaceBuilder();
		reloader.reloadLanguage();
		reloader.reloadArtifactoriesMetaData();
		if (graph != null && !graph.serverList().isEmpty()) {
			ProjectInfo info = new CesarAccessor(this.module.getProject()).projectInfo();
			if (info != null) new ProcessOutputLoader(module.getProject()).loadOutputs(info.processInfos());
		}
		if (graph != null && graph.artifact() != null) graph.artifact().save$();
	}

	public DependencyAuditor dependencyAuditor() {
		return dependencyAuditor;
	}

	public List<Repository.Type> repositoryTypes() {
		List<Repository.Type> repos = new ArrayList<>();
		for (Repository r : safeList(() -> graph.repositoryList())) repos.addAll(r.typeList());
		return repos;
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

	public List<RunConfiguration> runConfigurations() {
		return safeList(() -> graph.runConfigurationList());
	}

	public void version(String version) {
		reload();//TODO
	}

	@Override
	public LegioModel model() {
		if (safe(() -> graph.artifact().asLevel()) == null) return null;
		return new LegioModel(module, legioFile, graph.artifact().asLevel().model());
	}

	@Override
	public LegioBox box() {
		Artifact.Box safe = safe(() -> graph.artifact().box());
		return safe == null ? null : new LegioBox(safe);
	}

	public String workingPackage() {
		return safe(() -> graph.artifact().code().targetPackage(),
				(groupId() + "." + artifactId()).replace("-", "").replace("_", ""));
	}

	public String nativeLanguage() {
		return "java";
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

	public List<DeployConfiguration> deployConfigurations() {
		final List<Artifact.Deployment> deploys = safeList(() -> safe(() -> graph.artifact().deploymentList()));
		if (deploys == null || deploys.isEmpty()) return Collections.emptyList();
		return deploys.stream().flatMap(deploy -> deploy.destinations().stream()).map(this::createDeployConfiguration).collect(Collectors.toList());
	}

	@NotNull
	private DeployConfiguration createDeployConfiguration(final Destination deployment) {
		return new LegioDeployConfiguration(deployment);
	}

	public PsiFile legioFile() {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return PsiManager.getInstance(module.getProject()).findFile(legioFile);
		return application.runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(module.getProject()).findFile(legioFile));
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	private Node findDependency(Node imports, String[] ids) {
		return imports.components().stream().filter(node -> parameterValue("groupId", node.parameters()).equals(ids[0]) && parameterValue("artifactId", node.parameters()).equals(ids[1])).findFirst().orElse(null);
	}

	private String parameterValue(String parameterName, List<Parameter> parameters) {
		return parameters.stream().filter(parameter -> parameter.name().equals(parameterName)).findFirst().map(parameter -> parameter.values().get(0).toString()).orElse("");
	}

	private Node createImports() {
		return null;//TODO
	}

	private void addDependency(TaraNode imports, DependencyScope type, String groupId, String artifactId, String version) {
		TaraElementFactory factory = TaraElementFactory.getInstance(module.getProject());
		Node node = factory.createFullNode(type.label() + "(groupId = \"" + groupId + "\", artifactId = \"" + artifactId + "\", version = \"" + version + "\")");
		node.type("Artifact.Imports." + type.label());
		((TaraNodeImpl) node).getSignature().getLastChild().getPrevSibling().delete();
		PsiElement last;
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
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Stash stash = loadNewLegio(new PrintStream(out));
		if (stash == null) {
			processCompilerOutput(out.toString());
			return null;
		}
		dependencyAuditor.reload(stash);
		return GraphLoader.loadGraph(stash, stashFile());
	}

	private void processCompilerOutput(String out) {
		String[] lines = out.split("\n");
		String prefix = "%%merror#%%#%%%#%%%%%%%%%#";
		Arrays.stream(lines).filter(line -> line.startsWith(prefix)).
				map(line -> line.replace(prefix, "").replace("#%%#%%%#%%%%%%%%%#", " ").replace("/%m", "")).
				findFirst().ifPresent(message -> Bus.notify(new Notification("Tara Language", "Error parsing Intino configuration", message, NotificationType.ERROR), this.module.getProject()));
	}

	private Stash loadNewLegio(PrintStream printStream) {
		try {
			return new StashBuilder(Collections.singletonMap(new File(legioFile.getPath()), legioFile.getCharset()), new tara.dsl.Legio(), module.getName(), printStream).build();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	@NotNull
	private File stashFile() {
		return new File(IntinoDirectory.artifactsDirectory(module.getProject()).getPath(), module.getName() + ".conf");
	}
}
