package io.intino.legio.graph;

import io.intino.legio.graph.*;


public class Server extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String cesar;

	public Server(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String cesar() {
		return cesar;
	}

	public Server cesar(java.lang.String value) {
		this.cesar = value;
		return (Server) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("cesar", new java.util.ArrayList(java.util.Collections.singletonList(this.cesar)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("cesar")) this.cesar = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("cesar")) this.cesar = (java.lang.String) values.get(0);
	}


	public io.intino.legio.graph.LegioGraph graph() {
		return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
	}
}
