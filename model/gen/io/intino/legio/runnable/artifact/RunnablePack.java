package io.intino.legio.runnable.artifact;

import io.intino.legio.*;


public class RunnablePack extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String mainClass;
	
	protected io.intino.legio.Artifact.Pack _pack;

	public RunnablePack(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String mainClass() {
		return mainClass;
	}

	public io.intino.legio.Artifact.Pack.Mode mode() {
		return _pack.mode();
	}

	public boolean attachSources() {
		return _pack.attachSources();
	}

	public boolean attachDoc() {
		return _pack.attachDoc();
	}

	public boolean includeTests() {
		return _pack.includeTests();
	}

	public java.lang.String classpathPrefix() {
		return _pack.classpathPrefix();
	}

	public java.lang.String finalName() {
		return _pack.finalName();
	}

	public void mainClass(java.lang.String value) {
		this.mainClass = value;
	}

	public void mode(io.intino.legio.Artifact.Pack.Mode value) {
		this._pack.mode(value);
	}

	public void attachSources(boolean value) {
		this._pack.attachSources(value);
	}

	public void attachDoc(boolean value) {
		this._pack.attachDoc(value);
	}

	public void includeTests(boolean value) {
		this._pack.includeTests(value);
	}

	public void classpathPrefix(java.lang.String value) {
		this._pack.classpathPrefix(value);
	}

	public void finalName(java.lang.String value) {
		this._pack.finalName(value);
	}

	public java.util.List<io.intino.legio.Artifact.Pack.MavenPlugin> mavenPluginList() {
		return (java.util.List<io.intino.legio.Artifact.Pack.MavenPlugin>) _pack.mavenPluginList();
	}

	public io.intino.legio.Artifact.Pack.MavenPlugin mavenPluginList(int index) {
		return _pack.mavenPluginList().get(index);
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
		return this.graph().concept(io.intino.legio.Artifact.Pack.class);
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
	    if (layer instanceof io.intino.legio.Artifact.Pack) _pack = (io.intino.legio.Artifact.Pack) layer;
	    
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

		public io.intino.legio.Artifact.Pack.MavenPlugin mavenPlugin(java.lang.String code) {
		    io.intino.legio.Artifact.Pack.MavenPlugin newElement = graph().concept(io.intino.legio.Artifact.Pack.MavenPlugin.class).createNode(name, node()).as(io.intino.legio.Artifact.Pack.MavenPlugin.class);
			newElement.node().set(newElement, "code", java.util.Collections.singletonList(code)); 
		    return newElement;
		}
		
	}
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
