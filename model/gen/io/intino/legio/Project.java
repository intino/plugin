package io.intino.legio;

import io.intino.legio.*;


public class Project extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String groupId;
	protected java.lang.String version;
	protected io.intino.legio.Project.License license;
	protected io.intino.legio.Project.Repositories repositories;
	protected io.intino.legio.Project.Dependencies dependencies;
	protected io.intino.legio.Project.WebDependencies webDependencies;
	protected io.intino.legio.Project.Factory factory;

	public Project(io.intino.tara.magritte.Node node) {
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

	public io.intino.legio.Project.License license() {
		return license;
	}

	public io.intino.legio.Project.Repositories repositories() {
		return repositories;
	}

	public io.intino.legio.Project.Dependencies dependencies() {
		return dependencies;
	}

	public io.intino.legio.Project.WebDependencies webDependencies() {
		return webDependencies;
	}

	public io.intino.legio.Project.Factory factory() {
		return factory;
	}

	public void license(io.intino.legio.Project.License value) {
		this.license = value;
	}

	public void repositories(io.intino.legio.Project.Repositories value) {
		this.repositories = value;
	}

	public void dependencies(io.intino.legio.Project.Dependencies value) {
		this.dependencies = value;
	}

	public void webDependencies(io.intino.legio.Project.WebDependencies value) {
		this.webDependencies = value;
	}

	public void factory(io.intino.legio.Project.Factory value) {
		this.factory = value;
	}

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		if (license != null) components.add(this.license.node());
		if (repositories != null) components.add(this.repositories.node());
		if (dependencies != null) components.add(this.dependencies.node());
		if (webDependencies != null) components.add(this.webDependencies.node());
		if (factory != null) components.add(this.factory.node());
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
		return this.graph().concept(io.intino.legio.Project.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		super.addNode(node);
		if (node.is("Project$License")) this.license = node.as(io.intino.legio.Project.License.class);
		if (node.is("Project$Repositories")) this.repositories = node.as(io.intino.legio.Project.Repositories.class);
		if (node.is("Project$Dependencies")) this.dependencies = node.as(io.intino.legio.Project.Dependencies.class);
		if (node.is("Project$WebDependencies")) this.webDependencies = node.as(io.intino.legio.Project.WebDependencies.class);
		if (node.is("Project$Factory")) this.factory = node.as(io.intino.legio.Project.Factory.class);
	}

	@Override
    protected void removeNode(io.intino.tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("Project$License")) this.license = null;
        if (node.is("Project$Repositories")) this.repositories = null;
        if (node.is("Project$Dependencies")) this.dependencies = null;
        if (node.is("Project$WebDependencies")) this.webDependencies = null;
        if (node.is("Project$Factory")) this.factory = null;
    }

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("groupId")) this.groupId = tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("version")) this.version = tara.magritte.loaders.StringLoader.load(values, this).get(0);
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

		public io.intino.legio.Project.License license(io.intino.legio.Project.License.Type type) {
		    io.intino.legio.Project.License newElement = graph().concept(io.intino.legio.Project.License.class).createNode(name, node()).as(io.intino.legio.Project.License.class);
			newElement.node().set(newElement, "type", java.util.Collections.singletonList(type)); 
		    return newElement;
		}

		public io.intino.legio.Project.Repositories repositories() {
		    io.intino.legio.Project.Repositories newElement = graph().concept(io.intino.legio.Project.Repositories.class).createNode(name, node()).as(io.intino.legio.Project.Repositories.class);
		    return newElement;
		}

		public io.intino.legio.Project.Dependencies dependencies() {
		    io.intino.legio.Project.Dependencies newElement = graph().concept(io.intino.legio.Project.Dependencies.class).createNode(name, node()).as(io.intino.legio.Project.Dependencies.class);
		    return newElement;
		}

		public io.intino.legio.Project.WebDependencies webDependencies() {
		    io.intino.legio.Project.WebDependencies newElement = graph().concept(io.intino.legio.Project.WebDependencies.class).createNode(name, node()).as(io.intino.legio.Project.WebDependencies.class);
		    return newElement;
		}

		public io.intino.legio.Project.Factory factory() {
		    io.intino.legio.Project.Factory newElement = graph().concept(io.intino.legio.Project.Factory.class).createNode(name, node()).as(io.intino.legio.Project.Factory.class);
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

		public void type(io.intino.legio.Project.License.Type value) {
			this.type = value;
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("type", new java.util.ArrayList(java.util.Collections.singletonList(this.type)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Project.License.class);
		}

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("type")) this.type = WordLoader.load(values, Type.class, this).get(0);
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
		
		public io.intino.legio.LegioApplication application() {
			return ((io.intino.legio.LegioApplication) graph().application());
		}
	}
	
	public static class Repositories extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		
		protected java.util.List<io.intino.legio.Project.Repositories.Repository> repositoryList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Repositories.Release> releaseList = new java.util.ArrayList<>();
		protected io.intino.legio.Project.Repositories.Snapshot snapshot;
		protected io.intino.legio.Project.Repositories.Language language;

		public Repositories(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<io.intino.legio.Project.Repositories.Repository> repositoryList() {
			return java.util.Collections.unmodifiableList(repositoryList);
		}

		public io.intino.legio.Project.Repositories.Repository repository(int index) {
			return repositoryList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Repositories.Repository> repositoryList(java.util.function.Predicate<io.intino.legio.Project.Repositories.Repository> predicate) {
			return repositoryList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.Repositories.Release> releaseList() {
			return java.util.Collections.unmodifiableList(releaseList);
		}

		public io.intino.legio.Project.Repositories.Release release(int index) {
			return releaseList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Repositories.Release> releaseList(java.util.function.Predicate<io.intino.legio.Project.Repositories.Release> predicate) {
			return releaseList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public io.intino.legio.Project.Repositories.Snapshot snapshot() {
			return snapshot;
		}

		public io.intino.legio.Project.Repositories.Language language() {
			return language;
		}

		

		

		public void snapshot(io.intino.legio.Project.Repositories.Snapshot value) {
			this.snapshot = value;
		}

		public void language(io.intino.legio.Project.Repositories.Language value) {
			this.language = value;
		}

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			repositoryList.stream().forEach(c -> components.add(c.node()));
			releaseList.stream().forEach(c -> components.add(c.node()));
			if (snapshot != null) components.add(this.snapshot.node());
			if (language != null) components.add(this.language.node());
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Project.Repositories.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Repositories$Repository")) this.repositoryList.add(node.as(io.intino.legio.Project.Repositories.Repository.class));
			if (node.is("Project$Repositories$Release")) this.releaseList.add(node.as(io.intino.legio.Project.Repositories.Release.class));
			if (node.is("Project$Repositories$Snapshot")) this.snapshot = node.as(io.intino.legio.Project.Repositories.Snapshot.class);
			if (node.is("Project$Repositories$Language")) this.language = node.as(io.intino.legio.Project.Repositories.Language.class);
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Repositories$Repository")) this.repositoryList.remove(node.as(io.intino.legio.Project.Repositories.Repository.class));
	        if (node.is("Project$Repositories$Release")) this.releaseList.remove(node.as(io.intino.legio.Project.Repositories.Release.class));
	        if (node.is("Project$Repositories$Snapshot")) this.snapshot = null;
	        if (node.is("Project$Repositories$Language")) this.language = null;
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

			

			public io.intino.legio.Project.Repositories.Release release(java.lang.String url, java.lang.String mavenId) {
			    io.intino.legio.Project.Repositories.Release newElement = graph().concept(io.intino.legio.Project.Repositories.Release.class).createNode(name, node()).as(io.intino.legio.Project.Repositories.Release.class);
				newElement.node().set(newElement, "url", java.util.Collections.singletonList(url));
				newElement.node().set(newElement, "mavenId", java.util.Collections.singletonList(mavenId)); 
			    return newElement;
			}

			public io.intino.legio.Project.Repositories.Snapshot snapshot(java.lang.String url, java.lang.String mavenId) {
			    io.intino.legio.Project.Repositories.Snapshot newElement = graph().concept(io.intino.legio.Project.Repositories.Snapshot.class).createNode(name, node()).as(io.intino.legio.Project.Repositories.Snapshot.class);
				newElement.node().set(newElement, "url", java.util.Collections.singletonList(url));
				newElement.node().set(newElement, "mavenId", java.util.Collections.singletonList(mavenId)); 
			    return newElement;
			}

			public io.intino.legio.Project.Repositories.Language language(java.lang.String url, java.lang.String mavenId) {
			    io.intino.legio.Project.Repositories.Language newElement = graph().concept(io.intino.legio.Project.Repositories.Language.class).createNode(name, node()).as(io.intino.legio.Project.Repositories.Language.class);
				newElement.node().set(newElement, "url", java.util.Collections.singletonList(url));
				newElement.node().set(newElement, "mavenId", java.util.Collections.singletonList(mavenId)); 
			    return newElement;
			}
			
		}
		
		public static abstract class Repository extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String url;
			protected java.lang.String mavenId;

			public Repository(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String url() {
				return url;
			}

			public java.lang.String mavenId() {
				return mavenId;
			}

			public void url(java.lang.String value) {
				this.url = value;
			}

			public void mavenId(java.lang.String value) {
				this.mavenId = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
				map.put("mavenId", new java.util.ArrayList(java.util.Collections.singletonList(this.mavenId)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Repositories.Repository.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("url")) this.url = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("mavenId")) this.mavenId = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("mavenId")) this.mavenId = (java.lang.String) values.get(0);
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
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Release extends io.intino.legio.Project.Repositories.Repository implements io.intino.tara.magritte.tags.Terminal {
			

			public Release(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Repositories.Release.class);
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

			public class Create extends io.intino.legio.Project.Repositories.Repository.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Snapshot extends io.intino.legio.Project.Repositories.Repository implements io.intino.tara.magritte.tags.Terminal {
			

			public Snapshot(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Repositories.Snapshot.class);
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

			public class Create extends io.intino.legio.Project.Repositories.Repository.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Language extends io.intino.legio.Project.Repositories.Repository implements io.intino.tara.magritte.tags.Terminal {
			

			public Language(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Repositories.Language.class);
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

			public class Create extends io.intino.legio.Project.Repositories.Repository.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		
		public io.intino.legio.LegioApplication application() {
			return ((io.intino.legio.LegioApplication) graph().application());
		}
	}
	
	public static class Dependencies extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		
		protected java.util.List<io.intino.legio.Project.Dependencies.Dependency> dependencyList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Dependencies.Compile> compileList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Dependencies.Runtime> runtimeList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Dependencies.Provided> providedList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Dependencies.Test> testList = new java.util.ArrayList<>();

		public Dependencies(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Dependency> dependencyList() {
			return java.util.Collections.unmodifiableList(dependencyList);
		}

		public io.intino.legio.Project.Dependencies.Dependency dependency(int index) {
			return dependencyList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Dependency> dependencyList(java.util.function.Predicate<io.intino.legio.Project.Dependencies.Dependency> predicate) {
			return dependencyList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Compile> compileList() {
			return java.util.Collections.unmodifiableList(compileList);
		}

		public io.intino.legio.Project.Dependencies.Compile compile(int index) {
			return compileList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Compile> compileList(java.util.function.Predicate<io.intino.legio.Project.Dependencies.Compile> predicate) {
			return compileList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Runtime> runtimeList() {
			return java.util.Collections.unmodifiableList(runtimeList);
		}

		public io.intino.legio.Project.Dependencies.Runtime runtime(int index) {
			return runtimeList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Runtime> runtimeList(java.util.function.Predicate<io.intino.legio.Project.Dependencies.Runtime> predicate) {
			return runtimeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Provided> providedList() {
			return java.util.Collections.unmodifiableList(providedList);
		}

		public io.intino.legio.Project.Dependencies.Provided provided(int index) {
			return providedList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Provided> providedList(java.util.function.Predicate<io.intino.legio.Project.Dependencies.Provided> predicate) {
			return providedList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Test> testList() {
			return java.util.Collections.unmodifiableList(testList);
		}

		public io.intino.legio.Project.Dependencies.Test test(int index) {
			return testList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Test> testList(java.util.function.Predicate<io.intino.legio.Project.Dependencies.Test> predicate) {
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
			return this.graph().concept(io.intino.legio.Project.Dependencies.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Dependencies$Dependency")) this.dependencyList.add(node.as(io.intino.legio.Project.Dependencies.Dependency.class));
			if (node.is("Project$Dependencies$Compile")) this.compileList.add(node.as(io.intino.legio.Project.Dependencies.Compile.class));
			if (node.is("Project$Dependencies$Runtime")) this.runtimeList.add(node.as(io.intino.legio.Project.Dependencies.Runtime.class));
			if (node.is("Project$Dependencies$Provided")) this.providedList.add(node.as(io.intino.legio.Project.Dependencies.Provided.class));
			if (node.is("Project$Dependencies$Test")) this.testList.add(node.as(io.intino.legio.Project.Dependencies.Test.class));
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Dependencies$Dependency")) this.dependencyList.remove(node.as(io.intino.legio.Project.Dependencies.Dependency.class));
	        if (node.is("Project$Dependencies$Compile")) this.compileList.remove(node.as(io.intino.legio.Project.Dependencies.Compile.class));
	        if (node.is("Project$Dependencies$Runtime")) this.runtimeList.remove(node.as(io.intino.legio.Project.Dependencies.Runtime.class));
	        if (node.is("Project$Dependencies$Provided")) this.providedList.remove(node.as(io.intino.legio.Project.Dependencies.Provided.class));
	        if (node.is("Project$Dependencies$Test")) this.testList.remove(node.as(io.intino.legio.Project.Dependencies.Test.class));
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

			

			public io.intino.legio.Project.Dependencies.Compile compile(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.Project.Dependencies.Compile newElement = graph().concept(io.intino.legio.Project.Dependencies.Compile.class).createNode(name, node()).as(io.intino.legio.Project.Dependencies.Compile.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.node().set(newElement, "artifacts", artifacts); 
			    return newElement;
			}

			public io.intino.legio.Project.Dependencies.Runtime runtime(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.Project.Dependencies.Runtime newElement = graph().concept(io.intino.legio.Project.Dependencies.Runtime.class).createNode(name, node()).as(io.intino.legio.Project.Dependencies.Runtime.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.node().set(newElement, "artifacts", artifacts); 
			    return newElement;
			}

			public io.intino.legio.Project.Dependencies.Provided provided(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.Project.Dependencies.Provided newElement = graph().concept(io.intino.legio.Project.Dependencies.Provided.class).createNode(name, node()).as(io.intino.legio.Project.Dependencies.Provided.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.node().set(newElement, "artifacts", artifacts); 
			    return newElement;
			}

			public io.intino.legio.Project.Dependencies.Test test(java.lang.String groupId, java.lang.String artifactId, java.lang.String version, java.util.List<java.lang.String> artifacts) {
			    io.intino.legio.Project.Dependencies.Test newElement = graph().concept(io.intino.legio.Project.Dependencies.Test.class).createNode(name, node()).as(io.intino.legio.Project.Dependencies.Test.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
				newElement.node().set(newElement, "artifacts", artifacts); 
			    return newElement;
			}
			
		}
		
		public static abstract class Dependency extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected Expression<java.lang.String> identifier;
			protected Expression<java.lang.String> name;
			protected java.lang.String groupId;
			protected java.lang.String artifactId;
			protected java.lang.String version;
			protected java.lang.String effectiveVersion;
			protected boolean transitive;
			protected java.util.List<java.lang.String> artifacts = new java.util.ArrayList<>();
			protected boolean resolved;
			protected boolean toModule;

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

			public boolean resolved() {
				return resolved;
			}

			public boolean toModule() {
				return toModule;
			}

			public void identifier(Expression<java.lang.String> value) {
				this.identifier = FunctionLoader.load(value, this, Expression.class);
			}

			public void name(Expression<java.lang.String> value) {
				this.name = FunctionLoader.load(value, this, Expression.class);
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

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("identifier", new java.util.ArrayList(java.util.Collections.singletonList(this.identifier)));
				map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name)));
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
				return this.graph().concept(io.intino.legio.Project.Dependencies.Dependency.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = FunctionLoader.load(values, this, Expression.class).get(0);
				else if (name.equalsIgnoreCase("name")) this.name = FunctionLoader.load(values, this, Expression.class).get(0);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("transitive")) this.transitive = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifacts")) this.artifacts = tara.magritte.loaders.StringLoader.load(values, this);
				else if (name.equalsIgnoreCase("resolved")) this.resolved = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("toModule")) this.toModule = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = FunctionLoader.load(values.get(0), this, Expression.class);
				else if (name.equalsIgnoreCase("name")) this.name = FunctionLoader.load(values.get(0), this, Expression.class);
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
				
			}
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Compile extends io.intino.legio.Project.Dependencies.Dependency implements io.intino.tara.magritte.tags.Terminal {
			

			public Compile(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Dependencies.Compile.class);
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

			public class Create extends io.intino.legio.Project.Dependencies.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Runtime extends io.intino.legio.Project.Dependencies.Dependency implements io.intino.tara.magritte.tags.Terminal {
			

			public Runtime(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Dependencies.Runtime.class);
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

			public class Create extends io.intino.legio.Project.Dependencies.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Provided extends io.intino.legio.Project.Dependencies.Dependency implements io.intino.tara.magritte.tags.Terminal {
			

			public Provided(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Dependencies.Provided.class);
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

			public class Create extends io.intino.legio.Project.Dependencies.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Test extends io.intino.legio.Project.Dependencies.Dependency implements io.intino.tara.magritte.tags.Terminal {
			

			public Test(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Dependencies.Test.class);
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

			public class Create extends io.intino.legio.Project.Dependencies.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		
		public io.intino.legio.LegioApplication application() {
			return ((io.intino.legio.LegioApplication) graph().application());
		}
	}
	
	public static class WebDependencies extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String webDirectory;
		protected java.util.List<io.intino.legio.Project.WebDependencies.Resolution> resolutionList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.WebDependencies.WebComponent> webComponentList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.WebDependencies.WebActivity> webActivityList = new java.util.ArrayList<>();

		public WebDependencies(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String webDirectory() {
			return webDirectory;
		}

		public void webDirectory(java.lang.String value) {
			this.webDirectory = value;
		}

		public java.util.List<io.intino.legio.Project.WebDependencies.Resolution> resolutionList() {
			return java.util.Collections.unmodifiableList(resolutionList);
		}

		public io.intino.legio.Project.WebDependencies.Resolution resolution(int index) {
			return resolutionList.get(index);
		}

		public java.util.List<io.intino.legio.Project.WebDependencies.Resolution> resolutionList(java.util.function.Predicate<io.intino.legio.Project.WebDependencies.Resolution> predicate) {
			return resolutionList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.WebDependencies.WebComponent> webComponentList() {
			return java.util.Collections.unmodifiableList(webComponentList);
		}

		public io.intino.legio.Project.WebDependencies.WebComponent webComponent(int index) {
			return webComponentList.get(index);
		}

		public java.util.List<io.intino.legio.Project.WebDependencies.WebComponent> webComponentList(java.util.function.Predicate<io.intino.legio.Project.WebDependencies.WebComponent> predicate) {
			return webComponentList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.WebDependencies.WebActivity> webActivityList() {
			return java.util.Collections.unmodifiableList(webActivityList);
		}

		public io.intino.legio.Project.WebDependencies.WebActivity webActivity(int index) {
			return webActivityList.get(index);
		}

		public java.util.List<io.intino.legio.Project.WebDependencies.WebActivity> webActivityList(java.util.function.Predicate<io.intino.legio.Project.WebDependencies.WebActivity> predicate) {
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
			return this.graph().concept(io.intino.legio.Project.WebDependencies.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$WebDependencies$Resolution")) this.resolutionList.add(node.as(io.intino.legio.Project.WebDependencies.Resolution.class));
			if (node.is("Project$WebDependencies$WebComponent")) this.webComponentList.add(node.as(io.intino.legio.Project.WebDependencies.WebComponent.class));
			if (node.is("Project$WebDependencies$WebActivity")) this.webActivityList.add(node.as(io.intino.legio.Project.WebDependencies.WebActivity.class));
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$WebDependencies$Resolution")) this.resolutionList.remove(node.as(io.intino.legio.Project.WebDependencies.Resolution.class));
	        if (node.is("Project$WebDependencies$WebComponent")) this.webComponentList.remove(node.as(io.intino.legio.Project.WebDependencies.WebComponent.class));
	        if (node.is("Project$WebDependencies$WebActivity")) this.webActivityList.remove(node.as(io.intino.legio.Project.WebDependencies.WebActivity.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("webDirectory")) this.webDirectory = tara.magritte.loaders.StringLoader.load(values, this).get(0);
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

			public io.intino.legio.Project.WebDependencies.Resolution resolution(java.lang.String name, java.lang.String version) {
			    io.intino.legio.Project.WebDependencies.Resolution newElement = graph().concept(io.intino.legio.Project.WebDependencies.Resolution.class).createNode(name, node()).as(io.intino.legio.Project.WebDependencies.Resolution.class);
				newElement.node().set(newElement, "name", java.util.Collections.singletonList(name));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}

			public io.intino.legio.Project.WebDependencies.WebComponent webComponent(java.lang.String version) {
			    io.intino.legio.Project.WebDependencies.WebComponent newElement = graph().concept(io.intino.legio.Project.WebDependencies.WebComponent.class).createNode(name, node()).as(io.intino.legio.Project.WebDependencies.WebComponent.class);
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}

			public io.intino.legio.Project.WebDependencies.WebActivity webActivity(java.lang.String groupId, java.lang.String artifactId, java.lang.String version) {
			    io.intino.legio.Project.WebDependencies.WebActivity newElement = graph().concept(io.intino.legio.Project.WebDependencies.WebActivity.class).createNode(name, node()).as(io.intino.legio.Project.WebDependencies.WebActivity.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
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

			public void name(java.lang.String value) {
				this.name = value;
			}

			public void version(java.lang.String value) {
				this.version = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.WebDependencies.Resolution.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("name")) this.name = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("name")) this.name = (java.lang.String) values.get(0);
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
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
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
				return this.graph().concept(io.intino.legio.Project.WebDependencies.WebComponent.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("url")) this.url = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = tara.magritte.loaders.StringLoader.load(values, this).get(0);
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
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class WebActivity extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected Expression<java.lang.String> identifier;
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

			public void identifier(Expression<java.lang.String> value) {
				this.identifier = FunctionLoader.load(value, this, Expression.class);
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
				return this.graph().concept(io.intino.legio.Project.WebDependencies.WebActivity.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = FunctionLoader.load(values, this, Expression.class).get(0);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = FunctionLoader.load(values.get(0), this, Expression.class);
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
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		
		public io.intino.legio.LegioApplication application() {
			return ((io.intino.legio.LegioApplication) graph().application());
		}
	}
	
	public static class Factory extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String inPackage;
		protected io.intino.legio.Project.Factory.Interface interface$;
		protected io.intino.legio.Project.Factory.Behavior behavior;

		public Factory(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String inPackage() {
			return inPackage;
		}

		public void inPackage(java.lang.String value) {
			this.inPackage = value;
		}

		public io.intino.legio.Project.Factory.Interface interface$() {
			return interface$;
		}

		public io.intino.legio.Project.Factory.Behavior behavior() {
			return behavior;
		}

		public void interface$(io.intino.legio.Project.Factory.Interface value) {
			this.interface$ = value;
		}

		public void behavior(io.intino.legio.Project.Factory.Behavior value) {
			this.behavior = value;
		}

		public io.intino.legio.platform.project.PlatformFactory asPlatform() {
			return this.as(io.intino.legio.platform.project.PlatformFactory.class);
		}

		public io.intino.legio.platform.project.PlatformFactory asPlatform(java.lang.String language, java.lang.String version) {
			io.intino.legio.platform.project.PlatformFactory newElement = addFacet(io.intino.legio.platform.project.PlatformFactory.class);
			newElement.node().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}

		public boolean isPlatform() {
			return is(io.intino.legio.platform.project.PlatformFactory.class);
		}

		public void removePlatform() {
			this.removeFacet(io.intino.legio.platform.project.PlatformFactory.class);
		}

		public io.intino.legio.level.project.LevelFactory asLevel() {
			return this.as(io.intino.legio.level.project.LevelFactory.class);
		}

		public io.intino.legio.level.project.LevelFactory asLevel(java.lang.String language, java.lang.String version) {
			io.intino.legio.level.project.LevelFactory newElement = addFacet(io.intino.legio.level.project.LevelFactory.class);
			newElement.node().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}

		public boolean isLevel() {
			return is(io.intino.legio.level.project.LevelFactory.class);
		}

		public void removeLevel() {
			this.removeFacet(io.intino.legio.level.project.LevelFactory.class);
		}

		public io.intino.legio.application.project.ApplicationFactory asApplication() {
			return this.as(io.intino.legio.application.project.ApplicationFactory.class);
		}

		public io.intino.legio.application.project.ApplicationFactory asApplication(java.lang.String language, java.lang.String version) {
			io.intino.legio.application.project.ApplicationFactory newElement = addFacet(io.intino.legio.application.project.ApplicationFactory.class);
			newElement.node().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}

		public boolean isApplication() {
			return is(io.intino.legio.application.project.ApplicationFactory.class);
		}

		public void removeApplication() {
			this.removeFacet(io.intino.legio.application.project.ApplicationFactory.class);
		}

		public io.intino.legio.system.project.SystemFactory asSystem() {
			return this.as(io.intino.legio.system.project.SystemFactory.class);
		}

		public io.intino.legio.system.project.SystemFactory asSystem(java.lang.String language, java.lang.String version) {
			io.intino.legio.system.project.SystemFactory newElement = addFacet(io.intino.legio.system.project.SystemFactory.class);
			newElement.node().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}

		public boolean isSystem() {
			return is(io.intino.legio.system.project.SystemFactory.class);
		}

		public void removeSystem() {
			this.removeFacet(io.intino.legio.system.project.SystemFactory.class);
		}

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			if (interface$ != null) components.add(this.interface$.node());
			if (behavior != null) components.add(this.behavior.node());
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("inPackage", new java.util.ArrayList(java.util.Collections.singletonList(this.inPackage)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Project.Factory.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Factory$Interface")) this.interface$ = node.as(io.intino.legio.Project.Factory.Interface.class);
			if (node.is("Project$Factory$Behavior")) this.behavior = node.as(io.intino.legio.Project.Factory.Behavior.class);
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Factory$Interface")) this.interface$ = null;
	        if (node.is("Project$Factory$Behavior")) this.behavior = null;
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("inPackage")) this.inPackage = tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("inPackage")) this.inPackage = (java.lang.String) values.get(0);
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

			public io.intino.legio.Project.Factory.Interface interface$(java.lang.String version) {
			    io.intino.legio.Project.Factory.Interface newElement = graph().concept(io.intino.legio.Project.Factory.Interface.class).createNode(name, node()).as(io.intino.legio.Project.Factory.Interface.class);
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}

			public io.intino.legio.Project.Factory.Behavior behavior(java.lang.String version) {
			    io.intino.legio.Project.Factory.Behavior newElement = graph().concept(io.intino.legio.Project.Factory.Behavior.class).createNode(name, node()).as(io.intino.legio.Project.Factory.Behavior.class);
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}
			
		}
		
		public static class Interface extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String version;

			public Interface(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String version() {
				return version;
			}

			public void version(java.lang.String value) {
				this.version = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Factory.Interface.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("version")) this.version = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
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
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Behavior extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String version;

			public Behavior(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String version() {
				return version;
			}

			public void version(java.lang.String value) {
				this.version = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Factory.Behavior.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("version")) this.version = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
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
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		
		public io.intino.legio.LegioApplication application() {
			return ((io.intino.legio.LegioApplication) graph().application());
		}
	}
	
	
	public io.intino.legio.LegioApplication application() {
		return ((io.intino.legio.LegioApplication) graph().application());
	}
}
