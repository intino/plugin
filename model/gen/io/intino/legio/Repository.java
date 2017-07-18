package io.intino.legio;

import io.intino.legio.*;


public class Repository extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String identifier;
	protected java.util.List<io.intino.legio.Repository.Type> typeList = new java.util.ArrayList<>();
	protected java.util.List<io.intino.legio.Repository.Release> releaseList = new java.util.ArrayList<>();
	protected io.intino.legio.Repository.Snapshot snapshot;
	protected io.intino.legio.Repository.Language language;

	public Repository(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String identifier() {
		return identifier;
	}

	public void identifier(java.lang.String value) {
		this.identifier = value;
	}

	public java.util.List<io.intino.legio.Repository.Type> typeList() {
		return java.util.Collections.unmodifiableList(typeList);
	}

	public io.intino.legio.Repository.Type type(int index) {
		return typeList.get(index);
	}

	public java.util.List<io.intino.legio.Repository.Type> typeList(java.util.function.Predicate<io.intino.legio.Repository.Type> predicate) {
		return typeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public java.util.List<io.intino.legio.Repository.Release> releaseList() {
		return java.util.Collections.unmodifiableList(releaseList);
	}

	public io.intino.legio.Repository.Release release(int index) {
		return releaseList.get(index);
	}

	public java.util.List<io.intino.legio.Repository.Release> releaseList(java.util.function.Predicate<io.intino.legio.Repository.Release> predicate) {
		return releaseList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.legio.Repository.Snapshot snapshot() {
		return snapshot;
	}

	public io.intino.legio.Repository.Language language() {
		return language;
	}

	

	

	public void snapshot(io.intino.legio.Repository.Snapshot value) {
		this.snapshot = value;
	}

	public void language(io.intino.legio.Repository.Language value) {
		this.language = value;
	}

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		typeList.stream().forEach(c -> components.add(c.node()));
		releaseList.stream().forEach(c -> components.add(c.node()));
		if (snapshot != null) components.add(this.snapshot.node());
		if (language != null) components.add(this.language.node());
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("identifier", new java.util.ArrayList(java.util.Collections.singletonList(this.identifier)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Repository.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		super.addNode(node);
		if (node.is("Repository$Type")) this.typeList.add(node.as(io.intino.legio.Repository.Type.class));
		if (node.is("Repository$Release")) this.releaseList.add(node.as(io.intino.legio.Repository.Release.class));
		if (node.is("Repository$Snapshot")) this.snapshot = node.as(io.intino.legio.Repository.Snapshot.class);
		if (node.is("Repository$Language")) this.language = node.as(io.intino.legio.Repository.Language.class);
	}

	@Override
    protected void removeNode(io.intino.tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("Repository$Type")) this.typeList.remove(node.as(io.intino.legio.Repository.Type.class));
        if (node.is("Repository$Release")) this.releaseList.remove(node.as(io.intino.legio.Repository.Release.class));
        if (node.is("Repository$Snapshot")) this.snapshot = null;
        if (node.is("Repository$Language")) this.language = null;
    }

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("identifier")) this.identifier = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("identifier")) this.identifier = (java.lang.String) values.get(0);
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

		

		public io.intino.legio.Repository.Release release(java.lang.String url) {
		    io.intino.legio.Repository.Release newElement = graph().concept(io.intino.legio.Repository.Release.class).createNode(name, node()).as(io.intino.legio.Repository.Release.class);
			newElement.node().set(newElement, "url", java.util.Collections.singletonList(url)); 
		    return newElement;
		}

		public io.intino.legio.Repository.Snapshot snapshot(java.lang.String url) {
		    io.intino.legio.Repository.Snapshot newElement = graph().concept(io.intino.legio.Repository.Snapshot.class).createNode(name, node()).as(io.intino.legio.Repository.Snapshot.class);
			newElement.node().set(newElement, "url", java.util.Collections.singletonList(url)); 
		    return newElement;
		}

		public io.intino.legio.Repository.Language language(java.lang.String url) {
		    io.intino.legio.Repository.Language newElement = graph().concept(io.intino.legio.Repository.Language.class).createNode(name, node()).as(io.intino.legio.Repository.Language.class);
			newElement.node().set(newElement, "url", java.util.Collections.singletonList(url)); 
		    return newElement;
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

		public void url(java.lang.String value) {
			this.url = value;
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
			map.put("mavenID", new java.util.ArrayList(java.util.Collections.singletonList(this.mavenID)));
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Repository.Type.class);
		}

		@Override
		protected void _load(java.lang.String name, java.util.List<?> values) {
			super._load(name, values);
			if (name.equalsIgnoreCase("url")) this.url = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("mavenID")) this.mavenID = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
		}

		@Override
		protected void _set(java.lang.String name, java.util.List<?> values) {
			super._set(name, values);
			if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("mavenID")) this.mavenID = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
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
	
	public static class Release extends io.intino.legio.Repository.Type implements io.intino.tara.magritte.tags.Terminal {
		

		public Release(io.intino.tara.magritte.Node node) {
			super(node);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Repository.Release.class);
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

		public class Create extends io.intino.legio.Repository.Type.Create {
			

			public Create(java.lang.String name) {
				super(name);
			}
			
		}
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	public static class Snapshot extends io.intino.legio.Repository.Type implements io.intino.tara.magritte.tags.Terminal {
		

		public Snapshot(io.intino.tara.magritte.Node node) {
			super(node);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Repository.Snapshot.class);
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

		public class Create extends io.intino.legio.Repository.Type.Create {
			

			public Create(java.lang.String name) {
				super(name);
			}
			
		}
		
		public io.intino.legio.Legio legioWrapper() {
			return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
		}
	}
	
	public static class Language extends io.intino.legio.Repository.Type implements io.intino.tara.magritte.tags.Terminal {
		

		public Language(io.intino.tara.magritte.Node node) {
			super(node);
		}

		@Override
		public java.util.Map<java.lang.String, java.util.List<?>> variables() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
			return map;
		}

		public io.intino.tara.magritte.Concept concept() {
			return this.graph().concept(io.intino.legio.Repository.Language.class);
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

		public class Create extends io.intino.legio.Repository.Type.Create {
			

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
