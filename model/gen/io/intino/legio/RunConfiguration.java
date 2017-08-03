package io.intino.legio;

public class RunConfiguration extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String mainClass;
	protected java.util.List<io.intino.legio.Argument> argumentList = new java.util.ArrayList<>();

	public RunConfiguration(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String mainClass() {
		return mainClass;
	}

	public void mainClass(java.lang.String value) {
		this.mainClass = value;
	}

	public java.util.List<io.intino.legio.Argument> argumentList() {
		return java.util.Collections.unmodifiableList(argumentList);
	}

	public io.intino.legio.Argument argument(int index) {
		return argumentList.get(index);
	}

	public java.util.List<io.intino.legio.Argument> argumentList(java.util.function.Predicate<io.intino.legio.Argument> predicate) {
		return argumentList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	

	public java.util.List<io.intino.tara.magritte.Node> componentList() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList());
		argumentList.stream().forEach(c -> components.add(c.node()));
		return new java.util.ArrayList<>(components);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("mainClass", new java.util.ArrayList(java.util.Collections.singletonList(this.mainClass)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.RunConfiguration.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		super.addNode(node);
		if (node.is("Argument")) this.argumentList.add(node.as(io.intino.legio.Argument.class));
	}

	@Override
    protected void removeNode(io.intino.tara.magritte.Node node) {
        super.removeNode(node);
        if (node.is("Argument")) this.argumentList.remove(node.as(io.intino.legio.Argument.class));
    }

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("mainClass")) this.mainClass = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("mainClass")) this.mainClass = (java.lang.String) values.get(0);
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

		public io.intino.legio.Argument argument(java.lang.String name$, java.lang.String value) {
		    io.intino.legio.Argument newElement = graph().concept(io.intino.legio.Argument.class).createNode(name, node()).as(io.intino.legio.Argument.class);
			newElement.node().set(newElement, "name", java.util.Collections.singletonList(name$));
			newElement.node().set(newElement, "value", java.util.Collections.singletonList(value)); 
		    return newElement;
		}
		
	}
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
