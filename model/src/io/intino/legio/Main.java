package io.intino.legio;

import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Destination;
import io.intino.legio.graph.LegioGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
	public static Map<String, String> finalArguments(io.intino.legio.graph.RunConfiguration self) {
		final Map<String, String> arguments = new HashMap<>();
		self.argumentList().forEach(a -> arguments.put(a.name(), a.value()));
		final Artifact artifact = self.core$().graph().as(LegioGraph.class).artifact();
		artifact.parameterList().stream().filter(parameter -> !arguments.containsKey(parameter.name())).forEach(parameter -> arguments.put(parameter.name(), parameter.defaultValue()));
		return arguments;
	}

	public static List<Destination> destinations(Artifact.Deployment self) {
		List<Destination> destinations = new ArrayList<>();
		if (self.pre() != null) destinations.add(self.pre());
		if (self.pro() != null) destinations.add(self.pro());
		return destinations;
	}
}