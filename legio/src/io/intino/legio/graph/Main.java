package io.intino.legio.graph;

import java.util.HashMap;
import java.util.Map;

public class Main {
	public static Map<String, String> finalArguments(io.intino.legio.graph.RunConfiguration self) {
		final Map<String, String> arguments = new HashMap<>();
		self.argumentList().forEach(a -> arguments.put(a.name(), a.value()));
		final Artifact artifact = self.core$().graph().as(LegioGraph.class).artifact();
		artifact.parameterList().stream().filter(parameter -> !arguments.containsKey(parameter.name())).forEach(parameter -> arguments.put(parameter.name(), parameter.defaultValue()));
		return arguments;
	}
}