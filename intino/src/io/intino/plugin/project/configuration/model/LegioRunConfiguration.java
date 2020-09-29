package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioRunConfiguration implements Configuration.RunConfiguration {
	private final LegioArtifact artifact;
	private final Node node;

	public LegioRunConfiguration(LegioArtifact artifact, Node node) {
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
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> argument : finalArguments().entrySet())
			builder.append("\"").append(argument.getKey()).append("=").append(argument.getValue()).append("\" ");
		return builder.toString();
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
		private final Node node;

		public LegioArgument(LegioRunConfiguration runConfiguration, Node node) {
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
