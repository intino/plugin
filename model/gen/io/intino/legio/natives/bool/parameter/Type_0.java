package io.intino.legio.natives.bool.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#99#2**/
public class Type_0 implements tara.magritte.Expression<String> {
	private io.intino.legio.bool.BoolParameter self;

	@Override
	public String value() {
		return "Boolean";
	}

	@Override
	public void self(tara.magritte.Layer context) {
		self = (io.intino.legio.bool.BoolParameter) context;
	}

	@Override
	public Class<? extends tara.magritte.Layer> selfClass() {
		return io.intino.legio.bool.BoolParameter.class;
	}
}