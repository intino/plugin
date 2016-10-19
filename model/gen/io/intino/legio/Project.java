package io.intino.legio;

import io.intino.legio.*;

import java.util.*;

public class Project extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
	protected java.lang.String groupId;
	protected java.lang.String version;
	protected io.intino.legio.Project.Repositories repositories;
	protected io.intino.legio.Project.Dependencies dependencies;
	protected io.intino.legio.Project.Factory factory;
	protected io.intino.legio.Project.Build build;

	public Project(tara.magritte.Node node) {
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

	public io.intino.legio.Project.Repositories repositories() {
		return repositories;
	}

	public io.intino.legio.Project.Dependencies dependencies() {
		return dependencies;
	}

	public io.intino.legio.Project.Factory factory() {
		return factory;
	}

	public io.intino.legio.Project.Build build() {
		return build;
	}

	public void repositories(io.intino.legio.Project.Repositories value) {
		this.repositories = value;
	}

	public void dependencies(io.intino.legio.Project.Dependencies value) {
		this.dependencies = value;
	}

	public void factory(io.intino.legio.Project.Factory value) {
		this.factory = value;
	}

	public void build(io.intino.legio.Project.Build value) {
		this.build = value;
	}

	public List<tara.magritte.Node> componentList() {
		java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		if (repositories != null) components.add(this.repositories.node());
		if (dependencies != null) components.add(this.dependencies.node());
		if (factory != null) components.add(this.factory.node());
		if (build != null) components.add(this.build.node());
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
		map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
		return map;
	}

	public tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Project.class);
	}

	@Override
	protected void addNode(tara.magritte.Node node) {
		super.addNode(node);
		if (node.is("Project$Repositories")) this.repositories = node.as(io.intino.legio.Project.Repositories.class);
		if (node.is("Project$Dependencies")) this.dependencies = node.as(io.intino.legio.Project.Dependencies.class);
		if (node.is("Project$Factory")) this.factory = node.as(io.intino.legio.Project.Factory.class);
		if (node.is("Project$Build")) this.build = node.as(io.intino.legio.Project.Build.class);
	}

	@Override
    protected void removeNode(tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("Project$Repositories")) this.repositories = null;
        if (node.is("Project$Dependencies")) this.dependencies = null;
        if (node.is("Project$Factory")) this.factory = null;
        if (node.is("Project$Build")) this.build = null;
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

		public io.intino.legio.Project.Repositories repositories() {
		    io.intino.legio.Project.Repositories newElement = graph().concept(io.intino.legio.Project.Repositories.class).createNode(name, node()).as(io.intino.legio.Project.Repositories.class);
		    return newElement;
		}

		public io.intino.legio.Project.Dependencies dependencies() {
		    io.intino.legio.Project.Dependencies newElement = graph().concept(io.intino.legio.Project.Dependencies.class).createNode(name, node()).as(io.intino.legio.Project.Dependencies.class);
		    return newElement;
		}

		public io.intino.legio.Project.Factory factory() {
		    io.intino.legio.Project.Factory newElement = graph().concept(io.intino.legio.Project.Factory.class).createNode(name, node()).as(io.intino.legio.Project.Factory.class);
		    return newElement;
		}

		public io.intino.legio.Project.Build build() {
		    io.intino.legio.Project.Build newElement = graph().concept(io.intino.legio.Project.Build.class).createNode(name, node()).as(io.intino.legio.Project.Build.class);
		    return newElement;
		}
		
	}
	
	public static class Repositories extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		
		protected java.util.List<io.intino.legio.Project.Repositories.Repository> repositoryList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Repositories.Release> releaseList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Repositories.Snapshot> snapshotList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Repositories.Distribution> distributionList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Repositories.Language> languageList = new java.util.ArrayList<>();

		public Repositories(tara.magritte.Node node) {
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

		public java.util.List<io.intino.legio.Project.Repositories.Snapshot> snapshotList() {
			return java.util.Collections.unmodifiableList(snapshotList);
		}

		public io.intino.legio.Project.Repositories.Snapshot snapshot(int index) {
			return snapshotList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Repositories.Snapshot> snapshotList(java.util.function.Predicate<io.intino.legio.Project.Repositories.Snapshot> predicate) {
			return snapshotList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.Repositories.Distribution> distributionList() {
			return java.util.Collections.unmodifiableList(distributionList);
		}

		public io.intino.legio.Project.Repositories.Distribution distribution(int index) {
			return distributionList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Repositories.Distribution> distributionList(java.util.function.Predicate<io.intino.legio.Project.Repositories.Distribution> predicate) {
			return distributionList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.Repositories.Language> languageList() {
			return java.util.Collections.unmodifiableList(languageList);
		}

		public io.intino.legio.Project.Repositories.Language language(int index) {
			return languageList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Repositories.Language> languageList(java.util.function.Predicate<io.intino.legio.Project.Repositories.Language> predicate) {
			return languageList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		

		

		

		

		

		public List<tara.magritte.Node> componentList() {
			java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			repositoryList.stream().forEach(c -> components.add(c.node()));
			releaseList.stream().forEach(c -> components.add(c.node()));
			snapshotList.stream().forEach(c -> components.add(c.node()));
			distributionList.stream().forEach(c -> components.add(c.node()));
			languageList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			return map;
		}

		public tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Project.Repositories.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Repositories$Repository")) this.repositoryList.add(node.as(io.intino.legio.Project.Repositories.Repository.class));
			if (node.is("Project$Repositories$Release")) this.releaseList.add(node.as(io.intino.legio.Project.Repositories.Release.class));
			if (node.is("Project$Repositories$Snapshot")) this.snapshotList.add(node.as(io.intino.legio.Project.Repositories.Snapshot.class));
			if (node.is("Project$Repositories$Distribution")) this.distributionList.add(node.as(io.intino.legio.Project.Repositories.Distribution.class));
			if (node.is("Project$Repositories$Language")) this.languageList.add(node.as(io.intino.legio.Project.Repositories.Language.class));
		}

		@Override
	    protected void removeNode(tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Repositories$Repository")) this.repositoryList.remove(node.as(io.intino.legio.Project.Repositories.Repository.class));
	        if (node.is("Project$Repositories$Release")) this.releaseList.remove(node.as(io.intino.legio.Project.Repositories.Release.class));
	        if (node.is("Project$Repositories$Snapshot")) this.snapshotList.remove(node.as(io.intino.legio.Project.Repositories.Snapshot.class));
	        if (node.is("Project$Repositories$Distribution")) this.distributionList.remove(node.as(io.intino.legio.Project.Repositories.Distribution.class));
	        if (node.is("Project$Repositories$Language")) this.languageList.remove(node.as(io.intino.legio.Project.Repositories.Language.class));
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

			public io.intino.legio.Project.Repositories.Distribution distribution(java.lang.String url, java.lang.String mavenId) {
			    io.intino.legio.Project.Repositories.Distribution newElement = graph().concept(io.intino.legio.Project.Repositories.Distribution.class).createNode(name, node()).as(io.intino.legio.Project.Repositories.Distribution.class);
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
		
		public static abstract class Repository extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
			protected java.lang.String url;
			protected java.lang.String mavenId;

			public Repository(tara.magritte.Node node) {
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

			public tara.magritte.Concept concept() {
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
		
		public static class Release extends io.intino.legio.Project.Repositories.Repository implements tara.magritte.tags.Terminal {
			

			public Release(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
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
		
		public static class Snapshot extends io.intino.legio.Project.Repositories.Repository implements tara.magritte.tags.Terminal {
			

			public Snapshot(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
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
		
		public static class Distribution extends io.intino.legio.Project.Repositories.Repository implements tara.magritte.tags.Terminal {
			

			public Distribution(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Repositories.Distribution.class);
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
		
		public static class Language extends io.intino.legio.Project.Repositories.Repository implements tara.magritte.tags.Terminal {
			

			public Language(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
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
	
	public static class Dependencies extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		
		protected java.util.List<io.intino.legio.Project.Dependencies.Dependency> dependencyList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Dependencies.Compile> compileList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Dependencies.Runtime> runtimeList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Dependencies.Test> testList = new java.util.ArrayList<>();

		public Dependencies(tara.magritte.Node node) {
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

		public java.util.List<io.intino.legio.Project.Dependencies.Test> testList() {
			return java.util.Collections.unmodifiableList(testList);
		}

		public io.intino.legio.Project.Dependencies.Test test(int index) {
			return testList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Dependencies.Test> testList(java.util.function.Predicate<io.intino.legio.Project.Dependencies.Test> predicate) {
			return testList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		

		

		

		

		public List<tara.magritte.Node> componentList() {
			java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			dependencyList.stream().forEach(c -> components.add(c.node()));
			compileList.stream().forEach(c -> components.add(c.node()));
			runtimeList.stream().forEach(c -> components.add(c.node()));
			testList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			return map;
		}

		public tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Project.Dependencies.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Dependencies$Dependency")) this.dependencyList.add(node.as(io.intino.legio.Project.Dependencies.Dependency.class));
			if (node.is("Project$Dependencies$Compile")) this.compileList.add(node.as(io.intino.legio.Project.Dependencies.Compile.class));
			if (node.is("Project$Dependencies$Runtime")) this.runtimeList.add(node.as(io.intino.legio.Project.Dependencies.Runtime.class));
			if (node.is("Project$Dependencies$Test")) this.testList.add(node.as(io.intino.legio.Project.Dependencies.Test.class));
		}

		@Override
	    protected void removeNode(tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Dependencies$Dependency")) this.dependencyList.remove(node.as(io.intino.legio.Project.Dependencies.Dependency.class));
	        if (node.is("Project$Dependencies$Compile")) this.compileList.remove(node.as(io.intino.legio.Project.Dependencies.Compile.class));
	        if (node.is("Project$Dependencies$Runtime")) this.runtimeList.remove(node.as(io.intino.legio.Project.Dependencies.Runtime.class));
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

			

			public io.intino.legio.Project.Dependencies.Compile compile(java.lang.String groupId, java.lang.String artifactId, java.lang.String version) {
			    io.intino.legio.Project.Dependencies.Compile newElement = graph().concept(io.intino.legio.Project.Dependencies.Compile.class).createNode(name, node()).as(io.intino.legio.Project.Dependencies.Compile.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}

			public io.intino.legio.Project.Dependencies.Runtime runtime(java.lang.String groupId, java.lang.String artifactId, java.lang.String version) {
			    io.intino.legio.Project.Dependencies.Runtime newElement = graph().concept(io.intino.legio.Project.Dependencies.Runtime.class).createNode(name, node()).as(io.intino.legio.Project.Dependencies.Runtime.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}

			public io.intino.legio.Project.Dependencies.Test test(java.lang.String groupId, java.lang.String artifactId, java.lang.String version) {
			    io.intino.legio.Project.Dependencies.Test newElement = graph().concept(io.intino.legio.Project.Dependencies.Test.class).createNode(name, node()).as(io.intino.legio.Project.Dependencies.Test.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}
			
		}
		
		public static abstract class Dependency extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
			protected tara.magritte.Expression<java.lang.String> identifier;
			protected tara.magritte.Expression<java.lang.String> name;
			protected java.lang.String groupId;
			protected java.lang.String artifactId;
			protected java.lang.String version;
			protected boolean transitive;

			public Dependency(tara.magritte.Node node) {
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

			public boolean transitive() {
				return transitive;
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

			public void transitive(boolean value) {
				this.transitive = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("identifier", new java.util.ArrayList(java.util.Collections.singletonList(this.identifier)));
				map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name)));
				map.put("groupId", new java.util.ArrayList(java.util.Collections.singletonList(this.groupId)));
				map.put("artifactId", new java.util.ArrayList(java.util.Collections.singletonList(this.artifactId)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				map.put("transitive", new java.util.ArrayList(java.util.Collections.singletonList(this.transitive)));
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Dependencies.Dependency.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = tara.magritte.loaders.FunctionLoader.load(values, this, tara.magritte.Expression.class).get(0);
				else if (name.equalsIgnoreCase("name")) this.name = tara.magritte.loaders.FunctionLoader.load(values, this, tara.magritte.Expression.class).get(0);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("transitive")) this.transitive = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("identifier")) this.identifier = tara.magritte.loaders.FunctionLoader.load(values.get(0), this, tara.magritte.Expression.class);
				else if (name.equalsIgnoreCase("name")) this.name = tara.magritte.loaders.FunctionLoader.load(values.get(0), this, tara.magritte.Expression.class);
				else if (name.equalsIgnoreCase("groupId")) this.groupId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("artifactId")) this.artifactId = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("transitive")) this.transitive = (java.lang.Boolean) values.get(0);
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
		
		public static class Compile extends io.intino.legio.Project.Dependencies.Dependency implements tara.magritte.tags.Terminal {
			

			public Compile(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
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
		
		public static class Runtime extends io.intino.legio.Project.Dependencies.Dependency implements tara.magritte.tags.Terminal {
			

			public Runtime(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
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
		
		public static class Test extends io.intino.legio.Project.Dependencies.Dependency implements tara.magritte.tags.Terminal {
			

			public Test(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
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
	
	public static class Factory extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		protected java.lang.String generationPackage;
		protected boolean persistent;
		protected int refactorId;
		protected io.intino.legio.Project.Factory.Modeling modeling;

		public Factory(tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String generationPackage() {
			return generationPackage;
		}

		public boolean persistent() {
			return persistent;
		}

		public int refactorId() {
			return refactorId;
		}

		public void generationPackage(java.lang.String value) {
			this.generationPackage = value;
		}

		public void persistent(boolean value) {
			this.persistent = value;
		}

		public void refactorId(int value) {
			this.refactorId = value;
		}

		public io.intino.legio.Project.Factory.Modeling modeling() {
			return modeling;
		}

		public void modeling(io.intino.legio.Project.Factory.Modeling value) {
			this.modeling = value;
		}

		public io.intino.legio.platform.project.PlatformFactory asPlatform() {
			tara.magritte.Layer as = this.as(io.intino.legio.platform.project.PlatformFactory.class);
			return as != null ? (io.intino.legio.platform.project.PlatformFactory) as : addFacet(io.intino.legio.platform.project.PlatformFactory.class);
		}

		public boolean isPlatform() {
			return is(io.intino.legio.platform.project.PlatformFactory.class);
		}

		public io.intino.legio.level.project.LevelFactory asLevel() {
			tara.magritte.Layer as = this.as(io.intino.legio.level.project.LevelFactory.class);
			return as != null ? (io.intino.legio.level.project.LevelFactory) as : null;
		}

		public boolean isLevel() {
			return is(io.intino.legio.level.project.LevelFactory.class);
		}

		public io.intino.legio.application.project.ApplicationFactory asApplication() {
			tara.magritte.Layer as = this.as(io.intino.legio.application.project.ApplicationFactory.class);
			return as != null ? (io.intino.legio.application.project.ApplicationFactory) as : addFacet(io.intino.legio.application.project.ApplicationFactory.class);
		}

		public boolean isApplication() {
			return is(io.intino.legio.application.project.ApplicationFactory.class);
		}

		public io.intino.legio.system.project.SystemFactory asSystem() {
			tara.magritte.Layer as = this.as(io.intino.legio.system.project.SystemFactory.class);
			return as != null ? (io.intino.legio.system.project.SystemFactory) as : addFacet(io.intino.legio.system.project.SystemFactory.class);
		}

		public boolean isSystem() {
			return is(io.intino.legio.system.project.SystemFactory.class);
		}

		public List<tara.magritte.Node> componentList() {
			java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			if (modeling != null) components.add(this.modeling.node());
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("generationPackage", new java.util.ArrayList(java.util.Collections.singletonList(this.generationPackage)));
			map.put("persistent", new java.util.ArrayList(java.util.Collections.singletonList(this.persistent)));
			map.put("refactorId", new java.util.ArrayList(java.util.Collections.singletonList(this.refactorId)));
			return map;
		}

		public tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Project.Factory.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Factory$Modeling")) this.modeling = node.as(io.intino.legio.Project.Factory.Modeling.class);
		}

		@Override
	    protected void removeNode(tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Factory$Modeling")) this.modeling = null;
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("generationPackage")) this.generationPackage = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("persistent")) this.persistent = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("refactorId")) this.refactorId = tara.magritte.loaders.IntegerLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("generationPackage")) this.generationPackage = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("persistent")) this.persistent = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("refactorId")) this.refactorId = (java.lang.Integer) values.get(0);
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

			public io.intino.legio.Project.Factory.Modeling modeling(java.lang.String language, java.lang.String version) {
			    io.intino.legio.Project.Factory.Modeling newElement = graph().concept(io.intino.legio.Project.Factory.Modeling.class).createNode(name, node()).as(io.intino.legio.Project.Factory.Modeling.class);
				newElement.node().set(newElement, "language", java.util.Collections.singletonList(language));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}
			
		}
		
		public static class Modeling extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
			protected java.lang.String language;
			protected java.lang.String version;

			public Modeling(tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String language() {
				return language;
			}

			public java.lang.String version() {
				return version;
			}

			public void language(java.lang.String value) {
				this.language = value;
			}

			public void version(java.lang.String value) {
				this.version = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("language", new java.util.ArrayList(java.util.Collections.singletonList(this.language)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Factory.Modeling.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("language")) this.language = tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("language")) this.language = (java.lang.String) values.get(0);
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
	
	public static class Build extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		protected boolean attachSources;
		protected boolean attachDoc;
		protected boolean includeTests;
		protected java.lang.String mainClass;
		protected java.lang.String finalName;
		protected io.intino.legio.Project.Build.Package package$;
		protected java.util.List<io.intino.legio.Project.Build.Plugin> pluginList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.Project.Build.License> licenseList = new java.util.ArrayList<>();

		public Build(tara.magritte.Node node) {
			super(node);
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

		public java.lang.String mainClass() {
			return mainClass;
		}

		public java.lang.String finalName() {
			return finalName;
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

		public void mainClass(java.lang.String value) {
			this.mainClass = value;
		}

		public void finalName(java.lang.String value) {
			this.finalName = value;
		}

		public io.intino.legio.Project.Build.Package package$() {
			return package$;
		}

		public java.util.List<io.intino.legio.Project.Build.Plugin> pluginList() {
			return java.util.Collections.unmodifiableList(pluginList);
		}

		public io.intino.legio.Project.Build.Plugin plugin(int index) {
			return pluginList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Build.Plugin> pluginList(java.util.function.Predicate<io.intino.legio.Project.Build.Plugin> predicate) {
			return pluginList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.Project.Build.License> licenseList() {
			return java.util.Collections.unmodifiableList(licenseList);
		}

		public io.intino.legio.Project.Build.License license(int index) {
			return licenseList.get(index);
		}

		public java.util.List<io.intino.legio.Project.Build.License> licenseList(java.util.function.Predicate<io.intino.legio.Project.Build.License> predicate) {
			return licenseList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public void package$(io.intino.legio.Project.Build.Package value) {
			this.package$ = value;
		}

		

		

		public List<tara.magritte.Node> componentList() {
			java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			if (package$ != null) components.add(this.package$.node());
			pluginList.stream().forEach(c -> components.add(c.node()));
			licenseList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("attachSources", new java.util.ArrayList(java.util.Collections.singletonList(this.attachSources)));
			map.put("attachDoc", new java.util.ArrayList(java.util.Collections.singletonList(this.attachDoc)));
			map.put("includeTests", new java.util.ArrayList(java.util.Collections.singletonList(this.includeTests)));
			map.put("mainClass", new java.util.ArrayList(java.util.Collections.singletonList(this.mainClass)));
			map.put("finalName", new java.util.ArrayList(java.util.Collections.singletonList(this.finalName)));
			return map;
		}

		public tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Project.Build.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Build$Package")) this.package$ = node.as(io.intino.legio.Project.Build.Package.class);
			if (node.is("Project$Build$Plugin")) this.pluginList.add(node.as(io.intino.legio.Project.Build.Plugin.class));
			if (node.is("Project$Build$License")) this.licenseList.add(node.as(io.intino.legio.Project.Build.License.class));
		}

		@Override
	    protected void removeNode(tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Build$Package")) this.package$ = null;
	        if (node.is("Project$Build$Plugin")) this.pluginList.remove(node.as(io.intino.legio.Project.Build.Plugin.class));
	        if (node.is("Project$Build$License")) this.licenseList.remove(node.as(io.intino.legio.Project.Build.License.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("attachSources")) this.attachSources = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("attachDoc")) this.attachDoc = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("includeTests")) this.includeTests = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("mainClass")) this.mainClass = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("finalName")) this.finalName = tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("attachSources")) this.attachSources = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("attachDoc")) this.attachDoc = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("includeTests")) this.includeTests = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("mainClass")) this.mainClass = (java.lang.String) values.get(0);
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

			public io.intino.legio.Project.Build.Package package$(io.intino.legio.Project.Build.Package.Type type) {
			    io.intino.legio.Project.Build.Package newElement = graph().concept(io.intino.legio.Project.Build.Package.class).createNode(name, node()).as(io.intino.legio.Project.Build.Package.class);
				newElement.node().set(newElement, "type", java.util.Collections.singletonList(type)); 
			    return newElement;
			}

			public io.intino.legio.Project.Build.Plugin plugin(java.lang.String mavenCode) {
			    io.intino.legio.Project.Build.Plugin newElement = graph().concept(io.intino.legio.Project.Build.Plugin.class).createNode(name, node()).as(io.intino.legio.Project.Build.Plugin.class);
				newElement.node().set(newElement, "mavenCode", java.util.Collections.singletonList(mavenCode)); 
			    return newElement;
			}

			public io.intino.legio.Project.Build.License license(io.intino.legio.Project.Build.License.Type type) {
			    io.intino.legio.Project.Build.License newElement = graph().concept(io.intino.legio.Project.Build.License.class).createNode(name, node()).as(io.intino.legio.Project.Build.License.class);
				newElement.node().set(newElement, "type", java.util.Collections.singletonList(type)); 
			    return newElement;
			}
			
		}
		
		public static class Package extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
			protected Type type;

			public enum Type {
				LibrariesExtracted, OnlyLibrariesLinkedByManifest, AllDependenciesLinkedByManifest;
			}

			public Package(tara.magritte.Node node) {
				super(node);
			}

			public Type type() {
				return type;
			}

			public void type(io.intino.legio.Project.Build.Package.Type value) {
				this.type = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("type", new java.util.ArrayList(java.util.Collections.singletonList(this.type)));
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Build.Package.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("type")) this.type = tara.magritte.loaders.WordLoader.load(values, Type.class, this).get(0);
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
		
		public static class Plugin extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
			protected java.lang.String mavenCode;

			public Plugin(tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String mavenCode() {
				return mavenCode;
			}

			public void mavenCode(java.lang.String value) {
				this.mavenCode = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("mavenCode", new java.util.ArrayList(java.util.Collections.singletonList(this.mavenCode)));
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Build.Plugin.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("mavenCode")) this.mavenCode = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("mavenCode")) this.mavenCode = (java.lang.String) values.get(0);
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
		
		public static class License extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
			protected Type type;

			public enum Type {
				GPL, BSD;
			}

			public License(tara.magritte.Node node) {
				super(node);
			}

			public Type type() {
				return type;
			}

			public void type(io.intino.legio.Project.Build.License.Type value) {
				this.type = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("type", new java.util.ArrayList(java.util.Collections.singletonList(this.type)));
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.Project.Build.License.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("type")) this.type = tara.magritte.loaders.WordLoader.load(values, Type.class, this).get(0);
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
		
		
		public io.intino.legio.LegioApplication application() {
			return ((io.intino.legio.LegioApplication) graph().application());
		}
	}
	
	
	public io.intino.legio.LegioApplication application() {
		return ((io.intino.legio.LegioApplication) graph().application());
	}
}
