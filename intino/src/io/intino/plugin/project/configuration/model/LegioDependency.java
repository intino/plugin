package io.intino.plugin.project.configuration.model;

import com.intellij.openapi.application.ApplicationManager;
import io.intino.Configuration;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraMogramImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Parameter;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.MetadataResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioDependency implements Configuration.Artifact.Dependency {
	private final LegioArtifact artifact;
	private final TaraMogram mogram;

	public LegioDependency(LegioArtifact artifact, TaraMogram mogram) {
		this.artifact = artifact;
		this.mogram = mogram;
	}

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
		boolean versionFollower = mogram != null && mogram.appliedFacets().stream().anyMatch(a -> a.type().equals("ArtifactVersionFollower"));
		return versionFollower ?
				root().artifact().version() :
				parameterValue(mogram, "version", 2);
	}

	@Override
	public void version(String newVersion) {
		writeCommandAction(mogram.getProject(), mogram.getContainingFile()).run(() -> {
			Parameter version = mogram.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(mogram.parameters().get(2));
			if (version != null) version.substituteValues(Collections.singletonList(newVersion));
		});
		ApplicationManager.getApplication().invokeAndWait(() -> IntinoUtil.commitDocument(mogram.getContainingFile()));
	}

	@Override
	public String scope() {
		return ((TaraMogramImpl) mogram).simpleType();
	}

	@Override
	public List<Exclude> excludes() {
		return TaraPsiUtil.componentsOfType(mogram, "Exclude").stream().
				map(e -> new LegioExclude((TaraMogram) e)).
				collect(Collectors.toList());
	}

	@Override
	public String effectiveVersion() {
		MetadataResult result = MavenDependencyResolver.metadata(new DefaultArtifact(identifier()));
		return result.isResolved() ? result.getMetadata().getVersion() : null;
	}

	@Override
	public void effectiveVersion(String version) {
	}

	@Override
	public boolean transitive() {
		return false;
	}

	@Override
	public boolean toModule() {//TODO
		return false;
	}

	@Override
	public void toModule(boolean toModule) {
	}

	public TaraMogram node() {
		return mogram;
	}

	@Override
	public String toString() {
		return mogram.getText();
	}

	@Override
	public Configuration root() {
		return artifact.root();
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return artifact;
	}

	public static class LegioExclude implements Exclude {
		private final TaraMogram node;

		public LegioExclude(TaraMogram node) {
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
		LegioCompile(LegioArtifact artifact, TaraMogram node) {
			super(artifact, node);
		}
	}

	static class LegioTest extends LegioDependency implements Configuration.Artifact.Dependency.Test {
		LegioTest(LegioArtifact artifact, TaraMogram node) {
			super(artifact, node);
		}
	}

	static class LegioRuntime extends LegioDependency implements Configuration.Artifact.Dependency.Runtime {
		LegioRuntime(LegioArtifact artifact, TaraMogram node) {
			super(artifact, node);
		}
	}

	static class LegioProvided extends LegioDependency implements Configuration.Artifact.Dependency.Provided {
		LegioProvided(LegioArtifact artifact, TaraMogram node) {
			super(artifact, node);
		}
	}

	static class LegioWeb extends LegioDependency implements Configuration.Artifact.Dependency.Web {
		LegioWeb(LegioArtifact artifact, TaraMogram node) {
			super(artifact, node);
		}
	}
}