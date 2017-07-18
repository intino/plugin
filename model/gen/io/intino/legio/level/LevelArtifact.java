package io.intino.legio.level;

import io.intino.legio.*;


public abstract class LevelArtifact extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	
	protected io.intino.legio.level.LevelArtifact.Model model;
	
	
	
	
	
	
	
	
	
	
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

	public io.intino.legio.level.LevelArtifact.Model model() {
		return model;
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

	public io.intino.legio.Artifact.Box box() {
		return _artifact.box();
	}

	public io.intino.legio.Artifact.Code code() {
		return _artifact.code();
	}

	public java.util.List<io.intino.legio.Artifact.Exports> exportsList() {
		return (java.util.List<io.intino.legio.Artifact.Exports>) _artifact.exportsList();
	}

	public io.intino.legio.Artifact.Exports exportsList(int index) {
		return _artifact.exportsList().get(index);
	}

	public io.intino.legio.Artifact.Package package$() {
		return _artifact.package$();
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

	public void model(io.intino.legio.level.LevelArtifact.Model value) {
		this.model = value;
	}

	

	

	

	

	

	

	

	

	

	

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		if (model != null) components.add(this.model.node());
		
		
		
		
		
		
		
		
		
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
		if (node.is("Level#Artifact$Model")) this.model = node.as(io.intino.legio.level.LevelArtifact.Model.class);
	}

	@Override
    protected void removeNode(io.intino.tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("Level#Artifact$Model")) this.model = null;
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

		public io.intino.legio.level.LevelArtifact.Model model(java.lang.String language, java.lang.String version, java.lang.String sdk) {
		    io.intino.legio.level.LevelArtifact.Model newElement = graph().concept(io.intino.legio.level.LevelArtifact.Model.class).createNode(name, node()).as(io.intino.legio.level.LevelArtifact.Model.class);
			newElement.node().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.node().set(newElement, "version", java.util.Collections.singletonList(version));
			newElement.node().set(newElement, "sdk", java.util.Collections.singletonList(sdk)); 
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

		public io.intino.legio.Artifact.Distribution distribution(io.intino.legio.Repository.Release release) {
		    io.intino.legio.Artifact.Distribution newElement = graph().concept(io.intino.legio.Artifact.Distribution.class).createNode(name, node()).as(io.intino.legio.Artifact.Distribution.class);
			newElement.node().set(newElement, "release", java.util.Collections.singletonList(release)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.QualityAnalytics qualityAnalytics(java.lang.String url) {
		    io.intino.legio.Artifact.QualityAnalytics newElement = graph().concept(io.intino.legio.Artifact.QualityAnalytics.class).createNode(name, node()).as(io.intino.legio.Artifact.QualityAnalytics.class);
			newElement.node().set(newElement, "url", java.util.Collections.singletonList(url)); 
		    return newElement;
		}

		public io.intino.legio.Artifact.Deployment deployment() {
		    io.intino.legio.Artifact.Deployment newElement = graph().concept(io.intino.legio.Artifact.Deployment.class).createNode(name, node()).as(io.intino.legio.Artifact.Deployment.class);
		    return newElement;
		}
		
	}
	
	public static class Model extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String language;
		protected java.lang.String version;
		protected java.lang.String sdk;
		protected java.lang.String effectiveVersion;

		public Model(io.intino.tara.magritte.Node node) {
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

		public java.lang.String effectiveVersion() {
			return effectiveVersion;
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

		public void effectiveVersion(java.lang.String value) {
			this.effectiveVersion = value;
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("language", new java.util.ArrayList(java.util.Collections.singletonList(this.language)));
			map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
			map.put("sdk", new java.util.ArrayList(java.util.Collections.singletonList(this.sdk)));
			map.put("effectiveVersion", new java.util.ArrayList(java.util.Collections.singletonList(this.effectiveVersion)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.level.LevelArtifact.Model.class);
		}

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("language")) this.language = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("sdk")) this.sdk = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("language")) this.language = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("sdk")) this.sdk = (java.lang.String) values.get(0);
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
