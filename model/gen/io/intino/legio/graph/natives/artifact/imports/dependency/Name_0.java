package io.intino.legio.graph.natives.artifact.imports.dependency;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#12#3**/
public class Name_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.graph.Artifact.Imports.Dependency self;

	@Override
	public String value() {
		return "Legio: " + self.identifier();
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.legio.graph.Artifact.Imports.Dependency) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.legio.graph.Artifact.Imports.Dependency.class;
	}
}