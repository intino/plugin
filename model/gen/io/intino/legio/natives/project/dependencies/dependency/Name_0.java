package io.intino.legio.natives.project.dependencies.dependency;


/**
 * #/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#18#3
 **/
public class Name_0 implements tara.magritte.Expression<String> {
	private io.intino.legio.Project.Dependencies.Dependency self;

	@Override
	public String value() {
		return "Legio: " + self.identifier();
	}

	@Override
	public void self(tara.magritte.Layer context) {
		self = (io.intino.legio.Project.Dependencies.Dependency) context;
	}

	@Override
	public Class<? extends tara.magritte.Layer> selfClass() {
		return io.intino.legio.Project.Dependencies.Dependency.class;
	}
}