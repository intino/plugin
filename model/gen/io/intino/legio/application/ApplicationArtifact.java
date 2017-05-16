package io.intino.legio.application;

import io.intino.legio.*;


public class ApplicationArtifact extends io.intino.legio.level.LevelArtifact implements io.intino.tara.magritte.tags.Terminal {
	
	
	
	
	
	
	
	
	
	
	

	public ApplicationArtifact(io.intino.tara.magritte.Node node) {
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
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
