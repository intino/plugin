package io.intino.legio;

import io.intino.legio.*;


public class LifeCycle extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	
	protected io.intino.legio.LifeCycle.Package package$;
	protected io.intino.legio.LifeCycle.Distribution distribution;
	protected io.intino.legio.LifeCycle.QualityAnalytics qualityAnalytics;
	protected io.intino.legio.LifeCycle.Publishing publishing;

	public LifeCycle(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public io.intino.legio.LifeCycle.Package package$() {
		return package$;
	}

	public io.intino.legio.LifeCycle.Distribution distribution() {
		return distribution;
	}

	public io.intino.legio.LifeCycle.QualityAnalytics qualityAnalytics() {
		return qualityAnalytics;
	}

	public io.intino.legio.LifeCycle.Publishing publishing() {
		return publishing;
	}

	public void package$(io.intino.legio.LifeCycle.Package value) {
		this.package$ = value;
	}

	public void distribution(io.intino.legio.LifeCycle.Distribution value) {
		this.distribution = value;
	}

	public void qualityAnalytics(io.intino.legio.LifeCycle.QualityAnalytics value) {
		this.qualityAnalytics = value;
	}

	public void publishing(io.intino.legio.LifeCycle.Publishing value) {
		this.publishing = value;
	}

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		if (package$ != null) components.add(this.package$.node());
		if (distribution != null) components.add(this.distribution.node());
		if (qualityAnalytics != null) components.add(this.qualityAnalytics.node());
		if (publishing != null) components.add(this.publishing.node());
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.LifeCycle.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		super.addNode(node);
		if (node.is("LifeCycle$Package")) this.package$ = node.as(io.intino.legio.LifeCycle.Package.class);
		if (node.is("LifeCycle$Distribution")) this.distribution = node.as(io.intino.legio.LifeCycle.Distribution.class);
		if (node.is("LifeCycle$QualityAnalytics")) this.qualityAnalytics = node.as(io.intino.legio.LifeCycle.QualityAnalytics.class);
		if (node.is("LifeCycle$Publishing")) this.publishing = node.as(io.intino.legio.LifeCycle.Publishing.class);
	}

	@Override
    protected void removeNode(io.intino.tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("LifeCycle$Package")) this.package$ = null;
        if (node.is("LifeCycle$Distribution")) this.distribution = null;
        if (node.is("LifeCycle$QualityAnalytics")) this.qualityAnalytics = null;
        if (node.is("LifeCycle$Publishing")) this.publishing = null;
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

		public io.intino.legio.LifeCycle.Package package$(io.intino.legio.LifeCycle.Package.Type type) {
		    io.intino.legio.LifeCycle.Package newElement = graph().concept(io.intino.legio.LifeCycle.Package.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Package.class);
			newElement.node().set(newElement, "type", java.util.Collections.singletonList(type)); 
		    return newElement;
		}

		public io.intino.legio.LifeCycle.Distribution distribution() {
		    io.intino.legio.LifeCycle.Distribution newElement = graph().concept(io.intino.legio.LifeCycle.Distribution.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Distribution.class);
		    return newElement;
		}

		public io.intino.legio.LifeCycle.QualityAnalytics qualityAnalytics(java.lang.String serverUrl) {
		    io.intino.legio.LifeCycle.QualityAnalytics newElement = graph().concept(io.intino.legio.LifeCycle.QualityAnalytics.class).createNode(name, node()).as(io.intino.legio.LifeCycle.QualityAnalytics.class);
			newElement.node().set(newElement, "serverUrl", java.util.Collections.singletonList(serverUrl)); 
		    return newElement;
		}

		public io.intino.legio.LifeCycle.Publishing publishing(java.lang.String cesarURL) {
		    io.intino.legio.LifeCycle.Publishing newElement = graph().concept(io.intino.legio.LifeCycle.Publishing.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Publishing.class);
			newElement.node().set(newElement, "cesarURL", java.util.Collections.singletonList(cesarURL)); 
		    return newElement;
		}
		
	}
	
	public static class Package extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected Type type;

		public enum Type {
			ModulesAndLibrariesExtracted, LibrariesLinkedByManifest, ModulesAndLibrariesLinkedByManifest;
		}
		protected boolean attachSources;
		protected boolean attachDoc;
		protected boolean includeTests;
		protected java.lang.String classpathPrefix;
		protected java.lang.String finalName;
		protected java.util.List<io.intino.legio.LifeCycle.Package.MavenPlugin> mavenPluginList = new java.util.ArrayList<>();

		public Package(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public Type type() {
			return type;
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

		public void type(io.intino.legio.LifeCycle.Package.Type value) {
			this.type = value;
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

		public java.util.List<io.intino.legio.LifeCycle.Package.MavenPlugin> mavenPluginList() {
			return java.util.Collections.unmodifiableList(mavenPluginList);
		}

		public io.intino.legio.LifeCycle.Package.MavenPlugin mavenPlugin(int index) {
			return mavenPluginList.get(index);
		}

		public java.util.List<io.intino.legio.LifeCycle.Package.MavenPlugin> mavenPluginList(java.util.function.Predicate<io.intino.legio.LifeCycle.Package.MavenPlugin> predicate) {
			return mavenPluginList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		

		public io.intino.legio.runnable.lifecycle.RunnablePackage asRunnable() {
			return this.as(io.intino.legio.runnable.lifecycle.RunnablePackage.class);
		}

		public io.intino.legio.runnable.lifecycle.RunnablePackage asRunnable(java.lang.String mainClass) {
			io.intino.legio.runnable.lifecycle.RunnablePackage newElement = addFacet(io.intino.legio.runnable.lifecycle.RunnablePackage.class);
			newElement.node().set(newElement, "mainClass", java.util.Collections.singletonList(mainClass)); 
		    return newElement;
		}

		public boolean isRunnable() {
			return is(io.intino.legio.runnable.lifecycle.RunnablePackage.class);
		}

		public void removeRunnable() {
			this.removeFacet(io.intino.legio.runnable.lifecycle.RunnablePackage.class);
		}

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			mavenPluginList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("type", new java.util.ArrayList(java.util.Collections.singletonList(this.type)));
			map.put("attachSources", new java.util.ArrayList(java.util.Collections.singletonList(this.attachSources)));
			map.put("attachDoc", new java.util.ArrayList(java.util.Collections.singletonList(this.attachDoc)));
			map.put("includeTests", new java.util.ArrayList(java.util.Collections.singletonList(this.includeTests)));
			map.put("classpathPrefix", new java.util.ArrayList(java.util.Collections.singletonList(this.classpathPrefix)));
			map.put("finalName", new java.util.ArrayList(java.util.Collections.singletonList(this.finalName)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.LifeCycle.Package.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("LifeCycle$Package$MavenPlugin")) this.mavenPluginList.add(node.as(io.intino.legio.LifeCycle.Package.MavenPlugin.class));
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("LifeCycle$Package$MavenPlugin")) this.mavenPluginList.remove(node.as(io.intino.legio.LifeCycle.Package.MavenPlugin.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("type")) this.type = io.intino.tara.magritte.loaders.WordLoader.load(values, Type.class, this).get(0);
			else if (name.equalsIgnoreCase("attachSources")) this.attachSources = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("attachDoc")) this.attachDoc = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("includeTests")) this.includeTests = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("classpathPrefix")) this.classpathPrefix = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("finalName")) this.finalName = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("type")) this.type = (Type) values.get(0);
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

			public io.intino.legio.LifeCycle.Package.MavenPlugin mavenPlugin(java.lang.String code) {
			    io.intino.legio.LifeCycle.Package.MavenPlugin newElement = graph().concept(io.intino.legio.LifeCycle.Package.MavenPlugin.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Package.MavenPlugin.class);
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
				return this.graph().concept(io.intino.legio.LifeCycle.Package.MavenPlugin.class);
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
		
		protected io.intino.legio.Project.Repositories.Release release;
		protected io.intino.legio.Project.Repositories.Snapshot snapshot;
		protected io.intino.legio.Project.Repositories.Language language;
		protected io.intino.legio.LifeCycle.Distribution.Bitbucket bitbucket;

		public Distribution(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public io.intino.legio.Project.Repositories.Release release() {
			return release;
		}

		public io.intino.legio.Project.Repositories.Snapshot snapshot() {
			return snapshot;
		}

		public io.intino.legio.Project.Repositories.Language language() {
			return language;
		}

		public io.intino.legio.LifeCycle.Distribution.Bitbucket bitbucket() {
			return bitbucket;
		}

		public void release(io.intino.legio.Project.Repositories.Release value) {
			this.release = value;
		}

		public void snapshot(io.intino.legio.Project.Repositories.Snapshot value) {
			this.snapshot = value;
		}

		public void language(io.intino.legio.Project.Repositories.Language value) {
			this.language = value;
		}

		public void bitbucket(io.intino.legio.LifeCycle.Distribution.Bitbucket value) {
			this.bitbucket = value;
		}

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			if (release != null) components.add(this.release.node());
			if (snapshot != null) components.add(this.snapshot.node());
			if (language != null) components.add(this.language.node());
			if (bitbucket != null) components.add(this.bitbucket.node());
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.LifeCycle.Distribution.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Project$Repositories$Release")) this.release = node.as(io.intino.legio.Project.Repositories.Release.class);
			if (node.is("Project$Repositories$Snapshot")) this.snapshot = node.as(io.intino.legio.Project.Repositories.Snapshot.class);
			if (node.is("Project$Repositories$Language")) this.language = node.as(io.intino.legio.Project.Repositories.Language.class);
			if (node.is("LifeCycle$Distribution$Bitbucket")) this.bitbucket = node.as(io.intino.legio.LifeCycle.Distribution.Bitbucket.class);
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Project$Repositories$Release")) this.release = null;
	        if (node.is("Project$Repositories$Snapshot")) this.snapshot = null;
	        if (node.is("Project$Repositories$Language")) this.language = null;
	        if (node.is("LifeCycle$Distribution$Bitbucket")) this.bitbucket = null;
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

			public io.intino.legio.LifeCycle.Distribution.Bitbucket bitbucket(java.lang.String user, java.lang.String token) {
			    io.intino.legio.LifeCycle.Distribution.Bitbucket newElement = graph().concept(io.intino.legio.LifeCycle.Distribution.Bitbucket.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Distribution.Bitbucket.class);
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
				return this.graph().concept(io.intino.legio.LifeCycle.Distribution.Bitbucket.class);
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
		protected java.lang.String serverUrl;
		protected io.intino.legio.LifeCycle.QualityAnalytics.Authentication authentication;

		public QualityAnalytics(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String serverUrl() {
			return serverUrl;
		}

		public void serverUrl(java.lang.String value) {
			this.serverUrl = value;
		}

		public io.intino.legio.LifeCycle.QualityAnalytics.Authentication authentication() {
			return authentication;
		}

		public void authentication(io.intino.legio.LifeCycle.QualityAnalytics.Authentication value) {
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
			map.put("serverUrl", new java.util.ArrayList(java.util.Collections.singletonList(this.serverUrl)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.LifeCycle.QualityAnalytics.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("LifeCycle$QualityAnalytics$Authentication")) this.authentication = node.as(io.intino.legio.LifeCycle.QualityAnalytics.Authentication.class);
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("LifeCycle$QualityAnalytics$Authentication")) this.authentication = null;
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("serverUrl")) this.serverUrl = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("serverUrl")) this.serverUrl = (java.lang.String) values.get(0);
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

			public io.intino.legio.LifeCycle.QualityAnalytics.Authentication authentication(java.lang.String token) {
			    io.intino.legio.LifeCycle.QualityAnalytics.Authentication newElement = graph().concept(io.intino.legio.LifeCycle.QualityAnalytics.Authentication.class).createNode(name, node()).as(io.intino.legio.LifeCycle.QualityAnalytics.Authentication.class);
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
				return this.graph().concept(io.intino.legio.LifeCycle.QualityAnalytics.Authentication.class);
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
	
	public static class Publishing extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String cesarURL;
		protected int managementPort;
		protected java.util.List<io.intino.legio.LifeCycle.Publishing.Destination> destinationList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.LifeCycle.Publishing.PreDeploy> preDeployList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.LifeCycle.Publishing.Deploy> deployList = new java.util.ArrayList<>();

		public Publishing(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String cesarURL() {
			return cesarURL;
		}

		public int managementPort() {
			return managementPort;
		}

		public void cesarURL(java.lang.String value) {
			this.cesarURL = value;
		}

		public void managementPort(int value) {
			this.managementPort = value;
		}

		public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination> destinationList() {
			return java.util.Collections.unmodifiableList(destinationList);
		}

		public io.intino.legio.LifeCycle.Publishing.Destination destination(int index) {
			return destinationList.get(index);
		}

		public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination> destinationList(java.util.function.Predicate<io.intino.legio.LifeCycle.Publishing.Destination> predicate) {
			return destinationList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.LifeCycle.Publishing.PreDeploy> preDeployList() {
			return java.util.Collections.unmodifiableList(preDeployList);
		}

		public io.intino.legio.LifeCycle.Publishing.PreDeploy preDeploy(int index) {
			return preDeployList.get(index);
		}

		public java.util.List<io.intino.legio.LifeCycle.Publishing.PreDeploy> preDeployList(java.util.function.Predicate<io.intino.legio.LifeCycle.Publishing.PreDeploy> predicate) {
			return preDeployList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.LifeCycle.Publishing.Deploy> deployList() {
			return java.util.Collections.unmodifiableList(deployList);
		}

		public io.intino.legio.LifeCycle.Publishing.Deploy deploy(int index) {
			return deployList.get(index);
		}

		public java.util.List<io.intino.legio.LifeCycle.Publishing.Deploy> deployList(java.util.function.Predicate<io.intino.legio.LifeCycle.Publishing.Deploy> predicate) {
			return deployList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		

		

		

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			destinationList.stream().forEach(c -> components.add(c.node()));
			preDeployList.stream().forEach(c -> components.add(c.node()));
			deployList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("cesarURL", new java.util.ArrayList(java.util.Collections.singletonList(this.cesarURL)));
			map.put("managementPort", new java.util.ArrayList(java.util.Collections.singletonList(this.managementPort)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.LifeCycle.Publishing.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("LifeCycle$Publishing$Destination")) this.destinationList.add(node.as(io.intino.legio.LifeCycle.Publishing.Destination.class));
			if (node.is("LifeCycle$Publishing$PreDeploy")) this.preDeployList.add(node.as(io.intino.legio.LifeCycle.Publishing.PreDeploy.class));
			if (node.is("LifeCycle$Publishing$Deploy")) this.deployList.add(node.as(io.intino.legio.LifeCycle.Publishing.Deploy.class));
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("LifeCycle$Publishing$Destination")) this.destinationList.remove(node.as(io.intino.legio.LifeCycle.Publishing.Destination.class));
	        if (node.is("LifeCycle$Publishing$PreDeploy")) this.preDeployList.remove(node.as(io.intino.legio.LifeCycle.Publishing.PreDeploy.class));
	        if (node.is("LifeCycle$Publishing$Deploy")) this.deployList.remove(node.as(io.intino.legio.LifeCycle.Publishing.Deploy.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("cesarURL")) this.cesarURL = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("managementPort")) this.managementPort = io.intino.tara.magritte.loaders.IntegerLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("cesarURL")) this.cesarURL = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("managementPort")) this.managementPort = (java.lang.Integer) values.get(0);
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

			

			public io.intino.legio.LifeCycle.Publishing.PreDeploy preDeploy(java.lang.String publicURL) {
			    io.intino.legio.LifeCycle.Publishing.PreDeploy newElement = graph().concept(io.intino.legio.LifeCycle.Publishing.PreDeploy.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Publishing.PreDeploy.class);
				newElement.node().set(newElement, "publicURL", java.util.Collections.singletonList(publicURL)); 
			    return newElement;
			}

			public io.intino.legio.LifeCycle.Publishing.Deploy deploy(java.lang.String publicURL) {
			    io.intino.legio.LifeCycle.Publishing.Deploy newElement = graph().concept(io.intino.legio.LifeCycle.Publishing.Deploy.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Publishing.Deploy.class);
				newElement.node().set(newElement, "publicURL", java.util.Collections.singletonList(publicURL)); 
			    return newElement;
			}
			
		}
		
		public static abstract class Destination extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String publicURL;
			protected java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Requirements> requirementsList = new java.util.ArrayList<>();
			protected java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Configuration> configurationList = new java.util.ArrayList<>();

			public Destination(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String publicURL() {
				return publicURL;
			}

			public void publicURL(java.lang.String value) {
				this.publicURL = value;
			}

			public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Requirements> requirementsList() {
				return java.util.Collections.unmodifiableList(requirementsList);
			}

			public io.intino.legio.LifeCycle.Publishing.Destination.Requirements requirements(int index) {
				return requirementsList.get(index);
			}

			public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Requirements> requirementsList(java.util.function.Predicate<io.intino.legio.LifeCycle.Publishing.Destination.Requirements> predicate) {
				return requirementsList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}

			public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Configuration> configurationList() {
				return java.util.Collections.unmodifiableList(configurationList);
			}

			public io.intino.legio.LifeCycle.Publishing.Destination.Configuration configuration(int index) {
				return configurationList.get(index);
			}

			public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Configuration> configurationList(java.util.function.Predicate<io.intino.legio.LifeCycle.Publishing.Destination.Configuration> predicate) {
				return configurationList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
			}

			

			

			public java.util.List<io.intino.tara.magritte.Node> componentList() {
				java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
				requirementsList.stream().forEach(c -> components.add(c.node()));
				configurationList.stream().forEach(c -> components.add(c.node()));
				return new java.util.ArrayList<>(components);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("publicURL", new java.util.ArrayList(java.util.Collections.singletonList(this.publicURL)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.LifeCycle.Publishing.Destination.class);
			}

			@Override
			protected void addNode(io.intino.tara.magritte.Node node) {
				super.addNode(node);
				if (node.is("LifeCycle$Publishing$Destination$Requirements")) this.requirementsList.add(node.as(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.class));
				if (node.is("LifeCycle$Publishing$Destination$Configuration")) this.configurationList.add(node.as(io.intino.legio.LifeCycle.Publishing.Destination.Configuration.class));
			}

			@Override
		    protected void removeNode(io.intino.tara.magritte.Node node) {
		        super.removeNode(node);
		        if (node.is("LifeCycle$Publishing$Destination$Requirements")) this.requirementsList.remove(node.as(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.class));
		        if (node.is("LifeCycle$Publishing$Destination$Configuration")) this.configurationList.remove(node.as(io.intino.legio.LifeCycle.Publishing.Destination.Configuration.class));
		    }

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("publicURL")) this.publicURL = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("publicURL")) this.publicURL = (java.lang.String) values.get(0);
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

				public io.intino.legio.LifeCycle.Publishing.Destination.Requirements requirements() {
				    io.intino.legio.LifeCycle.Publishing.Destination.Requirements newElement = graph().concept(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.class);
				    return newElement;
				}

				public io.intino.legio.LifeCycle.Publishing.Destination.Configuration configuration() {
				    io.intino.legio.LifeCycle.Publishing.Destination.Configuration newElement = graph().concept(io.intino.legio.LifeCycle.Publishing.Destination.Configuration.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Publishing.Destination.Configuration.class);
				    return newElement;
				}
				
			}
			
			public static class Requirements extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
				
				protected java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory> memoryList = new java.util.ArrayList<>();
				protected java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU> cPUList = new java.util.ArrayList<>();

				public Requirements(io.intino.tara.magritte.Node node) {
					super(node);
				}

				public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory> memoryList() {
					return java.util.Collections.unmodifiableList(memoryList);
				}

				public io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory memory(int index) {
					return memoryList.get(index);
				}

				public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory> memoryList(java.util.function.Predicate<io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory> predicate) {
					return memoryList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
				}

				public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU> cPUList() {
					return java.util.Collections.unmodifiableList(cPUList);
				}

				public io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU cPU(int index) {
					return cPUList.get(index);
				}

				public java.util.List<io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU> cPUList(java.util.function.Predicate<io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU> predicate) {
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
					return this.graph().concept(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.class);
				}

				@Override
				protected void addNode(io.intino.tara.magritte.Node node) {
					super.addNode(node);
					if (node.is("LifeCycle$Publishing$Destination$Requirements$Memory")) this.memoryList.add(node.as(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory.class));
					if (node.is("LifeCycle$Publishing$Destination$Requirements$CPU")) this.cPUList.add(node.as(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU.class));
				}

				@Override
			    protected void removeNode(io.intino.tara.magritte.Node node) {
			        super.removeNode(node);
			        if (node.is("LifeCycle$Publishing$Destination$Requirements$Memory")) this.memoryList.remove(node.as(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory.class));
			        if (node.is("LifeCycle$Publishing$Destination$Requirements$CPU")) this.cPUList.remove(node.as(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU.class));
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

					public io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory memory() {
					    io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory newElement = graph().concept(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory.class);
					    return newElement;
					}

					public io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU cPU() {
					    io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU newElement = graph().concept(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU.class);
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
						return this.graph().concept(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.Memory.class);
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
						return this.graph().concept(io.intino.legio.LifeCycle.Publishing.Destination.Requirements.CPU.class);
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
				
				protected java.util.List<io.intino.legio.Parameter> parameterList = new java.util.ArrayList<>();

				public Configuration(io.intino.tara.magritte.Node node) {
					super(node);
				}

				public java.util.List<io.intino.legio.Parameter> parameterList() {
					return java.util.Collections.unmodifiableList(parameterList);
				}

				public io.intino.legio.Parameter parameter(int index) {
					return parameterList.get(index);
				}

				public java.util.List<io.intino.legio.Parameter> parameterList(java.util.function.Predicate<io.intino.legio.Parameter> predicate) {
					return parameterList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
				}

				

				public java.util.List<io.intino.tara.magritte.Node> componentList() {
					java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
					parameterList.stream().forEach(c -> components.add(c.node()));
					return new java.util.ArrayList<>(components);
				}

				@Override
				public java.util.Map<java.lang.String, java.util.List<?>> variables() {
					java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
					return map;
				}

				public io.intino.tara.magritte.Concept concept() {
					return this.graph().concept(io.intino.legio.LifeCycle.Publishing.Destination.Configuration.class);
				}

				@Override
				protected void addNode(io.intino.tara.magritte.Node node) {
					super.addNode(node);
					if (node.is("Parameter")) this.parameterList.add(node.as(io.intino.legio.Parameter.class));
				}

				@Override
			    protected void removeNode(io.intino.tara.magritte.Node node) {
			        super.removeNode(node);
			        if (node.is("Parameter")) this.parameterList.remove(node.as(io.intino.legio.Parameter.class));
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

					public io.intino.legio.Parameter parameter() {
					    io.intino.legio.Parameter newElement = graph().concept(io.intino.legio.Parameter.class).createNode(name, node()).as(io.intino.legio.Parameter.class);
					    return newElement;
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
		
		public static class PreDeploy extends io.intino.legio.LifeCycle.Publishing.Destination implements io.intino.tara.magritte.tags.Terminal {
			

			public PreDeploy(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.LifeCycle.Publishing.PreDeploy.class);
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

			public class Create extends io.intino.legio.LifeCycle.Publishing.Destination.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.Legio legioWrapper() {
				return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
			}
		}
		
		public static class Deploy extends io.intino.legio.LifeCycle.Publishing.Destination implements io.intino.tara.magritte.tags.Terminal {
			

			public Deploy(io.intino.tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.LifeCycle.Publishing.Deploy.class);
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

			public class Create extends io.intino.legio.LifeCycle.Publishing.Destination.Create {
				

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
	
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
