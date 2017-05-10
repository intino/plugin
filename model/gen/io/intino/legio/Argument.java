package io.intino.legio;

import io.intino.legio.*;


public class Argument extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Component, io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String value;

	public Argument(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String value() {
		return value;
	}

	public void value(java.lang.String value) {
		this.value = value;
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("value", new java.util.ArrayList(java.util.Collections.singletonList(this.value)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Argument.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("value")) this.value = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("value")) this.value = (java.lang.String) values.get(0);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create {
		protected final java.lang.String name;

		public Create(java.lang.String name) {
			this.name = name;
		}
		
	}
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
