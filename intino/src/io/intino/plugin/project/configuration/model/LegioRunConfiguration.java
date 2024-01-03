package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;

import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioRunConfiguration implements Configuration.RunConfiguration {
	private final LegioArtifact artifact;
	private final Mogram node;

	public LegioRunConfiguration(LegioArtifact artifact, Mogram node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	public String name() {
		return node.name();
	}

	@Override
	public String mainClass() {
		return parameterValue(node, "mainClass");
	}

	@Override
	public String vmOptions() {
		return parameterValue(node, "vmOptions");
	}

	@Override
	public List<Argument> arguments() {
		return TaraPsiUtil.componentsOfType(node, "Argument").stream().
				map(n -> new LegioArgument(this, n)).
				collect(Collectors.toList());
	}

	public String argumentsChain() {
		return finalArguments().entrySet().stream()
				.filter(argument -> argument.getValue() != null)
				.map(argument -> "\"" + argument.getKey() + "=" + argument.getValue() + "\" ").collect(Collectors.joining());
	}

	@Override
	public Configuration root() {
		return artifact.root();
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return null;
	}

	public static class LegioArgument implements Argument {
		private final LegioRunConfiguration runConfiguration;
		private final Mogram node;

		public LegioArgument(LegioRunConfiguration runConfiguration, Mogram node) {
			this.runConfiguration = runConfiguration;
			this.node = node;
		}

		@Override
		public String name() {
			return parameterValue(node, "name", 0);
		}

		@Override
		public String value() {
			return parameterValue(node, "value", 1);
		}

		@Override
		public Configuration root() {
			return runConfiguration.root();
		}

		@Override
		public Configuration.ConfigurationNode owner() {
			return runConfiguration;
		}
	}
}
