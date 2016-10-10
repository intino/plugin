package org.siani.legio.natives.project.dependencies.dependency;



/**#/Users/oroncal/workspace/legio/model/src/org/siani/legio/Main.tara#17#3**/
public class Name_0 implements tara.magritte.Expression<String> {
	private org.siani.legio.Project.Dependencies.Dependency self;

	@Override
	public String value() {
		return "Legio: " + self.identifier();
	}

	@Override
	public void self(tara.magritte.Layer context) {
		self = (org.siani.legio.Project.Dependencies.Dependency) context;
	}

	@Override
	public Class<? extends tara.magritte.Layer> selfClass() {
		return org.siani.legio.Project.Dependencies.Dependency.class;
	}
}