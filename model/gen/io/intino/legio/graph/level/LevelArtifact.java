package io.intino.legio.graph.level;

import io.intino.legio.graph.*;
import java.util.List;
import io.intino.legio.graph.Destination;


public class LevelArtifact extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {

	protected io.intino.legio.graph.level.LevelArtifact.Model model;










	protected io.intino.legio.graph.Artifact _artifact;

	public LevelArtifact(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String groupId() {
		return _artifact.groupId();
	}

	public java.lang.String version() {
		return _artifact.version();
	}

	public LevelArtifact groupId(java.lang.String value) {
		this._artifact.groupId(value);
		return (LevelArtifact) this;
	}

	public LevelArtifact version(java.lang.String value) {
		this._artifact.version(value);
		return (LevelArtifact) this;
	}

	public io.intino.legio.graph.level.LevelArtifact.Model model() {
		return model;
	}

	public io.intino.legio.graph.Artifact.License license() {
		return _artifact.license();
	}

	public io.intino.legio.graph.Artifact.Imports imports() {
		return _artifact.imports();
	}

	public io.intino.legio.graph.Artifact.WebImports webImports() {
		return _artifact.webImports();
	}

	public io.intino.legio.graph.Artifact.Box box() {
		return _artifact.box();
	}

	public io.intino.legio.graph.Artifact.Code code() {
		return _artifact.code();
	}

	public java.util.List<io.intino.legio.graph.Artifact.Exports> exportsList() {
		return (java.util.List<io.intino.legio.graph.Artifact.Exports>) _artifact.exportsList();
	}

	public io.intino.legio.graph.Artifact.Exports exportsList(int index) {
		return _artifact.exportsList().get(index);
	}

	public io.intino.legio.graph.Artifact.Package package$() {
		return _artifact.package$();
	}

	public io.intino.legio.graph.Artifact.Distribution distribution() {
		return _artifact.distribution();
	}

	public io.intino.legio.graph.Artifact.QualityAnalytics qualityAnalytics() {
		return _artifact.qualityAnalytics();
	}

	public java.util.List<io.intino.legio.graph.Artifact.Deployment> deploymentList() {
		return (java.util.List<io.intino.legio.graph.Artifact.Deployment>) _artifact.deploymentList();
	}

	public io.intino.legio.graph.Artifact.Deployment deploymentList(int index) {
		return _artifact.deploymentList().get(index);
	}

	public LevelArtifact model(io.intino.legio.graph.level.LevelArtifact.Model value) {
		this.model = value;
		return (LevelArtifact) this;
	}





















	protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
		if (model != null) components.add(this.model.core$());










		return new java.util.ArrayList<>(components);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();

		return map;
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		super.addNode$(node);
		if (node.is("Level#Artifact$Model")) this.model = node.as(io.intino.legio.graph.level.LevelArtifact.Model.class);
	}

	@Override
    protected void removeNode$(io.intino.tara.magritte.Node node) {
        super.removeNode$(node);
        if (node.is("Level#Artifact$Model")) this.model = null;
    }

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
	}

	@Override
	protected void sync$(io.intino.tara.magritte.Layer layer) {
		super.sync$(layer);
	    if (layer instanceof io.intino.legio.graph.Artifact) _artifact = (io.intino.legio.graph.Artifact) layer;

	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create  {
		protected final java.lang.String name;

		public Create(java.lang.String name) {
			this.name = name;
		}

		public io.intino.legio.graph.level.LevelArtifact.Model model(java.lang.String language, java.lang.String version, java.lang.String sdk) {
		    io.intino.legio.graph.level.LevelArtifact.Model newElement = core$().graph().concept(io.intino.legio.graph.level.LevelArtifact.Model.class).createNode(name, core$()).as(io.intino.legio.graph.level.LevelArtifact.Model.class);
			newElement.core$().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
			newElement.core$().set(newElement, "sdk", java.util.Collections.singletonList(sdk));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.License license(io.intino.legio.graph.Artifact.License.Type type) {
		    io.intino.legio.graph.Artifact.License newElement = core$().graph().concept(io.intino.legio.graph.Artifact.License.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.License.class);
			newElement.core$().set(newElement, "type", java.util.Collections.singletonList(type));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Imports imports() {
		    io.intino.legio.graph.Artifact.Imports newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Imports.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.Imports.class);

		    return newElement;
		}

		public io.intino.legio.graph.Artifact.WebImports webImports() {
		    io.intino.legio.graph.Artifact.WebImports newElement = core$().graph().concept(io.intino.legio.graph.Artifact.WebImports.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.WebImports.class);

		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Box box(java.lang.String language, java.lang.String version, java.lang.String sdk) {
		    io.intino.legio.graph.Artifact.Box newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Box.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.Box.class);
			newElement.core$().set(newElement, "language", java.util.Collections.singletonList(language));
			newElement.core$().set(newElement, "version", java.util.Collections.singletonList(version));
			newElement.core$().set(newElement, "sdk", java.util.Collections.singletonList(sdk));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Code code(java.lang.String targetPackage) {
		    io.intino.legio.graph.Artifact.Code newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Code.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.Code.class);
			newElement.core$().set(newElement, "targetPackage", java.util.Collections.singletonList(targetPackage));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Exports exports(io.intino.legio.graph.Repository.Type repository) {
		    io.intino.legio.graph.Artifact.Exports newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Exports.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.Exports.class);
			newElement.core$().set(newElement, "repository", java.util.Collections.singletonList(repository));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Package package$(io.intino.legio.graph.Artifact.Package.Mode mode) {
		    io.intino.legio.graph.Artifact.Package newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Package.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.Package.class);
			newElement.core$().set(newElement, "mode", java.util.Collections.singletonList(mode));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Distribution distribution(io.intino.legio.graph.Repository.Release release) {
		    io.intino.legio.graph.Artifact.Distribution newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Distribution.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.Distribution.class);
			newElement.core$().set(newElement, "release", java.util.Collections.singletonList(release));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.QualityAnalytics qualityAnalytics(java.lang.String url) {
		    io.intino.legio.graph.Artifact.QualityAnalytics newElement = core$().graph().concept(io.intino.legio.graph.Artifact.QualityAnalytics.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.QualityAnalytics.class);
			newElement.core$().set(newElement, "url", java.util.Collections.singletonList(url));
		    return newElement;
		}

		public io.intino.legio.graph.Artifact.Deployment deployment() {
		    io.intino.legio.graph.Artifact.Deployment newElement = core$().graph().concept(io.intino.legio.graph.Artifact.Deployment.class).createNode(name, core$()).as(io.intino.legio.graph.Artifact.Deployment.class);

		    return newElement;
		}

	}

	public Clear clear() {
		return new Clear();
	}

	public class Clear  {


		public void exports(java.util.function.Predicate<io.intino.legio.graph.Artifact.Exports> filter) {
			new java.util.ArrayList<>(exportsList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}

		public void deployment(java.util.function.Predicate<io.intino.legio.graph.Artifact.Deployment> filter) {
			new java.util.ArrayList<>(deploymentList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
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

		public Model language(java.lang.String value) {
			this.language = value;
			return (Model) this;
		}

		public Model version(java.lang.String value) {
			this.version = value;
			return (Model) this;
		}

		public Model sdk(java.lang.String value) {
			this.sdk = value;
			return (Model) this;
		}

		public Model effectiveVersion(java.lang.String value) {
			this.effectiveVersion = value;
			return (Model) this;
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("language", new java.util.ArrayList(java.util.Collections.singletonList(this.language)));
			map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
			map.put("sdk", new java.util.ArrayList(java.util.Collections.singletonList(this.sdk)));
			map.put("effectiveVersion", new java.util.ArrayList(java.util.Collections.singletonList(this.effectiveVersion)));
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("language")) this.language = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("sdk")) this.sdk = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("language")) this.language = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("version")) this.version = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("sdk")) this.sdk = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("effectiveVersion")) this.effectiveVersion = (java.lang.String) values.get(0);
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}


	public io.intino.legio.graph.LegioGraph graph() {
		return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
	}
}
