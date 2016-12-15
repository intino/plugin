package io.intino.legio.natives.project.webdependencies.webactivity;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#40#3**/
public class Identifier_0 implements tara.magritte.Expression<String> {
	private io.intino.legio.Project.WebDependencies.WebActivity self;

	@Override
	public String value() {
		return self.groupId() + ":" + self.artifactId() + ":" + self.version();
	}

	@Override
	public void self(tara.magritte.Layer context) {
		self = (io.intino.legio.Project.WebDependencies.WebActivity) context;
	}

	@Override
	public Class<? extends tara.magritte.Layer> selfClass() {
		return io.intino.legio.Project.WebDependencies.WebActivity.class;
	}
}