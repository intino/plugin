package io.intino.legio;

import io.intino.tara.magritte.Graph;

public class GraphWrapper extends io.intino.tara.magritte.GraphWrapper {

	protected io.intino.tara.magritte.Graph graph;
	private io.intino.legio.Artifact artifact;
	private java.util.List<io.intino.legio.Server> serverList;
	private java.util.List<io.intino.legio.Repository> repositoryList;
	private java.util.List<io.intino.legio.runnable.artifact.RunnablePack> runnablePackList;
	private java.util.List<io.intino.legio.level.LevelArtifact> levelArtifactList;
	private java.util.List<io.intino.legio.platform.PlatformArtifact> platformArtifactList;
	private java.util.List<io.intino.legio.application.ApplicationArtifact> applicationArtifactList;
	private java.util.List<io.intino.legio.system.SystemArtifact> systemArtifactList;

	public GraphWrapper(io.intino.tara.magritte.Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("Legio");
	}

	public void update() {
		artifact = this.graph.rootList(io.intino.legio.Artifact.class).stream().findFirst().orElse(null);
		serverList = this.graph.rootList(io.intino.legio.Server.class);
		repositoryList = this.graph.rootList(io.intino.legio.Repository.class);
		runnablePackList = this.graph.rootList(io.intino.legio.runnable.artifact.RunnablePack.class);
		levelArtifactList = this.graph.rootList(io.intino.legio.level.LevelArtifact.class);
		platformArtifactList = this.graph.rootList(io.intino.legio.platform.PlatformArtifact.class);
		applicationArtifactList = this.graph.rootList(io.intino.legio.application.ApplicationArtifact.class);
		systemArtifactList = this.graph.rootList(io.intino.legio.system.SystemArtifact.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		if (node.is("Artifact")) this.artifact = node.as(io.intino.legio.Artifact.class);
		if (node.is("Server")) this.serverList.add(node.as(io.intino.legio.Server.class));
		if (node.is("Repository")) this.repositoryList.add(node.as(io.intino.legio.Repository.class));
		if (node.is("Runnable#Artifact$Pack")) this.runnablePackList.add(node.as(io.intino.legio.runnable.artifact.RunnablePack.class));
		if (node.is("Level#Artifact")) this.levelArtifactList.add(node.as(io.intino.legio.level.LevelArtifact.class));
		if (node.is("Platform#Artifact")) this.platformArtifactList.add(node.as(io.intino.legio.platform.PlatformArtifact.class));
		if (node.is("Application#Artifact")) this.applicationArtifactList.add(node.as(io.intino.legio.application.ApplicationArtifact.class));
		if (node.is("System#Artifact")) this.systemArtifactList.add(node.as(io.intino.legio.system.SystemArtifact.class));
	}

	@Override
	protected void removeNode(io.intino.tara.magritte.Node node) {
		if (node.is("Artifact")) this.artifact = null;
		if (node.is("Server")) this.serverList.remove(node.as(io.intino.legio.Server.class));
		if (node.is("Repository")) this.repositoryList.remove(node.as(io.intino.legio.Repository.class));
		if (node.is("Runnable#Artifact$Pack")) this.runnablePackList.remove(node.as(io.intino.legio.runnable.artifact.RunnablePack.class));
		if (node.is("Level#Artifact")) this.levelArtifactList.remove(node.as(io.intino.legio.level.LevelArtifact.class));
		if (node.is("Platform#Artifact")) this.platformArtifactList.remove(node.as(io.intino.legio.platform.PlatformArtifact.class));
		if (node.is("Application#Artifact")) this.applicationArtifactList.remove(node.as(io.intino.legio.application.ApplicationArtifact.class));
		if (node.is("System#Artifact")) this.systemArtifactList.remove(node.as(io.intino.legio.system.SystemArtifact.class));
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

	public java.util.List<io.intino.legio.level.LevelArtifact> levelArtifactList() {
		return levelArtifactList;
	}

	public java.util.List<io.intino.legio.platform.PlatformArtifact> platformArtifactList() {
		return platformArtifactList;
	}

	public java.util.List<io.intino.legio.application.ApplicationArtifact> applicationArtifactList() {
		return applicationArtifactList;
	}

	public java.util.List<io.intino.legio.system.SystemArtifact> systemArtifactList() {
		return systemArtifactList;
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

	public java.util.List<io.intino.legio.level.LevelArtifact> levelArtifactList(java.util.function.Predicate<io.intino.legio.level.LevelArtifact> predicate) {
		return levelArtifactList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.level.LevelArtifact levelArtifact(int index) {
		return levelArtifactList.get(index);
	}

	public java.util.List<io.intino.legio.platform.PlatformArtifact> platformArtifactList(java.util.function.Predicate<io.intino.legio.platform.PlatformArtifact> predicate) {
		return platformArtifactList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.platform.PlatformArtifact platformArtifact(int index) {
		return platformArtifactList.get(index);
	}

	public java.util.List<io.intino.legio.application.ApplicationArtifact> applicationArtifactList(java.util.function.Predicate<io.intino.legio.application.ApplicationArtifact> predicate) {
		return applicationArtifactList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.application.ApplicationArtifact applicationArtifact(int index) {
		return applicationArtifactList.get(index);
	}

	public java.util.List<io.intino.legio.system.SystemArtifact> systemArtifactList(java.util.function.Predicate<io.intino.legio.system.SystemArtifact> predicate) {
		return systemArtifactList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.system.SystemArtifact systemArtifact(int index) {
		return systemArtifactList.get(index);
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

		public io.intino.legio.Repository repository(java.lang.String identifier) {
			io.intino.legio.Repository newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.Repository.class, namespace, name).as(io.intino.legio.Repository.class);
			newElement.node().set(newElement, "identifier", java.util.Collections.singletonList(identifier));
			return newElement;
		}

		public io.intino.legio.runnable.artifact.RunnablePack runnablePack(java.lang.String mainClass) {
			io.intino.legio.runnable.artifact.RunnablePack newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.runnable.artifact.RunnablePack.class, namespace, name).as(io.intino.legio.runnable.artifact.RunnablePack.class);
			newElement.node().set(newElement, "mainClass", java.util.Collections.singletonList(mainClass));
			return newElement;
		}

		public io.intino.legio.platform.PlatformArtifact platformArtifact() {
			io.intino.legio.platform.PlatformArtifact newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.platform.PlatformArtifact.class, namespace, name).as(io.intino.legio.platform.PlatformArtifact.class);
			
			return newElement;
		}

		public io.intino.legio.application.ApplicationArtifact applicationArtifact() {
			io.intino.legio.application.ApplicationArtifact newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.application.ApplicationArtifact.class, namespace, name).as(io.intino.legio.application.ApplicationArtifact.class);
			
			return newElement;
		}

		public io.intino.legio.system.SystemArtifact systemArtifact() {
			io.intino.legio.system.SystemArtifact newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.system.SystemArtifact.class, namespace, name).as(io.intino.legio.system.SystemArtifact.class);
			
			return newElement;
		}

	}


}