package io.intino.legio.graph;

import io.intino.legio.graph.*;


public class Parameter extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Component, io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String name;
	protected java.lang.String defaultValue;
	protected java.lang.String description;

	public Parameter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String name() {
		return name;
	}

	public java.lang.String defaultValue() {
		return defaultValue;
	}

	public java.lang.String description() {
		return description;
	}

	public Parameter name(java.lang.String value) {
		this.name = value;
		return (Parameter) this;
	}

	public Parameter defaultValue(java.lang.String value) {
		this.defaultValue = value;
		return (Parameter) this;
	}

	public Parameter description(java.lang.String value) {
		this.description = value;
		return (Parameter) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name)));
		map.put("defaultValue", new java.util.ArrayList(java.util.Collections.singletonList(this.defaultValue)));
		map.put("description", new java.util.ArrayList(java.util.Collections.singletonList(this.description)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("name")) this.name = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("defaultValue")) this.defaultValue = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("description")) this.description = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("name")) this.name = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("defaultValue")) this.defaultValue = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("description")) this.description = (java.lang.String) values.get(0);
	}


	public io.intino.legio.graph.LegioGraph graph() {
		return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
	}
}
