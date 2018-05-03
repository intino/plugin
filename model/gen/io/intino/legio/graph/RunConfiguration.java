package io.intino.legio.graph;

import io.intino.legio.graph.*;


public class RunConfiguration extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String mainClass;
	protected java.lang.String vmOptions;
	protected java.util.List<io.intino.legio.graph.Argument> argumentList = new java.util.ArrayList<>();

	public RunConfiguration(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String mainClass() {
		return mainClass;
	}

	public java.lang.String vmOptions() {
		return vmOptions;
	}

	public RunConfiguration mainClass(java.lang.String value) {
		this.mainClass = value;
		return (RunConfiguration) this;
	}

	public RunConfiguration vmOptions(java.lang.String value) {
		this.vmOptions = value;
		return (RunConfiguration) this;
	}

	public java.util.List<io.intino.legio.graph.Argument> argumentList() {
		return java.util.Collections.unmodifiableList(argumentList);
	}

	public io.intino.legio.graph.Argument argument(int index) {
		return argumentList.get(index);
	}

	public java.util.List<io.intino.legio.graph.Argument> argumentList(java.util.function.Predicate<io.intino.legio.graph.Argument> predicate) {
		return argumentList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}



	protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
		new java.util.ArrayList<>(argumentList).forEach(c -> components.add(c.core$()));
		return new java.util.ArrayList<>(components);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("mainClass", new java.util.ArrayList(java.util.Collections.singletonList(this.mainClass)));
		map.put("vmOptions", new java.util.ArrayList(java.util.Collections.singletonList(this.vmOptions)));
		return map;
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		super.addNode$(node);
		if (node.is("Argument")) this.argumentList.add(node.as(io.intino.legio.graph.Argument.class));
	}

	@Override
    protected void removeNode$(io.intino.tara.magritte.Node node) {
        super.removeNode$(node);
        if (node.is("Argument")) this.argumentList.remove(node.as(io.intino.legio.graph.Argument.class));
    }

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("mainClass")) this.mainClass = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("vmOptions")) this.vmOptions = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("mainClass")) this.mainClass = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("vmOptions")) this.vmOptions = (java.lang.String) values.get(0);
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

		public io.intino.legio.graph.Argument argument(java.lang.String name, java.lang.String value) {
		    io.intino.legio.graph.Argument newElement = core$().graph().concept(io.intino.legio.graph.Argument.class).createNode(this.name, core$()).as(io.intino.legio.graph.Argument.class);
			newElement.core$().set(newElement, "name", java.util.Collections.singletonList(name));
			newElement.core$().set(newElement, "value", java.util.Collections.singletonList(value));
		    return newElement;
		}

	}

	public Clear clear() {
		return new Clear();
	}

	public class Clear  {
		public void argument(java.util.function.Predicate<io.intino.legio.graph.Argument> filter) {
			new java.util.ArrayList<>(argumentList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}
	}

	public io.intino.legio.graph.LegioGraph graph() {
		return (io.intino.legio.graph.LegioGraph) core$().graph().as(io.intino.legio.graph.LegioGraph.class);
	}
}
