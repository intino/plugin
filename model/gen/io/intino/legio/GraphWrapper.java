package io.intino.legio;

import io.intino.tara.magritte.Graph;

public class GraphWrapper extends io.intino.tara.magritte.GraphWrapper {

	protected io.intino.tara.magritte.Graph graph;
	private io.intino.legio.Artifact artifact;
	private java.util.List<io.intino.legio.Server> serverList;
	private java.util.List<io.intino.legio.Repository> repositoryList;
	private java.util.List<io.intino.legio.runnable.artifact.RunnablePack> runnablePackList;
	private java.util.List<io.intino.legio.level.artifact.LevelGeneration> levelGenerationList;
	private java.util.List<io.intino.legio.platform.artifact.PlatformGeneration> platformGenerationList;
	private java.util.List<io.intino.legio.application.artifact.ApplicationGeneration> applicationGenerationList;
	private java.util.List<io.intino.legio.system.artifact.SystemGeneration> systemGenerationList;

	public GraphWrapper(io.intino.tara.magritte.Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("Legio");
	}

	public void update() {
		artifact = this.graph.rootList(io.intino.legio.Artifact.class).stream().findFirst().orElse(null);
		serverList = this.graph.rootList(io.intino.legio.Server.class);
		repositoryList = this.graph.rootList(io.intino.legio.Repository.class);
		runnablePackList = this.graph.rootList(io.intino.legio.runnable.artifact.RunnablePack.class);
		levelGenerationList = this.graph.rootList(io.intino.legio.level.artifact.LevelGeneration.class);
		platformGenerationList = this.graph.rootList(io.intino.legio.platform.artifact.PlatformGeneration.class);
		applicationGenerationList = this.graph.rootList(io.intino.legio.application.artifact.ApplicationGeneration.class);
		systemGenerationList = this.graph.rootList(io.intino.legio.system.artifact.SystemGeneration.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		if (node.is("Artifact")) this.artifact = node.as(io.intino.legio.Artifact.class);
		if (node.is("Server")) this.serverList.add(node.as(io.intino.legio.Server.class));
		if (node.is("Repository")) this.repositoryList.add(node.as(io.intino.legio.Repository.class));
		if (node.is("Runnable#Artifact$Pack")) this.runnablePackList.add(node.as(io.intino.legio.runnable.artifact.RunnablePack.class));
		if (node.is("Level#Artifact$Generation")) this.levelGenerationList.add(node.as(io.intino.legio.level.artifact.LevelGeneration.class));
		if (node.is("Platform#Artifact$Generation")) this.platformGenerationList.add(node.as(io.intino.legio.platform.artifact.PlatformGeneration.class));
		if (node.is("Application#Artifact$Generation")) this.applicationGenerationList.add(node.as(io.intino.legio.application.artifact.ApplicationGeneration.class));
		if (node.is("System#Artifact$Generation")) this.systemGenerationList.add(node.as(io.intino.legio.system.artifact.SystemGeneration.class));
	}

	@Override
	protected void removeNode(io.intino.tara.magritte.Node node) {
		if (node.is("Artifact")) this.artifact = null;
		if (node.is("Server")) this.serverList.remove(node.as(io.intino.legio.Server.class));
		if (node.is("Repository")) this.repositoryList.remove(node.as(io.intino.legio.Repository.class));
		if (node.is("Runnable#Artifact$Pack")) this.runnablePackList.remove(node.as(io.intino.legio.runnable.artifact.RunnablePack.class));
		if (node.is("Level#Artifact$Generation")) this.levelGenerationList.remove(node.as(io.intino.legio.level.artifact.LevelGeneration.class));
		if (node.is("Platform#Artifact$Generation")) this.platformGenerationList.remove(node.as(io.intino.legio.platform.artifact.PlatformGeneration.class));
		if (node.is("Application#Artifact$Generation")) this.applicationGenerationList.remove(node.as(io.intino.legio.application.artifact.ApplicationGeneration.class));
		if (node.is("System#Artifact$Generation")) this.systemGenerationList.remove(node.as(io.intino.legio.system.artifact.SystemGeneration.class));
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

	public io.intino.tara.magritte.Concept concept(String concept) {
		return graph.concept(concept);
	}

	public io.intino.tara.magritte.Concept concept(java.lang.Class<? extends io.intino.tara.magritte.Layer> layerClass) {
		return graph.concept(layerClass);
	}

	public java.util.List<io.intino.tara.magritte.Concept> conceptList() {
		return graph.conceptList();
	}

	public java.util.List<io.intino.tara.magritte.Concept> conceptList(java.util.function.Predicate<io.intino.tara.magritte.Concept> predicate) {
		return graph.conceptList(predicate);
	}

	public io.intino.tara.magritte.Node createRoot(io.intino.tara.magritte.Concept concept, String namespace) {
		return graph.createRoot(concept, namespace);
	}

	public <T extends io.intino.tara.magritte.Layer> T createRoot(java.lang.Class<T> layerClass, String namespace) {
		return graph.createRoot(layerClass, namespace);
	}

	public io.intino.tara.magritte.Node createRoot(String concept, String namespace) {
		return graph.createRoot(concept, namespace);
	}

	public <T extends io.intino.tara.magritte.Layer> T createRoot(java.lang.Class<T> layerClass, String namespace, String id) {
		return graph.createRoot(layerClass, namespace, id);
	}

	public io.intino.tara.magritte.Node createRoot(String concept, String namespace, String id) {
		return graph.createRoot(concept, namespace, id);
	}

	public io.intino.tara.magritte.Node createRoot(io.intino.tara.magritte.Concept concept, String namespace, String id) {
		return graph.createRoot(concept, namespace, id);
	}

	public io.intino.legio.Artifact artifact() {
		return artifact;
	}

	public java.util.List<io.intino.legio.Server> serverList() {
		return serverList;
	}

	public java.util.List<io.intino.legio.Repository> repositoryList() {
		return repositoryList;
	}

	public java.util.List<io.intino.legio.runnable.artifact.RunnablePack> runnablePackList() {
		return runnablePackList;
	}

	public java.util.List<io.intino.legio.level.artifact.LevelGeneration> levelGenerationList() {
		return levelGenerationList;
	}

	public java.util.List<io.intino.legio.platform.artifact.PlatformGeneration> platformGenerationList() {
		return platformGenerationList;
	}

	public java.util.List<io.intino.legio.application.artifact.ApplicationGeneration> applicationGenerationList() {
		return applicationGenerationList;
	}

	public java.util.List<io.intino.legio.system.artifact.SystemGeneration> systemGenerationList() {
		return systemGenerationList;
	}

	public java.util.List<io.intino.legio.Server> serverList(java.util.function.Predicate<io.intino.legio.Server> predicate) {
		return serverList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.Server server(int index) {
		return serverList.get(index);
	}

	public java.util.List<io.intino.legio.Repository> repositoryList(java.util.function.Predicate<io.intino.legio.Repository> predicate) {
		return repositoryList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.Repository repository(int index) {
		return repositoryList.get(index);
	}

	public java.util.List<io.intino.legio.runnable.artifact.RunnablePack> runnablePackList(java.util.function.Predicate<io.intino.legio.runnable.artifact.RunnablePack> predicate) {
		return runnablePackList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.runnable.artifact.RunnablePack runnablePack(int index) {
		return runnablePackList.get(index);
	}

	public java.util.List<io.intino.legio.level.artifact.LevelGeneration> levelGenerationList(java.util.function.Predicate<io.intino.legio.level.artifact.LevelGeneration> predicate) {
		return levelGenerationList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.level.artifact.LevelGeneration levelGeneration(int index) {
		return levelGenerationList.get(index);
	}

	public java.util.List<io.intino.legio.platform.artifact.PlatformGeneration> platformGenerationList(java.util.function.Predicate<io.intino.legio.platform.artifact.PlatformGeneration> predicate) {
		return platformGenerationList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.platform.artifact.PlatformGeneration platformGeneration(int index) {
		return platformGenerationList.get(index);
	}

	public java.util.List<io.intino.legio.application.artifact.ApplicationGeneration> applicationGenerationList(java.util.function.Predicate<io.intino.legio.application.artifact.ApplicationGeneration> predicate) {
		return applicationGenerationList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.application.artifact.ApplicationGeneration applicationGeneration(int index) {
		return applicationGenerationList.get(index);
	}

	public java.util.List<io.intino.legio.system.artifact.SystemGeneration> systemGenerationList(java.util.function.Predicate<io.intino.legio.system.artifact.SystemGeneration> predicate) {
		return systemGenerationList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.system.artifact.SystemGeneration systemGeneration(int index) {
		return systemGenerationList.get(index);
	}

	public io.intino.tara.magritte.Graph graph() {
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

		public io.intino.legio.Artifact artifact(java.lang.String groupId, java.lang.String version) {
			io.intino.legio.Artifact newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.Artifact.class, namespace, name).as(io.intino.legio.Artifact.class);
			newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
			return newElement;
		}

		public io.intino.legio.Server server(java.lang.String cesar) {
			io.intino.legio.Server newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.Server.class, namespace, name).as(io.intino.legio.Server.class);
			newElement.node().set(newElement, "cesar", java.util.Collections.singletonList(cesar));
			return newElement;
		}

		public io.intino.legio.Repository repository(java.lang.String mavenId) {
			io.intino.legio.Repository newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.Repository.class, namespace, name).as(io.intino.legio.Repository.class);
			newElement.node().set(newElement, "mavenId", java.util.Collections.singletonList(mavenId));
			return newElement;
		}

		public io.intino.legio.runnable.artifact.RunnablePack runnablePack(java.lang.String mainClass) {
			io.intino.legio.runnable.artifact.RunnablePack newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.runnable.artifact.RunnablePack.class, namespace, name).as(io.intino.legio.runnable.artifact.RunnablePack.class);
			newElement.node().set(newElement, "mainClass", java.util.Collections.singletonList(mainClass));
			return newElement;
		}

		public io.intino.legio.platform.artifact.PlatformGeneration platformGeneration() {
			io.intino.legio.platform.artifact.PlatformGeneration newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.platform.artifact.PlatformGeneration.class, namespace, name).as(io.intino.legio.platform.artifact.PlatformGeneration.class);
			
			return newElement;
		}

		public io.intino.legio.application.artifact.ApplicationGeneration applicationGeneration() {
			io.intino.legio.application.artifact.ApplicationGeneration newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.application.artifact.ApplicationGeneration.class, namespace, name).as(io.intino.legio.application.artifact.ApplicationGeneration.class);
			
			return newElement;
		}

		public io.intino.legio.system.artifact.SystemGeneration systemGeneration() {
			io.intino.legio.system.artifact.SystemGeneration newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.system.artifact.SystemGeneration.class, namespace, name).as(io.intino.legio.system.artifact.SystemGeneration.class);
			
			return newElement;
		}

	}


}