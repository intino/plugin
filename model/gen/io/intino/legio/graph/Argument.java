package io.intino.legio.graph;

import io.intino.legio.graph.*;


public class Argument extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Component, io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String name;
	protected java.lang.String value;

	public Argument(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String name() {
		return name;
	}

	public java.lang.String value() {
		return value;
	}

	public Argument name(java.lang.String value) {
		this.name = value;
		return (Argument) this;
	}

	public Argument value(java.lang.String value) {
		this.value = value;
		return (Argument) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name)));
		map.put("value", new java.util.ArrayList(java.util.Collections.singletonList(this.value)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("name")) this.name = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("value")) this.value = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("name")) this.name = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("value")) this.value = (java.lang.String) values.get(0);
	}


	public io.intino.legio.graph.LegioGraph graph() {
		return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
	}
}
