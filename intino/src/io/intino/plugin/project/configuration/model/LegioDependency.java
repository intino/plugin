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
	private final TaraMogram mogram;

	public LegioDependency(TaraMogram mogram) {
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
		return parameterValue(mogram, "version", 2);
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
		LegioCompile(TaraMogram node) {
			super(node);
		}
	}

	static class LegioTest extends LegioDependency implements Configuration.Artifact.Dependency.Test {
		LegioTest(TaraMogram node) {
			super(node);
		}
	}

	static class LegioRuntime extends LegioDependency implements Configuration.Artifact.Dependency.Runtime {
		LegioRuntime(TaraMogram node) {
			super(node);
		}
	}

	static class LegioProvided extends LegioDependency implements Configuration.Artifact.Dependency.Provided {
		LegioProvided(TaraMogram node) {
			super(node);
		}
	}

	static class LegioWeb extends LegioDependency implements Configuration.Artifact.Dependency.Web {
		LegioWeb(TaraMogram node) {
			super(node);
		}
	}
}