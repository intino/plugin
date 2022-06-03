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
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraNodeImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioDependency.*;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonatype.aether.util.artifact.JavaScopes;

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
import static io.intino.magritte.compiler.shared.TaraBuildConstants.*;
import static io.intino.plugin.archetype.Formatters.firstUpperCase;
import static io.intino.plugin.archetype.Formatters.snakeCaseToCamelCase;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.read;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class LegioArtifact implements Configuration.Artifact {
	private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(LegioArtifact.class.getName());

	public static final String EQ = "=";
	private final LegioConfiguration root;
	private final DependencyAuditor dependencyAuditor;
	private final TaraNode node;
	private String groupId;
	private String name;
	private String version;

	public LegioArtifact(LegioConfiguration root, DependencyAuditor dependencyAuditor, TaraNode node) {
		this.root = root;
		this.dependencyAuditor = dependencyAuditor;
		this.node = node;
	}

	@Override
	public String groupId() {
		return groupId == null ? groupId = parameterValue(node, "groupId", 0) : groupId;
	}

	@Override
	public String name() {
		if (node == null) return null;
		return name == null ? name = node.name() : name;
	}

	@Override
	public void name(String newName) {
		writeCommandAction(node.getProject(), node.getContainingFile()).run(() -> node.name(newName));
		ApplicationManager.getApplication().invokeAndWait(() -> IntinoUtil.commitDocument(node.getContainingFile()));
	}

	@Override
	public String version() {
		return version == null ? version = parameterValue(node, "version", 1) : version;
	}

	@Override
	public String description() {
		return parameterValue(node, "description", 2);
	}

	@Override
	public String url() {
		return parameterValue(node, "url", 2);
	}

	@Override
	public void version(String newVersion) {
		writeCommandAction(node.getProject(), node.getContainingFile()).run(() -> {
			io.intino.magritte.lang.model.Parameter version = node.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(node.parameters().get(1));
			if (version != null) version.substituteValues(singletonList(newVersion));
		});
		ApplicationManager.getApplication().invokeAndWait(() -> IntinoUtil.commitDocument(node.getContainingFile()));
	}

	@Override
	@NotNull
	public Code code() {
		return new LegioCode(this, (TaraNode) TaraPsiUtil.componentOfType(node, "Code"));
	}

	@Override
	@Nullable
	public Model model() {
		TaraNode model = (TaraNode) TaraPsiUtil.componentOfType(node, "Model");
		return model == null ? null : new LegioModel(this, model);
	}

	@Override
	public Dependency.DataHub datahub() {
		Node dataHub = TaraPsiUtil.componentOfType(node, "DataHub");
		if (dataHub == null) return null;
		return new LegioDataHub(this, dependencyAuditor, (TaraNode) dataHub);
	}

	@Override
	public Dependency.Archetype archetype() {
		Node archetype = TaraPsiUtil.componentOfType(node, "Archetype");
		if (archetype == null) return null;
		return new LegioArchetype(this, dependencyAuditor, (TaraNode) archetype);
	}

	@Override
	public List<Dependency> dependencies() {
		Node imports = TaraPsiUtil.componentOfType(node, "Imports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOf(imports);
		nodes.addAll(stream(((TaraNode) imports).getChildren()).filter(c -> c instanceof Node).map(c -> (Node) c).collect(toList()));
		List<Dependency> dependencies = new ArrayList<>();
		for (Node dependency : nodes) {
			if (((TaraNode) dependency).simpleType().equals("Compile"))
				dependencies.add(new LegioCompile(this, dependencyAuditor, (TaraNode) dependency));
			else if (((TaraNode) dependency).simpleType().equals("Test"))
				dependencies.add(new LegioTest(this, dependencyAuditor, (TaraNode) dependency));
			else if (((TaraNode) dependency).simpleType().equals("Runtime"))
				dependencies.add(new LegioRuntime(this, dependencyAuditor, (TaraNode) dependency));
			else if (((TaraNode) dependency).simpleType().equals("Provided"))
				dependencies.add(new LegioProvided(this, dependencyAuditor, (TaraNode) dependency));
			else if (((TaraNode) dependency).simpleType().equals("Web"))
				dependencies.add(new LegioWeb(this, dependencyAuditor, (TaraNode) dependency));
		}
		return dependencies;
	}

	public List<Dependency.Web> webDependencies() {
		Node imports = TaraPsiUtil.componentOfType(node, "Imports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOf(imports);
		return nodes.stream().
				filter(d -> d.type().equals("Web") || d.type().equals("Artifact.Imports.Web")).
				map(d -> new LegioWeb(this, dependencyAuditor, (TaraNode) d)).
				collect(toList());
	}

	@Override
	public List<WebComponent> webComponents() {
		Node imports = TaraPsiUtil.componentOfType(node, "WebImports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOfType(imports, "WebComponent");
		return nodes.stream().
				map(d -> new LegioWebComponent((TaraNode) d)).
				collect(toList());
	}

	@Override
	public List<WebResolution> webResolutions() {
		Node imports = TaraPsiUtil.componentOfType(node, "WebImports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOfType(imports, "Resolution");
		return nodes.stream().
				map(d -> new LegioWebResolution((TaraNode) d)).
				collect(toList());
	}

	@Override
	public List<WebArtifact> webArtifacts() {
		Node imports = TaraPsiUtil.componentOfType(node, "WebImports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOfType(imports, "WebArtifact");
		return nodes.stream().
				map(d -> new LegioWebArtifact((TaraNode) d)).
				collect(toList());
	}

	@Override
	public List<Plugin> plugins() {
		List<Node> plugins = TaraPsiUtil.componentsOfType(node, "IntinoPlugin");
		return plugins.stream().map(p -> new LegioPlugin((TaraNode) p)).collect(toList());
	}

	@Override
	@Nullable
	public Box box() {
		Node node = TaraPsiUtil.componentOfType(this.node, "Box");
		return node == null ? null : new LegioBox(this, node);
	}

	@Override
	public License license() {
		final Node licence = TaraPsiUtil.componentOfType(node, "License");
		return licence == null ? null : new License() {
			final Node licenceNode = licence;

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
		final Node scm = TaraPsiUtil.componentOfType(node, "Scm");
		return (scm == null) ? null : new Scm() {
			final Node scmNode = scm;

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
		final List<Node> developers = TaraPsiUtil.componentsOfType(node, "Developer");
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
		List<Node> nodes = TaraPsiUtil.componentsOfType(node, "Parameter");
		return nodes.stream().map(p -> new LegioParameter(this, (TaraNode) p)).collect(toList());
	}

	public void addDependencies(Dependency... dependencies) {
		if (dependencies.length == 0) return;
		Module module = read(() -> ModuleProvider.moduleOf(node));
		if (TaraPsiUtil.componentsOfType(node, "Imports").isEmpty()) createImportsNode();
		writeCommandAction(module.getProject(), node.getContainingFile()).run(() -> stream(dependencies).forEach(this::addDependency));
	}

	public void addParameters(String... parameters) {
		if (parameters.length == 0) return;
		Module module = read(() -> ModuleProvider.moduleOf(node));
		writeCommandAction(module.getProject(), node.getContainingFile()).run(() -> stream(parameters).forEach(this::addParameter));
	}

	@Override
	@NotNull
	public LegioPackage packageConfiguration() {
		return new LegioPackage(this, (TaraNode) TaraPsiUtil.componentOfType(node, "Package"));
	}

	@Override
	@Nullable
	public LegioDistribution distribution() {
		Node distribution = TaraPsiUtil.componentOfType(node, "Distribution");
		return distribution == null ? null : new LegioDistribution(this, (TaraNode) distribution);
	}

	@Override
	@NotNull
	public List<Configuration.Deployment> deployments() {
		return TaraPsiUtil.componentsOfType(node, "Deployment").stream().
				map(d -> new LegioDeployment(this, (TaraNode) d)).
				collect(toList());
	}

	@Override
	@NotNull
	public LegioConfiguration root() {
		return root;
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return null;
	}

	TaraNode node() {
		return node;
	}

	public byte[] serialize() {
		Model model = model();
		String parent = parent(model);
		Box box = box();
		String builder = GROUP_ID + EQ + groupId() + "\n" +
				ARTIFACT_ID + EQ + name() + "\n" +
				VERSION + EQ + version() + "\n" +
				(parent != null ? KonosBuildConstants.PARENT_INTERFACE + EQ + parent + "\n" : "") +
				PARAMETERS + EQ + parameters().stream().map(Parameter::name).collect(Collectors.joining(";")) + "\n" +
				GENERATION_PACKAGE + EQ + code().generationPackage() + "." + code().modelPackage() + "\n";

		if (model != null) {
			builder += LANGUAGE + EQ + model.language().name() + "\n" +
					LANGUAGE_VERSION + EQ + model.language().version() + "\n" +
					OUT_DSL + EQ + model.outLanguage() + "\n" +
					OUT_DSL_VERSION + EQ + model.outLanguageVersion() + "\n";
			if (model.level() != null) builder += LEVEL + EQ + model.level().name() + "\n";
			if (model.language().generationPackage() != null)
				builder += KonosBuildConstants.LANGUAGE_GENERATION_PACKAGE + EQ + model.language().generationPackage() + "\n";
		}
		if (box != null) builder += BOX_GENERATION_PACKAGE + EQ + box().targetPackage() + "\n";
		Dependency.DataHub datahub = datahub();
		if (datahub != null) builder += DATAHUB + EQ + datahub.identifier() + "\n";
		if (datahub != null)
			builder += "library" + EQ + datahub.identifier() + "\n";//FIXME added by retro-compatibility. remove in future
		Dependency.Archetype archetype = archetype();
		if (archetype != null) builder += ARCHETYPE + EQ + archetype.identifier() + "\n";
		List<String> dependencies = dependencies().stream()
				.filter(d -> d.scope().equalsIgnoreCase(JavaScopes.COMPILE) && d.groupId().startsWith("io.intino"))
				.map(Dependency::identifier)
				.collect(toList());
		if (!dependencies.isEmpty()) builder += CURRENT_DEPENDENCIES + EQ + String.join(",", dependencies) + "\n";
		return builder.getBytes();
	}

	private String parent(Model model) {
		final Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return calculateParent(model);
		return application.<String>runReadAction(() -> calculateParent(model));
	}

	private String calculateParent(Model model) {
		try {
			LOG.warn("Owner interface: Searching");
			if (node == null || model == null) {
				LOG.info("Owner interface: Model not found; " + (node == null ? "node null" : "model null"));
				return null;
			}
			Module module = ModuleProvider.moduleOf(node);
			final JavaPsiFacade facade = JavaPsiFacade.getInstance(module.getProject());
			Model.Language language = model.language();
			if (language == null || language.generationPackage() == null) {
				LOG.info("Owner interface: Language not found; " + (language == null ? "language null" : "generationPackage null"));
				return null;
			}
			final String workingPackage = language.generationPackage().replace(".graph", "");
			final String languageId = LanguageResolver.languageId(language.name(), language.effectiveVersion());
			if (languageId == null) {
				LOG.error("Owner interface: LanguageId = null;" + language.name() + "," + language.effectiveVersion());
				return null;
			}
			String artifact = languageId.split(":")[1];
			final String parentBoxName = parentBoxName(workingPackage, artifact);
			LOG.info("Owner interface: parentBoxName = " + parentBoxName);

			PsiClass aClass = DumbService.getInstance(node.getProject()).computeWithAlternativeResolveEnabled(() -> facade.findClass(parentBoxName, allScope(module.getProject())));
			if (aClass == null)
				aClass = DumbService.getInstance(node.getProject()).computeWithAlternativeResolveEnabled(() -> facade.findClass(parentBoxName(workingPackage, language.name()), allScope(module.getProject())));
			if (aClass != null) {
				String qualifiedName = aClass.getQualifiedName();
				if (qualifiedName == null) {
					LOG.warn("Owner interface cannot be collected. QualifiedName = null");
					return null;
				}
				return qualifiedName.substring(0, qualifiedName.length() - 3);
			}
		} catch (IndexNotReadyException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
		LOG.warn("Owner interface cannot be collected. aClass = null");
		return null;
	}

	private String parentBoxName(String workingPackage, String artifact) {
		return workingPackage + ".box." + firstUpperCase(snakeCaseToCamelCase(artifact)) + "Box";
	}

	private void addParameter(String p) {
		TaraElementFactory factory = factory();
		Node node = factory.createFullNode("Parameter(name = \"" + p + "\")");
		node.type("Artifact.Parameter");
		((TaraNodeImpl) node).getSignature().getLastChild().getPrevSibling().delete();
		final PsiElement last = (PsiElement) this.node.components().get(this.node.components().size() - 1);
		PsiElement separator = this.node.addAfter(factory.createBodyNewLine(), last);
		this.node.addAfter((PsiElement) node, separator);
	}

	private void addDependency(Dependency d) {
		TaraNode imports = (TaraNode) TaraPsiUtil.componentsOfType(node, "Imports").get(0);
		TaraElementFactory factory = factory();
		String type = d.scope();
		Node node = factory.createFullNode(type + "(groupId = \"" + d.groupId() + "\", artifactId = \"" + d.artifactId() + "\", version = \"" + d.version() + "\")");
		node.type("Artifact.Imports." + type);
		((TaraNodeImpl) node).getSignature().getLastChild().getPrevSibling().delete();
		PsiElement last;
		if (imports.components().isEmpty()) {
			imports.add(factory.createBodyNewLine(2));
			last = imports;
		} else last = (PsiElement) imports.components().get(imports.components().size() - 1);
		PsiElement separator = imports.addAfter(factory.createBodyNewLine(2), last);
		imports.addAfter((PsiElement) node, separator);
	}

	private void createImportsNode() {
		TaraElementFactory factory = factory();
		Node node = factory.createFullNode("Imports");
		node.type("Artifact.Imports");
		final PsiElement last = (PsiElement) this.node.components().get(this.node.components().size() - 1);
		PsiElement separator = this.node.addAfter(factory.createBodyNewLine(), last);
		this.node.addAfter((PsiElement) node, separator);
	}

	private TaraElementFactory factory() {
		return TaraElementFactory.getInstance(this.node.getProject());
	}

	private static class LegioWebComponent implements WebComponent {
		private final TaraNode node;

		LegioWebComponent(TaraNode node) {
			this.node = node;
		}

		@Override
		public String name() {
			return parameterValue(node, "url", 0);
		}

		@Override
		public String version() {
			return parameterValue(node, "version", 1);
		}
	}

	private static class LegioWebResolution implements WebResolution {
		private final TaraNode node;

		LegioWebResolution(TaraNode node) {
			this.node = node;
		}

		@Override
		public String url() {
			return parameterValue(node, "url", 0);
		}

		@Override
		public String version() {
			return parameterValue(node, "version", 1);
		}
	}

	private static class LegioWebArtifact implements WebArtifact {
		private final TaraNode node;

		LegioWebArtifact(TaraNode node) {
			this.node = node;
		}

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

	private static class LegioPlugin implements Plugin {
		private final TaraNode node;

		public LegioPlugin(TaraNode node) {
			this.node = node;
		}

		@Override
		public String artifact() {
			return parameterValue(node, "artifact", 0);
		}

		@Override
		public String pluginClass() {
			return parameterValue(node, "pluginClass", 1);
		}

		@Override
		public Phase phase() {
			return Phase.valueOf(parameterValue(node, "phase", 2));
		}
	}

	private static class LegioParameter implements Parameter {
		private final LegioArtifact artifact;
		private final TaraNode node;
		private String name;
		private String value;
		private String description;

		public LegioParameter(LegioArtifact artifact, TaraNode node) {
			this.artifact = artifact;
			this.node = node;
		}

		@Override
		public String name() {
			return name == null ? name = parameterValue(node, "name", 0) : name;
		}

		@Override
		public String value() {
			return value == null ? value = parameterValue(node, "value", 1) : value;
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
