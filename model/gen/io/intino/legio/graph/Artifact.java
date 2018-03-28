package io.intino.legio.graph;

import io.intino.legio.graph.*;
import java.util.List;
import io.intino.legio.graph.Destination;


public class Artifact extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String groupId;
	protected java.lang.String version;
	protected io.intino.legio.graph.Artifact.License license;
	protected io.intino.legio.graph.Artifact.Imports imports;
	protected io.intino.legio.graph.Artifact.WebImports webImports;
	protected io.intino.legio.graph.Artifact.Box box;
	protected io.intino.legio.graph.Artifact.Code code;
	protected java.util.List<io.intino.legio.graph.Artifact.Exports> exportsList = new java.util.ArrayList<>();
	protected io.intino.legio.graph.Artifact.Package package$;
	protected io.intino.legio.graph.Artifact.Distribution distribution;
	protected io.intino.legio.graph.Artifact.QualityAnalytics qualityAnalytics;
	protected java.util.List<io.intino.legio.graph.Artifact.Deployment> deploymentList = new java.util.ArrayList<>();

	public Artifact(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String groupId() {
		return groupId;
	}

	public java.lang.String version() {
		return version;
	}

	public Artifact groupId(java.lang.String value) {
		this.groupId = value;
		return (Artifact) this;
	}

	public Artifact version(java.lang.String value) {
		this.version = value;
		return (Artifact) this;
	}

	public io.intino.legio.graph.Artifact.License license() {
		return license;
	}

	public io.intino.legio.graph.Artifact.Imports imports() {
		return imports;
	}

	public io.intino.legio.graph.Artifact.WebImports webImports() {
		return webImports;
	}

	public io.intino.legio.graph.Artifact.Box box() {
		return box;
	}

	public io.intino.legio.graph.Artifact.Code code() {
		return code;
	}

	public java.util.List<io.intino.legio.graph.Artifact.Exports> exportsList() {
		return java.util.Collections.unmodifiableList(exportsList);
	}

	public io.intino.legio.graph.Artifact.Exports exports(int index) {
		return exportsList.get(index);
	}

	public java.util.List<io.intino.legio.graph.Artifact.Exports> exportsList(java.util.function.Predicate<io.intino.legio.graph.Artifact.Exports> predicate) {
		return exportsList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.graph.Artifact.Package package$() {
		return package$;
	}

	public io.intino.legio.graph.Artifact.Distribution distribution() {
		return distribution;
	}

	public io.intino.legio.graph.Artifact.QualityAnalytics qualityAnalytics() {
		return qualityAnalytics;
	}

	public java.util.List<io.intino.legio.graph.Artifact.Deployment> deploymentList() {
		return java.util.Collections.unmodifiableList(deploymentList);
	}

	public io.intino.legio.graph.Artifact.Deployment deployment(int index) {
		return deploymentList.get(index);
	}

	public java.util.List<io.intino.legio.graph.Artifact.Deployment> deploymentList(java.util.function.Predicate<io.intino.legio.graph.Artifact.Deployment> predicate) {
		return deploymentList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public Artifact license(io.intino.legio.graph.Artifact.License value) {
		this.license = value;
		return (Artifact) this;
	}

	public Artifact imports(io.intino.legio.graph.Artifact.Imports value) {
		this.imports = value;
		return (Artifact) this;
	}

	public Artifact webImports(io.intino.legio.graph.Artifact.WebImports value) {
		this.webImports = value;
		return (Artifact) this;
	}

	public Artifact box(io.intino.legio.graph.Artifact.Box value) {
		this.box = value;
		return (Artifact) this;
	}

	public Artifact code(io.intino.legio.graph.Artifact.Code value) {
		this.code = value;
		return (Artifact) this;
	}



	public Artifact package$(io.intino.legio.graph.Artifact.Package value) {
		this.package$ = value;
		return (Artifact) this;
	}

	public Artifact distribution(io.intino.legio.graph.Artifact.Distribution value) {
		this.distribution = value;
		return (Artifact) this;
	}

	public Artifact qualityAnalytics(io.intino.legio.graph.Artifact.QualityAnalytics value) {
		this.qualityAnalytics = value;
		return (Artifact) this;
	}



	public io.intino.legio.graph.platform.PlatformArtifact asPlatform() {
		io.intino.tara.magritte.Layer as = a$(io.intino.legio.graph.platform.PlatformArtifact.class);
		return as != null ? (io.intino.legio.graph.platform.PlatformArtifact) as : core$().addFacet(io.intino.legio.graph.platform.PlatformArtifact.class);
	}

	public boolean isPlatform() {
		return core$().is(io.intino.legio.graph.platform.PlatformArtifact.class);
	}

	public io.intino.legio.graph.product.ProductArtifact asProduct() {
		io.intino.tara.magritte.Layer as = a$(io.intino.legio.graph.product.ProductArtifact.class);
		return as != null ? (io.intino.legio.graph.product.ProductArtifact) as : core$().addFacet(io.intino.legio.graph.product.ProductArtifact.class);
	}

	public boolean isProduct() {
		return core$().is(io.intino.legio.graph.product.ProductArtifact.class);
	}

	public io.intino.legio.graph.level.LevelArtifact asLevel() {
		io.intino.tara.magritte.Layer as = a$(io.intino.legio.graph.level.LevelArtifact.class);
		return as != null ? (io.intino.legio.graph.level.LevelArtifact) as : null;
	}

	public boolean isLevel() {
		return core$().is(io.intino.legio.graph.level.LevelArtifact.class);
	}

	public io.intino.legio.graph.solution.SolutionArtifact asSolution() {
		io.intino.tara.magritte.Layer as = a$(io.intino.legio.graph.solution.SolutionArtifact.class);
		return as != null ? (io.intino.legio.graph.solution.SolutionArtifact) as : core$().addFacet(io.intino.legio.graph.solution.SolutionArtifact.class);
	}

	public boolean isSolution() {
		return core$().is(io.intino.legio.graph.solution.SolutionArtifact.class);
	}

	protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
		if (license != null) components.add(this.license.core$());
		if (imports != null) components.add(this.imports.core$());
		if (webImports != null) components.add(this.webImports.core$());
		if (box != null) components.add(this.box.core$());
		if (code != null) components.add(this.code.core$());
		new java.util.ArrayList<>(exportsList).forEach(c -> components.add(c.core$()));
		if (package$ != null) components.add(this.package$.core$());
		if (distribution != null) components.add(this.distribution.core$());
		if (qualityAnalytics != null) components.add(this.qualityAnalytics.core$());
		new java.util.ArrayList<>(deploymentList).forEach(c -> components.add(c.core$()));
		return new java.util.ArrayList<>(components);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
		map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
		return map;
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		super.addNode$(node);
		if (node.is("Artifact$License")) this.license = node.as(io.intino.legio.graph.Artifact.License.class);
		if (node.is("Artifact$Imports")) this.imports = node.as(io.intino.legio.graph.Artifact.Imports.class);
		if (node.is("Artifact$WebImports")) this.webImports = node.as(io.intino.legio.graph.Artifact.WebImports.class);
		if (node.is("Artifact$Box")) this.box = node.as(io.intino.legio.graph.Artifact.Box.class);
		if (node.is("Artifact$Code")) this.code = node.as(io.intino.legio.graph.Artifact.Code.class);
		if (node.is("Artifact$Exports")) this.exportsList.add(node.as(io.intino.legio.graph.Artifact.Exports.class));
		if (node.is("Artifact$Package")) this.package$ = node.as(io.intino.legio.graph.Artifact.Package.class);
		if (node.is("Artifact$Distribution")) this.distribution = node.as(io.intino.legio.graph.Artifact.Distribution.class);
		if (node.is("Artifact$QualityAnalytics")) this.qualityAnalytics = node.as(io.intino.legio.graph.Artifact.QualityAnalytics.class);
		if (node.is("Artifact$Deployment")) this.deploymentList.add(node.as(io.intino.legio.graph.Artifact.Deployment.class));
	}

	@Override
    protected void removeNode$(io.intino.tara.magritte.Node node) {
        super.removeNode$(node);
        if (node.is("Artifact$License")) this.license = null;
        if (node.is("Artifact$Imports")) this.imports = null;
        if (node.is("Artifact$WebImports")) this.webImports = null;
        if (node.is("Artifact$Box")) this.box = null;
        if (node.is("Artifact$Code")) this.code = null;
        if (node.is("Artifact$Exports")) this.exportsList.remove(node.as(io.intino.legio.graph.Artifact.Exports.class));
        if (node.is("Artifact$Package")) this.package$ = null;
        if (node.is("Artifact$Distribution")) this.distribution = null;
        if (node.is("Artifact$QualityAnalytics")) this.qualityAnalytics = null;
        if (node.is("Artifact$Deployment")) this.deploymentList.remove(node.as(io.intino.legio.graph.Artifact.Deployment.class));
    }

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("groupId")) this.groupId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("groupId")) this.groupId = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create  {
		protected final java.lang.String name;

		public Create(java.lang.String name) {
			this.name = name;
		}

		public io.intino.legio.graph.Artifact.License license(java.lang.String author, io.intino.legio.graph.Artifact.License.Type type) {
		    io.intino.legio.graph.Artifact.License newElement = core$().graph().concept(io.intino.legio.graph.Artifact.License.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.License.class);
			newElement.core$().set(newElement, "author", java.util.Collections.singletonList(author));
			newElement.core$().set(newElement, "type", java.util.Collections.singletonList(type));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Imports imports() {
		    io.intino.legio.graph.Artifact.Imports newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Imports.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Imports.class);

		    return newElement;
		}

		public io.intino.legio.graph.Artifact.WebImports webImports() {
		    io.intino.legio.graph.Artifact.WebImports newElement = core$().graph().concept(io.intino.legio.graph.Artifact.WebImports.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.WebImports.class);

		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Box box(java.lang.String language, java.lang.String version, java.lang.String sdk) {
		    io.intino.legio.graph.Artifact.Box newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Box.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Box.class);
			newElement.core$().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
			newElement.core$().set(newElement, "sdk", java.util.Collections.singletonList(sdk));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Code code(java.lang.String targetPackage) {
		    io.intino.legio.graph.Artifact.Code newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Code.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Code.class);
			newElement.core$().set(newElement, "targetPackage", java.util.Collections.singletonList(targetPackage));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Exports exports(io.intino.legio.graph.Repository.Type repository) {
		    io.intino.legio.graph.Artifact.Exports newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Exports.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Exports.class);
			newElement.core$().set(newElement, "repository", java.util.Collections.singletonList(repository));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Package package$(io.intino.legio.graph.Artifact.Package.Mode mode) {
		    io.intino.legio.graph.Artifact.Package newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Package.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Package.class);
			newElement.core$().set(newElement, "mode", java.util.Collections.singletonList(mode));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Distribution distribution(io.intino.legio.graph.Repository.Release release) {
		    io.intino.legio.graph.Artifact.Distribution newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Distribution.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Distribution.class);
			newElement.core$().set(newElement, "release", java.util.Collections.singletonList(release));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.QualityAnalytics qualityAnalytics(java.lang.String url) {
		    io.intino.legio.graph.Artifact.QualityAnalytics newElement = core$().graph().concept(io.intino.legio.graph.Artifact.QualityAnalytics.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.QualityAnalytics.class);
			newElement.core$().set(newElement, "url", java.util.Collections.singletonList(url));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Deployment deployment() {
		    io.intino.legio.graph.Artifact.Deployment newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Deployment.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Deployment.class);

		    return newElement;
		}

	}

	public Clear clear() {
		return new Clear();
	}

	public class Clear  {










		public void exports(java.util.function.Predicate<io.intino.legio.graph.Artifact.Exports> filter) {
			new java.util.ArrayList<>(exportsList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}







		public void deployment(java.util.function.Predicate<io.intino.legio.graph.Artifact.Deployment> filter) {
			new java.util.ArrayList<>(deploymentList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}
	}

	public static class License extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String author;
		protected Type type;

		public enum Type {
			GPL, BSD;
		}

		public License(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String author() {
			return author;
		}

		public Type type() {
			return type;
		}

		public License author(java.lang.String value) {
			this.author = value;
			return (License) this;
		}

		public License type(io.intino.legio.graph.Artifact.License.Type value) {
			this.type = value;
			return (License) this;
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("author", new java.util.ArrayList(java.util.Collections.singletonList(this.author)));
			map.put("type", new java.util.ArrayList(java.util.Collections.singletonList(this.type)));
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("author")) this.author = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("type")) this.type = io.intino.tara.magritte.loaders.WordLoader.load(values, Type.class, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("author")) this.author = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("type")) this.type = (Type) values.get(0);
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Imports extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {

		protected java.util.List<io.intino.legio.graph.Artifact.Imports.Dependency> dependencyList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.graph.Artifact.Imports.Compile> compileList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.graph.Artifact.Imports.Runtime> runtimeList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.graph.Artifact.Imports.Provided> providedList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.graph.Artifact.Imports.Test> testList = new java.util.ArrayList<>();

		public Imports(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Dependency> dependencyList() {
			return java.util.Collections.unmodifiableList(dependencyList);
		}

		public io.intino.legio.graph.Artifact.Imports.Dependency dependency(int index) {
			return dependencyList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Dependency> dependencyList(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Dependency> predicate) {
			return dependencyList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Compile> compileList() {
			return java.util.Collections.unmodifiableList(compileList);
		}

		public io.intino.legio.graph.Artifact.Imports.Compile compile(int index) {
			return compileList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Compile> compileList(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Compile> predicate) {
			return compileList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Runtime> runtimeList() {
			return java.util.Collections.unmodifiableList(runtimeList);
		}

		public io.intino.legio.graph.Artifact.Imports.Runtime runtime(int index) {
			return runtimeList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Runtime> runtimeList(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Runtime> predicate) {
			return runtimeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Provided> providedList() {
			return java.util.Collections.unmodifiableList(providedList);
		}

		public io.intino.legio.graph.Artifact.Imports.Provided provided(int index) {
			return providedList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Provided> providedList(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Provided> predicate) {
			return providedList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Test> testList() {
			return java.util.Collections.unmodifiableList(testList);
		}

		public io.intino.legio.graph.Artifact.Imports.Test test(int index) {
			return testList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Artifact.Imports.Test> testList(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Test> predicate) {
			return testList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}











		protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
			new java.util.ArrayList<>(dependencyList).forEach(c -> components.add(c.core$()));
			new java.util.ArrayList<>(compileList).forEach(c -> components.add(c.core$()));
			new java.util.ArrayList<>(runtimeList).forEach(c -> components.add(c.core$()));
			new java.util.ArrayList<>(providedList).forEach(c -> components.add(c.core$()));
			new java.util.ArrayList<>(testList).forEach(c -> components.add(c.core$()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();

			return map;
		}

		@Override
		protected void addNode$(io.intino.tara.magritte.Node node) {
			super.addNode$(node);
			if (node.is("Artifact$Imports$Dependency")) this.dependencyList.add(node.as(io.intino.legio.graph.Artifact.Imports.Dependency.class));
			if (node.is("Artifact$Imports$Compile")) this.compileList.add(node.as(io.intino.legio.graph.Artifact.Imports.Compile.class));
			if (node.is("Artifact$Imports$Runtime")) this.runtimeList.add(node.as(io.intino.legio.graph.Artifact.Imports.Runtime.class));
			if (node.is("Artifact$Imports$Provided")) this.providedList.add(node.as(io.intino.legio.graph.Artifact.Imports.Provided.class));
			if (node.is("Artifact$Imports$Test")) this.testList.add(node.as(io.intino.legio.graph.Artifact.Imports.Test.class));
		}

		@Override
	    protected void removeNode$(io.intino.tara.magritte.Node node) {
	        super.removeNode$(node);
	        if (node.is("Artifact$Imports$Dependency")) this.dependencyList.remove(node.as(io.intino.legio.graph.Artifact.Imports.Dependency.class));
	        if (node.is("Artifact$Imports$Compile")) this.compileList.remove(node.as(io.intino.legio.graph.Artifact.Imports.Compile.class));
	        if (node.is("Artifact$Imports$Runtime")) this.runtimeList.remove(node.as(io.intino.legio.graph.Artifact.Imports.Runtime.class));
	        if (node.is("Artifact$Imports$Provided")) this.providedList.remove(node.as(io.intino.legio.graph.Artifact.Imports.Provided.class));
	        if (node.is("Artifact$Imports$Test")) this.testList.remove(node.as(io.intino.legio.graph.Artifact.Imports.Test.class));
	    }

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
		}

		public Create create() {
			return new Create(null);
		}

		public Create create(java.lang.String name) {
			return new Create(name);
		}

		public class Create  {
			protected final java.lang.String name;

			public Create(java.lang.String name) {
				this.name = name;
			}



			public io.intino.legio.graph.Artifact.Imports.Compile compile(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.graph.Artifact.Imports.Compile newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Imports.Compile.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Imports.Compile.class);
				newElement.core$().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.core$().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.core$().set(newElement, "artifacts", artifacts);
			    return newElement;
			}

			public io.intino.legio.graph.Artifact.Imports.Runtime runtime(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.graph.Artifact.Imports.Runtime newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Imports.Runtime.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Imports.Runtime.class);
				newElement.core$().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.core$().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.core$().set(newElement, "artifacts", artifacts);
			    return newElement;
			}

			public io.intino.legio.graph.Artifact.Imports.Provided provided(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.graph.Artifact.Imports.Provided newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Imports.Provided.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Imports.Provided.class);
				newElement.core$().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.core$().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.core$().set(newElement, "artifacts", artifacts);
			    return newElement;
			}

			public io.intino.legio.graph.Artifact.Imports.Test test(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.graph.Artifact.Imports.Test newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Imports.Test.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Imports.Test.class);
				newElement.core$().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.core$().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.core$().set(newElement, "artifacts", artifacts);
			    return newElement;
			}

		}

		public Clear clear() {
			return new Clear();
		}

		public class Clear  {


			public void compile(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Compile> filter) {
				new java.util.ArrayList<>(compileList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}

			public void runtime(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Runtime> filter) {
				new java.util.ArrayList<>(runtimeList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}

			public void provided(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Provided> filter) {
				new java.util.ArrayList<>(providedList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}

			public void test(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Test> filter) {
				new java.util.ArrayList<>(testList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}
		}

		public static abstract class Dependency extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected io.intino.tara.magritte.Expression<java.lang.String> identifier;
			protected io.intino.tara.magritte.Expression<java.lang.String> name;
			protected java.lang.String groupId;
			protected java.lang.String artifactId;
			protected java.lang.String version;
			protected java.lang.String effectiveVersion;
			protected boolean transitive;
			protected java.util.List<java.lang.String> artifacts = new java.util.ArrayList<>();
			protected boolean resolve;
			protected boolean toModule;
			protected java.util.List<io.intino.legio.graph.Artifact.Imports.Dependency.Exclude> excludeList = new java.util.ArrayList<>();

			public Dependency(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String identifier() {
				return identifier.value();
			}

			public java.lang.String name() {
				return name.value();
			}

			public java.lang.String groupId() {
				return groupId;
			}

			public java.lang.String artifactId() {
				return artifactId;
			}

			public java.lang.String version() {
				return version;
			}

			public java.lang.String effectiveVersion() {
				return effectiveVersion;
			}

			public boolean transitive() {
				return transitive;
			}

			public java.util.List<java.lang.String> artifacts() {
				return artifacts;
			}

			public java.lang.String artifacts(int index) {
				return artifacts.get(index);
			}

			public java.util.List<java.lang.String> artifacts(java.util.function.Predicate<java.lang.String> predicate) {
				return artifacts().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}

			public boolean resolve() {
				return resolve;
			}

			public boolean toModule() {
				return toModule;
			}

			public Dependency identifier(io.intino.tara.magritte.Expression<java.lang.String> value) {
				this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
				return (Dependency) this;
			}

			public Dependency name(io.intino.tara.magritte.Expression<java.lang.String> value) {
				this.name = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
				return (Dependency) this;
			}

			public Dependency groupId(java.lang.String value) {
				this.groupId = value;
				return (Dependency) this;
			}

			public Dependency artifactId(java.lang.String value) {
				this.artifactId = value;
				return (Dependency) this;
			}

			public Dependency version(java.lang.String value) {
				this.version = value;
				return (Dependency) this;
			}

			public Dependency effectiveVersion(java.lang.String value) {
				this.effectiveVersion = value;
				return (Dependency) this;
			}

			public Dependency transitive(boolean value) {
				this.transitive = value;
				return (Dependency) this;
			}

			public Dependency resolve(boolean value) {
				this.resolve = value;
				return (Dependency) this;
			}

			public Dependency toModule(boolean value) {
				this.toModule = value;
				return (Dependency) this;
			}

			public java.util.List<io.intino.legio.graph.Artifact.Imports.Dependency.Exclude> excludeList() {
				return java.util.Collections.unmodifiableList(excludeList);
			}

			public io.intino.legio.graph.Artifact.Imports.Dependency.Exclude exclude(int index) {
				return excludeList.get(index);
			}

			public java.util.List<io.intino.legio.graph.Artifact.Imports.Dependency.Exclude> excludeList(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Dependency.Exclude> predicate) {
				return excludeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}



			protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
				java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
				new java.util.ArrayList<>(excludeList).forEach(c -> components.add(c.core$()));
				return new java.util.ArrayList<>(components);
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("identifier", new java.util.ArrayList(java.util.Collections.singletonList(this.identifier)));
				map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name)));
				map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
				map.put("artifactId", new java.util.ArrayList(java.util.Collections.singletonList(this.artifactId)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				map.put("effectiveVersion", new java.util.ArrayList(java.util.Collections.singletonList(this.effectiveVersion)));
				map.put("transitive", new java.util.ArrayList(java.util.Collections.singletonList(this.transitive)));
				map.put("artifacts", this.artifacts);
				map.put("resolve", new java.util.ArrayList(java.util.Collections.singletonList(this.resolve)));
				map.put("toModule", new java.util.ArrayList(java.util.Collections.singletonList(this.toModule)));
				return map;
			}

			@Override
			protected void addNode$(io.intino.tara.magritte.Node node) {
				super.addNode$(node);
				if (node.is("Artifact$Imports$Dependency$Exclude")) this.excludeList.add(node.as(io.intino.legio.graph.Artifact.Imports.Dependency.Exclude.class));
			}

			@Override
		    protected void removeNode$(io.intino.tara.magritte.Node node) {
		        super.removeNode$(node);
		        if (node.is("Artifact$Imports$Dependency$Exclude")) this.excludeList.remove(node.as(io.intino.legio.graph.Artifact.Imports.Dependency.Exclude.class));
		    }

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
				else if (name.equalsIgnoreCase("name")) this.name = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("transitive")) this.transitive = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifacts")) this.artifacts = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
				else if (name.equalsIgnoreCase("resolve")) this.resolve = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("toModule")) this.toModule = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
				else if (name.equalsIgnoreCase("name")) this.name = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("transitive")) this.transitive = (java.lang.Boolean) values.get(0);
				else if (name.equalsIgnoreCase("artifacts")) this.artifacts = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
				else if (name.equalsIgnoreCase("resolve")) this.resolve = (java.lang.Boolean) values.get(0);
				else if (name.equalsIgnoreCase("toModule")) this.toModule = (java.lang.Boolean) values.get(0);
			}

			public Create create() {
				return new Create(null);
			}

			public Create create(java.lang.String name) {
				return new Create(name);
			}

			public class Create  {
				protected final java.lang.String name;

				public Create(java.lang.String name) {
					this.name = name;
				}

				public io.intino.legio.graph.Artifact.Imports.Dependency.Exclude exclude(java.lang.String groupId, java.lang.String artifactId) {
				    io.intino.legio.graph.Artifact.Imports.Dependency.Exclude newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Imports.Dependency.Exclude.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Imports.Dependency.Exclude.class);
					newElement.core$().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
					newElement.core$().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				    return newElement;
				}

			}

			public Clear clear() {
				return new Clear();
			}

			public class Clear  {
				public void exclude(java.util.function.Predicate<io.intino.legio.graph.Artifact.Imports.Dependency.Exclude> filter) {
					new java.util.ArrayList<>(excludeList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
				}
			}

			public static class Exclude extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
				protected java.lang.String groupId;
				protected java.lang.String artifactId;

				public Exclude(io.intino.tara.magritte.Node node) {
					super(node);
				}

				public java.lang.String groupId() {
					return groupId;
				}

				public java.lang.String artifactId() {
					return artifactId;
				}

				public Exclude groupId(java.lang.String value) {
					this.groupId = value;
					return (Exclude) this;
				}

				public Exclude artifactId(java.lang.String value) {
					this.artifactId = value;
					return (Exclude) this;
				}

				@Override
				protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
					java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
					map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
					map.put("artifactId", new java.util.ArrayList(java.util.Collections.singletonList(this.artifactId)));
					return map;
				}

				@Override
				protected void load$(java.lang.String name, java.util.List<?> values) {
					super.load$(name, values);
					if (name.equalsIgnoreCase("groupId")) this.groupId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
					else if (name.equalsIgnoreCase("artifactId")) this.artifactId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				}

				@Override
				protected void set$(java.lang.String name, java.util.List<?> values) {
					super.set$(name, values);
					if (name.equalsIgnoreCase("groupId")) this.groupId = (java.lang.String) values.get(0);
					else if (name.equalsIgnoreCase("artifactId")) this.artifactId = (java.lang.String) values.get(0);
				}


				public io.intino.legio.graph.LegioGraph graph() {
					return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
				}
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}

		public static class Compile extends io.intino.legio.graph.Artifact.Imports.Dependency implements io.intino.tara.magritte.tags.Terminal {


			public Compile(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());

				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}

		public static class Runtime extends io.intino.legio.graph.Artifact.Imports.Dependency implements io.intino.tara.magritte.tags.Terminal {


			public Runtime(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());

				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}

		public static class Provided extends io.intino.legio.graph.Artifact.Imports.Dependency implements io.intino.tara.magritte.tags.Terminal {


			public Provided(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());

				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}

		public static class Test extends io.intino.legio.graph.Artifact.Imports.Dependency implements io.intino.tara.magritte.tags.Terminal {


			public Test(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());

				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class WebImports extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String webDirectory;
		protected java.util.List<io.intino.legio.graph.Artifact.WebImports.Resolution> resolutionList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.graph.Artifact.WebImports.WebComponent> webComponentList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.graph.Artifact.WebImports.WebArtifact> webArtifactList = new java.util.ArrayList<>();

		public WebImports(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String webDirectory() {
			return webDirectory;
		}

		public WebImports webDirectory(java.lang.String value) {
			this.webDirectory = value;
			return (WebImports) this;
		}

		public java.util.List<io.intino.legio.graph.Artifact.WebImports.Resolution> resolutionList() {
			return java.util.Collections.unmodifiableList(resolutionList);
		}

		public io.intino.legio.graph.Artifact.WebImports.Resolution resolution(int index) {
			return resolutionList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Artifact.WebImports.Resolution> resolutionList(java.util.function.Predicate<io.intino.legio.graph.Artifact.WebImports.Resolution> predicate) {
			return resolutionList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.graph.Artifact.WebImports.WebComponent> webComponentList() {
			return java.util.Collections.unmodifiableList(webComponentList);
		}

		public io.intino.legio.graph.Artifact.WebImports.WebComponent webComponent(int index) {
			return webComponentList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Artifact.WebImports.WebComponent> webComponentList(java.util.function.Predicate<io.intino.legio.graph.Artifact.WebImports.WebComponent> predicate) {
			return webComponentList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.graph.Artifact.WebImports.WebArtifact> webArtifactList() {
			return java.util.Collections.unmodifiableList(webArtifactList);
		}

		public io.intino.legio.graph.Artifact.WebImports.WebArtifact webArtifact(int index) {
			return webArtifactList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Artifact.WebImports.WebArtifact> webArtifactList(java.util.function.Predicate<io.intino.legio.graph.Artifact.WebImports.WebArtifact> predicate) {
			return webArtifactList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}







		protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
			new java.util.ArrayList<>(resolutionList).forEach(c -> components.add(c.core$()));
			new java.util.ArrayList<>(webComponentList).forEach(c -> components.add(c.core$()));
			new java.util.ArrayList<>(webArtifactList).forEach(c -> components.add(c.core$()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("webDirectory", new java.util.ArrayList(java.util.Collections.singletonList(this.webDirectory)));
			return map;
		}

		@Override
		protected void addNode$(io.intino.tara.magritte.Node node) {
			super.addNode$(node);
			if (node.is("Artifact$WebImports$Resolution")) this.resolutionList.add(node.as(io.intino.legio.graph.Artifact.WebImports.Resolution.class));
			if (node.is("Artifact$WebImports$WebComponent")) this.webComponentList.add(node.as(io.intino.legio.graph.Artifact.WebImports.WebComponent.class));
			if (node.is("Artifact$WebImports$WebArtifact")) this.webArtifactList.add(node.as(io.intino.legio.graph.Artifact.WebImports.WebArtifact.class));
		}

		@Override
	    protected void removeNode$(io.intino.tara.magritte.Node node) {
	        super.removeNode$(node);
	        if (node.is("Artifact$WebImports$Resolution")) this.resolutionList.remove(node.as(io.intino.legio.graph.Artifact.WebImports.Resolution.class));
	        if (node.is("Artifact$WebImports$WebComponent")) this.webComponentList.remove(node.as(io.intino.legio.graph.Artifact.WebImports.WebComponent.class));
	        if (node.is("Artifact$WebImports$WebArtifact")) this.webArtifactList.remove(node.as(io.intino.legio.graph.Artifact.WebImports.WebArtifact.class));
	    }

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("webDirectory")) this.webDirectory = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("webDirectory")) this.webDirectory = (java.lang.String) values.get(0);
		}

		public Create create() {
			return new Create(null);
		}

		public Create create(java.lang.String name) {
			return new Create(name);
		}

		public class Create  {
			protected final java.lang.String name;

			public Create(java.lang.String name) {
				this.name = name;
			}

			public io.intino.legio.graph.Artifact.WebImports.Resolution resolution(java.lang.String name, java.lang.String version) {
			    io.intino.legio.graph.Artifact.WebImports.Resolution newElement = core$().graph().concept(io.intino.legio.graph.Artifact.WebImports.Resolution.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.WebImports.Resolution.class);
				newElement.core$().set(newElement, "name", java.util.Collections.singletonList(name));
				newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
			    return newElement;
			}

			public io.intino.legio.graph.Artifact.WebImports.WebComponent webComponent(java.lang.String version) {
			    io.intino.legio.graph.Artifact.WebImports.WebComponent newElement = core$().graph().concept(io.intino.legio.graph.Artifact.WebImports.WebComponent.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.WebImports.WebComponent.class);
				newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
			    return newElement;
			}

			public io.intino.legio.graph.Artifact.WebImports.WebArtifact webArtifact(java.lang.String groupId, java.lang.String artifactId, java.lang.String version) {
			    io.intino.legio.graph.Artifact.WebImports.WebArtifact newElement = core$().graph().concept(io.intino.legio.graph.Artifact.WebImports.WebArtifact.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.WebImports.WebArtifact.class);
				newElement.core$().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.core$().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
			    return newElement;
			}

		}

		public Clear clear() {
			return new Clear();
		}

		public class Clear  {
			public void resolution(java.util.function.Predicate<io.intino.legio.graph.Artifact.WebImports.Resolution> filter) {
				new java.util.ArrayList<>(resolutionList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}

			public void webComponent(java.util.function.Predicate<io.intino.legio.graph.Artifact.WebImports.WebComponent> filter) {
				new java.util.ArrayList<>(webComponentList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}

			public void webArtifact(java.util.function.Predicate<io.intino.legio.graph.Artifact.WebImports.WebArtifact> filter) {
				new java.util.ArrayList<>(webArtifactList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}
		}

		public static class Resolution extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String name;
			protected java.lang.String version;

			public Resolution(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String name() {
				return name;
			}

			public java.lang.String version() {
				return version;
			}

			public Resolution name(java.lang.String value) {
				this.name = value;
				return (Resolution) this;
			}

			public Resolution version(java.lang.String value) {
				this.version = value;
				return (Resolution) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("name")) this.name = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("name")) this.name = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}

		public static class WebComponent extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String url;
			protected java.lang.String version;

			public WebComponent(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String url() {
				return url;
			}

			public java.lang.String version() {
				return version;
			}

			public WebComponent url(java.lang.String value) {
				this.url = value;
				return (WebComponent) this;
			}

			public WebComponent version(java.lang.String value) {
				this.version = value;
				return (WebComponent) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("url")) this.url = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}

		public static class WebArtifact extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected io.intino.tara.magritte.Expression<java.lang.String> identifier;
			protected java.lang.String groupId;
			protected java.lang.String artifactId;
			protected java.lang.String version;

			public WebArtifact(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String identifier() {
				return identifier.value();
			}

			public java.lang.String groupId() {
				return groupId;
			}

			public java.lang.String artifactId() {
				return artifactId;
			}

			public java.lang.String version() {
				return version;
			}

			public WebArtifact identifier(io.intino.tara.magritte.Expression<java.lang.String> value) {
				this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
				return (WebArtifact) this;
			}

			public WebArtifact groupId(java.lang.String value) {
				this.groupId = value;
				return (WebArtifact) this;
			}

			public WebArtifact artifactId(java.lang.String value) {
				this.artifactId = value;
				return (WebArtifact) this;
			}

			public WebArtifact version(java.lang.String value) {
				this.version = value;
				return (WebArtifact) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("identifier", new java.util.ArrayList(java.util.Collections.singletonList(this.identifier)));
				map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
				map.put("artifactId", new java.util.ArrayList(java.util.Collections.singletonList(this.artifactId)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Box extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String language;
		protected java.lang.String version;
		protected java.lang.String sdk;
		protected java.lang.String targetPackage;

		public Box(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String language() {
			return language;
		}

		public java.lang.String version() {
			return version;
		}

		public java.lang.String sdk() {
			return sdk;
		}

		public java.lang.String targetPackage() {
			return targetPackage;
		}

		public Box language(java.lang.String value) {
			this.language = value;
			return (Box) this;
		}

		public Box version(java.lang.String value) {
			this.version = value;
			return (Box) this;
		}

		public Box sdk(java.lang.String value) {
			this.sdk = value;
			return (Box) this;
		}

		public Box targetPackage(java.lang.String value) {
			this.targetPackage = value;
			return (Box) this;
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("language", new java.util.ArrayList(java.util.Collections.singletonList(this.language)));
			map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
			map.put("sdk", new java.util.ArrayList(java.util.Collections.singletonList(this.sdk)));
			map.put("targetPackage", new java.util.ArrayList(java.util.Collections.singletonList(this.targetPackage)));
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("language")) this.language = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("sdk")) this.sdk = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("targetPackage")) this.targetPackage = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("language")) this.language = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("sdk")) this.sdk = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("targetPackage")) this.targetPackage = (java.lang.String) values.get(0);
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Code extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String targetPackage;

		public Code(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String targetPackage() {
			return targetPackage;
		}

		public Code targetPackage(java.lang.String value) {
			this.targetPackage = value;
			return (Code) this;
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("targetPackage", new java.util.ArrayList(java.util.Collections.singletonList(this.targetPackage)));
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("targetPackage")) this.targetPackage = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("targetPackage")) this.targetPackage = (java.lang.String) values.get(0);
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Exports extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected io.intino.legio.graph.Repository.Type repository;

		public Exports(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public io.intino.legio.graph.Repository.Type repository() {
			return repository;
		}

		public Exports repository(io.intino.legio.graph.Repository.Type value) {
			this.repository = value;
			return (Exports) this;
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("repository", this.repository != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.repository)) : java.util.Collections.emptyList());
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("repository")) this.repository = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.legio.graph.Repository.Type.class, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("repository")) this.repository = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.legio.graph.Repository.Type.class) : null;
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Package extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected Mode mode;

		public enum Mode {
			ModulesAndLibrariesExtracted, LibrariesLinkedByManifest, ModulesAndLibrariesLinkedByManifest;
		}
		protected boolean attachSources;
		protected boolean attachDoc;
		protected boolean includeTests;
		protected java.lang.String classpathPrefix;
		protected java.lang.String finalName;
		protected java.util.List<io.intino.legio.graph.Parameter> parameterList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.graph.Artifact.Package.MavenPlugin> mavenPluginList = new java.util.ArrayList<>();

		public Package(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public Mode mode() {
			return mode;
		}

		public boolean attachSources() {
			return attachSources;
		}

		public boolean attachDoc() {
			return attachDoc;
		}

		public boolean includeTests() {
			return includeTests;
		}

		public java.lang.String classpathPrefix() {
			return classpathPrefix;
		}

		public java.lang.String finalName() {
			return finalName;
		}

		public Package mode(io.intino.legio.graph.Artifact.Package.Mode value) {
			this.mode = value;
			return (Package) this;
		}

		public Package attachSources(boolean value) {
			this.attachSources = value;
			return (Package) this;
		}

		public Package attachDoc(boolean value) {
			this.attachDoc = value;
			return (Package) this;
		}

		public Package includeTests(boolean value) {
			this.includeTests = value;
			return (Package) this;
		}

		public Package classpathPrefix(java.lang.String value) {
			this.classpathPrefix = value;
			return (Package) this;
		}

		public Package finalName(java.lang.String value) {
			this.finalName = value;
			return (Package) this;
		}

		public java.util.List<io.intino.legio.graph.Parameter> parameterList() {
			return java.util.Collections.unmodifiableList(parameterList);
		}

		public io.intino.legio.graph.Parameter parameter(int index) {
			return parameterList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Parameter> parameterList(java.util.function.Predicate<io.intino.legio.graph.Parameter> predicate) {
			return parameterList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.graph.Artifact.Package.MavenPlugin> mavenPluginList() {
			return java.util.Collections.unmodifiableList(mavenPluginList);
		}

		public io.intino.legio.graph.Artifact.Package.MavenPlugin mavenPlugin(int index) {
			return mavenPluginList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Artifact.Package.MavenPlugin> mavenPluginList(java.util.function.Predicate<io.intino.legio.graph.Artifact.Package.MavenPlugin> predicate) {
			return mavenPluginList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}





		public io.intino.legio.graph.runnable.artifact.RunnablePackage asRunnable() {
			return a$(io.intino.legio.graph.runnable.artifact.RunnablePackage.class);
		}

		public io.intino.legio.graph.runnable.artifact.RunnablePackage asRunnable(java.lang.String mainClass) {
			io.intino.legio.graph.runnable.artifact.RunnablePackage newElement = core$().addFacet(io.intino.legio.graph.runnable.artifact.RunnablePackage.class);
			newElement.core$().set(newElement, "mainClass", java.util.Collections.singletonList(mainClass));
		    return newElement;
		}

		public boolean isRunnable() {
			return core$().is(io.intino.legio.graph.runnable.artifact.RunnablePackage.class);
		}

		public void removeRunnable() {
			core$().removeFacet(io.intino.legio.graph.runnable.artifact.RunnablePackage.class);
		}

		public io.intino.legio.graph.macos.artifact.MacOSPackage asMacOS() {
			return a$(io.intino.legio.graph.macos.artifact.MacOSPackage.class);
		}

		public io.intino.legio.graph.macos.artifact.MacOSPackage asMacOS(java.lang.String macIcon) {
			io.intino.legio.graph.macos.artifact.MacOSPackage newElement = core$().addFacet(io.intino.legio.graph.macos.artifact.MacOSPackage.class);
			newElement.core$().set(newElement, "macIcon", java.util.Collections.singletonList(macIcon));
		    return newElement;
		}

		public boolean isMacOS() {
			return core$().is(io.intino.legio.graph.macos.artifact.MacOSPackage.class);
		}

		public void removeMacOS() {
			core$().removeFacet(io.intino.legio.graph.macos.artifact.MacOSPackage.class);
		}

		public io.intino.legio.graph.windows.artifact.WindowsPackage asWindows() {
			return a$(io.intino.legio.graph.windows.artifact.WindowsPackage.class);
		}

		public io.intino.legio.graph.windows.artifact.WindowsPackage asWindows(java.lang.String windowsIcon) {
			io.intino.legio.graph.windows.artifact.WindowsPackage newElement = core$().addFacet(io.intino.legio.graph.windows.artifact.WindowsPackage.class);
			newElement.core$().set(newElement, "windowsIcon", java.util.Collections.singletonList(windowsIcon));
		    return newElement;
		}

		public boolean isWindows() {
			return core$().is(io.intino.legio.graph.windows.artifact.WindowsPackage.class);
		}

		public void removeWindows() {
			core$().removeFacet(io.intino.legio.graph.windows.artifact.WindowsPackage.class);
		}

		protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
			new java.util.ArrayList<>(parameterList).forEach(c -> components.add(c.core$()));
			new java.util.ArrayList<>(mavenPluginList).forEach(c -> components.add(c.core$()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("mode", new java.util.ArrayList(java.util.Collections.singletonList(this.mode)));
			map.put("attachSources", new java.util.ArrayList(java.util.Collections.singletonList(this.attachSources)));
			map.put("attachDoc", new java.util.ArrayList(java.util.Collections.singletonList(this.attachDoc)));
			map.put("includeTests", new java.util.ArrayList(java.util.Collections.singletonList(this.includeTests)));
			map.put("classpathPrefix", new java.util.ArrayList(java.util.Collections.singletonList(this.classpathPrefix)));
			map.put("finalName", new java.util.ArrayList(java.util.Collections.singletonList(this.finalName)));
			return map;
		}

		@Override
		protected void addNode$(io.intino.tara.magritte.Node node) {
			super.addNode$(node);
			if (node.is("Parameter")) this.parameterList.add(node.as(io.intino.legio.graph.Parameter.class));
			if (node.is("Artifact$Package$MavenPlugin")) this.mavenPluginList.add(node.as(io.intino.legio.graph.Artifact.Package.MavenPlugin.class));
		}

		@Override
	    protected void removeNode$(io.intino.tara.magritte.Node node) {
	        super.removeNode$(node);
	        if (node.is("Parameter")) this.parameterList.remove(node.as(io.intino.legio.graph.Parameter.class));
	        if (node.is("Artifact$Package$MavenPlugin")) this.mavenPluginList.remove(node.as(io.intino.legio.graph.Artifact.Package.MavenPlugin.class));
	    }

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("mode")) this.mode = io.intino.tara.magritte.loaders.WordLoader.load(values, Mode.class, this).get(0);
			else if (name.equalsIgnoreCase("attachSources")) this.attachSources = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("attachDoc")) this.attachDoc = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("includeTests")) this.includeTests = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("classpathPrefix")) this.classpathPrefix = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("finalName")) this.finalName = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("mode")) this.mode = (Mode) values.get(0);
			else if (name.equalsIgnoreCase("attachSources")) this.attachSources = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("attachDoc")) this.attachDoc = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("includeTests")) this.includeTests = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("classpathPrefix")) this.classpathPrefix = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("finalName")) this.finalName = (java.lang.String) values.get(0);
		}

		public Create create() {
			return new Create(null);
		}

		public Create create(java.lang.String name) {
			return new Create(name);
		}

		public class Create  {
			protected final java.lang.String name;

			public Create(java.lang.String name) {
				this.name = name;
			}

			public io.intino.legio.graph.Parameter parameter(java.lang.String name, java.lang.String value) {
			    io.intino.legio.graph.Parameter newElement = core$().graph().concept(io.intino.legio.graph.Parameter.class).createNode(this.name, core$()).as(io.intino.legio.graph.Parameter.class);
				newElement.core$().set(newElement, "name", java.util.Collections.singletonList(name));
				newElement.core$().set(newElement, "value", java.util.Collections.singletonList(value));
			    return newElement;
			}

			public io.intino.legio.graph.Artifact.Package.MavenPlugin mavenPlugin(java.lang.String code) {
			    io.intino.legio.graph.Artifact.Package.MavenPlugin newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Package.MavenPlugin.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Package.MavenPlugin.class);
				newElement.core$().set(newElement, "code", java.util.Collections.singletonList(code));
			    return newElement;
			}

		}

		public Clear clear() {
			return new Clear();
		}

		public class Clear  {
			public void parameter(java.util.function.Predicate<io.intino.legio.graph.Parameter> filter) {
				new java.util.ArrayList<>(parameterList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}

			public void mavenPlugin(java.util.function.Predicate<io.intino.legio.graph.Artifact.Package.MavenPlugin> filter) {
				new java.util.ArrayList<>(mavenPluginList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}
		}

		public static class MavenPlugin extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String code;

			public MavenPlugin(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String code() {
				return code;
			}

			public MavenPlugin code(java.lang.String value) {
				this.code = value;
				return (MavenPlugin) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("code", new java.util.ArrayList(java.util.Collections.singletonList(this.code)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("code")) this.code = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("code")) this.code = (java.lang.String) values.get(0);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Distribution extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected io.intino.legio.graph.Repository.Release release;
		protected io.intino.legio.graph.Repository.Language language;
		protected io.intino.legio.graph.Artifact.Distribution.OnBitbucket onBitbucket;

		public Distribution(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public io.intino.legio.graph.Repository.Release release() {
			return release;
		}

		public io.intino.legio.graph.Repository.Language language() {
			return language;
		}

		public Distribution release(io.intino.legio.graph.Repository.Release value) {
			this.release = value;
			return (Distribution) this;
		}

		public Distribution language(io.intino.legio.graph.Repository.Language value) {
			this.language = value;
			return (Distribution) this;
		}

		public io.intino.legio.graph.Artifact.Distribution.OnBitbucket onBitbucket() {
			return onBitbucket;
		}

		public Distribution onBitbucket(io.intino.legio.graph.Artifact.Distribution.OnBitbucket value) {
			this.onBitbucket = value;
			return (Distribution) this;
		}

		protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
			if (onBitbucket != null) components.add(this.onBitbucket.core$());
			return new java.util.ArrayList<>(components);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("release", this.release != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.release)) : java.util.Collections.emptyList());
			map.put("language", this.language != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.language)) : java.util.Collections.emptyList());
			return map;
		}

		@Override
		protected void addNode$(io.intino.tara.magritte.Node node) {
			super.addNode$(node);
			if (node.is("Artifact$Distribution$OnBitbucket")) this.onBitbucket = node.as(io.intino.legio.graph.Artifact.Distribution.OnBitbucket.class);
		}

		@Override
	    protected void removeNode$(io.intino.tara.magritte.Node node) {
	        super.removeNode$(node);
	        if (node.is("Artifact$Distribution$OnBitbucket")) this.onBitbucket = null;
	    }

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("release")) this.release = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.legio.graph.Repository.Release.class, this).get(0);
			else if (name.equalsIgnoreCase("language")) this.language = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.legio.graph.Repository.Language.class, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("release")) this.release = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.legio.graph.Repository.Release.class) : null;
			else if (name.equalsIgnoreCase("language")) this.language = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.legio.graph.Repository.Language.class) : null;
		}

		public Create create() {
			return new Create(null);
		}

		public Create create(java.lang.String name) {
			return new Create(name);
		}

		public class Create  {
			protected final java.lang.String name;

			public Create(java.lang.String name) {
				this.name = name;
			}

			public io.intino.legio.graph.Artifact.Distribution.OnBitbucket onBitbucket(java.lang.String owner, java.lang.String slugName) {
			    io.intino.legio.graph.Artifact.Distribution.OnBitbucket newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Distribution.OnBitbucket.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Distribution.OnBitbucket.class);
				newElement.core$().set(newElement, "owner", java.util.Collections.singletonList(owner));
				newElement.core$().set(newElement, "slugName", java.util.Collections.singletonList(slugName));
			    return newElement;
			}

		}

		public Clear clear() {
			return new Clear();
		}

		public class Clear  {

		}

		public static class OnBitbucket extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String owner;
			protected java.lang.String slugName;

			public OnBitbucket(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String owner() {
				return owner;
			}

			public java.lang.String slugName() {
				return slugName;
			}

			public OnBitbucket owner(java.lang.String value) {
				this.owner = value;
				return (OnBitbucket) this;
			}

			public OnBitbucket slugName(java.lang.String value) {
				this.slugName = value;
				return (OnBitbucket) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("owner", new java.util.ArrayList(java.util.Collections.singletonList(this.owner)));
				map.put("slugName", new java.util.ArrayList(java.util.Collections.singletonList(this.slugName)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("owner")) this.owner = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("slugName")) this.slugName = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("owner")) this.owner = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("slugName")) this.slugName = (java.lang.String) values.get(0);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class QualityAnalytics extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String url;
		protected io.intino.legio.graph.Artifact.QualityAnalytics.Authentication authentication;

		public QualityAnalytics(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String url() {
			return url;
		}

		public QualityAnalytics url(java.lang.String value) {
			this.url = value;
			return (QualityAnalytics) this;
		}

		public io.intino.legio.graph.Artifact.QualityAnalytics.Authentication authentication() {
			return authentication;
		}

		public QualityAnalytics authentication(io.intino.legio.graph.Artifact.QualityAnalytics.Authentication value) {
			this.authentication = value;
			return (QualityAnalytics) this;
		}

		protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
			if (authentication != null) components.add(this.authentication.core$());
			return new java.util.ArrayList<>(components);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
			return map;
		}

		@Override
		protected void addNode$(io.intino.tara.magritte.Node node) {
			super.addNode$(node);
			if (node.is("Artifact$QualityAnalytics$Authentication")) this.authentication = node.as(io.intino.legio.graph.Artifact.QualityAnalytics.Authentication.class);
		}

		@Override
	    protected void removeNode$(io.intino.tara.magritte.Node node) {
	        super.removeNode$(node);
	        if (node.is("Artifact$QualityAnalytics$Authentication")) this.authentication = null;
	    }

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("url")) this.url = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
		}

		public Create create() {
			return new Create(null);
		}

		public Create create(java.lang.String name) {
			return new Create(name);
		}

		public class Create  {
			protected final java.lang.String name;

			public Create(java.lang.String name) {
				this.name = name;
			}

			public io.intino.legio.graph.Artifact.QualityAnalytics.Authentication authentication(java.lang.String token) {
			    io.intino.legio.graph.Artifact.QualityAnalytics.Authentication newElement = core$().graph().concept(io.intino.legio.graph.Artifact.QualityAnalytics.Authentication.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.QualityAnalytics.Authentication.class);
				newElement.core$().set(newElement, "token", java.util.Collections.singletonList(token));
			    return newElement;
			}

		}

		public Clear clear() {
			return new Clear();
		}

		public class Clear  {

		}

		public static class Authentication extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String token;

			public Authentication(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String token() {
				return token;
			}

			public Authentication token(java.lang.String value) {
				this.token = value;
				return (Authentication) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("token", new java.util.ArrayList(java.util.Collections.singletonList(this.token)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("token")) this.token = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("token")) this.token = (java.lang.String) values.get(0);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Deployment extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.util.List<java.lang.String> tags = new java.util.ArrayList<>();
		protected io.intino.legio.graph.functions.Destinations destinations;
		protected io.intino.legio.graph.Artifact.Deployment.Dev dev;
		protected io.intino.legio.graph.Artifact.Deployment.Pro pro;

		public Deployment(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<java.lang.String> tags() {
			return tags;
		}

		public java.lang.String tags(int index) {
			return tags.get(index);
		}

		public java.util.List<java.lang.String> tags(java.util.function.Predicate<java.lang.String> predicate) {
			return tags().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public List<Destination> destinations() {
			return destinations.destinations();
		}

		public Deployment destinations(io.intino.legio.graph.functions.Destinations value) {
			this.destinations = io.intino.tara.magritte.loaders.FunctionLoader.load(destinations, this, io.intino.legio.graph.functions.Destinations.class);
			return (Deployment) this;
		}

		public io.intino.legio.graph.Artifact.Deployment.Dev dev() {
			return dev;
		}

		public io.intino.legio.graph.Artifact.Deployment.Pro pro() {
			return pro;
		}

		public Deployment dev(io.intino.legio.graph.Artifact.Deployment.Dev value) {
			this.dev = value;
			return (Deployment) this;
		}

		public Deployment pro(io.intino.legio.graph.Artifact.Deployment.Pro value) {
			this.pro = value;
			return (Deployment) this;
		}

		protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
			if (dev != null) components.add(this.dev.core$());
			if (pro != null) components.add(this.pro.core$());
			return new java.util.ArrayList<>(components);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("tags", this.tags);
			map.put("destinations", this.destinations != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.destinations)) : java.util.Collections.emptyList());
			return map;
		}

		@Override
		protected void addNode$(io.intino.tara.magritte.Node node) {
			super.addNode$(node);
			if (node.is("Artifact$Deployment$Dev")) this.dev = node.as(io.intino.legio.graph.Artifact.Deployment.Dev.class);
			if (node.is("Artifact$Deployment$Pro")) this.pro = node.as(io.intino.legio.graph.Artifact.Deployment.Pro.class);
		}

		@Override
	    protected void removeNode$(io.intino.tara.magritte.Node node) {
	        super.removeNode$(node);
	        if (node.is("Artifact$Deployment$Dev")) this.dev = null;
	        if (node.is("Artifact$Deployment$Pro")) this.pro = null;
	    }

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("tags")) this.tags = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
			else if (name.equalsIgnoreCase("destinations")) this.destinations = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.legio.graph.functions.Destinations.class).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("tags")) this.tags = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
			else if (name.equalsIgnoreCase("destinations")) this.destinations = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.legio.graph.functions.Destinations.class);
		}

		public Create create() {
			return new Create(null);
		}

		public Create create(java.lang.String name) {
			return new Create(name);
		}

		public class Create  {
			protected final java.lang.String name;

			public Create(java.lang.String name) {
				this.name = name;
			}

			public io.intino.legio.graph.Artifact.Deployment.Dev dev(io.intino.legio.graph.Server server, io.intino.legio.graph.RunConfiguration runConfiguration) {
			    io.intino.legio.graph.Artifact.Deployment.Dev newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Deployment.Dev.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Deployment.Dev.class);
				newElement.core$().set(newElement, "server", java.util.Collections.singletonList(server));
				newElement.core$().set(newElement, "runConfiguration", java.util.Collections.singletonList(runConfiguration));
			    return newElement;
			}

			public io.intino.legio.graph.Artifact.Deployment.Pro pro(io.intino.legio.graph.Server server, io.intino.legio.graph.RunConfiguration runConfiguration) {
			    io.intino.legio.graph.Artifact.Deployment.Pro newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Deployment.Pro.class).createNode(this.name, core$()).as(io.intino.legio.graph.Artifact.Deployment.Pro.class);
				newElement.core$().set(newElement, "server", java.util.Collections.singletonList(server));
				newElement.core$().set(newElement, "runConfiguration", java.util.Collections.singletonList(runConfiguration));
			    return newElement;
			}

		}

		public Clear clear() {
			return new Clear();
		}

		public class Clear  {



		}

		public static class Dev extends io.intino.legio.graph.Destination implements io.intino.tara.magritte.tags.Component, io.intino.tara.magritte.tags.Terminal {


			public Dev(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());

				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}

		public static class Pro extends io.intino.legio.graph.Destination implements io.intino.tara.magritte.tags.Component, io.intino.tara.magritte.tags.Terminal {


			public Pro(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());

				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}


	public io.intino.legio.graph.LegioGraph graph() {
		return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
	}
}
