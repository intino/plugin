package org.siani.legio.system;

import org.siani.legio.*;

import java.util.*;

public class SystemProject extends org.siani.legio.level.LevelProject implements tara.magritte.tags.Terminal {
	
	
	
	
	

	public SystemProject(tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String groupId() {
		return _project.groupId();
	}

	public java.lang.String version() {
		return _project.version();
	}

	public void groupId(java.lang.String value) {
		this._project.groupId(value);
	}

	public void version(java.lang.String value) {
		this._project.version(value);
	}

	public java.util.List<org.siani.legio.Repository> repositoryList() {
		return (java.util.List<org.siani.legio.Repository>) _project.repositoryList();
	}

	public org.siani.legio.Repository repositoryList(int index) {
		return _project.repositoryList().get(index);
	}

	public org.siani.legio.Project.DSL dSL() {
		return _project.dSL();
	}

	public org.siani.legio.Project.Generation generation() {
		return _project.generation();
	}

	public java.util.List<org.siani.legio.Project.Dependencies> dependenciesList() {
		return (java.util.List<org.siani.legio.Project.Dependencies>) _project.dependenciesList();
	}

	public org.siani.legio.Project.Dependencies dependenciesList(int index) {
		return _project.dependenciesList().get(index);
	}

	

	

	

	

	public List<tara.magritte.Node> componentList() {
		java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		
		
		
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
		return map;
	}

	public tara.magritte.Concept concept() {
		return this.graph().concept(org.siani.legio.Project.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		_project.node().load(_project, name, values);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		_project.node().set(_project, name, values);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create extends org.siani.legio.level.LevelProject.Create {
		

		public Create(java.lang.String name) {
			super(name);
		}

		public org.siani.legio.Repository repository(java.lang.String url) {
		    org.siani.legio.Repository newElement = graph().concept(org.siani.legio.Repository.class).createNode(name, node()).as(org.siani.legio.Repository.class);
			newElement.node().set(newElement, "url", java.util.Collections.singletonList(url)); 
		    return newElement;
		}

		public org.siani.legio.Project.DSL dSL(java.lang.String version) {
		    org.siani.legio.Project.DSL newElement = graph().concept(org.siani.legio.Project.DSL.class).createNode(name, node()).as(org.siani.legio.Project.DSL.class);
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}

		public org.siani.legio.Project.Generation generation() {
		    org.siani.legio.Project.Generation newElement = graph().concept(org.siani.legio.Project.Generation.class).createNode(name, node()).as(org.siani.legio.Project.Generation.class);
		    return newElement;
		}

		public org.siani.legio.Project.Dependencies dependencies() {
		    org.siani.legio.Project.Dependencies newElement = graph().concept(org.siani.legio.Project.Dependencies.class).createNode(name, node()).as(org.siani.legio.Project.Dependencies.class);
		    return newElement;
		}
		
	}
	
	public org.siani.legio.LegioApplication application() {
		return ((org.siani.legio.LegioApplication) graph().application());
	}
}
