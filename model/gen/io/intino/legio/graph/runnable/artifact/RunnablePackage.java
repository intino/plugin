package io.intino.legio.graph.runnable.artifact;

import io.intino.legio.graph.*;


public class RunnablePackage extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String mainClass;


	protected io.intino.legio.graph.Artifact.Package _package;

	public RunnablePackage(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String mainClass() {
		return mainClass;
	}

	public io.intino.legio.graph.Artifact.Package.Mode mode() {
		return _package.mode();
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

	public RunnablePackage mainClass(java.lang.String value) {
		this.mainClass = value;
		return (RunnablePackage) this;
	}

	public RunnablePackage mode(io.intino.legio.graph.Artifact.Package.Mode value) {
		this._package.mode(value);
		return (RunnablePackage) this;
	}

	public RunnablePackage attachSources(boolean value) {
		this._package.attachSources(value);
		return (RunnablePackage) this;
	}

	public RunnablePackage attachDoc(boolean value) {
		this._package.attachDoc(value);
		return (RunnablePackage) this;
	}

	public RunnablePackage includeTests(boolean value) {
		this._package.includeTests(value);
		return (RunnablePackage) this;
	}

	public RunnablePackage classpathPrefix(java.lang.String value) {
		this._package.classpathPrefix(value);
		return (RunnablePackage) this;
	}

	public RunnablePackage finalName(java.lang.String value) {
		this._package.finalName(value);
		return (RunnablePackage) this;
	}

	public java.util.List<io.intino.legio.graph.Parameter> parameterList() {
		return (java.util.List<io.intino.legio.graph.Parameter>) _package.parameterList();
	}

	public io.intino.legio.graph.Parameter parameterList(int index) {
		return _package.parameterList().get(index);
	}

	public java.util.List<io.intino.legio.graph.Artifact.Package.MavenPlugin> mavenPluginList() {
		return (java.util.List<io.intino.legio.graph.Artifact.Package.MavenPlugin>) _package.mavenPluginList();
	}

	public io.intino.legio.graph.Artifact.Package.MavenPlugin mavenPluginList(int index) {
		return _package.mavenPluginList().get(index);
	}





	protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());


		return new java.util.ArrayList<>(components);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("mainClass", new java.util.ArrayList(java.util.Collections.singletonList(this.mainClass)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("mainClass")) this.mainClass = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("mainClass")) this.mainClass = (java.lang.String) values.get(0);
	}

	@Override
	protected void sync$(io.intino.tara.magritte.Layer layer) {
		super.sync$(layer);
	    if (layer instanceof io.intino.legio.graph.Artifact.Package) _package = (io.intino.legio.graph.Artifact.Package) layer;

	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create  {
		protected final java.lang.String name;

		public Create(java.lang.String name) {
			this.name = name;
		}

		public io.intino.legio.graph.Parameter parameter(java.lang.String name, java.lang.String value) {
		    io.intino.legio.graph.Parameter newElement = core$().graph().concept(io.intino.legio.graph.Parameter.class).createNode(name, core$()).as(io.intino.legio.graph.Parameter.class);
			newElement.core$().set(newElement, "name", java.util.Collections.singletonList(name));
			newElement.core$().set(newElement, "value", java.util.Collections.singletonList(value));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Package.MavenPlugin mavenPlugin(java.lang.String code) {
		    io.intino.legio.graph.Artifact.Package.MavenPlugin newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Package.MavenPlugin.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.Package.MavenPlugin.class);
			newElement.core$().set(newElement, "code", java.util.Collections.singletonList(code));
		    return newElement;
		}

	}

	public Clear clear() {
		return new Clear();
	}

	public class Clear  {
		public void parameter(java.util.function.Predicate<io.intino.legio.graph.Parameter> filter) {
			new java.util.ArrayList<>(parameterList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}

		public void mavenPlugin(java.util.function.Predicate<io.intino.legio.graph.Artifact.Package.MavenPlugin> filter) {
			new java.util.ArrayList<>(mavenPluginList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}
	}

	public io.intino.legio.graph.LegioGraph graph() {
		return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
	}
}
