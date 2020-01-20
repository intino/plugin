package io.intino.plugin.project.configuration.model;

import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioDependency.*;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.compiler.shared.TaraBuildConstants;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioArtifact implements Configuration.Artifact {
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
				filter(d -> d.type().equals("Web")).
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

			@Override
			public String author() {
				return licenceNode == null ? null : parameterValue(node, "author");
			}

			@Override
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
	public List<Configuration.Parameter> parameters() {
		List<Node> nodes = TaraPsiUtil.componentsOfType(node, "Parameter");
		return nodes.stream().map(p -> new LegioParameter(this, (TaraNode) p)).collect(Collectors.toList());
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
		String builder = "groupId=" + groupId() + "\n" +
				"artifactId=" + name() + "\n" +
				"version=" + version() + "\n" +
				TaraBuildConstants.LANGUAGE + "=" + model.language().name() + "\n" +
				TaraBuildConstants.LANGUAGE_VERSION + "=" + model.language().version() + "\n" +
				"level=" + model.level().name() + "\n" +
				TaraBuildConstants.OUT_DSL + "=" + model.outLanguage() + "\n" +
				TaraBuildConstants.OUT_DSL_VERSION + "=" + model.outLanguageVersion() + "\n" +
				TaraBuildConstants.GENERATION_PACKAGE + "=" + code().generationPackage() + "\n";
		return builder.getBytes();
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

	private static class LegioParameter implements Configuration.Parameter {
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
