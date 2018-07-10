package io.intino.cesar.graph;

import io.intino.tara.magritte.Graph;

public class CesarGraph extends io.intino.cesar.graph.AbstractGraph {

	public CesarGraph(Graph graph) {
		super(graph);
	}

	public CesarGraph(io.intino.tara.magritte.Graph graph, CesarGraph wrapper) {
	    super(graph, wrapper);
	}
}