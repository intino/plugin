package io.intino.plugin.project.configuration.model;

import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.compiler.shared.Configuration;

import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioDependency implements Configuration.Artifact.Dependency {
	private final LegioArtifact artifact;
	private final DependencyAuditor auditor;
	private final TaraNode node;

	public LegioDependency(LegioArtifact artifact, DependencyAuditor auditor, TaraNode node) {
		this.artifact = artifact;
		this.auditor = auditor;
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
		return parameterValue(node, "version", 2);
	}

	@Override
	public String scope() {
		return node.simpleType();
	}

	@Override
	public List<Exclude> excludes() {
		return TaraPsiUtil.componentsOfType(node, "Exclude").stream().
				map(e -> new LegioExclude(this, (TaraNode) e)).
				collect(Collectors.toList());
	}

	@Override
	public String effectiveVersion() {
		return auditor.effectiveVersion(this.node);
	}

	@Override
	public void effectiveVersion(String version) {
		auditor.effectiveVersion(this.node, version);
	}

	@Override
	public boolean transitive() {
		return false;
	}

	@Override
	public boolean resolved() {
		return auditor.isResolved(this.node);
	}

	@Override
	public void resolved(boolean resolved) {
		auditor.isResolved(node, resolved);
	}

	@Override
	public boolean toModule() {
		return auditor.isToModule(node);
	}

	@Override
	public void toModule(boolean toModule) {
		auditor.isToModule(node, toModule);
	}

	public TaraNode node() {
		return node;
	}

	public static class LegioExclude implements Exclude {
		private final LegioDependency dependency;
		private final TaraNode node;

		public LegioExclude(LegioDependency dependency, TaraNode node) {
			this.dependency = dependency;
			this.node = node;
		}

		@Override
		public String groupId() {
			return parameterValue(node, "groupId");
		}

		@Override
		public String artifactId() {
			return parameterValue(node, "artifactId");
		}
	}

	static class LegioCompile extends LegioDependency implements Configuration.Artifact.Dependency.Compile {
		LegioCompile(LegioArtifact artifact, DependencyAuditor auditor, TaraNode node) {
			super(artifact, auditor, node);
		}
	}


	static class LegioTest extends LegioDependency implements Configuration.Artifact.Dependency.Test {
		LegioTest(LegioArtifact artifact, DependencyAuditor auditor, TaraNode node) {
			super(artifact, auditor, node);
		}
	}

	static class LegioRuntime extends LegioDependency implements Configuration.Artifact.Dependency.Runtime {
		LegioRuntime(LegioArtifact artifact, DependencyAuditor auditor, TaraNode node) {
			super(artifact, auditor, node);
		}
	}

	static class LegioProvided extends LegioDependency implements Configuration.Artifact.Dependency.Provided {
		LegioProvided(LegioArtifact artifact, DependencyAuditor auditor, TaraNode node) {
			super(artifact, auditor, node);
		}
	}

	static class LegioWeb extends LegioDependency implements Configuration.Artifact.Dependency.Web {
		LegioWeb(LegioArtifact artifact, DependencyAuditor auditor, TaraNode node) {
			super(artifact, auditor, node);
		}
	}
}