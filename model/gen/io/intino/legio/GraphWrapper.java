package io.intino.legio;

import tara.magritte.Graph;

import java.util.List;

public class GraphWrapper extends tara.magritte.GraphWrapper {

	protected Graph graph;
	private io.intino.legio.Project project;
	private List<io.intino.legio.level.project.LevelFactory> levelFactoryList;
	private List<io.intino.legio.platform.project.PlatformFactory> platformFactoryList;
	private List<io.intino.legio.application.project.ApplicationFactory> applicationFactoryList;
	private List<io.intino.legio.system.project.SystemFactory> systemFactoryList;

	public GraphWrapper(Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("Legio");
	    update();
	}

	protected void update() {
		project = this.graph.rootList(io.intino.legio.Project.class).stream().findFirst().orElse(null);
		levelFactoryList = this.graph.rootList(io.intino.legio.level.project.LevelFactory.class);
		platformFactoryList = this.graph.rootList(io.intino.legio.platform.project.PlatformFactory.class);
		applicationFactoryList = this.graph.rootList(io.intino.legio.application.project.ApplicationFactory.class);
		systemFactoryList = this.graph.rootList(io.intino.legio.system.project.SystemFactory.class);
	}

	@Override
	protected void addNode(tara.magritte.Node node) {
		if (node.is("Project")) this.project = node.as(io.intino.legio.Project.class);
		if (node.is("LevelFactory")) this.levelFactoryList.add(node.as(io.intino.legio.level.project.LevelFactory.class));
		if (node.is("PlatformFactory")) this.platformFactoryList.add(node.as(io.intino.legio.platform.project.PlatformFactory.class));
		if (node.is("ApplicationFactory")) this.applicationFactoryList.add(node.as(io.intino.legio.application.project.ApplicationFactory.class));
		if (node.is("SystemFactory")) this.systemFactoryList.add(node.as(io.intino.legio.system.project.SystemFactory.class));
	}

	@Override
	protected void removeNode(tara.magritte.Node node) {
		if (node.is("Project")) this.project = null;
		if (node.is("LevelFactory")) this.levelFactoryList.remove(node.as(io.intino.legio.level.project.LevelFactory.class));
		if (node.is("PlatformFactory")) this.platformFactoryList.remove(node.as(io.intino.legio.platform.project.PlatformFactory.class));
		if (node.is("ApplicationFactory")) this.applicationFactoryList.remove(node.as(io.intino.legio.application.project.ApplicationFactory.class));
		if (node.is("SystemFactory")) this.systemFactoryList.remove(node.as(io.intino.legio.system.project.SystemFactory.class));
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

	public io.intino.legio.Project project() {
	    return project;
	}

	public List<io.intino.legio.level.project.LevelFactory> levelFactoryList() {
	    return levelFactoryList;
	}

	public List<io.intino.legio.platform.project.PlatformFactory> platformFactoryList() {
	    return platformFactoryList;
	}

	public List<io.intino.legio.application.project.ApplicationFactory> applicationFactoryList() {
	    return applicationFactoryList;
	}

	public List<io.intino.legio.system.project.SystemFactory> systemFactoryList() {
	    return systemFactoryList;
	}

	public List<io.intino.legio.level.project.LevelFactory> levelFactoryList(java.util.function.Predicate<io.intino.legio.level.project.LevelFactory> predicate) {
	    return levelFactoryList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.level.project.LevelFactory levelFactory(int index) {
		return levelFactoryList.get(index);
	}

	public List<io.intino.legio.platform.project.PlatformFactory> platformFactoryList(java.util.function.Predicate<io.intino.legio.platform.project.PlatformFactory> predicate) {
	    return platformFactoryList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.platform.project.PlatformFactory platformFactory(int index) {
		return platformFactoryList.get(index);
	}

	public List<io.intino.legio.application.project.ApplicationFactory> applicationFactoryList(java.util.function.Predicate<io.intino.legio.application.project.ApplicationFactory> predicate) {
	    return applicationFactoryList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.application.project.ApplicationFactory applicationFactory(int index) {
		return applicationFactoryList.get(index);
	}

	public List<io.intino.legio.system.project.SystemFactory> systemFactoryList(java.util.function.Predicate<io.intino.legio.system.project.SystemFactory> predicate) {
	    return systemFactoryList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.system.project.SystemFactory systemFactory(int index) {
		return systemFactoryList.get(index);
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

		public io.intino.legio.Project project(java.lang.String groupId, java.lang.String version) {
			io.intino.legio.Project newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.Project.class, namespace, name).as(io.intino.legio.Project.class);
			newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
			return newElement;
		}

		public io.intino.legio.level.project.LevelFactory levelFactory() {
			io.intino.legio.level.project.LevelFactory newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.level.project.LevelFactory.class, namespace, name).as(io.intino.legio.level.project.LevelFactory.class);
			
			return newElement;
		}

		public io.intino.legio.platform.project.PlatformFactory platformFactory() {
			io.intino.legio.platform.project.PlatformFactory newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.platform.project.PlatformFactory.class, namespace, name).as(io.intino.legio.platform.project.PlatformFactory.class);
			
			return newElement;
		}

		public io.intino.legio.application.project.ApplicationFactory applicationFactory() {
			io.intino.legio.application.project.ApplicationFactory newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.application.project.ApplicationFactory.class, namespace, name).as(io.intino.legio.application.project.ApplicationFactory.class);
			
			return newElement;
		}

		public io.intino.legio.system.project.SystemFactory systemFactory() {
			io.intino.legio.system.project.SystemFactory newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.system.project.SystemFactory.class, namespace, name).as(io.intino.legio.system.project.SystemFactory.class);
			
			return newElement;
		}

	}


}