package io.intino.plugin.project.configuration.model;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import io.intino.alexandria.logger.Logger;
import io.intino.konos.compiler.shared.KonosBuildConstants;
import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraNodeImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioDependency.*;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.compiler.shared.Configuration.Parameter;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static com.intellij.psi.search.GlobalSearchScope.moduleWithDependenciesAndLibrariesScope;
import static io.intino.konos.compiler.shared.KonosBuildConstants.LIBRARY;
import static io.intino.konos.compiler.shared.KonosBuildConstants.PARAMETERS;
import static io.intino.plugin.actions.archetype.Formatters.firstUpperCase;
import static io.intino.plugin.actions.archetype.Formatters.snakeCaseToCamelCase;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;
import static io.intino.tara.compiler.shared.TaraBuildConstants.*;
import static java.util.Arrays.stream;

public class LegioArtifact implements Configuration.Artifact {
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
		return name == null ? name = node.name() : name;
	}

	@Override
	public String version() {
		return version == null ? version = parameterValue(node, "version", 1) : version;
	}

	@Override
	public void version(String version) {
		writeCommandAction(node.getProject(), node.getContainingFile()).run(() ->
				node.parameters().stream().filter(p -> p.name().equals("version")).findFirst().
						ifPresent(p -> p.substituteValues(Collections.singletonList(version))));
	}

	@Override
	@NotNull
	public Code code() {
		return new LegioCode(this, (TaraNode) TaraPsiUtil.componentOfType(node, "Code"));
	}

	@Override
	@NotNull
	public Model model() {
		return new LegioModel(this, (TaraNode) TaraPsiUtil.componentOfType(node, "Model"));
	}

	@Override
	@NotNull
	public Dependency.DataHub datahub() {
		return new LegioDataHub(this, dependencyAuditor, (TaraNode) TaraPsiUtil.componentOfType(node, "DataHub"));
	}

	@Override
	public List<Dependency> dependencies() {
		Node imports = TaraPsiUtil.componentOfType(node, "Imports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOf(imports);
		List<Dependency> dependencies = new ArrayList<>();
		for (Node dependency : nodes)
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
		return dependencies;
	}

	public List<Dependency.Web> webDependencies() {
		Node imports = TaraPsiUtil.componentOfType(node, "Imports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOf(imports);
		return nodes.stream().
				filter(d -> d.type().equals("Web") || d.type().equals("Artifact.Imports.Web")).
				map(d -> new LegioWeb(this, dependencyAuditor, (TaraNode) d)).
				collect(Collectors.toList());
	}

	@Override
	public List<WebComponent> webComponents() {
		Node imports = TaraPsiUtil.componentOfType(node, "WebImports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOfType(imports, "WebComponent");
		return nodes.stream().
				map(d -> new LegioWebComponent((TaraNode) d)).
				collect(Collectors.toList());
	}

	@Override
	public List<WebResolution> webResolutions() {
		Node imports = TaraPsiUtil.componentOfType(node, "WebImports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOfType(imports, "Resolution");
		return nodes.stream().
				map(d -> new LegioWebResolution((TaraNode) d)).
				collect(Collectors.toList());
	}

	@Override
	public List<WebArtifact> webArtifacts() {
		Node imports = TaraPsiUtil.componentOfType(node, "WebImports");
		if (imports == null) return Collections.emptyList();
		List<Node> nodes = TaraPsiUtil.componentsOfType(imports, "WebArtifact");
		return nodes.stream().
				map(d -> new LegioWebArtifact((TaraNode) d)).
				collect(Collectors.toList());
	}

	@Override
	public List<Plugin> plugins() {
		List<Node> plugins = TaraPsiUtil.componentsOfType(node, "Plugin");
		return plugins.stream().map(p -> new LegioPlugin((TaraNode) p)).collect(Collectors.toList());
	}

	@Override
	public Box box() {
		Node node = TaraPsiUtil.componentOfType(this.node, "Box");
		return new LegioBox(this, node);
	}

	@Override
	public Licence licence() {
		return new Licence() {
			Node licenceNode = TaraPsiUtil.componentOfType(node, "Licence");

			public String author() {
				return licenceNode == null ? null : parameterValue(node, "author");
			}

			public LicenceType type() {
				if (licenceNode == null) return null;
				else {
					String type = parameterValue(node, "type");
					return type != null ? LicenceType.valueOf(type) : null;
				}
			}
		};
	}

	@Override
	public QualityAnalytics qualityAnalytics() {
		return null;
	}

	@Override
	@NotNull
	public List<Parameter> parameters() {
		List<Node> nodes = TaraPsiUtil.componentsOfType(node, "Parameter");
		return nodes.stream().map(p -> new LegioParameter(this, (TaraNode) p)).collect(Collectors.toList());
	}

	public void addDependencies(Dependency... dependencies) {
		if (dependencies.length == 0) return;
		Module module = ModuleProvider.moduleOf(node);
		if (TaraPsiUtil.componentsOfType(node, "Imports").isEmpty()) createImportsNode();
		writeCommandAction(module.getProject(), node.getContainingFile()).run(() -> stream(dependencies).forEach(this::addDependency));
	}

	public void addParameters(String... parameters) {
		if (parameters.length == 0) return;
		Module module = ModuleProvider.moduleOf(node);
		writeCommandAction(module.getProject(), node.getContainingFile()).run(() -> stream(parameters).forEach(this::addParameter));
	}

	@Override
	@NotNull
	public LegioPackage packageConfiguration() {
		return new LegioPackage(this, (TaraNode) TaraPsiUtil.componentOfType(node, "Package"));
	}

	@Override
	@NotNull
	public LegioDistribution distribution() {
		return new LegioDistribution(this, (TaraNode) TaraPsiUtil.componentOfType(node, "Distribution"));
	}

	@Override
	@NotNull
	public List<Configuration.Deployment> deployments() {
		return TaraPsiUtil.componentsOfType(node, "Deployment").stream().
				map(d -> new LegioDeployment(this, (TaraNode) d)).
				collect(Collectors.toList());
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
		String parent = parent();
		String builder = GROUP_ID + EQ + groupId() + "\n" +
				ARTIFACT_ID + EQ + name() + "\n" +
				VERSION + EQ + version() + "\n" +
				(parent != null ? KonosBuildConstants.PARENT_INTERFACE + EQ + parent + "\n" : "") +
				PARAMETERS + EQ + parameters().stream().map(Parameter::name).collect(Collectors.joining(";")) + "\n" +
				GENERATION_PACKAGE + EQ + code().generationPackage() + "\n";
		if (model().language() != null) {
			builder += LANGUAGE + EQ + model.language().name() + "\n" +
					LANGUAGE_VERSION + EQ + model.language().version() + "\n" +
					LEVEL + EQ + model.level().name() + "\n" +
					OUT_DSL + EQ + model.outLanguage() + "\n" +
					OUT_DSL_VERSION + EQ + model.outLanguageVersion() + "\n";
			if (model.language().generationPackage() != null)
				builder += KonosBuildConstants.LANGUAGE_GENERATION_PACKAGE + EQ + model.language().generationPackage() + "\n";
		}
		String identifier = datahub().identifier();
		if (identifier != null) builder += LIBRARY + EQ + datahub().identifier();
		return builder.getBytes();
	}

	private String parent() {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return calculateParent();
		return application.<String>runReadAction(this::calculateParent);
	}

	private String calculateParent() {
		try {
			if (node == null) return null;
			Module module = ModuleProvider.moduleOf(node);
			if (module == null) return null;
			final JavaPsiFacade facade = JavaPsiFacade.getInstance(module.getProject());
			Model.Language language = model().language();
			if (language == null || language.generationPackage() == null) return null;
			final String workingPackage = language.generationPackage().replace(".graph", "");
			String artifact = LanguageResolver.languageId(language.name(), language.effectiveVersion()).split(":")[1];
			Application application = ApplicationManager.getApplication();
			PsiClass aClass = application.runReadAction((Computable<PsiClass>) () -> facade.findClass(parentBoxName(workingPackage, artifact), moduleWithDependenciesAndLibrariesScope(module)));
			if (aClass == null)
				aClass = application.runReadAction((Computable<PsiClass>) () -> facade.findClass(parentBoxName(workingPackage, language.name()), moduleWithDependenciesAndLibrariesScope(module)));
			if (aClass != null)
				return workingPackage.toLowerCase() + ".box." + firstUpperCase(language.name());
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
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

	private void addDependency(Dependency dependency) {
		TaraNode imports = (TaraNode) TaraPsiUtil.componentsOfType(node, "Imports").get(0);
		TaraElementFactory factory = factory();
		String type = dependency.getClass().getSimpleName();
		Node node = factory.createFullNode(type + "(" + ")");
		node.type("Artifact.Imports." + type);
		((TaraNodeImpl) node).getSignature().getLastChild().getPrevSibling().delete();
		final PsiElement last = (PsiElement) imports.components().get(imports.components().size() - 1);
		PsiElement separator = imports.addAfter(factory.createBodyNewLine(), last);
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
			return parameterValue(node, "name", 0);
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
		public String groupId() {
			return parameterValue(node, "groupId", 0);
		}

		@Override
		public String artifactId() {
			return parameterValue(node, "artifactId", 1);
		}

		@Override
		public String version() {
			return null;
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
