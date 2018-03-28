package io.intino.legio.graph;

import io.intino.legio.graph.*;


public class Destination extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Component, io.intino.tara.magritte.tags.Terminal {
	protected io.intino.legio.graph.Server server;
	protected io.intino.legio.graph.RunConfiguration runConfiguration;
	protected java.lang.String url;
	protected java.lang.String project;
	protected io.intino.legio.graph.Destination.BugTracking bugTracking;
	protected io.intino.legio.graph.Destination.Requirements requirements;

	public Destination(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public io.intino.legio.graph.Server server() {
		return server;
	}

	public io.intino.legio.graph.RunConfiguration runConfiguration() {
		return runConfiguration;
	}

	public java.lang.String url() {
		return url;
	}

	public java.lang.String project() {
		return project;
	}

	public Destination server(io.intino.legio.graph.Server value) {
		this.server = value;
		return (Destination) this;
	}

	public Destination runConfiguration(io.intino.legio.graph.RunConfiguration value) {
		this.runConfiguration = value;
		return (Destination) this;
	}

	public Destination url(java.lang.String value) {
		this.url = value;
		return (Destination) this;
	}

	public Destination project(java.lang.String value) {
		this.project = value;
		return (Destination) this;
	}

	public io.intino.legio.graph.Destination.BugTracking bugTracking() {
		return bugTracking;
	}

	public io.intino.legio.graph.Destination.Requirements requirements() {
		return requirements;
	}

	public Destination bugTracking(io.intino.legio.graph.Destination.BugTracking value) {
		this.bugTracking = value;
		return (Destination) this;
	}

	public Destination requirements(io.intino.legio.graph.Destination.Requirements value) {
		this.requirements = value;
		return (Destination) this;
	}

	protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
		if (bugTracking != null) components.add(this.bugTracking.core$());
		if (requirements != null) components.add(this.requirements.core$());
		return new java.util.ArrayList<>(components);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("server", this.server != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.server)) : java.util.Collections.emptyList());
		map.put("runConfiguration", this.runConfiguration != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.runConfiguration)) : java.util.Collections.emptyList());
		map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
		map.put("project", new java.util.ArrayList(java.util.Collections.singletonList(this.project)));
		return map;
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		super.addNode$(node);
		if (node.is("Destination$BugTracking")) this.bugTracking = node.as(io.intino.legio.graph.Destination.BugTracking.class);
		if (node.is("Destination$Requirements")) this.requirements = node.as(io.intino.legio.graph.Destination.Requirements.class);
	}

	@Override
    protected void removeNode$(io.intino.tara.magritte.Node node) {
        super.removeNode$(node);
        if (node.is("Destination$BugTracking")) this.bugTracking = null;
        if (node.is("Destination$Requirements")) this.requirements = null;
    }

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("server")) this.server = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.legio.graph.Server.class, this).get(0);
		else if (name.equalsIgnoreCase("runConfiguration")) this.runConfiguration = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.legio.graph.RunConfiguration.class, this).get(0);
		else if (name.equalsIgnoreCase("url")) this.url = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("project")) this.project = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("server")) this.server = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.legio.graph.Server.class) : null;
		else if (name.equalsIgnoreCase("runConfiguration")) this.runConfiguration = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.legio.graph.RunConfiguration.class) : null;
		else if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("project")) this.project = (java.lang.String) values.get(0);
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

		public io.intino.legio.graph.Destination.BugTracking bugTracking() {
		    io.intino.legio.graph.Destination.BugTracking newElement = core$().graph().concept(io.intino.legio.graph.Destination.BugTracking.class).createNode(this.name, core$()).as(io.intino.legio.graph.Destination.BugTracking.class);

		    return newElement;
		}

		public io.intino.legio.graph.Destination.Requirements requirements() {
		    io.intino.legio.graph.Destination.Requirements newElement = core$().graph().concept(io.intino.legio.graph.Destination.Requirements.class).createNode(this.name, core$()).as(io.intino.legio.graph.Destination.Requirements.class);

		    return newElement;
		}

	}

	public Clear clear() {
		return new Clear();
	}

	public class Clear  {



	}

	public static class BugTracking extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.util.List<java.lang.String> slackUsers = new java.util.ArrayList<>();

		public BugTracking(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<java.lang.String> slackUsers() {
			return slackUsers;
		}

		public java.lang.String slackUsers(int index) {
			return slackUsers.get(index);
		}

		public java.util.List<java.lang.String> slackUsers(java.util.function.Predicate<java.lang.String> predicate) {
			return slackUsers().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("slackUsers", this.slackUsers);
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("slackUsers")) this.slackUsers = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("slackUsers")) this.slackUsers = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}

	public static class Requirements extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {

		protected java.util.List<io.intino.legio.graph.Destination.Requirements.HDD> hDDList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.graph.Destination.Requirements.Memory> memoryList = new java.util.ArrayList<>();
		protected java.util.List<io.intino.legio.graph.Destination.Requirements.CPU> cPUList = new java.util.ArrayList<>();

		public Requirements(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<io.intino.legio.graph.Destination.Requirements.HDD> hDDList() {
			return java.util.Collections.unmodifiableList(hDDList);
		}

		public io.intino.legio.graph.Destination.Requirements.HDD hDD(int index) {
			return hDDList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Destination.Requirements.HDD> hDDList(java.util.function.Predicate<io.intino.legio.graph.Destination.Requirements.HDD> predicate) {
			return hDDList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.graph.Destination.Requirements.Memory> memoryList() {
			return java.util.Collections.unmodifiableList(memoryList);
		}

		public io.intino.legio.graph.Destination.Requirements.Memory memory(int index) {
			return memoryList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Destination.Requirements.Memory> memoryList(java.util.function.Predicate<io.intino.legio.graph.Destination.Requirements.Memory> predicate) {
			return memoryList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public java.util.List<io.intino.legio.graph.Destination.Requirements.CPU> cPUList() {
			return java.util.Collections.unmodifiableList(cPUList);
		}

		public io.intino.legio.graph.Destination.Requirements.CPU cPU(int index) {
			return cPUList.get(index);
		}

		public java.util.List<io.intino.legio.graph.Destination.Requirements.CPU> cPUList(java.util.function.Predicate<io.intino.legio.graph.Destination.Requirements.CPU> predicate) {
			return cPUList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}







		protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
			new java.util.ArrayList<>(hDDList).forEach(c -> components.add(c.core$()));
			new java.util.ArrayList<>(memoryList).forEach(c -> components.add(c.core$()));
			new java.util.ArrayList<>(cPUList).forEach(c -> components.add(c.core$()));
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
			if (node.is("Destination$Requirements$HDD")) this.hDDList.add(node.as(io.intino.legio.graph.Destination.Requirements.HDD.class));
			if (node.is("Destination$Requirements$Memory")) this.memoryList.add(node.as(io.intino.legio.graph.Destination.Requirements.Memory.class));
			if (node.is("Destination$Requirements$CPU")) this.cPUList.add(node.as(io.intino.legio.graph.Destination.Requirements.CPU.class));
		}

		@Override
	    protected void removeNode$(io.intino.tara.magritte.Node node) {
	        super.removeNode$(node);
	        if (node.is("Destination$Requirements$HDD")) this.hDDList.remove(node.as(io.intino.legio.graph.Destination.Requirements.HDD.class));
	        if (node.is("Destination$Requirements$Memory")) this.memoryList.remove(node.as(io.intino.legio.graph.Destination.Requirements.Memory.class));
	        if (node.is("Destination$Requirements$CPU")) this.cPUList.remove(node.as(io.intino.legio.graph.Destination.Requirements.CPU.class));
	    }

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
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

			public io.intino.legio.graph.Destination.Requirements.HDD hDD(int min) {
			    io.intino.legio.graph.Destination.Requirements.HDD newElement = core$().graph().concept(io.intino.legio.graph.Destination.Requirements.HDD.class).createNode(this.name, core$()).as(io.intino.legio.graph.Destination.Requirements.HDD.class);
				newElement.core$().set(newElement, "min", java.util.Collections.singletonList(min));
			    return newElement;
			}

			public io.intino.legio.graph.Destination.Requirements.Memory memory(int min) {
			    io.intino.legio.graph.Destination.Requirements.Memory newElement = core$().graph().concept(io.intino.legio.graph.Destination.Requirements.Memory.class).createNode(this.name, core$()).as(io.intino.legio.graph.Destination.Requirements.Memory.class);
				newElement.core$().set(newElement, "min", java.util.Collections.singletonList(min));
			    return newElement;
			}

			public io.intino.legio.graph.Destination.Requirements.CPU cPU(int cores) {
			    io.intino.legio.graph.Destination.Requirements.CPU newElement = core$().graph().concept(io.intino.legio.graph.Destination.Requirements.CPU.class).createNode(this.name, core$()).as(io.intino.legio.graph.Destination.Requirements.CPU.class);
				newElement.core$().set(newElement, "cores", java.util.Collections.singletonList(cores));
			    return newElement;
			}

		}

		public Clear clear() {
			return new Clear();
		}

		public class Clear  {
			public void hDD(java.util.function.Predicate<io.intino.legio.graph.Destination.Requirements.HDD> filter) {
				new java.util.ArrayList<>(hDDList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}

			public void memory(java.util.function.Predicate<io.intino.legio.graph.Destination.Requirements.Memory> filter) {
				new java.util.ArrayList<>(memoryList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}

			public void cPU(java.util.function.Predicate<io.intino.legio.graph.Destination.Requirements.CPU> filter) {
				new java.util.ArrayList<>(cPUList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
			}
		}

		public static class HDD extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected int min;

			public HDD(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public int min() {
				return min;
			}

			public HDD min(int value) {
				this.min = value;
				return (HDD) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("min", new java.util.ArrayList(java.util.Collections.singletonList(this.min)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("min")) this.min = io.intino.tara.magritte.loaders.IntegerLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("min")) this.min = (java.lang.Integer) values.get(0);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}

		public static class Memory extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected int min;

			public Memory(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public int min() {
				return min;
			}

			public Memory min(int value) {
				this.min = value;
				return (Memory) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("min", new java.util.ArrayList(java.util.Collections.singletonList(this.min)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("min")) this.min = io.intino.tara.magritte.loaders.IntegerLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("min")) this.min = (java.lang.Integer) values.get(0);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}

		public static class CPU extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected int cores;

			public CPU(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public int cores() {
				return cores;
			}

			public CPU cores(int value) {
				this.cores = value;
				return (CPU) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("cores", new java.util.ArrayList(java.util.Collections.singletonList(this.cores)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("cores")) this.cores = io.intino.tara.magritte.loaders.IntegerLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("cores")) this.cores = (java.lang.Integer) values.get(0);
			}


			public io.intino.legio.graph.LegioGraph graph() {
				return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
			}
		}


		public io.intino.legio.graph.LegioGraph graph() {
			return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
		}
	}


	public io.intino.legio.graph.LegioGraph graph() {
		return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
	}
}
