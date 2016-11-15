package io.intino.legio;

import io.intino.legio.*;

import java.util.*;

public class Parameter extends tara.magritte.Layer implements tara.magritte.tags.Component, tara.magritte.tags.Terminal {
	protected boolean required;

	public Parameter(tara.magritte.Node node) {
		super(node);
	}

	public boolean required() {
		return required;
	}

	public void required(boolean value) {
		this.required = value;
	}

	public io.intino.legio.integer.IntegerParameter asInteger() {
		tara.magritte.Layer as = this.as(io.intino.legio.integer.IntegerParameter.class);
		return as != null ? (io.intino.legio.integer.IntegerParameter) as : addFacet(io.intino.legio.integer.IntegerParameter.class);
	}

	public boolean isInteger() {
		return is(io.intino.legio.integer.IntegerParameter.class);
	}

	public io.intino.legio.type.TypeParameter asType() {
		return this.as(io.intino.legio.type.TypeParameter.class);
	}

	public io.intino.legio.type.TypeParameter asType(tara.magritte.Expression<java.lang.String> type) {
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
		tara.magritte.Layer as = this.as(io.intino.legio.bool.BoolParameter.class);
		return as != null ? (io.intino.legio.bool.BoolParameter) as : addFacet(io.intino.legio.bool.BoolParameter.class);
	}

	public boolean isBool() {
		return is(io.intino.legio.bool.BoolParameter.class);
	}

	public io.intino.legio.real.RealParameter asReal() {
		tara.magritte.Layer as = this.as(io.intino.legio.real.RealParameter.class);
		return as != null ? (io.intino.legio.real.RealParameter) as : addFacet(io.intino.legio.real.RealParameter.class);
	}

	public boolean isReal() {
		return is(io.intino.legio.real.RealParameter.class);
	}

	public io.intino.legio.text.TextParameter asText() {
		tara.magritte.Layer as = this.as(io.intino.legio.text.TextParameter.class);
		return as != null ? (io.intino.legio.text.TextParameter) as : addFacet(io.intino.legio.text.TextParameter.class);
	}

	public boolean isText() {
		return is(io.intino.legio.text.TextParameter.class);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("required", new java.util.ArrayList(java.util.Collections.singletonList(this.required)));
		return map;
	}

	public tara.magritte.Concept concept() {
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
