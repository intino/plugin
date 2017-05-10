package io.intino.legio.application.artifact;

import io.intino.legio.*;


public class ApplicationGeneration extends io.intino.legio.level.artifact.LevelGeneration implements io.intino.tara.magritte.tags.Terminal {
	

	public ApplicationGeneration(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String version() {
		return _generation.version();
	}

	public java.lang.String inPackage() {
		return _generation.inPackage();
	}

	public void version(java.lang.String value) {
		this._generation.version(value);
	}

	public void inPackage(java.lang.String value) {
		this._generation.inPackage(value);
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables());
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.legio.Artifact.Generation.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		_generation.node().load(_generation, name, values);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		_generation.node().set(_generation, name, values);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create extends io.intino.legio.level.artifact.LevelGeneration.Create {
		

		public Create(java.lang.String name) {
			super(name);
		}
		
	}
	
	public io.intino.legio.Legio legioWrapper() {
		return (io.intino.legio.Legio) graph().wrapper(io.intino.legio.Legio.class);
	}
}
