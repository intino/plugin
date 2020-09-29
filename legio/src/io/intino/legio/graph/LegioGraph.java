package io.intino.legio.graph;


import io.intino.magritte.framework.Graph;

public class LegioGraph extends io.intino.legio.graph.AbstractGraph {

	public LegioGraph(Graph graph) {
		super(graph);
	}

	public LegioGraph(Graph graph, LegioGraph wrapper) {
		super(graph, wrapper);
	}
}