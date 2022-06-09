package io.intino.legio.model;


import io.intino.magritte.framework.Graph;

public class LegioGraph extends AbstractGraph {

	public LegioGraph(Graph graph) {
		super(graph);
	}

	public LegioGraph(Graph graph, LegioGraph wrapper) {
		super(graph, wrapper);
	}
}