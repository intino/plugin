package io.intino.plugin.project.configuration.model;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.intino.Configuration;
import io.intino.Configuration.Parameter;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraMogramImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioDependency.*;
import io.intino.plugin.project.configuration.model.retrocompatibility.LegioBox;
import io.intino.plugin.project.configuration.model.retrocompatibility.LegioModel;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.*;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class LegioArtifact implements Configuration.Artifact {
	private static final Logger LOG = Logger.getInstance(LegioArtifact.class.getName());
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
		return new LegioCode(this, (TaraMogram) componentOfType(mogram, "Code"));
	}

	@Override
	public List<Configuration.Artifact.Dsl> dsls() {
		List<Dsl> dsls = componentsOfType(mogram, "Dsl").stream()
				.map(d -> new LegioDsl(LegioArtifact.this, (TaraMogram) d))
				.collect(toList());
		Mogram model = componentOfType(mogram, "Model");
		if (model != null) dsls.add(new LegioModel(this, (TaraMogram) model));
		Mogram box = componentOfType(mogram, "Box");
		if (box != null) dsls.add(new LegioBox(this, box));
		return dsls;
	}

	@Override
	public Dependency.DataHub datahub() {
		Mogram dataHub = componentOfType(mogram, "DataHub");
		return dataHub == null ? null : new LegioDataHub(this, (TaraMogram) dataHub);
	}

	@Override
	public Dependency.Archetype archetype() {
		Mogram archetype = componentOfType(mogram, "Archetype");
		return archetype == null ? null : new LegioArchetype(this, (TaraMogram) archetype);
	}

	@Override
	public List<Dependency> dependencies() {
		Mogram imports = componentOfType(mogram, "Imports");
		if (imports == null) return Collections.emptyList();
		List<Mogram> nodes = TaraPsiUtil.componentsOf(imports);
		nodes.addAll(stream(((TaraMogram) imports).getChildren()).filter(c -> c instanceof Mogram).map(c -> (Mogram) c).toList());
		List<Dependency> dependencies = new ArrayList<>();
		for (Mogram dependency : nodes) {
			if (((TaraMogramImpl) dependency).simpleType().equals("Compile"))
				dependencies.add(new LegioCompile(this, (TaraMogram) dependency));
			else if (((TaraMogramImpl) dependency).simpleType().equals("Test"))
				dependencies.add(new LegioTest(this, (TaraMogram) dependency));
			else if (((TaraMogramImpl) dependency).simpleType().equals("Runtime"))
				dependencies.add(new LegioRuntime(this, (TaraMogram) dependency));
			else if (((TaraMogramImpl) dependency).simpleType().equals("Provided"))
				dependencies.add(new LegioProvided(this, (TaraMogram) dependency));
			else if (((TaraMogramImpl) dependency).simpleType().equals("Web"))
				dependencies.add(new LegioWeb(this, (TaraMogram) dependency));
		}
		return dependencies;
	}

	public List<Dependency.Web> webDependencies() {
		Mogram imports = componentOfType(mogram, "Imports");
		if (imports == null) return Collections.emptyList();
		List<Mogram> nodes = TaraPsiUtil.componentsOf(imports);
		nodes.addAll(stream(((TaraMogram) imports).getChildren()).filter(c -> c instanceof Mogram).map(c -> (Mogram) c).toList());
		return nodes.stream().
				filter(d -> d.type().equals("Web") || d.type().equals("Artifact.Imports.Web")).
				map(d -> new LegioWeb(this,(TaraMogram) d)).
				collect(toList());
	}

	@Override
	public List<WebComponent> webComponents() {
		Mogram imports = componentOfType(mogram, "WebImports");
		if (imports == null) return Collections.emptyList();
		return componentsOfType(imports, "WebComponent").stream().
				map(d -> new LegioWebComponent((TaraMogram) d)).
				collect(toList());
	}

	@Override
	public List<WebResolution> webResolutions() {
		Mogram imports = componentOfType(mogram, "WebImports");
		if (imports == null) return Collections.emptyList();
		return componentsOfType(imports, "Resolution").stream().
				map(d -> new LegioWebResolution((TaraMogram) d)).
				collect(toList());
	}

	@Override
	public List<WebArtifact> webArtifacts() {
		Mogram imports = componentOfType(mogram, "WebImports");
		if (imports == null) return Collections.emptyList();
		return componentsOfType(imports, "WebArtifact").stream().
				map(d -> new LegioWebArtifact((TaraMogram) d)).
				collect(toList());
	}

	@Override
	public List<Plugin> plugins() {
		return componentsOfType(mogram, "IntinoPlugin").stream().map(p -> new LegioPlugin(LegioArtifact.this, (TaraMogram) p)).collect(toList());
	}

	@Override
	public License license() {
		final Mogram licence = componentOfType(mogram, "License");
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
		final Mogram scm = componentOfType(mogram, "Scm");
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
		return componentsOfType(mogram, "Developer").stream().map(d -> new Developer() {
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
		return componentsOfType(mogram, "Parameter").stream().map(p -> new LegioParameter(this, (TaraMogram) p)).collect(toList());
	}

	public void addDependencies(Dependency... dependencies) {
		if (dependencies.length == 0) return;
		Module module = read(() -> ModuleProvider.moduleOf(mogram));
		if (componentsOfType(mogram, "Imports").isEmpty()) createImportsNode();
		PsiFile containingFile = mogram.getContainingFile();
		writeCommandAction(module.getProject(), containingFile).run(() -> stream(dependencies).forEach(this::addDependency));
	}

	public void addParameters(String... parameters) {
		if (parameters.length == 0) return;
		Module module = read(() -> ModuleProvider.moduleOf(mogram));
		PsiFile containingFile = mogram.getContainingFile();
		writeCommandAction(module.getProject(), containingFile).run(() -> stream(parameters).forEach(this::addParameter));
	}

	@Override
	@NotNull
	public LegioPackage packageConfiguration() {
		return new LegioPackage(this, (TaraMogram) componentOfType(mogram, "Package"));
	}

	@Override
	@Nullable
	public LegioDistribution distribution() {
		Mogram distribution = componentOfType(mogram, "Distribution");
		return distribution == null ? null : new LegioDistribution(this, (TaraMogram) distribution);
	}

	@Override
	@NotNull
	public List<Configuration.Deployment> deployments() {
		return componentsOfType(mogram, "Deployment").stream().
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

	public TaraMogram mogram() {
		return mogram;
	}

	private void addParameter(String p) {
		TaraElementFactory factory = factory();
		Mogram mogram = factory.createFullMogram("Parameter(name = \"" + p + "\")");
		mogram.type("Artifact.Parameter");
		((TaraMogramImpl) mogram).getSignature().getLastChild().getPrevSibling().delete();
		final PsiElement last = (PsiElement) this.mogram.components().get(this.mogram.components().size() - 1);
		PsiElement separator = this.mogram.addAfter(factory.createBodyNewLine(), last);
		this.mogram.addAfter((PsiElement) mogram, separator);
	}

	private void addDependency(Dependency d) {
		TaraMogram imports = (TaraMogram) componentsOfType(mogram, "Imports").get(0);
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

	public record LegioPlugin(LegioArtifact artifact, TaraMogram mogram) implements Plugin {

		@Override
		public String groupId() {
			return parameterValue(mogram, "groupId", 0);
		}

		@Override
		public String artifactId() {
			return parameterValue(mogram, "artifactId", 1);
		}

		@Override
		public String version() {
			return parameterValue(mogram, "version", 2);
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
		public Phase phase() {
			return Phase.valueOf(parameterValue(mogram, "phase", 3));
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

	private static class LegioParameter implements Parameter {
		private final LegioArtifact artifact;
		private final TaraMogram mogram;
		private String name;
		private String value;
		private String description;

		public LegioParameter(LegioArtifact artifact, TaraMogram mogram) {
			this.artifact = artifact;
			this.mogram = mogram;
		}

		@Override
		public String name() {
			return name == null ? name = parameterValue(mogram, "name", 0) : name;
		}

		@Override
		public String value() {
			return value == null ? value = parameterValue(mogram, "defaultValue", 1) : value;
		}

		@Override
		public String description() {
			return description == null ? description = parameterValue(mogram, "description", 2) : description;
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
