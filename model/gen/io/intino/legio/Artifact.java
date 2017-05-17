package io.intino.legio;

public class Artifact extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String groupId;
	protected java.lang.String version;
	protected io.intino.legio.Artifact.License license;
	protected io.intino.legio.Artifact.Imports imports;
	protected io.intino.legio.Artifact.WebImports webImports;
	protected io.intino.legio.Artifact.Box box;
	protected io.intino.legio.Artifact.Code code;
	protected java.util.List<io.intino.legio.Artifact.Exports> exportsList = new java.util.ArrayList<>();
	protected io.intino.legio.Artifact.Package package$;
	protected io.intino.legio.Artifact.Distribution distribution;
	protected io.intino.legio.Artifact.QualityAnalytics qualityAnalytics;
	protected java.util.List<io.intino.legio.Artifact.Deployment> deploymentList = new java.util.ArrayList<>();

	public Artifact(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String groupId() {
		return groupId;
	}

	public java.lang.String version() {
		return version;
	}

	public void groupId(java.lang.String value) {
		this.groupId = value;
	}

	public void version(java.lang.String value) {
		this.version = value;
	}

	public io.intino.legio.Artifact.License license() {
		return license;
	}

	public io.intino.legio.Artifact.Imports imports() {
		return imports;
	}

	public io.intino.legio.Artifact.WebImports webImports() {
		return webImports;
	}

	public io.intino.legio.Artifact.Box box() {
		return box;
	}

	public io.intino.legio.Artifact.Code code() {
		return code;
	}

	public java.util.List<io.intino.legio.Artifact.Exports> exportsList() {
		return java.util.Collections.unmodifiableList(exportsList);
	}

	public io.intino.legio.Artifact.Exports exports(int index) {
		return exportsList.get(index);
	}

	public java.util.List<io.intino.legio.Artifact.Exports> exportsList(java.util.function.Predicate<io.intino.legio.Artifact.Exports> predicate) {
		return exportsList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.Artifact.Package package$() {
		return package$;
	}

	public io.intino.legio.Artifact.Distribution distribution() {
		return distribution;
	}

	public io.intino.legio.Artifact.QualityAnalytics qualityAnalytics() {
		return qualityAnalytics;
	}

	public java.util.List<io.intino.legio.Artifact.Deployment> deploymentList() {
		return java.util.Collections.unmodifiableList(deploymentList);
	}

	public io.intino.legio.Artifact.Deployment deployment(int index) {
		return deploymentList.get(index);
	}

	public java.util.List<io.intino.legio.Artifact.Deployment> deploymentList(java.util.function.Predicate<io.intino.legio.Artifact.Deployment> predicate) {
		return deploymentList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public void license(io.intino.legio.Artifact.License value) {
		this.license = value;
	}

	public void imports(io.intino.legio.Artifact.Imports value) {
		this.imports = value;
	}

	public void webImports(io.intino.legio.Artifact.WebImports value) {
		this.webImports = value;
	}

	public void box(io.intino.legio.Artifact.Box value) {
		this.box = value;
	}

	public void code(io.intino.legio.Artifact.Code value) {
		this.code = value;
	}

	

	public void package$(io.intino.legio.Artifact.Package value) {
		this.package$ = value;
	}

	public void distribution(io.intino.legio.Artifact.Distribution value) {
		this.distribution = value;
	}

	public void qualityAnalytics(io.intino.legio.Artifact.QualityAnalytics value) {
		this.qualityAnalytics = value;
	}

	

	public io.intino.legio.platform.PlatformArtifact asPlatform() {
		io.intino.tara.magritte.Layer as = this.as(io.intino.legio.platform.PlatformArtifact.class);
		return as != null ? (io.intino.legio.platform.PlatformArtifact) as : addFacet(io.intino.legio.platform.PlatformArtifact.class);
	}

	public boolean isPlatform() {
		return is(io.intino.legio.platform.PlatformArtifact.class);
	}

	public io.intino.legio.product.ProductArtifact asProduct() {
		io.intino.tara.magritte.Layer as = this.as(io.intino.legio.product.ProductArtifact.class);
		return as != null ? (io.intino.legio.product.ProductArtifact) as : addFacet(io.intino.legio.product.ProductArtifact.class);
	}

	public boolean isProduct() {
		return is(io.intino.legio.product.ProductArtifact.class);
	}

	public io.intino.legio.level.LevelArtifact asLevel() {
		io.intino.tara.magritte.Layer as = this.as(io.intino.legio.level.LevelArtifact.class);
		return as != null ? (io.intino.legio.level.LevelArtifact) as : null;
	}

	public boolean isLevel() {
		return is(io.intino.legio.level.LevelArtifact.class);
	}

	public io.intino.legio.system.SystemArtifact asSystem() {
		io.intino.tara.magritte.Layer as = this.as(io.intino.legio.system.SystemArtifact.class);
		return as != null ? (io.intino.legio.system.SystemArtifact) as : addFacet(io.intino.legio.system.SystemArtifact.class);
	}

	public boolean isSystem() {
		return is(io.intino.legio.system.SystemArtifact.class);
	}

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		if (license != null) components.add(this.license.node());
		if (imports != null) components.add(this.imports.node());
		if (webImports != null) components.add(this.webImports.node());
		if (box != null) components.add(this.box.node());
		if (code != null) components.add(this.code.node());
		exportsList.stream().forEach(c -> components.add(c.node()));
		if (package$ != null) components.add(this.package$.node());
		if (distribution != null) components.add(this.distribution.node());
		if (qualityAnalytics != null) components.add(this.qualityAnalytics.node());
		deploymentList.stream().forEach(c -> components.add(c.node()));
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
		map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Artifact.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		super.addNode(node);
		if (node.is("Artifact$License")) this.license = node.as(io.intino.legio.Artifact.License.class);
		if (node.is("Artifact$Imports")) this.imports = node.as(io.intino.legio.Artifact.Imports.class);
		if (node.is("Artifact$WebImports")) this.webImports = node.as(io.intino.legio.Artifact.WebImports.class);
		if (node.is("Artifact$Box")) this.box = node.as(io.intino.legio.Artifact.Box.class);
		if (node.is("Artifact$Code")) this.code = node.as(io.intino.legio.Artifact.Code.class);
		if (node.is("Artifact$Exports")) this.exportsList.add(node.as(io.intino.legio.Artifact.Exports.class));
		if (node.is("Artifact$Package")) this.package$ = node.as(io.intino.legio.Artifact.Package.class);
		if (node.is("Artifact$Distribution")) this.distribution = node.as(io.intino.legio.Artifact.Distribution.class);
		if (node.is("Artifact$QualityAnalytics")) this.qualityAnalytics = node.as(io.intino.legio.Artifact.QualityAnalytics.class);
		if (node.is("Artifact$Deployment")) this.deploymentList.add(node.as(io.intino.legio.Artifact.Deployment.class));
	}

	@Override
    protected void removeNode(io.intino.tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("Artifact$License")) this.license = null;
        if (node.is("Artifact$Imports")) this.imports = null;
        if (node.is("Artifact$WebImports")) this.webImports = null;
        if (node.is("Artifact$Box")) this.box = null;
        if (node.is("Artifact$Code")) this.code = null;
        if (node.is("Artifact$Exports")) this.exportsList.remove(node.as(io.intino.legio.Artifact.Exports.class));
        if (node.is("Artifact$Package")) this.package$ = null;
        if (node.is("Artifact$Distribution")) this.distribution = null;
        if (node.is("Artifact$QualityAnalytics")) this.qualityAnalytics = null;
        if (node.is("Artifact$Deployment")) this.deploymentList.remove(node.as(io.intino.legio.Artifact.Deployment.class));
    }

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("groupId")) this.groupId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("groupId")) this.groupId = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
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

		public io.intino.legio.Artifact.License license(io.intino.legio.Artifact.License.Type type) {
		    io.intino.legio.Artifact.License newElement = graph().concept(io.intino.legio.Artifact.License.class).createNode(name, node()).as(io.intino.legio.Artifact.License.class);
			newElement.node().set(newElement, "type", java.util.Collections.singletonList(type)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Imports imports() {
		    io.intino.legio.Artifact.Imports newElement = graph().concept(io.intino.legio.Artifact.Imports.class).createNode(name, node()).as(io.intino.legio.Artifact.Imports.class);
		    return newElement;
		}

		public io.intino.legio.Artifact.WebImports webImports() {
		    io.intino.legio.Artifact.WebImports newElement = graph().concept(io.intino.legio.Artifact.WebImports.class).createNode(name, node()).as(io.intino.legio.Artifact.WebImports.class);
		    return newElement;
		}

		public io.intino.legio.Artifact.Box box(java.lang.String language, java.lang.String version, java.lang.String sdk) {
		    io.intino.legio.Artifact.Box newElement = graph().concept(io.intino.legio.Artifact.Box.class).createNode(name, node()).as(io.intino.legio.Artifact.Box.class);
			newElement.node().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
			newElement.node().set(newElement, "sdk", java.util.Collections.singletonList(sdk)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Code code(java.lang.String targetPackage) {
		    io.intino.legio.Artifact.Code newElement = graph().concept(io.intino.legio.Artifact.Code.class).createNode(name, node()).as(io.intino.legio.Artifact.Code.class);
			newElement.node().set(newElement, "targetPackage", java.util.Collections.singletonList(targetPackage)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Exports exports(io.intino.legio.Repository.Type repository) {
		    io.intino.legio.Artifact.Exports newElement = graph().concept(io.intino.legio.Artifact.Exports.class).createNode(name, node()).as(io.intino.legio.Artifact.Exports.class);
			newElement.node().set(newElement, "repository", java.util.Collections.singletonList(repository)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Package package$(io.intino.legio.Artifact.Package.Mode mode) {
		    io.intino.legio.Artifact.Package newElement = graph().concept(io.intino.legio.Artifact.Package.class).createNode(name, node()).as(io.intino.legio.Artifact.Package.class);
			newElement.node().set(newElement, "mode", java.util.Collections.singletonList(mode)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Distribution distribution(io.intino.legio.Repository.Release release, io.intino.legio.Repository.Language language) {
		    io.intino.legio.Artifact.Distribution newElement = graph().concept(io.intino.legio.Artifact.Distribution.class).createNode(name, node()).as(io.intino.legio.Artifact.Distribution.class);
			newElement.node().set(newElement, "release", java.util.Collections.singletonList(release));
			newElement.node().set(newElement, "language", java.util.Collections.singletonList(language)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.QualityAnalytics qualityAnalytics(java.lang.String url) {
		    io.intino.legio.Artifact.QualityAnalytics newElement = graph().concept(io.intino.legio.Artifact.QualityAnalytics.class).createNode(name, node()).as(io.intino.legio.Artifact.QualityAnalytics.class);
			newElement.node().set(newElement, "url", java.util.Collections.singletonList(url)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Deployment deployment(io.intino.legio.Server server) {
		    io.intino.legio.Artifact.Deployment newElement = graph().concept(io.intino.legio.Artifact.Deployment.class).createNode(name, node()).as(io.intino.legio.Artifact.Deployment.class);
			newElement.node().set(newElement, "server", java.util.Collections.singletonList(server)); 
		    return newElement;
		}
		
	}
	
	public static class License extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected Type type;

		public enum Type {
			GPL, BSD;
		}

		public License(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public Type type() {
			return type;
		}

		public void type(io.intino.legio.Artifact.License.Type value) {
			this.type = value;
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("type", new java.util.ArrayList(java.util.Collections.singletonList(this.type)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.License.class);
		}

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("type")) this.type = io.intino.tara.magritte.loaders.WordLoader.load(values, Type.class, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("type")) this.type = (Type) values.get(0);
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
			
		}
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	public static class Imports extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		
		protected java.util.List<io.intino.legio.Artifact.Imports.Dependency> dependencyList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Artifact.Imports.Compile> compileList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Artifact.Imports.Runtime> runtimeList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Artifact.Imports.Provided> providedList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Artifact.Imports.Test> testList = new java.util.ArrayList<>();

		public Imports(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Dependency> dependencyList() {
			return java.util.Collections.unmodifiableList(dependencyList);
		}

		public io.intino.legio.Artifact.Imports.Dependency dependency(int index) {
			return dependencyList.get(index);
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Dependency> dependencyList(java.util.function.Predicate<io.intino.legio.Artifact.Imports.Dependency> predicate) {
			return dependencyList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Compile> compileList() {
			return java.util.Collections.unmodifiableList(compileList);
		}

		public io.intino.legio.Artifact.Imports.Compile compile(int index) {
			return compileList.get(index);
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Compile> compileList(java.util.function.Predicate<io.intino.legio.Artifact.Imports.Compile> predicate) {
			return compileList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Runtime> runtimeList() {
			return java.util.Collections.unmodifiableList(runtimeList);
		}

		public io.intino.legio.Artifact.Imports.Runtime runtime(int index) {
			return runtimeList.get(index);
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Runtime> runtimeList(java.util.function.Predicate<io.intino.legio.Artifact.Imports.Runtime> predicate) {
			return runtimeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Provided> providedList() {
			return java.util.Collections.unmodifiableList(providedList);
		}

		public io.intino.legio.Artifact.Imports.Provided provided(int index) {
			return providedList.get(index);
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Provided> providedList(java.util.function.Predicate<io.intino.legio.Artifact.Imports.Provided> predicate) {
			return providedList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Test> testList() {
			return java.util.Collections.unmodifiableList(testList);
		}

		public io.intino.legio.Artifact.Imports.Test test(int index) {
			return testList.get(index);
		}

		public java.util.List<io.intino.legio.Artifact.Imports.Test> testList(java.util.function.Predicate<io.intino.legio.Artifact.Imports.Test> predicate) {
			return testList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		

		

		

		

		

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			dependencyList.stream().forEach(c -> components.add(c.node()));
			compileList.stream().forEach(c -> components.add(c.node()));
			runtimeList.stream().forEach(c -> components.add(c.node()));
			providedList.stream().forEach(c -> components.add(c.node()));
			testList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.Imports.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Artifact$Imports$Dependency")) this.dependencyList.add(node.as(io.intino.legio.Artifact.Imports.Dependency.class));
			if (node.is("Artifact$Imports$Compile")) this.compileList.add(node.as(io.intino.legio.Artifact.Imports.Compile.class));
			if (node.is("Artifact$Imports$Runtime")) this.runtimeList.add(node.as(io.intino.legio.Artifact.Imports.Runtime.class));
			if (node.is("Artifact$Imports$Provided")) this.providedList.add(node.as(io.intino.legio.Artifact.Imports.Provided.class));
			if (node.is("Artifact$Imports$Test")) this.testList.add(node.as(io.intino.legio.Artifact.Imports.Test.class));
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Artifact$Imports$Dependency")) this.dependencyList.remove(node.as(io.intino.legio.Artifact.Imports.Dependency.class));
	        if (node.is("Artifact$Imports$Compile")) this.compileList.remove(node.as(io.intino.legio.Artifact.Imports.Compile.class));
	        if (node.is("Artifact$Imports$Runtime")) this.runtimeList.remove(node.as(io.intino.legio.Artifact.Imports.Runtime.class));
	        if (node.is("Artifact$Imports$Provided")) this.providedList.remove(node.as(io.intino.legio.Artifact.Imports.Provided.class));
	        if (node.is("Artifact$Imports$Test")) this.testList.remove(node.as(io.intino.legio.Artifact.Imports.Test.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
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

			

			public io.intino.legio.Artifact.Imports.Compile compile(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.Artifact.Imports.Compile newElement = graph().concept(io.intino.legio.Artifact.Imports.Compile.class).createNode(name, node()).as(io.intino.legio.Artifact.Imports.Compile.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.node().set(newElement, "artifacts", artifacts); 
			    return newElement;
			}

			public io.intino.legio.Artifact.Imports.Runtime runtime(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.Artifact.Imports.Runtime newElement = graph().concept(io.intino.legio.Artifact.Imports.Runtime.class).createNode(name, node()).as(io.intino.legio.Artifact.Imports.Runtime.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.node().set(newElement, "artifacts", artifacts); 
			    return newElement;
			}

			public io.intino.legio.Artifact.Imports.Provided provided(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.Artifact.Imports.Provided newElement = graph().concept(io.intino.legio.Artifact.Imports.Provided.class).createNode(name, node()).as(io.intino.legio.Artifact.Imports.Provided.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.node().set(newElement, "artifacts", artifacts); 
			    return newElement;
			}

			public io.intino.legio.Artifact.Imports.Test test(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.Artifact.Imports.Test newElement = graph().concept(io.intino.legio.Artifact.Imports.Test.class).createNode(name, node()).as(io.intino.legio.Artifact.Imports.Test.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.node().set(newElement, "artifacts", artifacts); 
			    return newElement;
			}
			
		}
		
		public static abstract class Dependency extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected io.intino.tara.magritte.Expression<java.lang.String> identifier;
			protected io.intino.tara.magritte.Expression<java.lang.String> name$;
			protected java.lang.String groupId;
			protected java.lang.String artifactId;
			protected java.lang.String version;
			protected java.lang.String effectiveVersion;
			protected boolean transitive;
			protected java.util.List<java.lang.String> artifacts = new java.util.ArrayList<>();
			protected boolean resolved;
			protected boolean toModule;
			protected java.util.List<io.intino.legio.Artifact.Imports.Dependency.Exclude> excludeList = new java.util.ArrayList<>();

			public Dependency(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String identifier() {
				return identifier.value();
			}

			public java.lang.String name$() {
				return name$.value();
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

			public boolean resolved() {
				return resolved;
			}

			public boolean toModule() {
				return toModule;
			}

			public void identifier(io.intino.tara.magritte.Expression<java.lang.String> value) {
				this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
			}

			public void name$(io.intino.tara.magritte.Expression<java.lang.String> value) {
				this.name$ = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
			}

			public void groupId(java.lang.String value) {
				this.groupId = value;
			}

			public void artifactId(java.lang.String value) {
				this.artifactId = value;
			}

			public void version(java.lang.String value) {
				this.version = value;
			}

			public void effectiveVersion(java.lang.String value) {
				this.effectiveVersion = value;
			}

			public void transitive(boolean value) {
				this.transitive = value;
			}

			public void resolved(boolean value) {
				this.resolved = value;
			}

			public void toModule(boolean value) {
				this.toModule = value;
			}

			public java.util.List<io.intino.legio.Artifact.Imports.Dependency.Exclude> excludeList() {
				return java.util.Collections.unmodifiableList(excludeList);
			}

			public io.intino.legio.Artifact.Imports.Dependency.Exclude exclude(int index) {
				return excludeList.get(index);
			}

			public java.util.List<io.intino.legio.Artifact.Imports.Dependency.Exclude> excludeList(java.util.function.Predicate<io.intino.legio.Artifact.Imports.Dependency.Exclude> predicate) {
				return excludeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}

			

			public java.util.List<io.intino.tara.magritte.Node> componentList() {
				java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
				excludeList.stream().forEach(c -> components.add(c.node()));
				return new java.util.ArrayList<>(components);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("identifier", new java.util.ArrayList(java.util.Collections.singletonList(this.identifier)));
				map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name$)));
				map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
				map.put("artifactId", new java.util.ArrayList(java.util.Collections.singletonList(this.artifactId)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				map.put("effectiveVersion", new java.util.ArrayList(java.util.Collections.singletonList(this.effectiveVersion)));
				map.put("transitive", new java.util.ArrayList(java.util.Collections.singletonList(this.transitive)));
				map.put("artifacts", this.artifacts);
				map.put("resolved", new java.util.ArrayList(java.util.Collections.singletonList(this.resolved)));
				map.put("toModule", new java.util.ArrayList(java.util.Collections.singletonList(this.toModule)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Imports.Dependency.class);
			}

			@Override
			protected void addNode(io.intino.tara.magritte.Node node) {
				super.addNode(node);
				if (node.is("Artifact$Imports$Dependency$Exclude")) this.excludeList.add(node.as(io.intino.legio.Artifact.Imports.Dependency.Exclude.class));
			}

			@Override
		    protected void removeNode(io.intino.tara.magritte.Node node) {
		        super.removeNode(node);
		        if (node.is("Artifact$Imports$Dependency$Exclude")) this.excludeList.remove(node.as(io.intino.legio.Artifact.Imports.Dependency.Exclude.class));
		    }

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
				else if (name.equalsIgnoreCase("name")) this.name$ = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("transitive")) this.transitive = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifacts")) this.artifacts = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
				else if (name.equalsIgnoreCase("resolved")) this.resolved = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("toModule")) this.toModule = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
				else if (name.equalsIgnoreCase("name")) this.name$ = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("transitive")) this.transitive = (java.lang.Boolean) values.get(0);
				else if (name.equalsIgnoreCase("artifacts")) this.artifacts = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
				else if (name.equalsIgnoreCase("resolved")) this.resolved = (java.lang.Boolean) values.get(0);
				else if (name.equalsIgnoreCase("toModule")) this.toModule = (java.lang.Boolean) values.get(0);
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

				public io.intino.legio.Artifact.Imports.Dependency.Exclude exclude(java.lang.String groupId, java.lang.String artifactId) {
				    io.intino.legio.Artifact.Imports.Dependency.Exclude newElement = graph().concept(io.intino.legio.Artifact.Imports.Dependency.Exclude.class).createNode(name, node()).as(io.intino.legio.Artifact.Imports.Dependency.Exclude.class);
					newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
					newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId)); 
				    return newElement;
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

				public void groupId(java.lang.String value) {
					this.groupId = value;
				}

				public void artifactId(java.lang.String value) {
					this.artifactId = value;
				}

				@Override
				public java.util.Map<java.lang.String, java.util.List<?>> variables() {
					java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
					map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
					map.put("artifactId", new java.util.ArrayList(java.util.Collections.singletonList(this.artifactId)));
					return map;
				}

				public io.intino.tara.magritte.Concept concept() {
					return this.graph().concept(io.intino.legio.Artifact.Imports.Dependency.Exclude.class);
				}

				@Override
				protected void _load(java.lang.String name, java.util.List<?> values) {
					super._load(name, values);
					if (name.equalsIgnoreCase("groupId")) this.groupId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
					else if (name.equalsIgnoreCase("artifactId")) this.artifactId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				}

				@Override
				protected void _set(java.lang.String name, java.util.List<?> values) {
					super._set(name, values);
					if (name.equalsIgnoreCase("groupId")) this.groupId = (java.lang.String) values.get(0);
					else if (name.equalsIgnoreCase("artifactId")) this.artifactId = (java.lang.String) values.get(0);
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
					
				}
				
				public io.intino.legio.Legio legioWrapper() {
					return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
				}
			}
			
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		public static class Compile extends io.intino.legio.Artifact.Imports.Dependency implements io.intino.tara.magritte.tags.Terminal {
			

			public Compile(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Imports.Compile.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
			}

			public Create create() {
				return new Create(null);
			}

			public Create create(java.lang.String name) {
				return new Create(name);
			}

			public class Create extends io.intino.legio.Artifact.Imports.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		public static class Runtime extends io.intino.legio.Artifact.Imports.Dependency implements io.intino.tara.magritte.tags.Terminal {
			

			public Runtime(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Imports.Runtime.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
			}

			public Create create() {
				return new Create(null);
			}

			public Create create(java.lang.String name) {
				return new Create(name);
			}

			public class Create extends io.intino.legio.Artifact.Imports.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		public static class Provided extends io.intino.legio.Artifact.Imports.Dependency implements io.intino.tara.magritte.tags.Terminal {
			

			public Provided(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Imports.Provided.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
			}

			public Create create() {
				return new Create(null);
			}

			public Create create(java.lang.String name) {
				return new Create(name);
			}

			public class Create extends io.intino.legio.Artifact.Imports.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		public static class Test extends io.intino.legio.Artifact.Imports.Dependency implements io.intino.tara.magritte.tags.Terminal {
			

			public Test(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Imports.Test.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
			}

			public Create create() {
				return new Create(null);
			}

			public Create create(java.lang.String name) {
				return new Create(name);
			}

			public class Create extends io.intino.legio.Artifact.Imports.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	public static class WebImports extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String webDirectory;
		protected java.util.List<io.intino.legio.Artifact.WebImports.Resolution> resolutionList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Artifact.WebImports.WebComponent> webComponentList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Artifact.WebImports.WebActivity> webActivityList = new java.util.ArrayList<>();

		public WebImports(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String webDirectory() {
			return webDirectory;
		}

		public void webDirectory(java.lang.String value) {
			this.webDirectory = value;
		}

		public java.util.List<io.intino.legio.Artifact.WebImports.Resolution> resolutionList() {
			return java.util.Collections.unmodifiableList(resolutionList);
		}

		public io.intino.legio.Artifact.WebImports.Resolution resolution(int index) {
			return resolutionList.get(index);
		}

		public java.util.List<io.intino.legio.Artifact.WebImports.Resolution> resolutionList(java.util.function.Predicate<io.intino.legio.Artifact.WebImports.Resolution> predicate) {
			return resolutionList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Artifact.WebImports.WebComponent> webComponentList() {
			return java.util.Collections.unmodifiableList(webComponentList);
		}

		public io.intino.legio.Artifact.WebImports.WebComponent webComponent(int index) {
			return webComponentList.get(index);
		}

		public java.util.List<io.intino.legio.Artifact.WebImports.WebComponent> webComponentList(java.util.function.Predicate<io.intino.legio.Artifact.WebImports.WebComponent> predicate) {
			return webComponentList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Artifact.WebImports.WebActivity> webActivityList() {
			return java.util.Collections.unmodifiableList(webActivityList);
		}

		public io.intino.legio.Artifact.WebImports.WebActivity webActivity(int index) {
			return webActivityList.get(index);
		}

		public java.util.List<io.intino.legio.Artifact.WebImports.WebActivity> webActivityList(java.util.function.Predicate<io.intino.legio.Artifact.WebImports.WebActivity> predicate) {
			return webActivityList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		

		

		

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			resolutionList.stream().forEach(c -> components.add(c.node()));
			webComponentList.stream().forEach(c -> components.add(c.node()));
			webActivityList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("webDirectory", new java.util.ArrayList(java.util.Collections.singletonList(this.webDirectory)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.WebImports.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Artifact$WebImports$Resolution")) this.resolutionList.add(node.as(io.intino.legio.Artifact.WebImports.Resolution.class));
			if (node.is("Artifact$WebImports$WebComponent")) this.webComponentList.add(node.as(io.intino.legio.Artifact.WebImports.WebComponent.class));
			if (node.is("Artifact$WebImports$WebActivity")) this.webActivityList.add(node.as(io.intino.legio.Artifact.WebImports.WebActivity.class));
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Artifact$WebImports$Resolution")) this.resolutionList.remove(node.as(io.intino.legio.Artifact.WebImports.Resolution.class));
	        if (node.is("Artifact$WebImports$WebComponent")) this.webComponentList.remove(node.as(io.intino.legio.Artifact.WebImports.WebComponent.class));
	        if (node.is("Artifact$WebImports$WebActivity")) this.webActivityList.remove(node.as(io.intino.legio.Artifact.WebImports.WebActivity.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("webDirectory")) this.webDirectory = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("webDirectory")) this.webDirectory = (java.lang.String) values.get(0);
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

			public io.intino.legio.Artifact.WebImports.Resolution resolution(java.lang.String name$, java.lang.String version) {
			    io.intino.legio.Artifact.WebImports.Resolution newElement = graph().concept(io.intino.legio.Artifact.WebImports.Resolution.class).createNode(name, node()).as(io.intino.legio.Artifact.WebImports.Resolution.class);
				newElement.node().set(newElement, "name", java.util.Collections.singletonList(name$));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}

			public io.intino.legio.Artifact.WebImports.WebComponent webComponent(java.lang.String version) {
			    io.intino.legio.Artifact.WebImports.WebComponent newElement = graph().concept(io.intino.legio.Artifact.WebImports.WebComponent.class).createNode(name, node()).as(io.intino.legio.Artifact.WebImports.WebComponent.class);
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}

			public io.intino.legio.Artifact.WebImports.WebActivity webActivity(java.lang.String groupId, java.lang.String artifactId, java.lang.String version) {
			    io.intino.legio.Artifact.WebImports.WebActivity newElement = graph().concept(io.intino.legio.Artifact.WebImports.WebActivity.class).createNode(name, node()).as(io.intino.legio.Artifact.WebImports.WebActivity.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}
			
		}
		
		public static class Resolution extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String name$;
			protected java.lang.String version;

			public Resolution(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String name$() {
				return name$;
			}

			public java.lang.String version() {
				return version;
			}

			public void name$(java.lang.String value) {
				this.name$ = value;
			}

			public void version(java.lang.String value) {
				this.version = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name$)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.WebImports.Resolution.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("name")) this.name$ = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("name")) this.name$ = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
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
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
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

			public void url(java.lang.String value) {
				this.url = value;
			}

			public void version(java.lang.String value) {
				this.version = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.WebImports.WebComponent.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("url")) this.url = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
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
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		public static class WebActivity extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected io.intino.tara.magritte.Expression<java.lang.String> identifier;
			protected java.lang.String groupId;
			protected java.lang.String artifactId;
			protected java.lang.String version;

			public WebActivity(io.intino.tara.magritte.Node node) {
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

			public void identifier(io.intino.tara.magritte.Expression<java.lang.String> value) {
				this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
			}

			public void groupId(java.lang.String value) {
				this.groupId = value;
			}

			public void artifactId(java.lang.String value) {
				this.artifactId = value;
			}

			public void version(java.lang.String value) {
				this.version = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("identifier", new java.util.ArrayList(java.util.Collections.singletonList(this.identifier)));
				map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
				map.put("artifactId", new java.util.ArrayList(java.util.Collections.singletonList(this.artifactId)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.WebImports.WebActivity.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
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
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	public static class Box extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String language;
		protected java.lang.String version;
		protected java.lang.String sdk;

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

		public void language(java.lang.String value) {
			this.language = value;
		}

		public void version(java.lang.String value) {
			this.version = value;
		}

		public void sdk(java.lang.String value) {
			this.sdk = value;
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("language", new java.util.ArrayList(java.util.Collections.singletonList(this.language)));
			map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
			map.put("sdk", new java.util.ArrayList(java.util.Collections.singletonList(this.sdk)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.Box.class);
		}

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("language")) this.language = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("sdk")) this.sdk = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("language")) this.language = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("sdk")) this.sdk = (java.lang.String) values.get(0);
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
			
		}
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
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

		public void targetPackage(java.lang.String value) {
			this.targetPackage = value;
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("targetPackage", new java.util.ArrayList(java.util.Collections.singletonList(this.targetPackage)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.Code.class);
		}

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("targetPackage")) this.targetPackage = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("targetPackage")) this.targetPackage = (java.lang.String) values.get(0);
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
			
		}
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	public static class Exports extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected io.intino.legio.Repository.Type repository;

		public Exports(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public io.intino.legio.Repository.Type repository() {
			return repository;
		}

		public void repository(io.intino.legio.Repository.Type value) {
			this.repository = value;
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("repository", this.repository != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.repository)) : java.util.Collections.emptyList());
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.Exports.class);
		}

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("repository")) this.repository = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.legio.Repository.Type.class, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("repository")) this.repository = values.get(0)!= null ? graph().loadNode(((io.intino.tara.magritte.Layer) values.get(0)).id()).as(io.intino.legio.Repository.Type.class) : null;
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
			
		}
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
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
		protected java.util.List<io.intino.legio.Artifact.Package.MavenPlugin> mavenPluginList = new java.util.ArrayList<>();

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

		public void mode(io.intino.legio.Artifact.Package.Mode value) {
			this.mode = value;
		}

		public void attachSources(boolean value) {
			this.attachSources = value;
		}

		public void attachDoc(boolean value) {
			this.attachDoc = value;
		}

		public void includeTests(boolean value) {
			this.includeTests = value;
		}

		public void classpathPrefix(java.lang.String value) {
			this.classpathPrefix = value;
		}

		public void finalName(java.lang.String value) {
			this.finalName = value;
		}

		public java.util.List<io.intino.legio.Artifact.Package.MavenPlugin> mavenPluginList() {
			return java.util.Collections.unmodifiableList(mavenPluginList);
		}

		public io.intino.legio.Artifact.Package.MavenPlugin mavenPlugin(int index) {
			return mavenPluginList.get(index);
		}

		public java.util.List<io.intino.legio.Artifact.Package.MavenPlugin> mavenPluginList(java.util.function.Predicate<io.intino.legio.Artifact.Package.MavenPlugin> predicate) {
			return mavenPluginList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		

		public io.intino.legio.runnable.artifact.RunnablePackage asRunnable() {
			return this.as(io.intino.legio.runnable.artifact.RunnablePackage.class);
		}

		public io.intino.legio.runnable.artifact.RunnablePackage asRunnable(java.lang.String mainClass) {
			io.intino.legio.runnable.artifact.RunnablePackage newElement = addFacet(io.intino.legio.runnable.artifact.RunnablePackage.class);
			newElement.node().set(newElement, "mainClass", java.util.Collections.singletonList(mainClass)); 
		    return newElement;
		}

		public boolean isRunnable() {
			return is(io.intino.legio.runnable.artifact.RunnablePackage.class);
		}

		public void removeRunnable() {
			this.removeFacet(io.intino.legio.runnable.artifact.RunnablePackage.class);
		}

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			mavenPluginList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("mode", new java.util.ArrayList(java.util.Collections.singletonList(this.mode)));
			map.put("attachSources", new java.util.ArrayList(java.util.Collections.singletonList(this.attachSources)));
			map.put("attachDoc", new java.util.ArrayList(java.util.Collections.singletonList(this.attachDoc)));
			map.put("includeTests", new java.util.ArrayList(java.util.Collections.singletonList(this.includeTests)));
			map.put("classpathPrefix", new java.util.ArrayList(java.util.Collections.singletonList(this.classpathPrefix)));
			map.put("finalName", new java.util.ArrayList(java.util.Collections.singletonList(this.finalName)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.Package.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Artifact$Package$MavenPlugin")) this.mavenPluginList.add(node.as(io.intino.legio.Artifact.Package.MavenPlugin.class));
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Artifact$Package$MavenPlugin")) this.mavenPluginList.remove(node.as(io.intino.legio.Artifact.Package.MavenPlugin.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("mode")) this.mode = io.intino.tara.magritte.loaders.WordLoader.load(values, Mode.class, this).get(0);
			else if (name.equalsIgnoreCase("attachSources")) this.attachSources = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("attachDoc")) this.attachDoc = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("includeTests")) this.includeTests = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("classpathPrefix")) this.classpathPrefix = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("finalName")) this.finalName = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
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

		public class Create {
			protected final java.lang.String name;

			public Create(java.lang.String name) {
				this.name = name;
			}

			public io.intino.legio.Artifact.Package.MavenPlugin mavenPlugin(java.lang.String code) {
			    io.intino.legio.Artifact.Package.MavenPlugin newElement = graph().concept(io.intino.legio.Artifact.Package.MavenPlugin.class).createNode(name, node()).as(io.intino.legio.Artifact.Package.MavenPlugin.class);
				newElement.node().set(newElement, "code", java.util.Collections.singletonList(code)); 
			    return newElement;
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

			public void code(java.lang.String value) {
				this.code = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("code", new java.util.ArrayList(java.util.Collections.singletonList(this.code)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Package.MavenPlugin.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("code")) this.code = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("code")) this.code = (java.lang.String) values.get(0);
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
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	public static class Distribution extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected io.intino.legio.Repository.Release release;
		protected io.intino.legio.Repository.Language language;
		protected io.intino.legio.Artifact.Distribution.Bitbucket bitbucket;

		public Distribution(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public io.intino.legio.Repository.Release release() {
			return release;
		}

		public io.intino.legio.Repository.Language language() {
			return language;
		}

		public void release(io.intino.legio.Repository.Release value) {
			this.release = value;
		}

		public void language(io.intino.legio.Repository.Language value) {
			this.language = value;
		}

		public io.intino.legio.Artifact.Distribution.Bitbucket bitbucket() {
			return bitbucket;
		}

		public void bitbucket(io.intino.legio.Artifact.Distribution.Bitbucket value) {
			this.bitbucket = value;
		}

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			if (bitbucket != null) components.add(this.bitbucket.node());
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("release", this.release != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.release)) : java.util.Collections.emptyList());
			map.put("language", this.language != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.language)) : java.util.Collections.emptyList());
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.Distribution.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Artifact$Distribution$Bitbucket")) this.bitbucket = node.as(io.intino.legio.Artifact.Distribution.Bitbucket.class);
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Artifact$Distribution$Bitbucket")) this.bitbucket = null;
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("release")) this.release = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.legio.Repository.Release.class, this).get(0);
			else if (name.equalsIgnoreCase("language")) this.language = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.legio.Repository.Language.class, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("release")) this.release = values.get(0)!= null ? graph().loadNode(((io.intino.tara.magritte.Layer) values.get(0)).id()).as(io.intino.legio.Repository.Release.class) : null;
			else if (name.equalsIgnoreCase("language")) this.language = values.get(0)!= null ? graph().loadNode(((io.intino.tara.magritte.Layer) values.get(0)).id()).as(io.intino.legio.Repository.Language.class) : null;
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

			public io.intino.legio.Artifact.Distribution.Bitbucket bitbucket(java.lang.String user, java.lang.String token) {
			    io.intino.legio.Artifact.Distribution.Bitbucket newElement = graph().concept(io.intino.legio.Artifact.Distribution.Bitbucket.class).createNode(name, node()).as(io.intino.legio.Artifact.Distribution.Bitbucket.class);
				newElement.node().set(newElement, "user", java.util.Collections.singletonList(user));
				newElement.node().set(newElement, "token", java.util.Collections.singletonList(token)); 
			    return newElement;
			}
			
		}
		
		public static class Bitbucket extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String user;
			protected java.lang.String token;

			public Bitbucket(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String user() {
				return user;
			}

			public java.lang.String token() {
				return token;
			}

			public void user(java.lang.String value) {
				this.user = value;
			}

			public void token(java.lang.String value) {
				this.token = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("user", new java.util.ArrayList(java.util.Collections.singletonList(this.user)));
				map.put("token", new java.util.ArrayList(java.util.Collections.singletonList(this.token)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Distribution.Bitbucket.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("user")) this.user = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("token")) this.token = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("user")) this.user = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("token")) this.token = (java.lang.String) values.get(0);
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
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	public static class QualityAnalytics extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String url;
		protected io.intino.legio.Artifact.QualityAnalytics.Authentication authentication;

		public QualityAnalytics(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String url() {
			return url;
		}

		public void url(java.lang.String value) {
			this.url = value;
		}

		public io.intino.legio.Artifact.QualityAnalytics.Authentication authentication() {
			return authentication;
		}

		public void authentication(io.intino.legio.Artifact.QualityAnalytics.Authentication value) {
			this.authentication = value;
		}

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			if (authentication != null) components.add(this.authentication.node());
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.QualityAnalytics.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Artifact$QualityAnalytics$Authentication")) this.authentication = node.as(io.intino.legio.Artifact.QualityAnalytics.Authentication.class);
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Artifact$QualityAnalytics$Authentication")) this.authentication = null;
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("url")) this.url = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
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

			public io.intino.legio.Artifact.QualityAnalytics.Authentication authentication(java.lang.String token) {
			    io.intino.legio.Artifact.QualityAnalytics.Authentication newElement = graph().concept(io.intino.legio.Artifact.QualityAnalytics.Authentication.class).createNode(name, node()).as(io.intino.legio.Artifact.QualityAnalytics.Authentication.class);
				newElement.node().set(newElement, "token", java.util.Collections.singletonList(token)); 
			    return newElement;
			}
			
		}
		
		public static class Authentication extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String token;

			public Authentication(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String token() {
				return token;
			}

			public void token(java.lang.String value) {
				this.token = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("token", new java.util.ArrayList(java.util.Collections.singletonList(this.token)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.QualityAnalytics.Authentication.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("token")) this.token = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("token")) this.token = (java.lang.String) values.get(0);
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
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	public static class Deployment extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected io.intino.legio.Server server;
		protected java.lang.String url;
		protected io.intino.legio.Artifact.Deployment.BugTracking bugTracking;
		protected io.intino.legio.Artifact.Deployment.Requirements requirements;
		protected io.intino.legio.Artifact.Deployment.Configuration configuration;

		public Deployment(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public io.intino.legio.Server server() {
			return server;
		}

		public java.lang.String url() {
			return url;
		}

		public void server(io.intino.legio.Server value) {
			this.server = value;
		}

		public void url(java.lang.String value) {
			this.url = value;
		}

		public io.intino.legio.Artifact.Deployment.BugTracking bugTracking() {
			return bugTracking;
		}

		public io.intino.legio.Artifact.Deployment.Requirements requirements() {
			return requirements;
		}

		public io.intino.legio.Artifact.Deployment.Configuration configuration() {
			return configuration;
		}

		public void bugTracking(io.intino.legio.Artifact.Deployment.BugTracking value) {
			this.bugTracking = value;
		}

		public void requirements(io.intino.legio.Artifact.Deployment.Requirements value) {
			this.requirements = value;
		}

		public void configuration(io.intino.legio.Artifact.Deployment.Configuration value) {
			this.configuration = value;
		}

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			if (bugTracking != null) components.add(this.bugTracking.node());
			if (requirements != null) components.add(this.requirements.node());
			if (configuration != null) components.add(this.configuration.node());
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("server", this.server != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.server)) : java.util.Collections.emptyList());
			map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Artifact.Deployment.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Artifact$Deployment$BugTracking")) this.bugTracking = node.as(io.intino.legio.Artifact.Deployment.BugTracking.class);
			if (node.is("Artifact$Deployment$Requirements")) this.requirements = node.as(io.intino.legio.Artifact.Deployment.Requirements.class);
			if (node.is("Artifact$Deployment$Configuration")) this.configuration = node.as(io.intino.legio.Artifact.Deployment.Configuration.class);
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Artifact$Deployment$BugTracking")) this.bugTracking = null;
	        if (node.is("Artifact$Deployment$Requirements")) this.requirements = null;
	        if (node.is("Artifact$Deployment$Configuration")) this.configuration = null;
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("server")) this.server = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.legio.Server.class, this).get(0);
			else if (name.equalsIgnoreCase("url")) this.url = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("server")) this.server = values.get(0)!= null ? graph().loadNode(((io.intino.tara.magritte.Layer) values.get(0)).id()).as(io.intino.legio.Server.class) : null;
			else if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
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

			public io.intino.legio.Artifact.Deployment.BugTracking bugTracking() {
			    io.intino.legio.Artifact.Deployment.BugTracking newElement = graph().concept(io.intino.legio.Artifact.Deployment.BugTracking.class).createNode(name, node()).as(io.intino.legio.Artifact.Deployment.BugTracking.class);
			    return newElement;
			}

			public io.intino.legio.Artifact.Deployment.Requirements requirements() {
			    io.intino.legio.Artifact.Deployment.Requirements newElement = graph().concept(io.intino.legio.Artifact.Deployment.Requirements.class).createNode(name, node()).as(io.intino.legio.Artifact.Deployment.Requirements.class);
			    return newElement;
			}

			public io.intino.legio.Artifact.Deployment.Configuration configuration() {
			    io.intino.legio.Artifact.Deployment.Configuration newElement = graph().concept(io.intino.legio.Artifact.Deployment.Configuration.class).createNode(name, node()).as(io.intino.legio.Artifact.Deployment.Configuration.class);
			    return newElement;
			}
			
		}
		
		public static class BugTracking extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.util.List<java.lang.String> slackUsers = new java.util.ArrayList<>();

			public BugTracking(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.util.List<java.lang.String> slackUsers() {
				return slackUsers;
			}

			public java.lang.String slackUsers(int index) {
				return slackUsers.get(index);
			}

			public java.util.List<java.lang.String> slackUsers(java.util.function.Predicate<java.lang.String> predicate) {
				return slackUsers().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("slackUsers", this.slackUsers);
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Deployment.BugTracking.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("slackUsers")) this.slackUsers = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("slackUsers")) this.slackUsers = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
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
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		public static class Requirements extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			
			protected java.util.List<io.intino.legio.Artifact.Deployment.Requirements.Memory> memoryList = new java.util.ArrayList<>();
			protected java.util.List<io.intino.legio.Artifact.Deployment.Requirements.CPU> cPUList = new java.util.ArrayList<>();

			public Requirements(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.util.List<io.intino.legio.Artifact.Deployment.Requirements.Memory> memoryList() {
				return java.util.Collections.unmodifiableList(memoryList);
			}

			public io.intino.legio.Artifact.Deployment.Requirements.Memory memory(int index) {
				return memoryList.get(index);
			}

			public java.util.List<io.intino.legio.Artifact.Deployment.Requirements.Memory> memoryList(java.util.function.Predicate<io.intino.legio.Artifact.Deployment.Requirements.Memory> predicate) {
				return memoryList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}

			public java.util.List<io.intino.legio.Artifact.Deployment.Requirements.CPU> cPUList() {
				return java.util.Collections.unmodifiableList(cPUList);
			}

			public io.intino.legio.Artifact.Deployment.Requirements.CPU cPU(int index) {
				return cPUList.get(index);
			}

			public java.util.List<io.intino.legio.Artifact.Deployment.Requirements.CPU> cPUList(java.util.function.Predicate<io.intino.legio.Artifact.Deployment.Requirements.CPU> predicate) {
				return cPUList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}

			

			

			public java.util.List<io.intino.tara.magritte.Node> componentList() {
				java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
				memoryList.stream().forEach(c -> components.add(c.node()));
				cPUList.stream().forEach(c -> components.add(c.node()));
				return new java.util.ArrayList<>(components);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Deployment.Requirements.class);
			}

			@Override
			protected void addNode(io.intino.tara.magritte.Node node) {
				super.addNode(node);
				if (node.is("Artifact$Deployment$Requirements$Memory")) this.memoryList.add(node.as(io.intino.legio.Artifact.Deployment.Requirements.Memory.class));
				if (node.is("Artifact$Deployment$Requirements$CPU")) this.cPUList.add(node.as(io.intino.legio.Artifact.Deployment.Requirements.CPU.class));
			}

			@Override
		    protected void removeNode(io.intino.tara.magritte.Node node) {
		        super.removeNode(node);
		        if (node.is("Artifact$Deployment$Requirements$Memory")) this.memoryList.remove(node.as(io.intino.legio.Artifact.Deployment.Requirements.Memory.class));
		        if (node.is("Artifact$Deployment$Requirements$CPU")) this.cPUList.remove(node.as(io.intino.legio.Artifact.Deployment.Requirements.CPU.class));
		    }

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
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

				public io.intino.legio.Artifact.Deployment.Requirements.Memory memory() {
				    io.intino.legio.Artifact.Deployment.Requirements.Memory newElement = graph().concept(io.intino.legio.Artifact.Deployment.Requirements.Memory.class).createNode(name, node()).as(io.intino.legio.Artifact.Deployment.Requirements.Memory.class);
				    return newElement;
				}

				public io.intino.legio.Artifact.Deployment.Requirements.CPU cPU() {
				    io.intino.legio.Artifact.Deployment.Requirements.CPU newElement = graph().concept(io.intino.legio.Artifact.Deployment.Requirements.CPU.class).createNode(name, node()).as(io.intino.legio.Artifact.Deployment.Requirements.CPU.class);
				    return newElement;
				}
				
			}
			
			public static class Memory extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
				

				public Memory(io.intino.tara.magritte.Node node) {
					super(node);
				}

				@Override
				public java.util.Map<java.lang.String, java.util.List<?>> variables() {
					java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
					return map;
				}

				public io.intino.tara.magritte.Concept concept() {
					return this.graph().concept(io.intino.legio.Artifact.Deployment.Requirements.Memory.class);
				}

				@Override
				protected void _load(java.lang.String name, java.util.List<?> values) {
					super._load(name, values);
				}

				@Override
				protected void _set(java.lang.String name, java.util.List<?> values) {
					super._set(name, values);
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
					
				}
				
				public io.intino.legio.Legio legioWrapper() {
					return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
				}
			}
			
			public static class CPU extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
				

				public CPU(io.intino.tara.magritte.Node node) {
					super(node);
				}

				@Override
				public java.util.Map<java.lang.String, java.util.List<?>> variables() {
					java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
					return map;
				}

				public io.intino.tara.magritte.Concept concept() {
					return this.graph().concept(io.intino.legio.Artifact.Deployment.Requirements.CPU.class);
				}

				@Override
				protected void _load(java.lang.String name, java.util.List<?> values) {
					super._load(name, values);
				}

				@Override
				protected void _set(java.lang.String name, java.util.List<?> values) {
					super._set(name, values);
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
					
				}
				
				public io.intino.legio.Legio legioWrapper() {
					return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
				}
			}
			
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		public static class Configuration extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			
			protected java.util.List<io.intino.legio.Argument> argumentList = new java.util.ArrayList<>();
			protected java.util.List<io.intino.legio.Artifact.Deployment.Configuration.Service> serviceList = new java.util.ArrayList<>();
			protected io.intino.legio.Artifact.Deployment.Configuration.Store store;

			public Configuration(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.util.List<io.intino.legio.Argument> argumentList() {
				return java.util.Collections.unmodifiableList(argumentList);
			}

			public io.intino.legio.Argument argument(int index) {
				return argumentList.get(index);
			}

			public java.util.List<io.intino.legio.Argument> argumentList(java.util.function.Predicate<io.intino.legio.Argument> predicate) {
				return argumentList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}

			public java.util.List<io.intino.legio.Artifact.Deployment.Configuration.Service> serviceList() {
				return java.util.Collections.unmodifiableList(serviceList);
			}

			public io.intino.legio.Artifact.Deployment.Configuration.Service service(int index) {
				return serviceList.get(index);
			}

			public java.util.List<io.intino.legio.Artifact.Deployment.Configuration.Service> serviceList(java.util.function.Predicate<io.intino.legio.Artifact.Deployment.Configuration.Service> predicate) {
				return serviceList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}

			public io.intino.legio.Artifact.Deployment.Configuration.Store store() {
				return store;
			}

			

			

			public void store(io.intino.legio.Artifact.Deployment.Configuration.Store value) {
				this.store = value;
			}

			public java.util.List<io.intino.tara.magritte.Node> componentList() {
				java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
				argumentList.stream().forEach(c -> components.add(c.node()));
				serviceList.stream().forEach(c -> components.add(c.node()));
				if (store != null) components.add(this.store.node());
				return new java.util.ArrayList<>(components);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Artifact.Deployment.Configuration.class);
			}

			@Override
			protected void addNode(io.intino.tara.magritte.Node node) {
				super.addNode(node);
				if (node.is("Argument")) this.argumentList.add(node.as(io.intino.legio.Argument.class));
				if (node.is("Artifact$Deployment$Configuration$Service")) this.serviceList.add(node.as(io.intino.legio.Artifact.Deployment.Configuration.Service.class));
				if (node.is("Artifact$Deployment$Configuration$Store")) this.store = node.as(io.intino.legio.Artifact.Deployment.Configuration.Store.class);
			}

			@Override
		    protected void removeNode(io.intino.tara.magritte.Node node) {
		        super.removeNode(node);
		        if (node.is("Argument")) this.argumentList.remove(node.as(io.intino.legio.Argument.class));
		        if (node.is("Artifact$Deployment$Configuration$Service")) this.serviceList.remove(node.as(io.intino.legio.Artifact.Deployment.Configuration.Service.class));
		        if (node.is("Artifact$Deployment$Configuration$Store")) this.store = null;
		    }

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
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

				public io.intino.legio.Argument argument() {
				    io.intino.legio.Argument newElement = graph().concept(io.intino.legio.Argument.class).createNode(name, node()).as(io.intino.legio.Argument.class);
				    return newElement;
				}

				public io.intino.legio.Artifact.Deployment.Configuration.Service service() {
				    io.intino.legio.Artifact.Deployment.Configuration.Service newElement = graph().concept(io.intino.legio.Artifact.Deployment.Configuration.Service.class).createNode(name, node()).as(io.intino.legio.Artifact.Deployment.Configuration.Service.class);
				    return newElement;
				}

				public io.intino.legio.Artifact.Deployment.Configuration.Store store(java.lang.String path) {
				    io.intino.legio.Artifact.Deployment.Configuration.Store newElement = graph().concept(io.intino.legio.Artifact.Deployment.Configuration.Store.class).createNode(name, node()).as(io.intino.legio.Artifact.Deployment.Configuration.Store.class);
					newElement.node().set(newElement, "path", java.util.Collections.singletonList(path)); 
				    return newElement;
				}
				
			}
			
			public static class Service extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
				
				protected java.util.List<io.intino.legio.Argument> argumentList = new java.util.ArrayList<>();

				public Service(io.intino.tara.magritte.Node node) {
					super(node);
				}

				public java.util.List<io.intino.legio.Argument> argumentList() {
					return java.util.Collections.unmodifiableList(argumentList);
				}

				public io.intino.legio.Argument argument(int index) {
					return argumentList.get(index);
				}

				public java.util.List<io.intino.legio.Argument> argumentList(java.util.function.Predicate<io.intino.legio.Argument> predicate) {
					return argumentList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
				}

				

				public java.util.List<io.intino.tara.magritte.Node> componentList() {
					java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
					argumentList.stream().forEach(c -> components.add(c.node()));
					return new java.util.ArrayList<>(components);
				}

				@Override
				public java.util.Map<java.lang.String, java.util.List<?>> variables() {
					java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
					return map;
				}

				public io.intino.tara.magritte.Concept concept() {
					return this.graph().concept(io.intino.legio.Artifact.Deployment.Configuration.Service.class);
				}

				@Override
				protected void addNode(io.intino.tara.magritte.Node node) {
					super.addNode(node);
					if (node.is("Argument")) this.argumentList.add(node.as(io.intino.legio.Argument.class));
				}

				@Override
			    protected void removeNode(io.intino.tara.magritte.Node node) {
			        super.removeNode(node);
			        if (node.is("Argument")) this.argumentList.remove(node.as(io.intino.legio.Argument.class));
			    }

				@Override
				protected void _load(java.lang.String name, java.util.List<?> values) {
					super._load(name, values);
				}

				@Override
				protected void _set(java.lang.String name, java.util.List<?> values) {
					super._set(name, values);
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

					public io.intino.legio.Argument argument() {
					    io.intino.legio.Argument newElement = graph().concept(io.intino.legio.Argument.class).createNode(name, node()).as(io.intino.legio.Argument.class);
					    return newElement;
					}
					
				}
				
				public io.intino.legio.Legio legioWrapper() {
					return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
				}
			}
			
			public static class Store extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
				protected java.lang.String path;

				public Store(io.intino.tara.magritte.Node node) {
					super(node);
				}

				public java.lang.String path() {
					return path;
				}

				public void path(java.lang.String value) {
					this.path = value;
				}

				@Override
				public java.util.Map<java.lang.String, java.util.List<?>> variables() {
					java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
					map.put("path", new java.util.ArrayList(java.util.Collections.singletonList(this.path)));
					return map;
				}

				public io.intino.tara.magritte.Concept concept() {
					return this.graph().concept(io.intino.legio.Artifact.Deployment.Configuration.Store.class);
				}

				@Override
				protected void _load(java.lang.String name, java.util.List<?> values) {
					super._load(name, values);
					if (name.equalsIgnoreCase("path")) this.path = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				}

				@Override
				protected void _set(java.lang.String name, java.util.List<?> values) {
					super._set(name, values);
					if (name.equalsIgnoreCase("path")) this.path = (java.lang.String) values.get(0);
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
					
				}
				
				public io.intino.legio.Legio legioWrapper() {
					return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
				}
			}
			
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
