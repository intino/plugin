package io.intino.legio.application.project;

import io.intino.legio.*;


public class ApplicationFactory extends io.intino.legio.level.project.LevelFactory implements tara.magritte.tags.Terminal {
	
	
	

	public ApplicationFactory(tara.magritte.Node node) {
		super(node);
	}

	public boolean persistent() {
		return _factory.persistent();
	}

	public int refactorId() {
		return _factory.refactorId();
	}

	public void persistent(boolean value) {
		this._factory.persistent(value);
	}

	public void refactorId(int value) {
		this._factory.refactorId(value);
	}

	public io.intino.legio.Project.Factory.Language language() {
		return _factory.language();
	}

	public io.intino.legio.Project.Factory.Generation generation() {
		return _factory.generation();
	}

	

	

	public java.util.List<tara.magritte.Node> componentList() {
		java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
		return map;
	}

	public tara.magritte.Concept concept() {
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

		public io.intino.legio.Project.Factory.Language language(java.lang.String version) {
		    io.intino.legio.Project.Factory.Language newElement = graph().concept(io.intino.legio.Project.Factory.Language.class).createNode(name, node()).as(io.intino.legio.Project.Factory.Language.class);
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}

		public io.intino.legio.Project.Factory.Generation generation() {
		    io.intino.legio.Project.Factory.Generation newElement = graph().concept(io.intino.legio.Project.Factory.Generation.class).createNode(name, node()).as(io.intino.legio.Project.Factory.Generation.class);
		    return newElement;
		}
		
	}
	
	public io.intino.legio.LegioApplication application() {
		return ((io.intino.legio.LegioApplication) graph().application());
	}
}
