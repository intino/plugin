package io.intino.legio.runnable.lifecycle;

import io.intino.legio.*;


public class RunnablePackage extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String mainClass;
	
	protected io.intino.legio.LifeCycle.Package _package;

	public RunnablePackage(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String mainClass() {
		return mainClass;
	}

	public io.intino.legio.LifeCycle.Package.Type Type() {
		return _package.type();
	}

	public boolean attachSources() {
		return _package.attachSources();
	}

	public boolean attachDoc() {
		return _package.attachDoc();
	}

	public boolean includeTests() {
		return _package.includeTests();
	}

	public java.lang.String classpathPrefix() {
		return _package.classpathPrefix();
	}

	public java.lang.String finalName() {
		return _package.finalName();
	}

	public void mainClass(java.lang.String value) {
		this.mainClass = value;
	}

	public void type(io.intino.legio.LifeCycle.Package.Type value) {
		this._package.type(value);
	}

	public void attachSources(boolean value) {
		this._package.attachSources(value);
	}

	public void attachDoc(boolean value) {
		this._package.attachDoc(value);
	}

	public void includeTests(boolean value) {
		this._package.includeTests(value);
	}

	public void classpathPrefix(java.lang.String value) {
		this._package.classpathPrefix(value);
	}

	public void finalName(java.lang.String value) {
		this._package.finalName(value);
	}

	public java.util.List<io.intino.legio.LifeCycle.Package.MavenPlugin> mavenPluginList() {
		return (java.util.List<io.intino.legio.LifeCycle.Package.MavenPlugin>) _package.mavenPluginList();
	}

	public io.intino.legio.LifeCycle.Package.MavenPlugin mavenPluginList(int index) {
		return _package.mavenPluginList().get(index);
	}

	

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("mainClass", new java.util.ArrayList(java.util.Collections.singletonList(this.mainClass)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.LifeCycle.Package.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("mainClass")) this.mainClass = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("mainClass")) this.mainClass = (java.lang.String) values.get(0);
	}

	@Override
	protected void _sync(io.intino.tara.magritte.Layer layer) {
		super._sync(layer);
	    if (layer instanceof io.intino.legio.LifeCycle.Package) _package = (io.intino.legio.LifeCycle.Package) layer;
	    
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

		public io.intino.legio.LifeCycle.Package.MavenPlugin mavenPlugin(java.lang.String code) {
		    io.intino.legio.LifeCycle.Package.MavenPlugin newElement = graph().concept(io.intino.legio.LifeCycle.Package.MavenPlugin.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Package.MavenPlugin.class);
			newElement.node().set(newElement, "code", java.util.Collections.singletonList(code)); 
		    return newElement;
		}
		
	}
	
	public io.intino.legio.LegioApplication application() {
		return ((io.intino.legio.LegioApplication) graph().application());
	}
}
