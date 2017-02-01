package io.intino.legio.real;

import io.intino.legio.*;


public class RealParameter extends io.intino.legio.type.TypeParameter implements io.intino.tara.magritte.tags.Terminal {
	protected double value;

	public RealParameter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public double value() {
		return value;
	}

	public boolean required() {
		return _parameter.required();
	}

	public void value(double value) {
		this.value = value;
	}

	public void required(boolean value) {
		this._parameter.required(value);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
		map.put("value", new java.util.ArrayList(java.util.Collections.singletonList(this.value)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Parameter.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		_parameter.node().load(_parameter, name, values);
		if (name.equalsIgnoreCase("value")) this.value = io.intino.tara.magritte.loaders.DoubleLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		_parameter.node().set(_parameter, name, values);
		if (name.equalsIgnoreCase("value")) this.value = (java.lang.Double) values.get(0);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create extends io.intino.legio.type.TypeParameter.Create {
		

		public Create(java.lang.String name) {
			super(name);
		}
		
	}
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
