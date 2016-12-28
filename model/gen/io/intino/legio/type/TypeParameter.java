package io.intino.legio.type;

import io.intino.legio.*;


public abstract class TypeParameter extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected Expression<java.lang.String> type;
	protected io.intino.legio.Parameter _parameter;

	public TypeParameter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String type() {
		return type.value();
	}

	public boolean required() {
		return _parameter.required();
	}

	public void type(Expression<java.lang.String> value) {
		this.type = FunctionLoader.load(value, this, Expression.class);
	}

	public void required(boolean value) {
		this._parameter.required(value);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("type", new java.util.ArrayList(java.util.Collections.singletonList(this.type)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Parameter.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("type")) this.type = FunctionLoader.load(values, this, Expression.class).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("type")) this.type = FunctionLoader.load(values.get(0), this, Expression.class);
	}

	@Override
	protected void _sync(io.intino.tara.magritte.Layer layer) {
		super._sync(layer);
	    if (layer instanceof io.intino.legio.Parameter) _parameter = (io.intino.legio.Parameter) layer;
	    
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
