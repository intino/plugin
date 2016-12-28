package io.intino.legio;

import io.intino.legio.*;


public class Parameter extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Component, io.intino.tara.magritte.tags.Terminal {
	protected boolean required;

	public Parameter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public boolean required() {
		return required;
	}

	public void required(boolean value) {
		this.required = value;
	}

	public io.intino.legio.type.TypeParameter asType() {
		return this.as(io.intino.legio.type.TypeParameter.class);
	}

	public io.intino.legio.type.TypeParameter asType(Expression<java.lang.String> type) {
		io.intino.legio.type.TypeParameter newElement = addFacet(io.intino.legio.type.TypeParameter.class);
		newElement.node().set(newElement, "type", java.util.Collections.singletonList(type)); 
	    return newElement;
	}

	public boolean isType() {
		return is(io.intino.legio.type.TypeParameter.class);
	}

	public void removeType() {
		this.removeFacet(io.intino.legio.type.TypeParameter.class);
	}

	public io.intino.legio.bool.BoolParameter asBool() {
		return this.as(io.intino.legio.bool.BoolParameter.class);
	}

	public io.intino.legio.bool.BoolParameter asBool(boolean value) {
		io.intino.legio.bool.BoolParameter newElement = addFacet(io.intino.legio.bool.BoolParameter.class);
		newElement.node().set(newElement, "value", java.util.Collections.singletonList(value)); 
	    return newElement;
	}

	public boolean isBool() {
		return is(io.intino.legio.bool.BoolParameter.class);
	}

	public void removeBool() {
		this.removeFacet(io.intino.legio.bool.BoolParameter.class);
	}

	public io.intino.legio.real.RealParameter asReal() {
		return this.as(io.intino.legio.real.RealParameter.class);
	}

	public io.intino.legio.real.RealParameter asReal(double value) {
		io.intino.legio.real.RealParameter newElement = addFacet(io.intino.legio.real.RealParameter.class);
		newElement.node().set(newElement, "value", java.util.Collections.singletonList(value)); 
	    return newElement;
	}

	public boolean isReal() {
		return is(io.intino.legio.real.RealParameter.class);
	}

	public void removeReal() {
		this.removeFacet(io.intino.legio.real.RealParameter.class);
	}

	public io.intino.legio.text.TextParameter asText() {
		return this.as(io.intino.legio.text.TextParameter.class);
	}

	public io.intino.legio.text.TextParameter asText(java.lang.String value) {
		io.intino.legio.text.TextParameter newElement = addFacet(io.intino.legio.text.TextParameter.class);
		newElement.node().set(newElement, "value", java.util.Collections.singletonList(value)); 
	    return newElement;
	}

	public boolean isText() {
		return is(io.intino.legio.text.TextParameter.class);
	}

	public void removeText() {
		this.removeFacet(io.intino.legio.text.TextParameter.class);
	}

	public io.intino.legio.int.IntParameter asInt() {
		return this.as(io.intino.legio.int.IntParameter.class);
	}

	public io.intino.legio.int.IntParameter asInt(int value) {
		io.intino.legio.int.IntParameter newElement = addFacet(io.intino.legio.int.IntParameter.class);
		newElement.node().set(newElement, "value", java.util.Collections.singletonList(value)); 
	    return newElement;
	}

	public boolean isInt() {
		return is(io.intino.legio.int.IntParameter.class);
	}

	public void removeInt() {
		this.removeFacet(io.intino.legio.int.IntParameter.class);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("required", new java.util.ArrayList(java.util.Collections.singletonList(this.required)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Parameter.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("required")) this.required = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("required")) this.required = (java.lang.Boolean) values.get(0);
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
	
	public io.intino.legio.LegioApplication application() {
		return ((io.intino.legio.LegioApplication) graph().application());
	}
}
