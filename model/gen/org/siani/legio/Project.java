package org.siani.legio;

import org.siani.legio.*;

import java.util.*;

public class Project extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
	protected java.lang.String groupId;
	protected java.lang.String version;
	protected org.siani.legio.Project.Repositories repositories;
	protected org.siani.legio.Project.Dependencies dependencies;
	protected org.siani.legio.Project.Factory factory;
	protected java.util.List<org.siani.legio.Project.Build> buildList = new java.util.ArrayList<>();

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

	public org.siani.legio.Project.Repositories repositories() {
		return repositories;
	}

	public org.siani.legio.Project.Dependencies dependencies() {
		return dependencies;
	}

	public org.siani.legio.Project.Factory factory() {
		return factory;
	}

	public java.util.List<org.siani.legio.Project.Build> buildList() {
		return java.util.Collections.unmodifiableList(buildList);
	}

	public org.siani.legio.Project.Build build(int index) {
		return buildList.get(index);
	}

	public java.util.List<org.siani.legio.Project.Build> buildList(java.util.function.Predicate<org.siani.legio.Project.Build> predicate) {
		return buildList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public void repositories(org.siani.legio.Project.Repositories value) {
		this.repositories = value;
	}

	public void dependencies(org.siani.legio.Project.Dependencies value) {
		this.dependencies = value;
	}

	public void factory(org.siani.legio.Project.Factory value) {
		this.factory = value;
	}

	

	public List<tara.magritte.Node> componentList() {
		java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		if (repositories != null) components.add(this.repositories.node());
		if (dependencies != null) components.add(this.dependencies.node());
		if (factory != null) components.add(this.factory.node());
		buildList.stream().forEach(c -> components.add(c.node()));
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
		return this.graph().concept(org.siani.legio.Project.class);
	}

	@Override
	protected void addNode(tara.magritte.Node node) {
		super.addNode(node);
		if (node.is("Project$Repositories")) this.repositories = node.as(org.siani.legio.Project.Repositories.class);
		if (node.is("Project$Dependencies")) this.dependencies = node.as(org.siani.legio.Project.Dependencies.class);
		if (node.is("Project$Factory")) this.factory = node.as(org.siani.legio.Project.Factory.class);
		if (node.is("Project$Build")) this.buildList.add(node.as(org.siani.legio.Project.Build.class));
	}

	@Override
    protected void removeNode(tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("Project$Repositories")) this.repositories = null;
        if (node.is("Project$Dependencies")) this.dependencies = null;
        if (node.is("Project$Factory")) this.factory = null;
        if (node.is("Project$Build")) this.buildList.remove(node.as(org.siani.legio.Project.Build.class));
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

		public org.siani.legio.Project.Repositories repositories() {
		    org.siani.legio.Project.Repositories newElement = graph().concept(org.siani.legio.Project.Repositories.class).createNode(name, node()).as(org.siani.legio.Project.Repositories.class);
		    return newElement;
		}

		public org.siani.legio.Project.Dependencies dependencies() {
		    org.siani.legio.Project.Dependencies newElement = graph().concept(org.siani.legio.Project.Dependencies.class).createNode(name, node()).as(org.siani.legio.Project.Dependencies.class);
		    return newElement;
		}

		public org.siani.legio.Project.Factory factory() {
		    org.siani.legio.Project.Factory newElement = graph().concept(org.siani.legio.Project.Factory.class).createNode(name, node()).as(org.siani.legio.Project.Factory.class);
		    return newElement;
		}

		public org.siani.legio.Project.Build build() {
		    org.siani.legio.Project.Build newElement = graph().concept(org.siani.legio.Project.Build.class).createNode(name, node()).as(org.siani.legio.Project.Build.class);
		    return newElement;
		}
		
	}
	
	public static class Repositories extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		
		protected java.util.List<org.siani.legio.Project.Repositories.Repository> repositoryList = new java.util.ArrayList<>();
		protected java.util.List<org.siani.legio.Project.Repositories.Release> releaseList = new java.util.ArrayList<>();
		protected java.util.List<org.siani.legio.Project.Repositories.Snapshot> snapshotList = new java.util.ArrayList<>();
		protected java.util.List<org.siani.legio.Project.Repositories.Distribution> distributionList = new java.util.ArrayList<>();
		protected java.util.List<org.siani.legio.Project.Repositories.Language> languageList = new java.util.ArrayList<>();

		public Repositories(tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<org.siani.legio.Project.Repositories.Repository> repositoryList() {
			return java.util.Collections.unmodifiableList(repositoryList);
		}

		public org.siani.legio.Project.Repositories.Repository repository(int index) {
			return repositoryList.get(index);
		}

		public java.util.List<org.siani.legio.Project.Repositories.Repository> repositoryList(java.util.function.Predicate<org.siani.legio.Project.Repositories.Repository> predicate) {
			return repositoryList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<org.siani.legio.Project.Repositories.Release> releaseList() {
			return java.util.Collections.unmodifiableList(releaseList);
		}

		public org.siani.legio.Project.Repositories.Release release(int index) {
			return releaseList.get(index);
		}

		public java.util.List<org.siani.legio.Project.Repositories.Release> releaseList(java.util.function.Predicate<org.siani.legio.Project.Repositories.Release> predicate) {
			return releaseList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<org.siani.legio.Project.Repositories.Snapshot> snapshotList() {
			return java.util.Collections.unmodifiableList(snapshotList);
		}

		public org.siani.legio.Project.Repositories.Snapshot snapshot(int index) {
			return snapshotList.get(index);
		}

		public java.util.List<org.siani.legio.Project.Repositories.Snapshot> snapshotList(java.util.function.Predicate<org.siani.legio.Project.Repositories.Snapshot> predicate) {
			return snapshotList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<org.siani.legio.Project.Repositories.Distribution> distributionList() {
			return java.util.Collections.unmodifiableList(distributionList);
		}

		public org.siani.legio.Project.Repositories.Distribution distribution(int index) {
			return distributionList.get(index);
		}

		public java.util.List<org.siani.legio.Project.Repositories.Distribution> distributionList(java.util.function.Predicate<org.siani.legio.Project.Repositories.Distribution> predicate) {
			return distributionList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<org.siani.legio.Project.Repositories.Language> languageList() {
			return java.util.Collections.unmodifiableList(languageList);
		}

		public org.siani.legio.Project.Repositories.Language language(int index) {
			return languageList.get(index);
		}

		public java.util.List<org.siani.legio.Project.Repositories.Language> languageList(java.util.function.Predicate<org.siani.legio.Project.Repositories.Language> predicate) {
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
			return this.graph().concept(org.siani.legio.Project.Repositories.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Repositories$Repository")) this.repositoryList.add(node.as(org.siani.legio.Project.Repositories.Repository.class));
			if (node.is("Project$Repositories$Release")) this.releaseList.add(node.as(org.siani.legio.Project.Repositories.Release.class));
			if (node.is("Project$Repositories$Snapshot")) this.snapshotList.add(node.as(org.siani.legio.Project.Repositories.Snapshot.class));
			if (node.is("Project$Repositories$Distribution")) this.distributionList.add(node.as(org.siani.legio.Project.Repositories.Distribution.class));
			if (node.is("Project$Repositories$Language")) this.languageList.add(node.as(org.siani.legio.Project.Repositories.Language.class));
		}

		@Override
	    protected void removeNode(tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Repositories$Repository")) this.repositoryList.remove(node.as(org.siani.legio.Project.Repositories.Repository.class));
	        if (node.is("Project$Repositories$Release")) this.releaseList.remove(node.as(org.siani.legio.Project.Repositories.Release.class));
	        if (node.is("Project$Repositories$Snapshot")) this.snapshotList.remove(node.as(org.siani.legio.Project.Repositories.Snapshot.class));
	        if (node.is("Project$Repositories$Distribution")) this.distributionList.remove(node.as(org.siani.legio.Project.Repositories.Distribution.class));
	        if (node.is("Project$Repositories$Language")) this.languageList.remove(node.as(org.siani.legio.Project.Repositories.Language.class));
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

			

			public org.siani.legio.Project.Repositories.Release release(java.lang.String url, java.lang.String mavenId) {
			    org.siani.legio.Project.Repositories.Release newElement = graph().concept(org.siani.legio.Project.Repositories.Release.class).createNode(name, node()).as(org.siani.legio.Project.Repositories.Release.class);
				newElement.node().set(newElement, "url", java.util.Collections.singletonList(url));
				newElement.node().set(newElement, "mavenId", java.util.Collections.singletonList(mavenId)); 
			    return newElement;
			}

			public org.siani.legio.Project.Repositories.Snapshot snapshot(java.lang.String url, java.lang.String mavenId) {
			    org.siani.legio.Project.Repositories.Snapshot newElement = graph().concept(org.siani.legio.Project.Repositories.Snapshot.class).createNode(name, node()).as(org.siani.legio.Project.Repositories.Snapshot.class);
				newElement.node().set(newElement, "url", java.util.Collections.singletonList(url));
				newElement.node().set(newElement, "mavenId", java.util.Collections.singletonList(mavenId)); 
			    return newElement;
			}

			public org.siani.legio.Project.Repositories.Distribution distribution(java.lang.String url, java.lang.String mavenId) {
			    org.siani.legio.Project.Repositories.Distribution newElement = graph().concept(org.siani.legio.Project.Repositories.Distribution.class).createNode(name, node()).as(org.siani.legio.Project.Repositories.Distribution.class);
				newElement.node().set(newElement, "url", java.util.Collections.singletonList(url));
				newElement.node().set(newElement, "mavenId", java.util.Collections.singletonList(mavenId)); 
			    return newElement;
			}

			public org.siani.legio.Project.Repositories.Language language(java.lang.String url, java.lang.String mavenId) {
			    org.siani.legio.Project.Repositories.Language newElement = graph().concept(org.siani.legio.Project.Repositories.Language.class).createNode(name, node()).as(org.siani.legio.Project.Repositories.Language.class);
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
				return this.graph().concept(org.siani.legio.Project.Repositories.Repository.class);
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
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Release extends org.siani.legio.Project.Repositories.Repository implements tara.magritte.tags.Terminal {
			

			public Release(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(org.siani.legio.Project.Repositories.Release.class);
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

			public class Create extends org.siani.legio.Project.Repositories.Repository.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Snapshot extends org.siani.legio.Project.Repositories.Repository implements tara.magritte.tags.Terminal {
			

			public Snapshot(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(org.siani.legio.Project.Repositories.Snapshot.class);
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

			public class Create extends org.siani.legio.Project.Repositories.Repository.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Distribution extends org.siani.legio.Project.Repositories.Repository implements tara.magritte.tags.Terminal {
			

			public Distribution(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(org.siani.legio.Project.Repositories.Distribution.class);
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

			public class Create extends org.siani.legio.Project.Repositories.Repository.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Language extends org.siani.legio.Project.Repositories.Repository implements tara.magritte.tags.Terminal {
			

			public Language(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(org.siani.legio.Project.Repositories.Language.class);
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

			public class Create extends org.siani.legio.Project.Repositories.Repository.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		
		public org.siani.legio.LegioApplication application() {
			return ((org.siani.legio.LegioApplication) graph().application());
		}
	}
	
	public static class Dependencies extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		
		protected java.util.List<org.siani.legio.Project.Dependencies.Dependency> dependencyList = new java.util.ArrayList<>();
		protected java.util.List<org.siani.legio.Project.Dependencies.Compile> compileList = new java.util.ArrayList<>();
		protected java.util.List<org.siani.legio.Project.Dependencies.Runtime> runtimeList = new java.util.ArrayList<>();
		protected java.util.List<org.siani.legio.Project.Dependencies.Test> testList = new java.util.ArrayList<>();

		public Dependencies(tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<org.siani.legio.Project.Dependencies.Dependency> dependencyList() {
			return java.util.Collections.unmodifiableList(dependencyList);
		}

		public org.siani.legio.Project.Dependencies.Dependency dependency(int index) {
			return dependencyList.get(index);
		}

		public java.util.List<org.siani.legio.Project.Dependencies.Dependency> dependencyList(java.util.function.Predicate<org.siani.legio.Project.Dependencies.Dependency> predicate) {
			return dependencyList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<org.siani.legio.Project.Dependencies.Compile> compileList() {
			return java.util.Collections.unmodifiableList(compileList);
		}

		public org.siani.legio.Project.Dependencies.Compile compile(int index) {
			return compileList.get(index);
		}

		public java.util.List<org.siani.legio.Project.Dependencies.Compile> compileList(java.util.function.Predicate<org.siani.legio.Project.Dependencies.Compile> predicate) {
			return compileList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<org.siani.legio.Project.Dependencies.Runtime> runtimeList() {
			return java.util.Collections.unmodifiableList(runtimeList);
		}

		public org.siani.legio.Project.Dependencies.Runtime runtime(int index) {
			return runtimeList.get(index);
		}

		public java.util.List<org.siani.legio.Project.Dependencies.Runtime> runtimeList(java.util.function.Predicate<org.siani.legio.Project.Dependencies.Runtime> predicate) {
			return runtimeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<org.siani.legio.Project.Dependencies.Test> testList() {
			return java.util.Collections.unmodifiableList(testList);
		}

		public org.siani.legio.Project.Dependencies.Test test(int index) {
			return testList.get(index);
		}

		public java.util.List<org.siani.legio.Project.Dependencies.Test> testList(java.util.function.Predicate<org.siani.legio.Project.Dependencies.Test> predicate) {
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
			return this.graph().concept(org.siani.legio.Project.Dependencies.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Dependencies$Dependency")) this.dependencyList.add(node.as(org.siani.legio.Project.Dependencies.Dependency.class));
			if (node.is("Project$Dependencies$Compile")) this.compileList.add(node.as(org.siani.legio.Project.Dependencies.Compile.class));
			if (node.is("Project$Dependencies$Runtime")) this.runtimeList.add(node.as(org.siani.legio.Project.Dependencies.Runtime.class));
			if (node.is("Project$Dependencies$Test")) this.testList.add(node.as(org.siani.legio.Project.Dependencies.Test.class));
		}

		@Override
	    protected void removeNode(tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Dependencies$Dependency")) this.dependencyList.remove(node.as(org.siani.legio.Project.Dependencies.Dependency.class));
	        if (node.is("Project$Dependencies$Compile")) this.compileList.remove(node.as(org.siani.legio.Project.Dependencies.Compile.class));
	        if (node.is("Project$Dependencies$Runtime")) this.runtimeList.remove(node.as(org.siani.legio.Project.Dependencies.Runtime.class));
	        if (node.is("Project$Dependencies$Test")) this.testList.remove(node.as(org.siani.legio.Project.Dependencies.Test.class));
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

			

			public org.siani.legio.Project.Dependencies.Compile compile(java.lang.String groupId, java.lang.String artifactId, java.lang.String version) {
			    org.siani.legio.Project.Dependencies.Compile newElement = graph().concept(org.siani.legio.Project.Dependencies.Compile.class).createNode(name, node()).as(org.siani.legio.Project.Dependencies.Compile.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}

			public org.siani.legio.Project.Dependencies.Runtime runtime(java.lang.String groupId, java.lang.String artifactId, java.lang.String version) {
			    org.siani.legio.Project.Dependencies.Runtime newElement = graph().concept(org.siani.legio.Project.Dependencies.Runtime.class).createNode(name, node()).as(org.siani.legio.Project.Dependencies.Runtime.class);
				newElement.node().set(newElement, "groupId", java.util.Collections.singletonList(groupId));
				newElement.node().set(newElement, "artifactId", java.util.Collections.singletonList(artifactId));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}

			public org.siani.legio.Project.Dependencies.Test test(java.lang.String groupId, java.lang.String artifactId, java.lang.String version) {
			    org.siani.legio.Project.Dependencies.Test newElement = graph().concept(org.siani.legio.Project.Dependencies.Test.class).createNode(name, node()).as(org.siani.legio.Project.Dependencies.Test.class);
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
				return this.graph().concept(org.siani.legio.Project.Dependencies.Dependency.class);
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
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Compile extends org.siani.legio.Project.Dependencies.Dependency implements tara.magritte.tags.Terminal {
			

			public Compile(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(org.siani.legio.Project.Dependencies.Compile.class);
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

			public class Create extends org.siani.legio.Project.Dependencies.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Runtime extends org.siani.legio.Project.Dependencies.Dependency implements tara.magritte.tags.Terminal {
			

			public Runtime(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(org.siani.legio.Project.Dependencies.Runtime.class);
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

			public class Create extends org.siani.legio.Project.Dependencies.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Test extends org.siani.legio.Project.Dependencies.Dependency implements tara.magritte.tags.Terminal {
			

			public Test(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(org.siani.legio.Project.Dependencies.Test.class);
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

			public class Create extends org.siani.legio.Project.Dependencies.Dependency.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		
		public org.siani.legio.LegioApplication application() {
			return ((org.siani.legio.LegioApplication) graph().application());
		}
	}
	
	public static class Factory extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		protected java.lang.String generationPackage;
		protected boolean persistent;
		protected int refactorId;
		protected org.siani.legio.Project.Factory.Modeling modeling;

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

		public org.siani.legio.Project.Factory.Modeling modeling() {
			return modeling;
		}

		public void modeling(org.siani.legio.Project.Factory.Modeling value) {
			this.modeling = value;
		}

		public org.siani.legio.platform.project.PlatformFactory asPlatform() {
			tara.magritte.Layer as = this.as(org.siani.legio.platform.project.PlatformFactory.class);
			return as != null ? (org.siani.legio.platform.project.PlatformFactory) as : addFacet(org.siani.legio.platform.project.PlatformFactory.class);
		}

		public boolean isPlatform() {
			return is(org.siani.legio.platform.project.PlatformFactory.class);
		}

		public org.siani.legio.level.project.LevelFactory asLevel() {
			tara.magritte.Layer as = this.as(org.siani.legio.level.project.LevelFactory.class);
			return as != null ? (org.siani.legio.level.project.LevelFactory) as : null;
		}

		public boolean isLevel() {
			return is(org.siani.legio.level.project.LevelFactory.class);
		}

		public org.siani.legio.application.project.ApplicationFactory asApplication() {
			tara.magritte.Layer as = this.as(org.siani.legio.application.project.ApplicationFactory.class);
			return as != null ? (org.siani.legio.application.project.ApplicationFactory) as : addFacet(org.siani.legio.application.project.ApplicationFactory.class);
		}

		public boolean isApplication() {
			return is(org.siani.legio.application.project.ApplicationFactory.class);
		}

		public org.siani.legio.system.project.SystemFactory asSystem() {
			tara.magritte.Layer as = this.as(org.siani.legio.system.project.SystemFactory.class);
			return as != null ? (org.siani.legio.system.project.SystemFactory) as : addFacet(org.siani.legio.system.project.SystemFactory.class);
		}

		public boolean isSystem() {
			return is(org.siani.legio.system.project.SystemFactory.class);
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
			return this.graph().concept(org.siani.legio.Project.Factory.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Factory$Modeling")) this.modeling = node.as(org.siani.legio.Project.Factory.Modeling.class);
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

			public org.siani.legio.Project.Factory.Modeling modeling(java.lang.String language, java.lang.String version) {
			    org.siani.legio.Project.Factory.Modeling newElement = graph().concept(org.siani.legio.Project.Factory.Modeling.class).createNode(name, node()).as(org.siani.legio.Project.Factory.Modeling.class);
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
				return this.graph().concept(org.siani.legio.Project.Factory.Modeling.class);
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
			
			public org.siani.legio.LegioApplication application() {
				return ((org.siani.legio.LegioApplication) graph().application());
			}
		}
		
		
		public org.siani.legio.LegioApplication application() {
			return ((org.siani.legio.LegioApplication) graph().application());
		}
	}
	
	public static class Build extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		

		public Build(tara.magritte.Node node) {
			super(node);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			return map;
		}

		public tara.magritte.Concept concept() {
			return this.graph().concept(org.siani.legio.Project.Build.class);
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
		
		public org.siani.legio.LegioApplication application() {
			return ((org.siani.legio.LegioApplication) graph().application());
		}
	}
	
	
	public org.siani.legio.LegioApplication application() {
		return ((org.siani.legio.LegioApplication) graph().application());
	}
}
