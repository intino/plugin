package io.intino.legio.graph;

import io.intino.legio.graph.*;


public class Repository extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String identifier;
	protected java.util.List<io.intino.legio.graph.Repository.Type> typeList = new java.util.ArrayList<>();
	protected java.util.List<io.intino.legio.graph.Repository.Release> releaseList = new java.util.ArrayList<>();
	protected io.intino.legio.graph.Repository.Snapshot snapshot;
	protected io.intino.legio.graph.Repository.Language language;

	public Repository(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String identifier() {
		return identifier;
	}

	public Repository identifier(java.lang.String value) {
		this.identifier = value;
		return (Repository) this;
	}

	public java.util.List<io.intino.legio.graph.Repository.Type> typeList() {
		return java.util.Collections.unmodifiableList(typeList);
	}

	public io.intino.legio.graph.Repository.Type type(int index) {
		return typeList.get(index);
	}

	public java.util.List<io.intino.legio.graph.Repository.Type> typeList(java.util.function.Predicate<io.intino.legio.graph.Repository.Type> predicate) {
		return typeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public java.util.List<io.intino.legio.graph.Repository.Release> releaseList() {
		return java.util.Collections.unmodifiableList(releaseList);
	}

	public io.intino.legio.graph.Repository.Release release(int index) {
		return releaseList.get(index);
	}

	public java.util.List<io.intino.legio.graph.Repository.Release> releaseList(java.util.function.Predicate<io.intino.legio.graph.Repository.Release> predicate) {
		return releaseList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.graph.Repository.Snapshot snapshot() {
		return snapshot;
	}

	public io.intino.legio.graph.Repository.Language language() {
		return language;
	}





	public Repository snapshot(io.intino.legio.graph.Repository.Snapshot value) {
		this.snapshot = value;
		return (Repository) this;
	}

	public Repository language(io.intino.legio.graph.Repository.Language value) {
		this.language = value;
		return (Repository) this;
	}

	protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
		new java.util.ArrayList<>(typeList).forEach(c -> components.add(c.core$()));
		new java.util.ArrayList<>(releaseList).forEach(c -> components.add(c.core$()));
		if (snapshot != null) components.add(this.snapshot.core$());
		if (language != null) components.add(this.language.core$());
		return new java.util.ArrayList<>(components);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("identifier", new java.util.ArrayList(java.util.Collections.singletonList(this.identifier)));
		return map;
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		super.addNode$(node);
		if (node.is("Repository$Type")) this.typeList.add(node.as(io.intino.legio.graph.Repository.Type.class));
		if (node.is("Repository$Release")) this.releaseList.add(node.as(io.intino.legio.graph.Repository.Release.class));
		if (node.is("Repository$Snapshot")) this.snapshot = node.as(io.intino.legio.graph.Repository.Snapshot.class);
		if (node.is("Repository$Language")) this.language = node.as(io.intino.legio.graph.Repository.Language.class);
	}

	@Override
    protected void removeNode$(io.intino.tara.magritte.Node node) {
        super.removeNode$(node);
        if (node.is("Repository$Type")) this.typeList.remove(node.as(io.intino.legio.graph.Repository.Type.class));
        if (node.is("Repository$Release")) this.releaseList.remove(node.as(io.intino.legio.graph.Repository.Release.class));
        if (node.is("Repository$Snapshot")) this.snapshot = null;
        if (node.is("Repository$Language")) this.language = null;
    }

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("identifier")) this.identifier = (java.lang.String) values.get(0);
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



		public io.intino.legio.graph.Repository.Release release(java.lang.String url) {
		    io.intino.legio.graph.Repository.Release newElement = core$().graph().concept(io.intino.legio.graph.Repository.Release.class).createNode(name, core$()).as(io.intino.legio.graph.Repository.Release.class);
			newElement.core$().set(newElement, "url", java.util.Collections.singletonList(url));
		    return newElement;
		}

		public io.intino.legio.graph.Repository.Snapshot snapshot(java.lang.String url) {
		    io.intino.legio.graph.Repository.Snapshot newElement = core$().graph().concept(io.intino.legio.graph.Repository.Snapshot.class).createNode(name, core$()).as(io.intino.legio.graph.Repository.Snapshot.class);
			newElement.core$().set(newElement, "url", java.util.Collections.singletonList(url));
		    return newElement;
		}

		public io.intino.legio.graph.Repository.Language language(java.lang.String url) {
		    io.intino.legio.graph.Repository.Language newElement = core$().graph().concept(io.intino.legio.graph.Repository.Language.class).createNode(name, core$()).as(io.intino.legio.graph.Repository.Language.class);
			newElement.core$().set(newElement, "url", java.util.Collections.singletonList(url));
		    return newElement;
		}

	}

	public Clear clear() {
		return new Clear();
	}

	public class Clear  {


		public void release(java.util.function.Predicate<io.intino.legio.graph.Repository.Release> filter) {
			new java.util.ArrayList<>(releaseList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}




	}

	public static abstract class Type extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String url;
		protected io.intino.tara.magritte.Expression<java.lang.String> mavenID;

		public Type(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String url() {
			return url;
		}

		public java.lang.String mavenID() {
			return mavenID.value();
		}

		public Type url(java.lang.String value) {
			this.url = value;
			return (Type) this;
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
			map.put("mavenID", new java.util.ArrayList(java.util.Collections.singletonList(this.mavenID)));
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("url")) this.url = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("mavenID")) this.mavenID = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("mavenID")) this.mavenID = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
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



		}

		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Release extends io.intino.legio.graph.Repository.Type implements io.intino.tara.magritte.tags.Terminal {


		public Release(io.intino.tara.magritte.Node node) {
			super(node);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());

			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Snapshot extends io.intino.legio.graph.Repository.Type implements io.intino.tara.magritte.tags.Terminal {


		public Snapshot(io.intino.tara.magritte.Node node) {
			super(node);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());

			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Language extends io.intino.legio.graph.Repository.Type implements io.intino.tara.magritte.tags.Terminal {


		public Language(io.intino.tara.magritte.Node node) {
			super(node);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());

			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}


	public io.intino.legio.graph.LegioGraph graph() {
		return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
	}
}
