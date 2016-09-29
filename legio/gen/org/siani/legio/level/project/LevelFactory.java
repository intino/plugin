package org.siani.legio.level.project;

import org.siani.legio.*;

import java.util.*;

public abstract class LevelFactory extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
	
	
	protected org.siani.legio.Project.Factory _factory;

	public LevelFactory(tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String generationPackage() {
		return _factory.generationPackage();
	}

	public boolean persistent() {
		return _factory.persistent();
	}

	public int refactorId() {
		return _factory.refactorId();
	}

	public void generationPackage(java.lang.String value) {
		this._factory.generationPackage(value);
	}

	public void persistent(boolean value) {
		this._factory.persistent(value);
	}

	public void refactorId(int value) {
		this._factory.refactorId(value);
	}

	public org.siani.legio.Project.Factory.Modeling modeling() {
		return _factory.modeling();
	}

	

	public List<tara.magritte.Node> componentList() {
		java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		return map;
	}

	public tara.magritte.Concept concept() {
		return this.graph().concept(org.siani.legio.Project.Factory.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
	}

	@Override
	protected void _sync(tara.magritte.Layer layer) {
		super._sync(layer);
	    if (layer instanceof org.siani.legio.Project.Factory) _factory = (org.siani.legio.Project.Factory) layer;
	    
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

		public org.siani.legio.Project.Factory.Modeling modeling(java.lang.String language, java.lang.String version) {
		    org.siani.legio.Project.Factory.Modeling newElement = graph().concept(org.siani.legio.Project.Factory.Modeling.class).createNode(name, node()).as(org.siani.legio.Project.Factory.Modeling.class);
			newElement.node().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}
		
	}
	
	public org.siani.legio.LegioApplication application() {
		return ((org.siani.legio.LegioApplication) graph().application());
	}
}
