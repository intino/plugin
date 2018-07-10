package io.intino.legio.graph;

import io.intino.tara.magritte.Graph;

public class LegioGraph extends io.intino.legio.graph.AbstractGraph {

	public LegioGraph(Graph graph) {
		super(graph);
	}

	public LegioGraph(io.intino.tara.magritte.Graph graph, LegioGraph wrapper) {
	    super(graph, wrapper);
	}
}