package io.intino.legio.graph;

import io.intino.tara.magritte.Graph;

public class AbstractGraph extends io.intino.tara.magritte.GraphWrapper {

	protected io.intino.tara.magritte.Graph graph;
	private io.intino.legio.graph.Artifact artifact;
	private java.util.List<io.intino.legio.graph.RunConfiguration> runConfigurationList = new java.util.ArrayList<>();
	private java.util.List<io.intino.legio.graph.Server> serverList = new java.util.ArrayList<>();
	private java.util.List<io.intino.legio.graph.Repository> repositoryList = new java.util.ArrayList<>();
	private java.util.List<io.intino.legio.graph.runnable.artifact.RunnablePackage> runnablePackageList = new java.util.ArrayList<>();
	private java.util.List<io.intino.legio.graph.macos.artifact.MacOSPackage> macOSPackageList = new java.util.ArrayList<>();
	private java.util.List<io.intino.legio.graph.windows.artifact.WindowsPackage> windowsPackageList = new java.util.ArrayList<>();
	private java.util.List<io.intino.legio.graph.level.LevelArtifact> levelArtifactList = new java.util.ArrayList<>();
	private java.util.List<io.intino.legio.graph.platform.PlatformArtifact> platformArtifactList = new java.util.ArrayList<>();
	private java.util.List<io.intino.legio.graph.product.ProductArtifact> productArtifactList = new java.util.ArrayList<>();
	private java.util.List<io.intino.legio.graph.solution.SolutionArtifact> solutionArtifactList = new java.util.ArrayList<>();

	private java.util.Map<String, Indexer> index = fillIndex();

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
		this.macOSPackageList = new java.util.ArrayList<>(wrapper.macOSPackageList);
		this.windowsPackageList = new java.util.ArrayList<>(wrapper.windowsPackageList);
		this.levelArtifactList = new java.util.ArrayList<>(wrapper.levelArtifactList);
		this.platformArtifactList = new java.util.ArrayList<>(wrapper.platformArtifactList);
		this.productArtifactList = new java.util.ArrayList<>(wrapper.productArtifactList);
		this.solutionArtifactList = new java.util.ArrayList<>(wrapper.solutionArtifactList);
	}

	public <T extends io.intino.tara.magritte.GraphWrapper> T a$(Class<T> t) {
		return this.core$().as(t);
	}

    @Override
	public void update() {
		index.values().forEach(v -> v.clear());
		graph.rootList().forEach(r -> addNode$(r));
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		for (io.intino.tara.magritte.Concept c : node.conceptList()) if (index.containsKey(c.id())) index.get(c.id()).add(node);
		if (index.containsKey(node.id())) index.get(node.id()).add(node);
	}

	@Override
	protected void removeNode$(io.intino.tara.magritte.Node node) {
		for (io.intino.tara.magritte.Concept c : node.conceptList()) if (index.containsKey(c.id())) index.get(c.id()).remove(node);
		if (index.containsKey(node.id())) index.get(node.id()).remove(node);
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

	public java.util.List<io.intino.legio.graph.macos.artifact.MacOSPackage> macOSPackageList() {
		return macOSPackageList;
	}

	public java.util.List<io.intino.legio.graph.windows.artifact.WindowsPackage> windowsPackageList() {
		return windowsPackageList;
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

	public java.util.stream.Stream<io.intino.legio.graph.macos.artifact.MacOSPackage> macOSPackageList(java.util.function.Predicate<io.intino.legio.graph.macos.artifact.MacOSPackage> filter) {
		return macOSPackageList.stream().filter(filter);
	}

	public io.intino.legio.graph.macos.artifact.MacOSPackage macOSPackage(int index) {
		return macOSPackageList.get(index);
	}

	public java.util.stream.Stream<io.intino.legio.graph.windows.artifact.WindowsPackage> windowsPackageList(java.util.function.Predicate<io.intino.legio.graph.windows.artifact.WindowsPackage> filter) {
		return windowsPackageList.stream().filter(filter);
	}

	public io.intino.legio.graph.windows.artifact.WindowsPackage windowsPackage(int index) {
		return windowsPackageList.get(index);
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
			io.intino.legio.graph.Artifact newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.Artifact.class, stash, this.name).a$(io.intino.legio.graph.Artifact.class);
			newElement.core$().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
			newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
			return newElement;
		}

		public io.intino.legio.graph.RunConfiguration runConfiguration() {
			io.intino.legio.graph.RunConfiguration newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.RunConfiguration.class, stash, this.name).a$(io.intino.legio.graph.RunConfiguration.class);

			return newElement;
		}

		public io.intino.legio.graph.Server server(java.lang.String cesar) {
			io.intino.legio.graph.Server newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.Server.class, stash, this.name).a$(io.intino.legio.graph.Server.class);
			newElement.core$().set(newElement, "cesar", java.util.Collections.singletonList(cesar));
			return newElement;
		}

		public io.intino.legio.graph.Repository repository(java.lang.String identifier) {
			io.intino.legio.graph.Repository newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.Repository.class, stash, this.name).a$(io.intino.legio.graph.Repository.class);
			newElement.core$().set(newElement, "identifier", java.util.Collections.singletonList(identifier));
			return newElement;
		}

		public io.intino.legio.graph.runnable.artifact.RunnablePackage runnablePackage(java.lang.String mainClass) {
			io.intino.legio.graph.runnable.artifact.RunnablePackage newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.runnable.artifact.RunnablePackage.class, stash, this.name).a$(io.intino.legio.graph.runnable.artifact.RunnablePackage.class);
			newElement.core$().set(newElement, "mainClass", java.util.Collections.singletonList(mainClass));
			return newElement;
		}

		public io.intino.legio.graph.macos.artifact.MacOSPackage macOSPackage(java.lang.String macIcon) {
			io.intino.legio.graph.macos.artifact.MacOSPackage newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.macos.artifact.MacOSPackage.class, stash, this.name).a$(io.intino.legio.graph.macos.artifact.MacOSPackage.class);
			newElement.core$().set(newElement, "macIcon", java.util.Collections.singletonList(macIcon));
			return newElement;
		}

		public io.intino.legio.graph.windows.artifact.WindowsPackage windowsPackage(java.lang.String windowsIcon) {
			io.intino.legio.graph.windows.artifact.WindowsPackage newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.windows.artifact.WindowsPackage.class, stash, this.name).a$(io.intino.legio.graph.windows.artifact.WindowsPackage.class);
			newElement.core$().set(newElement, "windowsIcon", java.util.Collections.singletonList(windowsIcon));
			return newElement;
		}

		public io.intino.legio.graph.platform.PlatformArtifact platformArtifact() {
			io.intino.legio.graph.platform.PlatformArtifact newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.platform.PlatformArtifact.class, stash, this.name).a$(io.intino.legio.graph.platform.PlatformArtifact.class);

			return newElement;
		}

		public io.intino.legio.graph.product.ProductArtifact productArtifact() {
			io.intino.legio.graph.product.ProductArtifact newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.product.ProductArtifact.class, stash, this.name).a$(io.intino.legio.graph.product.ProductArtifact.class);

			return newElement;
		}

		public io.intino.legio.graph.solution.SolutionArtifact solutionArtifact() {
			io.intino.legio.graph.solution.SolutionArtifact newElement = AbstractGraph.this.graph.createRoot(io.intino.legio.graph.solution.SolutionArtifact.class, stash, this.name).a$(io.intino.legio.graph.solution.SolutionArtifact.class);

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

	    public void macOSPackage(java.util.function.Predicate<io.intino.legio.graph.macos.artifact.MacOSPackage> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.macOSPackageList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void windowsPackage(java.util.function.Predicate<io.intino.legio.graph.windows.artifact.WindowsPackage> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.windowsPackageList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
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


	private java.util.HashMap<String, Indexer> fillIndex() {
		return new java.util.HashMap<String, Indexer>() {{
			put("Artifact", new Indexer(node -> artifact = node.as(io.intino.legio.graph.Artifact.class), node -> artifact = null, () -> artifact = null));
			put("RunConfiguration", new Indexer(node -> runConfigurationList.add(node.as(io.intino.legio.graph.RunConfiguration.class)), node -> runConfigurationList.remove(node.as(io.intino.legio.graph.RunConfiguration.class)), () -> runConfigurationList.clear()));
			put("Server", new Indexer(node -> serverList.add(node.as(io.intino.legio.graph.Server.class)), node -> serverList.remove(node.as(io.intino.legio.graph.Server.class)), () -> serverList.clear()));
			put("Repository", new Indexer(node -> repositoryList.add(node.as(io.intino.legio.graph.Repository.class)), node -> repositoryList.remove(node.as(io.intino.legio.graph.Repository.class)), () -> repositoryList.clear()));
			put("Runnable#Artifact$Package", new Indexer(node -> runnablePackageList.add(node.as(io.intino.legio.graph.runnable.artifact.RunnablePackage.class)), node -> runnablePackageList.remove(node.as(io.intino.legio.graph.runnable.artifact.RunnablePackage.class)), () -> runnablePackageList.clear()));
			put("MacOS#Artifact$Package", new Indexer(node -> macOSPackageList.add(node.as(io.intino.legio.graph.macos.artifact.MacOSPackage.class)), node -> macOSPackageList.remove(node.as(io.intino.legio.graph.macos.artifact.MacOSPackage.class)), () -> macOSPackageList.clear()));
			put("Windows#Artifact$Package", new Indexer(node -> windowsPackageList.add(node.as(io.intino.legio.graph.windows.artifact.WindowsPackage.class)), node -> windowsPackageList.remove(node.as(io.intino.legio.graph.windows.artifact.WindowsPackage.class)), () -> windowsPackageList.clear()));
			put("Level#Artifact", new Indexer(node -> levelArtifactList.add(node.as(io.intino.legio.graph.level.LevelArtifact.class)), node -> levelArtifactList.remove(node.as(io.intino.legio.graph.level.LevelArtifact.class)), () -> levelArtifactList.clear()));
			put("Platform#Artifact", new Indexer(node -> platformArtifactList.add(node.as(io.intino.legio.graph.platform.PlatformArtifact.class)), node -> platformArtifactList.remove(node.as(io.intino.legio.graph.platform.PlatformArtifact.class)), () -> platformArtifactList.clear()));
			put("Product#Artifact", new Indexer(node -> productArtifactList.add(node.as(io.intino.legio.graph.product.ProductArtifact.class)), node -> productArtifactList.remove(node.as(io.intino.legio.graph.product.ProductArtifact.class)), () -> productArtifactList.clear()));
			put("Solution#Artifact", new Indexer(node -> solutionArtifactList.add(node.as(io.intino.legio.graph.solution.SolutionArtifact.class)), node -> solutionArtifactList.remove(node.as(io.intino.legio.graph.solution.SolutionArtifact.class)), () -> solutionArtifactList.clear()));
		}};
	}

	public static class Indexer {
		Add add;
		Remove remove;
		IndexClear clear;

		public Indexer(Add add, Remove remove, IndexClear clear) {
			this.add = add;
			this.remove = remove;
			this.clear = clear;
		}

		void add(io.intino.tara.magritte.Node node) {
			this.add.add(node);
		}

		void remove(io.intino.tara.magritte.Node node) {
			this.remove.remove(node);
		}

		void clear() {
			this.clear.clear();
		}
	}

	interface Add {
		void add(io.intino.tara.magritte.Node node);
	}

	interface Remove {
		void remove(io.intino.tara.magritte.Node node);
	}

	interface IndexClear {
		void clear();
	}
}