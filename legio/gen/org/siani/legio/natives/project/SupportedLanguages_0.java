package org.siani.legio.natives.project;


import org.siani.legio.Main;
import org.siani.legio.Project;

/**#/Users/oroncal/workspace/tara/ide/legio/legio-core/src/legio/Main.tara#24#1**/
public class SupportedLanguages_0 implements tara.magritte.Expression<java.util.List<String>> {
	private Project self;

	@Override
	public java.util.List<String> value() {
		return Main.calculate(self);
	}

	@Override
	public void self(tara.magritte.Layer context) {
		self = (Project) context;
	}

	@Override
	public Class<? extends tara.magritte.Layer> selfClass() {
		return Project.class;
	}
}