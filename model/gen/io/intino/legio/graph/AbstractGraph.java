package io.intino.legio.graph;

import io.intino.tara.magritte.Graph;

public class AbstractGraph extends io.intino.tara.magritte.GraphWrapper {

	protected io.intino.tara.magritte.Graph graph;
	private io.intino.legio.graph.Artifact artifact;
	private java.util.List<io.intino.legio.graph.RunConfiguration> runConfigurationList;
	private java.util.List<io.intino.legio.graph.Server> serverList;
	private java.util.List<io.intino.legio.graph.Repository> repositoryList;
	private java.util.List<io.intino.legio.graph.runnable.artifact.RunnablePackage> runnablePackageList;
	private java.util.List<io.intino.legio.graph.level.LevelArtifact> levelArtifactList;
	private java.util.List<io.intino.legio.graph.platform.PlatformArtifact> platformArtifactList;
	private java.util.List<io.intino.legio.graph.product.ProductArtifact> productArtifactList;
	private java.util.List<io.intino.legio.graph.solution.SolutionArtifact> solutionArtifactList;

	public AbstractGraph(io.intino.tara.magritte.Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("Legio");
	}

	public AbstractGraph(io.intino.tara.magritte.Graph graph, AbstractGraph wrapper) {
		this.graph = graph;
		this.graph.i18n().register("Legio");
		this.artifact = wrapper.artifact;
		this.runConfigurationList = new java.util.ArrayList<>(wrapper.runConfigurationList);
		this.serverList = new java.util.ArrayList<>(wrapper.serverList);
		this.repositoryList = new java.util.ArrayList<>(wrapper.repositoryList);
		this.runnablePackageList = new java.util.ArrayList<>(wrapper.runnablePackageList);
		this.levelArtifactList = new java.util.ArrayList<>(wrapper.levelArtifactList);
		this.platformArtifactList = new java.util.ArrayList<>(wrapper.platformArtifactList);
		this.productArtifactList = new java.util.ArrayList<>(wrapper.productArtifactList);
		this.solutionArtifactList = new java.util.ArrayList<>(wrapper.solutionArtifactList);
	}

    @Override
	public void update() {
		artifact = this.graph.rootList(io.intino.legio.graph.Artifact.class).stream().findFirst().orElse(null);
		runConfigurationList = this.graph.rootList(io.intino.legio.graph.RunConfiguration.class);
		serverList = this.graph.rootList(io.intino.legio.graph.Server.class);
		repositoryList = this.graph.rootList(io.intino.legio.graph.Repository.class);
		runnablePackageList = this.graph.rootList(io.intino.legio.graph.runnable.artifact.RunnablePackage.class);
		levelArtifactList = this.graph.rootList(io.intino.legio.graph.level.LevelArtifact.class);
		platformArtifactList = this.graph.rootList(io.intino.legio.graph.platform.PlatformArtifact.class);
		productArtifactList = this.graph.rootList(io.intino.legio.graph.product.ProductArtifact.class);
		solutionArtifactList = this.graph.rootList(io.intino.legio.graph.solution.SolutionArtifact.class);
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		if (node.is("Artifact")) this.artifact = node.as(io.intino.legio.graph.Artifact.class);
		if (node.is("RunConfiguration")) this.runConfigurationList.add(node.as(io.intino.legio.graph.RunConfiguration.class));
		if (node.is("Server")) this.serverList.add(node.as(io.intino.legio.graph.Server.class));
		if (node.is("Repository")) this.repositoryList.add(node.as(io.intino.legio.graph.Repository.class));
		if (node.is("Runnable#Artifact$Package")) this.runnablePackageList.add(node.as(io.intino.legio.graph.runnable.artifact.RunnablePackage.class));
		if (node.is("Level#Artifact")) this.levelArtifactList.add(node.as(io.intino.legio.graph.level.LevelArtifact.class));
		if (node.is("Platform#Artifact")) this.platformArtifactList.add(node.as(io.intino.legio.graph.platform.PlatformArtifact.class));
		if (node.is("Product#Artifact")) this.productArtifactList.add(node.as(io.intino.legio.graph.product.ProductArtifact.class));
		if (node.is("Solution#Artifact")) this.solutionArtifactList.add(node.as(io.intino.legio.graph.solution.SolutionArtifact.class));
	}

	@Override
	protected void removeNode$(io.intino.tara.magritte.Node node) {
		if (node.is("Artifact")) this.artifact = null;
		if (node.is("RunConfiguration")) this.runConfigurationList.remove(node.as(io.intino.legio.graph.RunConfiguration.class));
		if (node.is("Server")) this.serverList.remove(node.as(io.intino.legio.graph.Server.class));
		if (node.is("Repository")) this.repositoryList.remove(node.as(io.intino.legio.graph.Repository.class));
		if (node.is("Runnable#Artifact$Package")) this.runnablePackageList.remove(node.as(io.intino.legio.graph.runnable.artifact.RunnablePackage.class));
		if (node.is("Level#Artifact")) this.levelArtifactList.remove(node.as(io.intino.legio.graph.level.LevelArtifact.class));
		if (node.is("Platform#Artifact")) this.platformArtifactList.remove(node.as(io.intino.legio.graph.platform.PlatformArtifact.class));
		if (node.is("Product#Artifact")) this.productArtifactList.remove(node.as(io.intino.legio.graph.product.ProductArtifact.class));
		if (node.is("Solution#Artifact")) this.solutionArtifactList.remove(node.as(io.intino.legio.graph.solution.SolutionArtifact.class));
	}

	public java.net.URL resourceAsMessage$(String language, String key) {
		return graph.loadResource(graph.i18n().message(language, key));
	}

	public io.intino.legio.graph.Artifact artifact() {
		return artifact;
	}

	public java.util.List<io.intino.legio.graph.RunConfiguration> runConfigurationList() {
		return runConfigurationList;
	}

	public java.util.List<io.intino.legio.graph.Server> serverList() {
		return serverList;
	}

	public java.util.List<io.intino.legio.graph.Repository> repositoryList() {
		return repositoryList;
	}

	public java.util.List<io.intino.legio.graph.runnable.artifact.RunnablePackage> runnablePackageList() {
		return runnablePackageList;
	}

	public java.util.List<io.intino.legio.graph.level.LevelArtifact> levelArtifactList() {
		return levelArtifactList;
	}

	public java.util.List<io.intino.legio.graph.platform.PlatformArtifact> platformArtifactList() {
		return platformArtifactList;
	}

	public java.util.List<io.intino.legio.graph.product.ProductArtifact> productArtifactList() {
		return productArtifactList;
	}

	public java.util.List<io.intino.legio.graph.solution.SolutionArtifact> solutionArtifactList() {
		return solutionArtifactList;
	}

	public java.util.stream.Stream<io.intino.legio.graph.RunConfiguration> runConfigurationList(java.util.function.Predicate<io.intino.legio.graph.RunConfiguration> filter) {
		return runConfigurationList.stream().filter(filter);
	}

	public io.intino.legio.graph.RunConfiguration runConfiguration(int index) {
		return runConfigurationList.get(index);
	}

	public java.util.stream.Stream<io.intino.legio.graph.Server> serverList(java.util.function.Predicate<io.intino.legio.graph.Server> filter) {
		return serverList.stream().filter(filter);
	}

	public io.intino.legio.graph.Server server(int index) {
		return serverList.get(index);
	}

	public java.util.stream.Stream<io.intino.legio.graph.Repository> repositoryList(java.util.function.Predicate<io.intino.legio.graph.Repository> filter) {
		return repositoryList.stream().filter(filter);
	}

	public io.intino.legio.graph.Repository repository(int index) {
		return repositoryList.get(index);
	}

	public java.util.stream.Stream<io.intino.legio.graph.runnable.artifact.RunnablePackage> runnablePackageList(java.util.function.Predicate<io.intino.legio.graph.runnable.artifact.RunnablePackage> filter) {
		return runnablePackageList.stream().filter(filter);
	}

	public io.intino.legio.graph.runnable.artifact.RunnablePackage runnablePackage(int index) {
		return runnablePackageList.get(index);
	}

	public java.util.stream.Stream<io.intino.legio.graph.level.LevelArtifact> levelArtifactList(java.util.function.Predicate<io.intino.legio.graph.level.LevelArtifact> filter) {
		return levelArtifactList.stream().filter(filter);
	}

	public io.intino.legio.graph.level.LevelArtifact levelArtifact(int index) {
		return levelArtifactList.get(index);
	}

	public java.util.stream.Stream<io.intino.legio.graph.platform.PlatformArtifact> platformArtifactList(java.util.function.Predicate<io.intino.legio.graph.platform.PlatformArtifact> filter) {
		return platformArtifactList.stream().filter(filter);
	}

	public io.intino.legio.graph.platform.PlatformArtifact platformArtifact(int index) {
		return platformArtifactList.get(index);
	}

	public java.util.stream.Stream<io.intino.legio.graph.product.ProductArtifact> productArtifactList(java.util.function.Predicate<io.intino.legio.graph.product.ProductArtifact> filter) {
		return productArtifactList.stream().filter(filter);
	}

	public io.intino.legio.graph.product.ProductArtifact productArtifact(int index) {
		return productArtifactList.get(index);
	}

	public java.util.stream.Stream<io.intino.legio.graph.solution.SolutionArtifact> solutionArtifactList(java.util.function.Predicate<io.intino.legio.graph.solution.SolutionArtifact> filter) {
		return solutionArtifactList.stream().filter(filter);
	}

	public io.intino.legio.graph.solution.SolutionArtifact solutionArtifact(int index) {
		return solutionArtifactList.get(index);
	}

	public io.intino.tara.magritte.Graph core$() {
		return graph;
	}

	public io.intino.tara.magritte.utils.I18n i18n$() {
		return graph.i18n();
	}

	public Create create() {
		return new Create("Misc", null);
	}

	public Create create(String stash) {
		return new Create(stash, null);
	}

	public Create create(String stash, String name) {
		return new Create(stash, name);
	}

	public Clear clear() {
		return new Clear();
	}

	public class Create {
		private final String stash;
		private final String name;

		public Create(String stash, String name) {
			this.stash = stash;
			this.name = name;
		}

		public io.intino.legio.graph.Artifact artifact(java.lang.String groupId, java.lang.String version) {
			io.intino.legio.graph.Artifact newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.Artifact.class, stash, name).a$(io.intino.legio.graph.Artifact.class);
			newElement.core$().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
			newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
			return newElement;
		}

		public io.intino.legio.graph.RunConfiguration runConfiguration() {
			io.intino.legio.graph.RunConfiguration newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.RunConfiguration.class, stash, name).a$(io.intino.legio.graph.RunConfiguration.class);

			return newElement;
		}

		public io.intino.legio.graph.Server server(java.lang.String cesar) {
			io.intino.legio.graph.Server newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.Server.class, stash, name).a$(io.intino.legio.graph.Server.class);
			newElement.core$().set(newElement, "cesar", java.util.Collections.singletonList(cesar));
			return newElement;
		}

		public io.intino.legio.graph.Repository repository(java.lang.String identifier) {
			io.intino.legio.graph.Repository newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.Repository.class, stash, name).a$(io.intino.legio.graph.Repository.class);
			newElement.core$().set(newElement, "identifier", java.util.Collections.singletonList(identifier));
			return newElement;
		}

		public io.intino.legio.graph.runnable.artifact.RunnablePackage runnablePackage(java.lang.String mainClass) {
			io.intino.legio.graph.runnable.artifact.RunnablePackage newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.runnable.artifact.RunnablePackage.class, stash, name).a$(io.intino.legio.graph.runnable.artifact.RunnablePackage.class);
			newElement.core$().set(newElement, "mainClass", java.util.Collections.singletonList(mainClass));
			return newElement;
		}

		public io.intino.legio.graph.platform.PlatformArtifact platformArtifact() {
			io.intino.legio.graph.platform.PlatformArtifact newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.platform.PlatformArtifact.class, stash, name).a$(io.intino.legio.graph.platform.PlatformArtifact.class);

			return newElement;
		}

		public io.intino.legio.graph.product.ProductArtifact productArtifact() {
			io.intino.legio.graph.product.ProductArtifact newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.product.ProductArtifact.class, stash, name).a$(io.intino.legio.graph.product.ProductArtifact.class);

			return newElement;
		}

		public io.intino.legio.graph.solution.SolutionArtifact solutionArtifact() {
			io.intino.legio.graph.solution.SolutionArtifact newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.solution.SolutionArtifact.class, stash, name).a$(io.intino.legio.graph.solution.SolutionArtifact.class);

			return newElement;
		}
	}

	public class Clear {
	    public void runConfiguration(java.util.function.Predicate<io.intino.legio.graph.RunConfiguration> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.runConfigurationList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void server(java.util.function.Predicate<io.intino.legio.graph.Server> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.serverList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void repository(java.util.function.Predicate<io.intino.legio.graph.Repository> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.repositoryList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void runnablePackage(java.util.function.Predicate<io.intino.legio.graph.runnable.artifact.RunnablePackage> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.runnablePackageList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void platformArtifact(java.util.function.Predicate<io.intino.legio.graph.platform.PlatformArtifact> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.platformArtifactList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void productArtifact(java.util.function.Predicate<io.intino.legio.graph.product.ProductArtifact> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.productArtifactList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void solutionArtifact(java.util.function.Predicate<io.intino.legio.graph.solution.SolutionArtifact> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.solutionArtifactList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }
	}
}