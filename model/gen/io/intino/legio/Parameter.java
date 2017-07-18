package io.intino.legio;

import io.intino.legio.*;


public class Parameter extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Component, io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String name$;
	protected java.lang.String value;
	protected java.lang.String description;

	public Parameter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String name$() {
		return name$;
	}

	public java.lang.String value() {
		return value;
	}

	public java.lang.String description() {
		return description;
	}

	public void name$(java.lang.String value) {
		this.name$ = value;
	}

	public void value(java.lang.String value) {
		this.value = value;
	}

	public void description(java.lang.String value) {
		this.description = value;
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name$)));
		map.put("value", new java.util.ArrayList(java.util.Collections.singletonList(this.value)));
		map.put("description", new java.util.ArrayList(java.util.Collections.singletonList(this.description)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Parameter.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("name")) this.name$ = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("value")) this.value = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("description")) this.description = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("name")) this.name$ = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("value")) this.value = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("description")) this.description = (java.lang.String) values.get(0);
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
