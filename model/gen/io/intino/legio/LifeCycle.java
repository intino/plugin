package io.intino.legio;

import io.intino.legio.*;


public class LifeCycle extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
	
	protected io.intino.legio.LifeCycle.Package package$;
	protected io.intino.legio.LifeCycle.Distribution distribution;
	protected io.intino.legio.LifeCycle.QualityAnalytics qualityAnalytics;
	protected io.intino.legio.LifeCycle.Deployment deployment;

	public LifeCycle(tara.magritte.Node node) {
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

	public io.intino.legio.LifeCycle.Deployment deployment() {
		return deployment;
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

	public void deployment(io.intino.legio.LifeCycle.Deployment value) {
		this.deployment = value;
	}

	public java.util.List<tara.magritte.Node> componentList() {
		java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		if (package$ != null) components.add(this.package$.node());
		if (distribution != null) components.add(this.distribution.node());
		if (qualityAnalytics != null) components.add(this.qualityAnalytics.node());
		if (deployment != null) components.add(this.deployment.node());
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		return map;
	}

	public tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.LifeCycle.class);
	}

	@Override
	protected void addNode(tara.magritte.Node node) {
		super.addNode(node);
		if (node.is("LifeCycle$Package")) this.package$ = node.as(io.intino.legio.LifeCycle.Package.class);
		if (node.is("LifeCycle$Distribution")) this.distribution = node.as(io.intino.legio.LifeCycle.Distribution.class);
		if (node.is("LifeCycle$QualityAnalytics")) this.qualityAnalytics = node.as(io.intino.legio.LifeCycle.QualityAnalytics.class);
		if (node.is("LifeCycle$Deployment")) this.deployment = node.as(io.intino.legio.LifeCycle.Deployment.class);
	}

	@Override
    protected void removeNode(tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("LifeCycle$Package")) this.package$ = null;
        if (node.is("LifeCycle$Distribution")) this.distribution = null;
        if (node.is("LifeCycle$QualityAnalytics")) this.qualityAnalytics = null;
        if (node.is("LifeCycle$Deployment")) this.deployment = null;
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

		public io.intino.legio.LifeCycle.Distribution distribution(java.lang.String url, java.lang.String mavenId) {
		    io.intino.legio.LifeCycle.Distribution newElement = graph().concept(io.intino.legio.LifeCycle.Distribution.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Distribution.class);
			newElement.node().set(newElement, "url", java.util.Collections.singletonList(url));
			newElement.node().set(newElement, "mavenId", java.util.Collections.singletonList(mavenId)); 
		    return newElement;
		}

		public io.intino.legio.LifeCycle.QualityAnalytics qualityAnalytics(java.lang.String serverUrl) {
		    io.intino.legio.LifeCycle.QualityAnalytics newElement = graph().concept(io.intino.legio.LifeCycle.QualityAnalytics.class).createNode(name, node()).as(io.intino.legio.LifeCycle.QualityAnalytics.class);
			newElement.node().set(newElement, "serverUrl", java.util.Collections.singletonList(serverUrl)); 
		    return newElement;
		}

		public io.intino.legio.LifeCycle.Deployment deployment(java.lang.String cesarURL) {
		    io.intino.legio.LifeCycle.Deployment newElement = graph().concept(io.intino.legio.LifeCycle.Deployment.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Deployment.class);
			newElement.node().set(newElement, "cesarURL", java.util.Collections.singletonList(cesarURL)); 
		    return newElement;
		}
		
	}
	
	public static class Package extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		protected Type type;

		public enum Type {
			ModulesAndLibrariesExtracted, LibrariesLinkedByManifest, ModulesAndLibrariesLinkedByManifest;
		}
		protected boolean attachSources;
		protected boolean attachDoc;
		protected boolean includeTests;
		protected java.lang.String mainClass;
		protected java.lang.String classpathPrefix;
		protected java.lang.String finalName;
		protected java.util.List<io.intino.legio.LifeCycle.Package.MavenPlugin> mavenPluginList = new java.util.ArrayList<>();

		public Package(tara.magritte.Node node) {
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

		public java.lang.String mainClass() {
			return mainClass;
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

		public void mainClass(java.lang.String value) {
			this.mainClass = value;
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

		

		public java.util.List<tara.magritte.Node> componentList() {
			java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
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
			map.put("mainClass", new java.util.ArrayList(java.util.Collections.singletonList(this.mainClass)));
			map.put("classpathPrefix", new java.util.ArrayList(java.util.Collections.singletonList(this.classpathPrefix)));
			map.put("finalName", new java.util.ArrayList(java.util.Collections.singletonList(this.finalName)));
			return map;
		}

		public tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.LifeCycle.Package.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("LifeCycle$Package$MavenPlugin")) this.mavenPluginList.add(node.as(io.intino.legio.LifeCycle.Package.MavenPlugin.class));
		}

		@Override
	    protected void removeNode(tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("LifeCycle$Package$MavenPlugin")) this.mavenPluginList.remove(node.as(io.intino.legio.LifeCycle.Package.MavenPlugin.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("type")) this.type = tara.magritte.loaders.WordLoader.load(values, Type.class, this).get(0);
			else if (name.equalsIgnoreCase("attachSources")) this.attachSources = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("attachDoc")) this.attachDoc = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("includeTests")) this.includeTests = tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("mainClass")) this.mainClass = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("classpathPrefix")) this.classpathPrefix = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("finalName")) this.finalName = tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("type")) this.type = (Type) values.get(0);
			else if (name.equalsIgnoreCase("attachSources")) this.attachSources = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("attachDoc")) this.attachDoc = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("includeTests")) this.includeTests = (java.lang.Boolean) values.get(0);
			else if (name.equalsIgnoreCase("mainClass")) this.mainClass = (java.lang.String) values.get(0);
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
		
		public static class MavenPlugin extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
			protected java.lang.String code;

			public MavenPlugin(tara.magritte.Node node) {
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

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.LifeCycle.Package.MavenPlugin.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("code")) this.code = tara.magritte.loaders.StringLoader.load(values, this).get(0);
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
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
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
			return this.graph().concept(io.intino.legio.LifeCycle.Distribution.class);
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
	
	public static class QualityAnalytics extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		protected java.lang.String serverUrl;
		protected io.intino.legio.LifeCycle.QualityAnalytics.Authentication authentication;

		public QualityAnalytics(tara.magritte.Node node) {
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

		public java.util.List<tara.magritte.Node> componentList() {
			java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			if (authentication != null) components.add(this.authentication.node());
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("serverUrl", new java.util.ArrayList(java.util.Collections.singletonList(this.serverUrl)));
			return map;
		}

		public tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.LifeCycle.QualityAnalytics.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("LifeCycle$QualityAnalytics$Authentication")) this.authentication = node.as(io.intino.legio.LifeCycle.QualityAnalytics.Authentication.class);
		}

		@Override
	    protected void removeNode(tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("LifeCycle$QualityAnalytics$Authentication")) this.authentication = null;
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("serverUrl")) this.serverUrl = tara.magritte.loaders.StringLoader.load(values, this).get(0);
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
		
		public static class Authentication extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
			protected java.lang.String token;

			public Authentication(tara.magritte.Node node) {
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

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.LifeCycle.QualityAnalytics.Authentication.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("token")) this.token = tara.magritte.loaders.StringLoader.load(values, this).get(0);
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
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		
		public io.intino.legio.LegioApplication application() {
			return ((io.intino.legio.LegioApplication) graph().application());
		}
	}
	
	public static class Deployment extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
		protected java.lang.String cesarURL;
		protected java.util.List<io.intino.legio.LifeCycle.Deployment.Integration> integrationList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.LifeCycle.Deployment.PreDeploy> preDeployList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.LifeCycle.Deployment.Deploy> deployList = new java.util.ArrayList<>();

		public Deployment(tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String cesarURL() {
			return cesarURL;
		}

		public void cesarURL(java.lang.String value) {
			this.cesarURL = value;
		}

		public java.util.List<io.intino.legio.LifeCycle.Deployment.Integration> integrationList() {
			return java.util.Collections.unmodifiableList(integrationList);
		}

		public io.intino.legio.LifeCycle.Deployment.Integration integration(int index) {
			return integrationList.get(index);
		}

		public java.util.List<io.intino.legio.LifeCycle.Deployment.Integration> integrationList(java.util.function.Predicate<io.intino.legio.LifeCycle.Deployment.Integration> predicate) {
			return integrationList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.LifeCycle.Deployment.PreDeploy> preDeployList() {
			return java.util.Collections.unmodifiableList(preDeployList);
		}

		public io.intino.legio.LifeCycle.Deployment.PreDeploy preDeploy(int index) {
			return preDeployList.get(index);
		}

		public java.util.List<io.intino.legio.LifeCycle.Deployment.PreDeploy> preDeployList(java.util.function.Predicate<io.intino.legio.LifeCycle.Deployment.PreDeploy> predicate) {
			return preDeployList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.LifeCycle.Deployment.Deploy> deployList() {
			return java.util.Collections.unmodifiableList(deployList);
		}

		public io.intino.legio.LifeCycle.Deployment.Deploy deploy(int index) {
			return deployList.get(index);
		}

		public java.util.List<io.intino.legio.LifeCycle.Deployment.Deploy> deployList(java.util.function.Predicate<io.intino.legio.LifeCycle.Deployment.Deploy> predicate) {
			return deployList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		

		

		

		public java.util.List<tara.magritte.Node> componentList() {
			java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			integrationList.stream().forEach(c -> components.add(c.node()));
			preDeployList.stream().forEach(c -> components.add(c.node()));
			deployList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("cesarURL", new java.util.ArrayList(java.util.Collections.singletonList(this.cesarURL)));
			return map;
		}

		public tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.LifeCycle.Deployment.class);
		}

		@Override
		protected void addNode(tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("LifeCycle$Deployment$Integration")) this.integrationList.add(node.as(io.intino.legio.LifeCycle.Deployment.Integration.class));
			if (node.is("LifeCycle$Deployment$PreDeploy")) this.preDeployList.add(node.as(io.intino.legio.LifeCycle.Deployment.PreDeploy.class));
			if (node.is("LifeCycle$Deployment$Deploy")) this.deployList.add(node.as(io.intino.legio.LifeCycle.Deployment.Deploy.class));
		}

		@Override
	    protected void removeNode(tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("LifeCycle$Deployment$Integration")) this.integrationList.remove(node.as(io.intino.legio.LifeCycle.Deployment.Integration.class));
	        if (node.is("LifeCycle$Deployment$PreDeploy")) this.preDeployList.remove(node.as(io.intino.legio.LifeCycle.Deployment.PreDeploy.class));
	        if (node.is("LifeCycle$Deployment$Deploy")) this.deployList.remove(node.as(io.intino.legio.LifeCycle.Deployment.Deploy.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("cesarURL")) this.cesarURL = tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("cesarURL")) this.cesarURL = (java.lang.String) values.get(0);
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

			

			public io.intino.legio.LifeCycle.Deployment.PreDeploy preDeploy(java.lang.String server) {
			    io.intino.legio.LifeCycle.Deployment.PreDeploy newElement = graph().concept(io.intino.legio.LifeCycle.Deployment.PreDeploy.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Deployment.PreDeploy.class);
				newElement.node().set(newElement, "server", java.util.Collections.singletonList(server)); 
			    return newElement;
			}

			public io.intino.legio.LifeCycle.Deployment.Deploy deploy(java.lang.String server) {
			    io.intino.legio.LifeCycle.Deployment.Deploy newElement = graph().concept(io.intino.legio.LifeCycle.Deployment.Deploy.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Deployment.Deploy.class);
				newElement.node().set(newElement, "server", java.util.Collections.singletonList(server)); 
			    return newElement;
			}
			
		}
		
		public static abstract class Integration extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
			protected java.lang.String server;
			protected java.util.List<io.intino.legio.LifeCycle.Deployment.Integration.Requirements> requirementsList = new java.util.ArrayList<>();
			protected java.util.List<io.intino.legio.Parameter> parameterList = new java.util.ArrayList<>();

			public Integration(tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String server() {
				return server;
			}

			public void server(java.lang.String value) {
				this.server = value;
			}

			public java.util.List<io.intino.legio.LifeCycle.Deployment.Integration.Requirements> requirementsList() {
				return java.util.Collections.unmodifiableList(requirementsList);
			}

			public io.intino.legio.LifeCycle.Deployment.Integration.Requirements requirements(int index) {
				return requirementsList.get(index);
			}

			public java.util.List<io.intino.legio.LifeCycle.Deployment.Integration.Requirements> requirementsList(java.util.function.Predicate<io.intino.legio.LifeCycle.Deployment.Integration.Requirements> predicate) {
				return requirementsList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
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

			

			

			public java.util.List<tara.magritte.Node> componentList() {
				java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
				requirementsList.stream().forEach(c -> components.add(c.node()));
				parameterList.stream().forEach(c -> components.add(c.node()));
				return new java.util.ArrayList<>(components);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("server", new java.util.ArrayList(java.util.Collections.singletonList(this.server)));
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.LifeCycle.Deployment.Integration.class);
			}

			@Override
			protected void addNode(tara.magritte.Node node) {
				super.addNode(node);
				if (node.is("LifeCycle$Deployment$Integration$Requirements")) this.requirementsList.add(node.as(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.class));
				if (node.is("Parameter")) this.parameterList.add(node.as(io.intino.legio.Parameter.class));
			}

			@Override
		    protected void removeNode(tara.magritte.Node node) {
		        super.removeNode(node);
		        if (node.is("LifeCycle$Deployment$Integration$Requirements")) this.requirementsList.remove(node.as(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.class));
		        if (node.is("Parameter")) this.parameterList.remove(node.as(io.intino.legio.Parameter.class));
		    }

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("server")) this.server = tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("server")) this.server = (java.lang.String) values.get(0);
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

				public io.intino.legio.LifeCycle.Deployment.Integration.Requirements requirements() {
				    io.intino.legio.LifeCycle.Deployment.Integration.Requirements newElement = graph().concept(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.class);
				    return newElement;
				}

				public io.intino.legio.Parameter parameter() {
				    io.intino.legio.Parameter newElement = graph().concept(io.intino.legio.Parameter.class).createNode(name, node()).as(io.intino.legio.Parameter.class);
				    return newElement;
				}
				
			}
			
			public static class Requirements extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
				
				protected java.util.List<io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory> memoryList = new java.util.ArrayList<>();
				protected java.util.List<io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU> cPUList = new java.util.ArrayList<>();

				public Requirements(tara.magritte.Node node) {
					super(node);
				}

				public java.util.List<io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory> memoryList() {
					return java.util.Collections.unmodifiableList(memoryList);
				}

				public io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory memory(int index) {
					return memoryList.get(index);
				}

				public java.util.List<io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory> memoryList(java.util.function.Predicate<io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory> predicate) {
					return memoryList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
				}

				public java.util.List<io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU> cPUList() {
					return java.util.Collections.unmodifiableList(cPUList);
				}

				public io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU cPU(int index) {
					return cPUList.get(index);
				}

				public java.util.List<io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU> cPUList(java.util.function.Predicate<io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU> predicate) {
					return cPUList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
				}

				

				

				public java.util.List<tara.magritte.Node> componentList() {
					java.util.Set<tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
					memoryList.stream().forEach(c -> components.add(c.node()));
					cPUList.stream().forEach(c -> components.add(c.node()));
					return new java.util.ArrayList<>(components);
				}

				@Override
				public java.util.Map<java.lang.String, java.util.List<?>> variables() {
					java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
					return map;
				}

				public tara.magritte.Concept concept() {
					return this.graph().concept(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.class);
				}

				@Override
				protected void addNode(tara.magritte.Node node) {
					super.addNode(node);
					if (node.is("LifeCycle$Deployment$Integration$Requirements$Memory")) this.memoryList.add(node.as(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory.class));
					if (node.is("LifeCycle$Deployment$Integration$Requirements$CPU")) this.cPUList.add(node.as(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU.class));
				}

				@Override
			    protected void removeNode(tara.magritte.Node node) {
			        super.removeNode(node);
			        if (node.is("LifeCycle$Deployment$Integration$Requirements$Memory")) this.memoryList.remove(node.as(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory.class));
			        if (node.is("LifeCycle$Deployment$Integration$Requirements$CPU")) this.cPUList.remove(node.as(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU.class));
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

					public io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory memory() {
					    io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory newElement = graph().concept(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory.class);
					    return newElement;
					}

					public io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU cPU() {
					    io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU newElement = graph().concept(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU.class).createNode(name, node()).as(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU.class);
					    return newElement;
					}
					
				}
				
				public static class Memory extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
					

					public Memory(tara.magritte.Node node) {
						super(node);
					}

					@Override
					public java.util.Map<java.lang.String, java.util.List<?>> variables() {
						java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
						return map;
					}

					public tara.magritte.Concept concept() {
						return this.graph().concept(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.Memory.class);
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
					
					public io.intino.legio.LegioApplication application() {
						return ((io.intino.legio.LegioApplication) graph().application());
					}
				}
				
				public static class CPU extends tara.magritte.Layer implements tara.magritte.tags.Terminal {
					

					public CPU(tara.magritte.Node node) {
						super(node);
					}

					@Override
					public java.util.Map<java.lang.String, java.util.List<?>> variables() {
						java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
						return map;
					}

					public tara.magritte.Concept concept() {
						return this.graph().concept(io.intino.legio.LifeCycle.Deployment.Integration.Requirements.CPU.class);
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
		
		public static class PreDeploy extends io.intino.legio.LifeCycle.Deployment.Integration implements tara.magritte.tags.Terminal {
			

			public PreDeploy(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.LifeCycle.Deployment.PreDeploy.class);
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

			public class Create extends io.intino.legio.LifeCycle.Deployment.Integration.Create {
				

				public Create(java.lang.String name) {
					super(name);
				}
				
			}
			
			public io.intino.legio.LegioApplication application() {
				return ((io.intino.legio.LegioApplication) graph().application());
			}
		}
		
		public static class Deploy extends io.intino.legio.LifeCycle.Deployment.Integration implements tara.magritte.tags.Terminal {
			

			public Deploy(tara.magritte.Node node) {
				super(node);
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
				return map;
			}

			public tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.LifeCycle.Deployment.Deploy.class);
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

			public class Create extends io.intino.legio.LifeCycle.Deployment.Integration.Create {
				

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
	
	
	public io.intino.legio.LegioApplication application() {
		return ((io.intino.legio.LegioApplication) graph().application());
	}
}
