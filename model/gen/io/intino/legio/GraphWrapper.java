package io.intino.legio;

public class GraphWrapper extends io.intino.tara.magritte.GraphWrapper {

	protected io.intino.tara.magritte.Graph graph;
	private io.intino.legio.Project project;
	private io.intino.legio.LifeCycle lifeCycle;
	private java.util.List<io.intino.legio.runnable.lifecycle.RunnablePackage> runnablePackageList;
	private java.util.List<io.intino.legio.level.project.LevelFactory> levelFactoryList;
	private java.util.List<io.intino.legio.platform.project.PlatformFactory> platformFactoryList;
	private java.util.List<io.intino.legio.application.project.ApplicationFactory> applicationFactoryList;
	private java.util.List<io.intino.legio.system.project.SystemFactory> systemFactoryList;
	private java.util.List<io.intino.legio.type.TypeParameter> typeParameterList;
	private java.util.List<io.intino.legio.real.RealParameter> realParameterList;
	private java.util.List<io.intino.legio.number.NumberParameter> numberParameterList;
	private java.util.List<io.intino.legio.bool.BoolParameter> boolParameterList;
	private java.util.List<io.intino.legio.text.TextParameter> textParameterList;

	public GraphWrapper(io.intino.tara.magritte.Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("Legio");
	}

	public void update() {
		project = this.graph.rootList(io.intino.legio.Project.class).stream().findFirst().orElse(null);
		lifeCycle = this.graph.rootList(io.intino.legio.LifeCycle.class).stream().findFirst().orElse(null);
		runnablePackageList = this.graph.rootList(io.intino.legio.runnable.lifecycle.RunnablePackage.class);
		levelFactoryList = this.graph.rootList(io.intino.legio.level.project.LevelFactory.class);
		platformFactoryList = this.graph.rootList(io.intino.legio.platform.project.PlatformFactory.class);
		applicationFactoryList = this.graph.rootList(io.intino.legio.application.project.ApplicationFactory.class);
		systemFactoryList = this.graph.rootList(io.intino.legio.system.project.SystemFactory.class);
		typeParameterList = this.graph.rootList(io.intino.legio.type.TypeParameter.class);
		realParameterList = this.graph.rootList(io.intino.legio.real.RealParameter.class);
		numberParameterList = this.graph.rootList(io.intino.legio.number.NumberParameter.class);
		boolParameterList = this.graph.rootList(io.intino.legio.bool.BoolParameter.class);
		textParameterList = this.graph.rootList(io.intino.legio.text.TextParameter.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		if (node.is("Project")) this.project = node.as(io.intino.legio.Project.class);
		if (node.is("LifeCycle")) this.lifeCycle = node.as(io.intino.legio.LifeCycle.class);
		if (node.is("Runnable#LifeCycle$Package")) this.runnablePackageList.add(node.as(io.intino.legio.runnable.lifecycle.RunnablePackage.class));
		if (node.is("Level#Project$Factory")) this.levelFactoryList.add(node.as(io.intino.legio.level.project.LevelFactory.class));
		if (node.is("Platform#Project$Factory")) this.platformFactoryList.add(node.as(io.intino.legio.platform.project.PlatformFactory.class));
		if (node.is("Application#Project$Factory")) this.applicationFactoryList.add(node.as(io.intino.legio.application.project.ApplicationFactory.class));
		if (node.is("System#Project$Factory")) this.systemFactoryList.add(node.as(io.intino.legio.system.project.SystemFactory.class));
		if (node.is("Type#Parameter")) this.typeParameterList.add(node.as(io.intino.legio.type.TypeParameter.class));
		if (node.is("Real#Parameter")) this.realParameterList.add(node.as(io.intino.legio.real.RealParameter.class));
		if (node.is("Number#Parameter")) this.numberParameterList.add(node.as(io.intino.legio.number.NumberParameter.class));
		if (node.is("Bool#Parameter")) this.boolParameterList.add(node.as(io.intino.legio.bool.BoolParameter.class));
		if (node.is("Text#Parameter")) this.textParameterList.add(node.as(io.intino.legio.text.TextParameter.class));
	}

	@Override
	protected void removeNode(io.intino.tara.magritte.Node node) {
		if (node.is("Project")) this.project = null;
		if (node.is("LifeCycle")) this.lifeCycle = null;
		if (node.is("Runnable#LifeCycle$Package")) this.runnablePackageList.remove(node.as(io.intino.legio.runnable.lifecycle.RunnablePackage.class));
		if (node.is("Level#Project$Factory")) this.levelFactoryList.remove(node.as(io.intino.legio.level.project.LevelFactory.class));
		if (node.is("Platform#Project$Factory")) this.platformFactoryList.remove(node.as(io.intino.legio.platform.project.PlatformFactory.class));
		if (node.is("Application#Project$Factory")) this.applicationFactoryList.remove(node.as(io.intino.legio.application.project.ApplicationFactory.class));
		if (node.is("System#Project$Factory")) this.systemFactoryList.remove(node.as(io.intino.legio.system.project.SystemFactory.class));
		if (node.is("Type#Parameter")) this.typeParameterList.remove(node.as(io.intino.legio.type.TypeParameter.class));
		if (node.is("Real#Parameter")) this.realParameterList.remove(node.as(io.intino.legio.real.RealParameter.class));
		if (node.is("Number#Parameter")) this.numberParameterList.remove(node.as(io.intino.legio.number.NumberParameter.class));
		if (node.is("Bool#Parameter")) this.boolParameterList.remove(node.as(io.intino.legio.bool.BoolParameter.class));
		if (node.is("Text#Parameter")) this.textParameterList.remove(node.as(io.intino.legio.text.TextParameter.class));
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

	public io.intino.legio.Project project() {
		return project;
	}

	public io.intino.legio.LifeCycle lifeCycle() {
		return lifeCycle;
	}

	public java.util.List<io.intino.legio.runnable.lifecycle.RunnablePackage> runnablePackageList() {
		return runnablePackageList;
	}

	public java.util.List<io.intino.legio.level.project.LevelFactory> levelFactoryList() {
		return levelFactoryList;
	}

	public java.util.List<io.intino.legio.platform.project.PlatformFactory> platformFactoryList() {
		return platformFactoryList;
	}

	public java.util.List<io.intino.legio.application.project.ApplicationFactory> applicationFactoryList() {
		return applicationFactoryList;
	}

	public java.util.List<io.intino.legio.system.project.SystemFactory> systemFactoryList() {
		return systemFactoryList;
	}

	public java.util.List<io.intino.legio.type.TypeParameter> typeParameterList() {
		return typeParameterList;
	}

	public java.util.List<io.intino.legio.real.RealParameter> realParameterList() {
		return realParameterList;
	}

	public java.util.List<io.intino.legio.number.NumberParameter> numberParameterList() {
		return numberParameterList;
	}

	public java.util.List<io.intino.legio.bool.BoolParameter> boolParameterList() {
		return boolParameterList;
	}

	public java.util.List<io.intino.legio.text.TextParameter> textParameterList() {
		return textParameterList;
	}

	public java.util.List<io.intino.legio.runnable.lifecycle.RunnablePackage> runnablePackageList(java.util.function.Predicate<io.intino.legio.runnable.lifecycle.RunnablePackage> predicate) {
		return runnablePackageList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.runnable.lifecycle.RunnablePackage runnablePackage(int index) {
		return runnablePackageList.get(index);
	}

	public java.util.List<io.intino.legio.level.project.LevelFactory> levelFactoryList(java.util.function.Predicate<io.intino.legio.level.project.LevelFactory> predicate) {
		return levelFactoryList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.level.project.LevelFactory levelFactory(int index) {
		return levelFactoryList.get(index);
	}

	public java.util.List<io.intino.legio.platform.project.PlatformFactory> platformFactoryList(java.util.function.Predicate<io.intino.legio.platform.project.PlatformFactory> predicate) {
		return platformFactoryList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.platform.project.PlatformFactory platformFactory(int index) {
		return platformFactoryList.get(index);
	}

	public java.util.List<io.intino.legio.application.project.ApplicationFactory> applicationFactoryList(java.util.function.Predicate<io.intino.legio.application.project.ApplicationFactory> predicate) {
		return applicationFactoryList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.application.project.ApplicationFactory applicationFactory(int index) {
		return applicationFactoryList.get(index);
	}

	public java.util.List<io.intino.legio.system.project.SystemFactory> systemFactoryList(java.util.function.Predicate<io.intino.legio.system.project.SystemFactory> predicate) {
		return systemFactoryList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.system.project.SystemFactory systemFactory(int index) {
		return systemFactoryList.get(index);
	}

	public java.util.List<io.intino.legio.type.TypeParameter> typeParameterList(java.util.function.Predicate<io.intino.legio.type.TypeParameter> predicate) {
		return typeParameterList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.type.TypeParameter typeParameter(int index) {
		return typeParameterList.get(index);
	}

	public java.util.List<io.intino.legio.real.RealParameter> realParameterList(java.util.function.Predicate<io.intino.legio.real.RealParameter> predicate) {
		return realParameterList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.real.RealParameter realParameter(int index) {
		return realParameterList.get(index);
	}

	public java.util.List<io.intino.legio.number.NumberParameter> numberParameterList(java.util.function.Predicate<io.intino.legio.number.NumberParameter> predicate) {
		return numberParameterList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.number.NumberParameter numberParameter(int index) {
		return numberParameterList.get(index);
	}

	public java.util.List<io.intino.legio.bool.BoolParameter> boolParameterList(java.util.function.Predicate<io.intino.legio.bool.BoolParameter> predicate) {
		return boolParameterList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.bool.BoolParameter boolParameter(int index) {
		return boolParameterList.get(index);
	}

	public java.util.List<io.intino.legio.text.TextParameter> textParameterList(java.util.function.Predicate<io.intino.legio.text.TextParameter> predicate) {
		return textParameterList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.text.TextParameter textParameter(int index) {
		return textParameterList.get(index);
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

		public io.intino.legio.Project project(java.lang.String groupId, java.lang.String version) {
			io.intino.legio.Project newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.Project.class, namespace, name).as(io.intino.legio.Project.class);
			newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
			return newElement;
		}

		public io.intino.legio.LifeCycle lifeCycle() {
			io.intino.legio.LifeCycle newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.LifeCycle.class, namespace, name).as(io.intino.legio.LifeCycle.class);
			
			return newElement;
		}

		public io.intino.legio.runnable.lifecycle.RunnablePackage runnablePackage(java.lang.String mainClass) {
			io.intino.legio.runnable.lifecycle.RunnablePackage newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.runnable.lifecycle.RunnablePackage.class, namespace, name).as(io.intino.legio.runnable.lifecycle.RunnablePackage.class);
			newElement.node().set(newElement, "mainClass", java.util.Collections.singletonList(mainClass));
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

		public io.intino.legio.real.RealParameter realParameter(double value) {
			io.intino.legio.real.RealParameter newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.real.RealParameter.class, namespace, name).as(io.intino.legio.real.RealParameter.class);
			newElement.node().set(newElement, "value", java.util.Collections.singletonList(value));
			return newElement;
		}

		public io.intino.legio.number.NumberParameter numberParameter(int value) {
			io.intino.legio.number.NumberParameter newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.number.NumberParameter.class, namespace, name).as(io.intino.legio.number.NumberParameter.class);
			newElement.node().set(newElement, "value", java.util.Collections.singletonList(value));
			return newElement;
		}

		public io.intino.legio.bool.BoolParameter boolParameter(boolean value) {
			io.intino.legio.bool.BoolParameter newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.bool.BoolParameter.class, namespace, name).as(io.intino.legio.bool.BoolParameter.class);
			newElement.node().set(newElement, "value", java.util.Collections.singletonList(value));
			return newElement;
		}

		public io.intino.legio.text.TextParameter textParameter(java.lang.String value) {
			io.intino.legio.text.TextParameter newElement = GraphWrapper.this.graph.createRoot(io.intino.legio.text.TextParameter.class, namespace, name).as(io.intino.legio.text.TextParameter.class);
			newElement.node().set(newElement, "value", java.util.Collections.singletonList(value));
			return newElement;
		}

	}


}