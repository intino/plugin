package io.intino.legio.product;

import io.intino.legio.*;


public class ProductArtifact extends io.intino.legio.level.LevelArtifact implements io.intino.tara.magritte.tags.Terminal {
	
	
	
	
	
	
	
	
	
	
	

	public ProductArtifact(io.intino.tara.magritte.Node node) {
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

	

	

	

	

	

	

	

	

	

	

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		
		
		
		
		
		
		
		
		
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Artifact.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		_artifact.node().load(_artifact, name, values);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		_artifact.node().set(_artifact, name, values);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create extends io.intino.legio.level.LevelArtifact.Create {
		

		public Create(java.lang.String name) {
			super(name);
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
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
