package io.intino.plugin.project.configuration;

import io.intino.legio.graph.Argument;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Destination;
import io.intino.legio.graph.RunConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class LegioDeployConfiguration implements Configuration.DeployConfiguration {
	private final Destination deployment;

	public LegioDeployConfiguration(Destination deployment) {
		this.deployment = deployment;
	}

	public String name() {
		return deployment.name$();
	}

	public boolean pro() {
		return deployment.i$(Artifact.Deployment.Pro.class);
	}

	public RunConfiguration runConfiguration() {
		return deployment.runConfiguration();
	}

	public List<Parameter> parameters() {
		return deployment.runConfiguration().argumentList().stream().map(this::wrapParameter).collect(Collectors.toList()); //TODO merge with defaultValues
	}

	@NotNull
	private Parameter wrapParameter(final Argument p) {
		return new Parameter() {
			public String name() {
				return p.name();
			}

			public String value() {
				return p.value();
			}
		};
	}
}
