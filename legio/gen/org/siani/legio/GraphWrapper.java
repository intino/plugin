package org.siani.legio;

import tara.magritte.Graph;

import java.util.List;

public class GraphWrapper extends tara.magritte.GraphWrapper {

	protected Graph graph;
	private org.siani.legio.Project project;
	private List<org.siani.legio.level.LevelProject> levelProjectList;
	private List<org.siani.legio.platform.PlatformProject> platformProjectList;
	private List<org.siani.legio.application.ApplicationProject> applicationProjectList;
	private List<org.siani.legio.system.SystemProject> systemProjectList;

	public GraphWrapper(Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("Legio");
	    update();
	}

	protected void update() {
		project = this.graph.rootList(org.siani.legio.Project.class).stream().findFirst().orElse(null);
		levelProjectList = this.graph.rootList(org.siani.legio.level.LevelProject.class);
		platformProjectList = this.graph.rootList(org.siani.legio.platform.PlatformProject.class);
		applicationProjectList = this.graph.rootList(org.siani.legio.application.ApplicationProject.class);
		systemProjectList = this.graph.rootList(org.siani.legio.system.SystemProject.class);
	}

	@Override
	protected void addNode(tara.magritte.Node node) {
		if (node.is("Project")) this.project = node.as(org.siani.legio.Project.class);
		if (node.is("LevelProject")) this.levelProjectList.add(node.as(org.siani.legio.level.LevelProject.class));
		if (node.is("PlatformProject")) this.platformProjectList.add(node.as(org.siani.legio.platform.PlatformProject.class));
		if (node.is("ApplicationProject")) this.applicationProjectList.add(node.as(org.siani.legio.application.ApplicationProject.class));
		if (node.is("SystemProject")) this.systemProjectList.add(node.as(org.siani.legio.system.SystemProject.class));
	}

	@Override
	protected void removeNode(tara.magritte.Node node) {
		if (node.is("Project")) this.project = null;
		if (node.is("LevelProject")) this.levelProjectList.remove(node.as(org.siani.legio.level.LevelProject.class));
		if (node.is("PlatformProject")) this.platformProjectList.remove(node.as(org.siani.legio.platform.PlatformProject.class));
		if (node.is("ApplicationProject")) this.applicationProjectList.remove(node.as(org.siani.legio.application.ApplicationProject.class));
		if (node.is("SystemProject")) this.systemProjectList.remove(node.as(org.siani.legio.system.SystemProject.class));
	}

	public String message(String language, String key, Object... parameters) {
		return graph.i18n().message(language, key, parameters);
	}

	public java.net.URL resourceAsMessage(String language, String key) {
		return graph.loadResource(graph.i18n().message(language, key));
	}

	public java.util.Map<String,String> keysIn(String language) {
		return graph.i18n().wordsIn(language);
	}

	public tara.magritte.Concept concept(String concept) {
		return graph.concept(concept);
	}

	public tara.magritte.Concept concept(java.lang.Class<? extends tara.magritte.Layer> layerClass) {
		return graph.concept(layerClass);
	}

	public List<tara.magritte.Concept> conceptList() {
		return graph.conceptList();
	}

	public List<tara.magritte.Concept> conceptList(java.util.function.Predicate<tara.magritte.Concept> predicate) {
		return graph.conceptList(predicate);
	}

	public tara.magritte.Node createRoot(tara.magritte.Concept concept, String namespace) {
		return graph.createRoot(concept, namespace);
	}

	public <T extends tara.magritte.Layer> T createRoot(java.lang.Class<T> layerClass, String namespace) {
		return graph.createRoot(layerClass, namespace);
	}

	public tara.magritte.Node createRoot(String concept, String namespace) {
		return graph.createRoot(concept, namespace);
	}

	public <T extends tara.magritte.Layer> T createRoot(java.lang.Class<T> layerClass, String namespace, String id) {
		return graph.createRoot(layerClass, namespace, id);
	}

	public tara.magritte.Node createRoot(String concept, String namespace, String id) {
		return graph.createRoot(concept, namespace, id);
	}

	public tara.magritte.Node createRoot(tara.magritte.Concept concept, String namespace, String id) {
		return graph.createRoot(concept, namespace, id);
	}

	public org.siani.legio.Project project() {
	    return project;
	}

	public List<org.siani.legio.level.LevelProject> levelProjectList() {
	    return levelProjectList;
	}

	public List<org.siani.legio.platform.PlatformProject> platformProjectList() {
	    return platformProjectList;
	}

	public List<org.siani.legio.application.ApplicationProject> applicationProjectList() {
	    return applicationProjectList;
	}

	public List<org.siani.legio.system.SystemProject> systemProjectList() {
	    return systemProjectList;
	}

	public List<org.siani.legio.level.LevelProject> levelProjectList(java.util.function.Predicate<org.siani.legio.level.LevelProject> predicate) {
	    return levelProjectList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public org.siani.legio.level.LevelProject levelProject(int index) {
		return levelProjectList.get(index);
	}

	public List<org.siani.legio.platform.PlatformProject> platformProjectList(java.util.function.Predicate<org.siani.legio.platform.PlatformProject> predicate) {
	    return platformProjectList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public org.siani.legio.platform.PlatformProject platformProject(int index) {
		return platformProjectList.get(index);
	}

	public List<org.siani.legio.application.ApplicationProject> applicationProjectList(java.util.function.Predicate<org.siani.legio.application.ApplicationProject> predicate) {
	    return applicationProjectList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public org.siani.legio.application.ApplicationProject applicationProject(int index) {
		return applicationProjectList.get(index);
	}

	public List<org.siani.legio.system.SystemProject> systemProjectList(java.util.function.Predicate<org.siani.legio.system.SystemProject> predicate) {
	    return systemProjectList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public org.siani.legio.system.SystemProject systemProject(int index) {
		return systemProjectList.get(index);
	}

	public tara.magritte.Graph graph() {
		return graph;
	}

	public Create create() {
		return new Create("Misc", null);
	}

	public Create create(String namespace) {
		return new Create(namespace, null);
	}

	public Create create(String namespace, String name) {
		return new Create(namespace, name);
	}

	public class Create {
		private final String namespace;
		private final String name;

		public Create(String namespace, String name) {
			this.namespace = namespace;
			this.name = name;
		}

		public org.siani.legio.Project project(java.lang.String groupId, java.lang.String version) {
			org.siani.legio.Project newElement = GraphWrapper.this.graph.createRoot(org.siani.legio.Project.class, namespace, name).as(org.siani.legio.Project.class);
			newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
			return newElement;
		}

		public org.siani.legio.level.LevelProject levelProject() {
			org.siani.legio.level.LevelProject newElement = GraphWrapper.this.graph.createRoot(org.siani.legio.level.LevelProject.class, namespace, name).as(org.siani.legio.level.LevelProject.class);
			
			return newElement;
		}

		public org.siani.legio.platform.PlatformProject platformProject() {
			org.siani.legio.platform.PlatformProject newElement = GraphWrapper.this.graph.createRoot(org.siani.legio.platform.PlatformProject.class, namespace, name).as(org.siani.legio.platform.PlatformProject.class);
			
			return newElement;
		}

		public org.siani.legio.application.ApplicationProject applicationProject() {
			org.siani.legio.application.ApplicationProject newElement = GraphWrapper.this.graph.createRoot(org.siani.legio.application.ApplicationProject.class, namespace, name).as(org.siani.legio.application.ApplicationProject.class);
			
			return newElement;
		}

		public org.siani.legio.system.SystemProject systemProject() {
			org.siani.legio.system.SystemProject newElement = GraphWrapper.this.graph.createRoot(org.siani.legio.system.SystemProject.class, namespace, name).as(org.siani.legio.system.SystemProject.class);
			
			return newElement;
		}

	}


}