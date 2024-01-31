package io.intino.plugin.project.configuration.model;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.Configuration.Parameter;
import io.intino.konos.compiler.shared.KonosBuildConstants;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraMogramImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioDependency.*;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static io.intino.konos.compiler.shared.KonosBuildConstants.ARTIFACT_ID;
import static io.intino.konos.compiler.shared.KonosBuildConstants.GROUP_ID;
import static io.intino.konos.compiler.shared.KonosBuildConstants.LANGUAGE;
import static io.intino.konos.compiler.shared.KonosBuildConstants.LEVEL;
import static io.intino.konos.compiler.shared.KonosBuildConstants.VERSION;
import static io.intino.konos.compiler.shared.KonosBuildConstants.*;
import static io.intino.plugin.archetype.Formatters.firstUpperCase;
import static io.intino.plugin.archetype.Formatters.snakeCaseToCamelCase;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.read;
import static io.intino.tara.builder.shared.TaraBuildConstants.*;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class LegioArtifact implements Configuration.Artifact {
	private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(LegioArtifact.class.getName());

	public static final String EQ = "=";
	public static final String NL = "\n";
	private final ArtifactLegioConfiguration root;
	private final TaraMogram mogram;
	private String groupId;
	private String name;
	private String version;

	public LegioArtifact(ArtifactLegioConfiguration root, TaraMogram mogram) {
		this.root = root;
		this.mogram = mogram;
	}

	@Override
	public String groupId() {
		return groupId == null ? groupId = parameterValue(mogram, "groupId", 0) : groupId;
	}

	@Override
	public String name() {
		if (mogram == null) return null;
		return name == null ? name = mogram.name() : name;
	}

	@Override
	public void name(String newName) {
		writeCommandAction(mogram.getProject(), mogram.getContainingFile()).run(() -> mogram.name(newName));
		ApplicationManager.getApplication().invokeAndWait(() -> IntinoUtil.commitDocument(mogram.getContainingFile()));
	}

	@Override
	public String version() {
		return version == null ? version = parameterValue(mogram, "version", 1) : version;
	}

	@Override
	public String description() {
		return parameterValue(mogram, "description", 2);
	}

	@Override
	public String url() {
		return parameterValue(mogram, "url", 2);
	}

	@Override
	public void version(String newVersion) {
		writeCommandAction(mogram.getProject(), mogram.getContainingFile()).run(() -> {
			io.intino.tara.language.model.Parameter version = mogram.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(mogram.parameters().get(1));
			if (version != null) version.substituteValues(singletonList(newVersion));
		});
		ApplicationManager.getApplication().invokeAndWait(() -> IntinoUtil.commitDocument(mogram.getContainingFile()));
	}

	@Override
	@NotNull
	public Code code() {
		return new LegioCode(this, (TaraMogram) TaraPsiUtil.componentOfType(mogram, "Code"));
	}

	@Override
	@Nullable
	public Model model() {
		TaraMogram model = (TaraMogram) TaraPsiUtil.componentOfType(mogram, "Model");
		return model == null ? null : new LegioModel(this, model);
	}

	@Override
	public Dependency.DataHub datahub() {
		Mogram dataHub = TaraPsiUtil.componentOfType(mogram, "DataHub");
		if (dataHub == null) return null;
		return new LegioDataHub(this, (TaraMogram) dataHub);
	}

	@Override
	public Dependency.Archetype archetype() {
		Mogram archetype = TaraPsiUtil.componentOfType(mogram, "Archetype");
		if (archetype == null) return null;
		return new LegioArchetype(this, (TaraMogram) archetype);
	}

	@Override
	public List<Dependency> dependencies() {
		Mogram imports = TaraPsiUtil.componentOfType(mogram, "Imports");
		if (imports == null) return Collections.emptyList();
		List<Mogram> nodes = TaraPsiUtil.componentsOf(imports);
		nodes.addAll(stream(((TaraMogram) imports).getChildren()).filter(c -> c instanceof Mogram).map(c -> (Mogram) c).toList());
		List<Dependency> dependencies = new ArrayList<>();
		for (Mogram dependency : nodes) {
			if (((TaraMogramImpl) dependency).simpleType().equals("Compile"))
				dependencies.add(new LegioCompile((TaraMogram) dependency));
			else if (((TaraMogramImpl) dependency).simpleType().equals("Test"))
				dependencies.add(new LegioTest((TaraMogram) dependency));
			else if (((TaraMogramImpl) dependency).simpleType().equals("Runtime"))
				dependencies.add(new LegioRuntime((TaraMogram) dependency));
			else if (((TaraMogramImpl) dependency).simpleType().equals("Provided"))
				dependencies.add(new LegioProvided((TaraMogram) dependency));
			else if (((TaraMogramImpl) dependency).simpleType().equals("Web"))
				dependencies.add(new LegioWeb((TaraMogram) dependency));
		}
		return dependencies;
	}

	public List<Dependency.Web> webDependencies() {
		Mogram imports = TaraPsiUtil.componentOfType(mogram, "Imports");
		if (imports == null) return Collections.emptyList();
		List<Mogram> nodes = TaraPsiUtil.componentsOf(imports);
		nodes.addAll(stream(((TaraMogram) imports).getChildren()).filter(c -> c instanceof Mogram).map(c -> (Mogram) c).toList());
		return nodes.stream().
				filter(d -> d.type().equals("Web") || d.type().equals("Artifact.Imports.Web")).
				map(d -> new LegioWeb((TaraMogram) d)).
				collect(toList());
	}

	@Override
	public List<WebComponent> webComponents() {
		Mogram imports = TaraPsiUtil.componentOfType(mogram, "WebImports");
		if (imports == null) return Collections.emptyList();
		List<Mogram> nodes = TaraPsiUtil.componentsOfType(imports, "WebComponent");
		return nodes.stream().
				map(d -> new LegioWebComponent((TaraMogram) d)).
				collect(toList());
	}

	@Override
	public List<WebResolution> webResolutions() {
		Mogram imports = TaraPsiUtil.componentOfType(mogram, "WebImports");
		if (imports == null) return Collections.emptyList();
		List<Mogram> nodes = TaraPsiUtil.componentsOfType(imports, "Resolution");
		return nodes.stream().
				map(d -> new LegioWebResolution((TaraMogram) d)).
				collect(toList());
	}

	@Override
	public List<WebArtifact> webArtifacts() {
		Mogram imports = TaraPsiUtil.componentOfType(mogram, "WebImports");
		if (imports == null) return Collections.emptyList();
		List<Mogram> nodes = TaraPsiUtil.componentsOfType(imports, "WebArtifact");
		return nodes.stream().
				map(d -> new LegioWebArtifact((TaraMogram) d)).
				collect(toList());
	}

	@Override
	public List<Plugin> plugins() {
		List<Mogram> plugins = TaraPsiUtil.componentsOfType(mogram, "IntinoPlugin");
		return plugins.stream().map(p -> new LegioPlugin((TaraMogram) p)).collect(toList());
	}

	@Override
	@Nullable
	public Box box() {
		Mogram mogram = TaraPsiUtil.componentOfType(this.mogram, "Box");
		return mogram == null ? null : new LegioBox(this, mogram);
	}

	@Override
	public License license() {
		final Mogram licence = TaraPsiUtil.componentOfType(mogram, "License");
		return licence == null ? null : new License() {
			final Mogram licenceNode = licence;

			public LicenseType type() {
				if (licenceNode == null) return null;
				else {
					String type = parameterValue(licenceNode, "type", 0);
					return type != null ? LicenseType.valueOf(type) : null;
				}
			}
		};
	}

	@Override
	public Scm scm() {
		final Mogram scm = TaraPsiUtil.componentOfType(mogram, "Scm");
		return (scm == null) ? null : new Scm() {
			final Mogram scmNode = scm;

			@Override
			public String url() {
				return scmNode == null ? null : parameterValue(scmNode, "url", 0);
			}

			@Override
			public String connection() {
				return scmNode == null ? null : parameterValue(scmNode, "connection", 1);
			}

			@Override
			public String developerConnection() {
				return connection();
			}

			@Override
			public String tag() {
				return name() + "/" + version();
			}
		};
	}

	@Override
	public List<Developer> developers() {
		final List<Mogram> developers = TaraPsiUtil.componentsOfType(mogram, "Developer");
		return developers.stream().map(d -> new Developer() {
			@Override
			public String name() {
				return parameterValue(d, "name", 0);
			}

			@Override
			public String email() {
				return parameterValue(d, "email", 1);
			}

			@Override
			public String organization() {
				return parameterValue(d, "organization", 2);
			}

			@Override
			public String organizationUrl() {
				return parameterValue(d, "organizationUrl", 3);
			}
		}).collect(toList());
	}

	@Override
	public QualityAnalytics qualityAnalytics() {
		return null;
	}

	@Override
	@NotNull
	public List<Parameter> parameters() {
		List<Mogram> nodes = TaraPsiUtil.componentsOfType(mogram, "Parameter");
		return nodes.stream().map(p -> new LegioParameter(this, (TaraMogram) p)).collect(toList());
	}

	public void addDependencies(Dependency... dependencies) {
		if (dependencies.length == 0) return;
		Module module = read(() -> ModuleProvider.moduleOf(mogram));
		if (TaraPsiUtil.componentsOfType(mogram, "Imports").isEmpty()) createImportsNode();
		writeCommandAction(module.getProject(), mogram.getContainingFile()).run(() -> stream(dependencies).forEach(this::addDependency));
	}

	public void addParameters(String... parameters) {
		if (parameters.length == 0) return;
		Module module = read(() -> ModuleProvider.moduleOf(mogram));
		writeCommandAction(module.getProject(), mogram.getContainingFile()).run(() -> stream(parameters).forEach(this::addParameter));
	}

	@Override
	@NotNull
	public LegioPackage packageConfiguration() {
		return new LegioPackage(this, (TaraMogram) TaraPsiUtil.componentOfType(mogram, "Package"));
	}

	@Override
	@Nullable
	public LegioDistribution distribution() {
		Mogram distribution = TaraPsiUtil.componentOfType(mogram, "Distribution");
		return distribution == null ? null : new LegioDistribution(this, (TaraMogram) distribution);
	}

	@Override
	@NotNull
	public List<Configuration.Deployment> deployments() {
		return TaraPsiUtil.componentsOfType(mogram, "Deployment").stream().
				map(d -> new LegioDeployment(this, (TaraMogram) d)).
				collect(toList());
	}

	@Override
	@NotNull
	public ArtifactLegioConfiguration root() {
		return root;
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return null;
	}

	TaraMogram node() {
		return mogram;
	}

	public byte[] serialize() {
		Model model = model();
		String parent = parent(model);
		Box box = box();
		String builder = GROUP_ID + EQ + groupId() + NL +
				ARTIFACT_ID + EQ + name() + NL +
				VERSION + EQ + version() + NL +
				(parent != null ? KonosBuildConstants.PARENT_INTERFACE + EQ + parent + NL : "") +
				PARAMETERS + EQ + parameters().stream().map(Parameter::name).collect(Collectors.joining(";")) + NL +
				GENERATION_PACKAGE + EQ + code().generationPackage() + "." + code().modelPackage() + NL;

		if (model != null) {
			builder += LANGUAGE + EQ + model.language().name() + NL +
					LANGUAGE_VERSION + EQ + model.language().version() + NL +
					OUT_DSL + EQ + model.outLanguage() + NL +
					OUT_DSL_VERSION + EQ + model.outLanguageVersion() + NL;
			if (!model.excludedPhases().isEmpty())
				builder += EXCLUDED_PHASES + EQ + model.excludedPhases().stream().map(e -> String.valueOf(e.ordinal() + 8)).collect(Collectors.joining(" ")) + NL;
			if (model.level() != null) builder += LEVEL + EQ + model.level().name() + NL;
			if (model.language().generationPackage() != null)
				builder += KonosBuildConstants.LANGUAGE_GENERATION_PACKAGE + EQ + model.language().generationPackage() + NL;
		}
		if (box != null) builder += BOX_GENERATION_PACKAGE + EQ + box().targetPackage() + NL;
		Dependency.DataHub datahub = datahub();
		if (datahub != null) builder += DATAHUB + EQ + datahub.identifier() + NL;
		if (datahub != null)
			builder += "library" + EQ + datahub.identifier() + NL;//FIXME added by retro-compatibility. remove in future
		Dependency.Archetype archetype = archetype();
		if (archetype != null) builder += ARCHETYPE + EQ + archetype.identifier() + NL;
		List<String> dependencies = dependencies().stream()
				.filter(d -> d.scope().equalsIgnoreCase(JavaScopes.COMPILE) && d.groupId().startsWith("io.intino"))
				.map(Dependency::identifier)
				.collect(toList());
		if (!dependencies.isEmpty()) builder += CURRENT_DEPENDENCIES + EQ + String.join(",", dependencies) + NL;
		return builder.getBytes();
	}

	private String parent(Model model) {
		final Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return calculateParent(model);
		return application.<String>runReadAction(() -> calculateParent(model));
	}

	private String calculateParent(Model model) {
		try {
			if (mogram == null || model == null) return null;
			Module module = ModuleProvider.moduleOf(mogram);
			final JavaPsiFacade facade = JavaPsiFacade.getInstance(root.module().getProject());
			Model.Language language = model.language();
			if (language == null || language.generationPackage() == null) return null;
			final String workingPackage = language.generationPackage().replace(".model", "").replace(".graph", "");
			final String languageId = LanguageResolver.languageId(language.name(), language.effectiveVersion());
			if (languageId == null) {
				return null;
			}
			String artifact = languageId.split(":")[1];
			final String parentBoxName = parentBoxName(workingPackage, artifact);

			PsiClass aClass = DumbService.getInstance(mogram.getProject()).computeWithAlternativeResolveEnabled(() -> facade.findClass(parentBoxName, allScope(module.getProject())));
			if (aClass == null)
				aClass = DumbService.getInstance(mogram.getProject()).computeWithAlternativeResolveEnabled(() -> facade.findClass(parentBoxName(workingPackage, language.name()), allScope(module.getProject())));
			if (aClass != null) {
				String qualifiedName = aClass.getQualifiedName();
				if (qualifiedName == null) return null;
				return qualifiedName.substring(0, qualifiedName.length() - 3);
			}
		} catch (IndexNotReadyException ignored) {
		} catch (Throwable e) {
			LOG.error(e.getMessage());
		}
		return null;
	}

	private String parentBoxName(String workingPackage, String artifact) {
		return workingPackage + ".box." + firstUpperCase(snakeCaseToCamelCase(artifact)) + "Box";
	}

	private void addParameter(String p) {
		TaraElementFactory factory = factory();
		Mogram node = factory.createFullMogram("Parameter(name = \"" + p + "\")");
		node.type("Artifact.Parameter");
		((TaraMogramImpl) node).getSignature().getLastChild().getPrevSibling().delete();
		final PsiElement last = (PsiElement) this.mogram.components().get(this.mogram.components().size() - 1);
		PsiElement separator = this.mogram.addAfter(factory.createBodyNewLine(), last);
		this.mogram.addAfter((PsiElement) node, separator);
	}

	private void addDependency(Dependency d) {
		TaraMogram imports = (TaraMogram) TaraPsiUtil.componentsOfType(mogram, "Imports").get(0);
		TaraElementFactory factory = factory();
		String type = d.scope();
		Mogram mogram = factory.createFullMogram(type + "(groupId = \"" + d.groupId() + "\", artifactId = \"" + d.artifactId() + "\", version = \"" + d.version() + "\")");
		mogram.type("Artifact.Imports." + type);
		((TaraMogramImpl) mogram).getSignature().getLastChild().getPrevSibling().delete();
		PsiElement last;
		if (imports.components().isEmpty()) {
			imports.add(factory.createBodyNewLine(2));
			last = imports;
		} else last = (PsiElement) imports.components().get(imports.components().size() - 1);
		PsiElement separator = imports.addAfter(factory.createBodyNewLine(2), last);
		imports.addAfter((PsiElement) mogram, separator);
	}

	private void createImportsNode() {
		TaraElementFactory factory = factory();
		Mogram node = factory.createFullMogram("Imports");
		node.type("Artifact.Imports");
		final PsiElement last = (PsiElement) this.mogram.components().get(this.mogram.components().size() - 1);
		PsiElement separator = this.mogram.addAfter(factory.createBodyNewLine(), last);
		this.mogram.addAfter((PsiElement) node, separator);
	}

	private TaraElementFactory factory() {
		return TaraElementFactory.getInstance(this.mogram.getProject());
	}

	private record LegioWebComponent(TaraMogram node) implements WebComponent {

		@Override
		public String name() {
			return parameterValue(node, "url", 0);
		}

		@Override
		public String version() {
			return parameterValue(node, "version", 1);
		}
	}

	private record LegioWebResolution(TaraMogram node) implements WebResolution {

		@Override
		public String name() {
			return parameterValue(node, "name", 0);
		}

		@Override
		public String version() {
			return parameterValue(node, "version", 1);
		}
	}

	private record LegioWebArtifact(TaraMogram node) implements WebArtifact {

		@Override
		public String name() {
			return node.name();
		}

		@Override
		public String groupId() {
			return parameterValue(node, "groupId", 0);
		}

		@Override
		public String artifactId() {
			return parameterValue(node, "artifactId", 1);
		}

		@Override
		public String version() {
			return parameterValue(node, "version", 2);
		}
	}

	private record LegioPlugin(TaraMogram node) implements Plugin {

		@Override
		public String groupId() {
			return parameterValue(node, "groupId", 0);
		}

		@Override
		public String artifactId() {
			return parameterValue(node, "artifactId", 1);
		}

		@Override
		public String version() {
			return parameterValue(node, "version", 2);
		}

		@Override
		public Phase phase() {
			return Phase.valueOf(parameterValue(node, "phase", 3));
		}
	}

	private static class LegioParameter implements Parameter {
		private final LegioArtifact artifact;
		private final TaraMogram node;
		private String name;
		private String value;
		private String description;

		public LegioParameter(LegioArtifact artifact, TaraMogram node) {
			this.artifact = artifact;
			this.node = node;
		}

		@Override
		public String name() {
			return name == null ? name = parameterValue(node, "name", 0) : name;
		}

		@Override
		public String value() {
			return value == null ? value = parameterValue(node, "defaultValue", 1) : value;
		}

		@Override
		public String description() {
			return description == null ? description = parameterValue(node, "description", 2) : description;
		}

		@Override
		public Configuration root() {
			return artifact.root;
		}

		@Override
		public Configuration.ConfigurationNode owner() {
			return artifact;
		}
	}
}
