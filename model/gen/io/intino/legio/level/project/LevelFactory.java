package io.intino.legio.level.project;

import io.intino.legio.*;


public abstract class LevelFactory extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String language;
	protected java.lang.String version;
	protected java.lang.String effectiveVersion;
	
	protected io.intino.legio.Project.Factory _factory;

	public LevelFactory(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String language() {
		return language;
	}

	public java.lang.String version() {
		return version;
	}

	public java.lang.String effectiveVersion() {
		return effectiveVersion;
	}

	public java.lang.String inPackage() {
		return _factory.inPackage();
	}

	public void language(java.lang.String value) {
		this.language = value;
	}

	public void version(java.lang.String value) {
		this.version = value;
	}

	public void effectiveVersion(java.lang.String value) {
		this.effectiveVersion = value;
	}

	public void inPackage(java.lang.String value) {
		this._factory.inPackage(value);
	}

	public io.intino.legio.Project.Factory.Interface interface$() {
		return _factory.interface$();
	}

	

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("language", new java.util.ArrayList(java.util.Collections.singletonList(this.language)));
		map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
		map.put("effectiveVersion", new java.util.ArrayList(java.util.Collections.singletonList(this.effectiveVersion)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Project.Factory.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("language")) this.language = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("language")) this.language = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = (java.lang.String) values.get(0);
	}

	@Override
	protected void _sync(io.intino.tara.magritte.Layer layer) {
		super._sync(layer);
	    if (layer instanceof io.intino.legio.Project.Factory) _factory = (io.intino.legio.Project.Factory) layer;
	    
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

		public io.intino.legio.Project.Factory.Interface interface$(java.lang.String version) {
		    io.intino.legio.Project.Factory.Interface newElement = graph().concept(io.intino.legio.Project.Factory.Interface.class).createNode(name, node()).as(io.intino.legio.Project.Factory.Interface.class);
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}
		
	}
	
	public io.intino.legio.LegioApplication application() {
		return ((io.intino.legio.LegioApplication) graph().application());
	}
}
