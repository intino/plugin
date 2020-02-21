package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConfigurationDependencyCreationAction extends PostCompileAction {
	private final String artifactId;
	private final String version;
	private final String groupId;

	public ConfigurationDependencyCreationAction(Module module, List<String> parameters) {
		this(module, parameters.get(1));
	}

	public ConfigurationDependencyCreationAction(Module module, String identifier) {
		super(module);
		String[] split = identifier.split(":");
		this.groupId = split[0];
		this.artifactId = split[1];
		this.version = split[2];
	}

	@Override
	public FinishStatus execute() {
		Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration == null) return FinishStatus.NothingDone;
		Configuration.Artifact.Dependency dep = configuration.artifact().dependencies().stream().filter(d -> d.groupId().equals(groupId) && d.artifactId().equals(artifactId)).findFirst().orElse(null);
		if (dep == null) {
			((LegioArtifact) configuration.artifact()).addDependencies(dependency());
			return FinishStatus.RequiresReload;
		} else if (!dep.version().equals(version) && isHigher(dep.version())) {
			dep.version(version);
			return FinishStatus.RequiresReload;
		}
		return FinishStatus.NothingDone;
	}

	private boolean isHigher(String version) {
		try {
			return new Version(this.version).compareTo(new Version(version)) > 0;
		} catch (IntinoException e) {
			Logger.getInstance(this.getClass()).error(e);
			return false;
		}
	}

	@NotNull
	private Configuration.Artifact.Dependency.Compile dependency() {
		return new Configuration.Artifact.Dependency.Compile() {
			@Override
			public String groupId() {
				return groupId;
			}

			@Override
			public String artifactId() {
				return artifactId;
			}

			@Override
			public String version() {
				return version;
			}

			@Override
			public void version(String newVersion) {

			}

			@Override
			public String scope() {
				return "Compile";
			}

			@Override
			public List<Exclude> excludes() {
				return null;
			}

			@Override
			public String effectiveVersion() {
				return null;
			}

			@Override
			public void effectiveVersion(String version) {

			}

			@Override
			public boolean transitive() {
				return false;
			}

			@Override
			public boolean resolved() {
				return false;
			}

			@Override
			public void resolved(boolean resolved) {

			}

			@Override
			public boolean toModule() {
				return false;
			}

			@Override
			public void toModule(boolean toModule) {

			}
		};
	}
}
