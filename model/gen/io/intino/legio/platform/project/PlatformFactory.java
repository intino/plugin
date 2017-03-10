package io.intino.legio.platform.project;

import io.intino.legio.*;


public class PlatformFactory extends io.intino.legio.level.project.LevelFactory implements io.intino.tara.magritte.tags.Terminal {
	
	
	

	public PlatformFactory(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String version() {
		return _factory.version();
	}

	public java.lang.String inPackage() {
		return _factory.inPackage();
	}

	public void version(java.lang.String value) {
		this._factory.version(value);
	}

	public void inPackage(java.lang.String value) {
		this._factory.inPackage(value);
	}

	public java.util.List<io.intino.legio.Project.Factory.Language> languageList() {
		return (java.util.List<io.intino.legio.Project.Factory.Language>) _factory.languageList();
	}

	public io.intino.legio.Project.Factory.Language languageList(int index) {
		return _factory.languageList().get(index);
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
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Project.Factory.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		_factory.node().load(_factory, name, values);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		_factory.node().set(_factory, name, values);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create extends io.intino.legio.level.project.LevelFactory.Create {
		

		public Create(java.lang.String name) {
			super(name);
		}

		public io.intino.legio.Project.Factory.Language language(java.lang.String name$, java.lang.String version) {
		    io.intino.legio.Project.Factory.Language newElement = graph().concept(io.intino.legio.Project.Factory.Language.class).createNode(name, node()).as(io.intino.legio.Project.Factory.Language.class);
			newElement.node().set(newElement, "name", java.util.Collections.singletonList(name$));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}

		public io.intino.legio.Project.Factory.Interface interface$(java.lang.String version) {
		    io.intino.legio.Project.Factory.Interface newElement = graph().concept(io.intino.legio.Project.Factory.Interface.class).createNode(name, node()).as(io.intino.legio.Project.Factory.Interface.class);
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}
		
	}
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
