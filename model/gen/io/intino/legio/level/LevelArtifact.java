package io.intino.legio.level;

import io.intino.legio.*;


public abstract class LevelArtifact extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	
	protected io.intino.legio.level.LevelArtifact.Modeling modeling;
	
	
	
	
	
	
	
	
	
	
	protected io.intino.legio.Artifact _artifact;

	public LevelArtifact(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String groupId() {
		return _artifact.groupId();
	}

	public java.lang.String version() {
		return _artifact.version();
	}

	public void groupId(java.lang.String value) {
		this._artifact.groupId(value);
	}

	public void version(java.lang.String value) {
		this._artifact.version(value);
	}

	public io.intino.legio.level.LevelArtifact.Modeling modeling() {
		return modeling;
	}

	public io.intino.legio.Artifact.License license() {
		return _artifact.license();
	}

	public io.intino.legio.Artifact.Imports imports() {
		return _artifact.imports();
	}

	public io.intino.legio.Artifact.WebImports webImports() {
		return _artifact.webImports();
	}

	public io.intino.legio.Artifact.Generation generation() {
		return _artifact.generation();
	}

	public io.intino.legio.Artifact.Boxing boxing() {
		return _artifact.boxing();
	}

	public java.util.List<io.intino.legio.Artifact.Exports> exportsList() {
		return (java.util.List<io.intino.legio.Artifact.Exports>) _artifact.exportsList();
	}

	public io.intino.legio.Artifact.Exports exportsList(int index) {
		return _artifact.exportsList().get(index);
	}

	public io.intino.legio.Artifact.Pack pack() {
		return _artifact.pack();
	}

	public io.intino.legio.Artifact.Distribution distribution() {
		return _artifact.distribution();
	}

	public io.intino.legio.Artifact.QualityAnalytics qualityAnalytics() {
		return _artifact.qualityAnalytics();
	}

	public java.util.List<io.intino.legio.Artifact.Deployment> deploymentList() {
		return (java.util.List<io.intino.legio.Artifact.Deployment>) _artifact.deploymentList();
	}

	public io.intino.legio.Artifact.Deployment deploymentList(int index) {
		return _artifact.deploymentList().get(index);
	}

	public void modeling(io.intino.legio.level.LevelArtifact.Modeling value) {
		this.modeling = value;
	}

	

	

	

	

	

	

	

	

	

	

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		if (modeling != null) components.add(this.modeling.node());
		
		
		
		
		
		
		
		
		
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Artifact.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		super.addNode(node);
		if (node.is("Level#Artifact$Modeling")) this.modeling = node.as(io.intino.legio.level.LevelArtifact.Modeling.class);
	}

	@Override
    protected void removeNode(io.intino.tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("Level#Artifact$Modeling")) this.modeling = null;
    }

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
	}

	@Override
	protected void _sync(io.intino.tara.magritte.Layer layer) {
		super._sync(layer);
	    if (layer instanceof io.intino.legio.Artifact) _artifact = (io.intino.legio.Artifact) layer;
	    
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

		public io.intino.legio.level.LevelArtifact.Modeling modeling(java.lang.String version) {
		    io.intino.legio.level.LevelArtifact.Modeling newElement = graph().concept(io.intino.legio.level.LevelArtifact.Modeling.class).createNode(name, node()).as(io.intino.legio.level.LevelArtifact.Modeling.class);
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
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

		public io.intino.legio.Artifact.Generation generation(java.lang.String targetPackage) {
		    io.intino.legio.Artifact.Generation newElement = graph().concept(io.intino.legio.Artifact.Generation.class).createNode(name, node()).as(io.intino.legio.Artifact.Generation.class);
			newElement.node().set(newElement, "targetPackage", java.util.Collections.singletonList(targetPackage)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Boxing boxing(java.lang.String language, java.lang.String version) {
		    io.intino.legio.Artifact.Boxing newElement = graph().concept(io.intino.legio.Artifact.Boxing.class).createNode(name, node()).as(io.intino.legio.Artifact.Boxing.class);
			newElement.node().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Exports exports() {
		    io.intino.legio.Artifact.Exports newElement = graph().concept(io.intino.legio.Artifact.Exports.class).createNode(name, node()).as(io.intino.legio.Artifact.Exports.class);
		    return newElement;
		}

		public io.intino.legio.Artifact.Pack pack(io.intino.legio.Artifact.Pack.Mode mode) {
		    io.intino.legio.Artifact.Pack newElement = graph().concept(io.intino.legio.Artifact.Pack.class).createNode(name, node()).as(io.intino.legio.Artifact.Pack.class);
			newElement.node().set(newElement, "mode", java.util.Collections.singletonList(mode)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Distribution distribution(java.lang.String mavenId) {
		    io.intino.legio.Artifact.Distribution newElement = graph().concept(io.intino.legio.Artifact.Distribution.class).createNode(name, node()).as(io.intino.legio.Artifact.Distribution.class);
			newElement.node().set(newElement, "mavenId", java.util.Collections.singletonList(mavenId)); 
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
	
	public static class Modeling extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String version;
		protected java.util.List<io.intino.legio.level.LevelArtifact.Modeling.Language> languageList = new java.util.ArrayList<>();

		public Modeling(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String version() {
			return version;
		}

		public void version(java.lang.String value) {
			this.version = value;
		}

		public java.util.List<io.intino.legio.level.LevelArtifact.Modeling.Language> languageList() {
			return java.util.Collections.unmodifiableList(languageList);
		}

		public io.intino.legio.level.LevelArtifact.Modeling.Language language(int index) {
			return languageList.get(index);
		}

		public java.util.List<io.intino.legio.level.LevelArtifact.Modeling.Language> languageList(java.util.function.Predicate<io.intino.legio.level.LevelArtifact.Modeling.Language> predicate) {
			return languageList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		

		public java.util.List<io.intino.tara.magritte.Node> componentList() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
			languageList.stream().forEach(c -> components.add(c.node()));
			return new java.util.ArrayList<>(components);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.level.LevelArtifact.Modeling.class);
		}

		@Override
		protected void addNode(io.intino.tara.magritte.Node node) {
			super.addNode(node);
			if (node.is("Level#Artifact$Modeling$Language")) this.languageList.add(node.as(io.intino.legio.level.LevelArtifact.Modeling.Language.class));
		}

		@Override
	    protected void removeNode(io.intino.tara.magritte.Node node) {
	        super.removeNode(node);
	        if (node.is("Level#Artifact$Modeling$Language")) this.languageList.remove(node.as(io.intino.legio.level.LevelArtifact.Modeling.Language.class));
	    }

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
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

			public io.intino.legio.level.LevelArtifact.Modeling.Language language(java.lang.String name$, java.lang.String version) {
			    io.intino.legio.level.LevelArtifact.Modeling.Language newElement = graph().concept(io.intino.legio.level.LevelArtifact.Modeling.Language.class).createNode(name, node()).as(io.intino.legio.level.LevelArtifact.Modeling.Language.class);
				newElement.node().set(newElement, "name", java.util.Collections.singletonList(name$));
				newElement.node().set(newElement, "version", java.util.Collections.singletonList(version)); 
			    return newElement;
			}
			
		}
		
		public static class Language extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String name$;
			protected java.lang.String version;
			protected java.lang.String effectiveVersion;

			public Language(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String name$() {
				return name$;
			}

			public java.lang.String version() {
				return version;
			}

			public java.lang.String effectiveVersion() {
				return effectiveVersion;
			}

			public void name$(java.lang.String value) {
				this.name$ = value;
			}

			public void version(java.lang.String value) {
				this.version = value;
			}

			public void effectiveVersion(java.lang.String value) {
				this.effectiveVersion = value;
			}

			@Override
			public java.util.Map<java.lang.String, java.util.List<?>> variables() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name$)));
				map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
				map.put("effectiveVersion", new java.util.ArrayList(java.util.Collections.singletonList(this.effectiveVersion)));
				return map;
			}

			public io.intino.tara.magritte.Concept concept() {
				return this.graph().concept(io.intino.legio.level.LevelArtifact.Modeling.Language.class);
			}

			@Override
			protected void _load(java.lang.String name, java.util.List<?> values) {
				super._load(name, values);
				if (name.equalsIgnoreCase("name")) this.name$ = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void _set(java.lang.String name, java.util.List<?> values) {
				super._set(name, values);
				if (name.equalsIgnoreCase("name")) this.name$ = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = (java.lang.String) values.get(0);
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
