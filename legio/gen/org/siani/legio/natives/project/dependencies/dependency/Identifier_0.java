package org.siani.legio.natives.project.dependencies.dependency;



/**#/Users/oroncal/workspace/legio/legio/src/org/siani/legio/Main.tara#13#3**/
public class Identifier_0 implements tara.magritte.Expression<String> {
	private org.siani.legio.Project.Dependencies.Dependency self;

	@Override
	public String value() {
		return self.groupId() + ":" + self.artifactId() + ":" + self.version();
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