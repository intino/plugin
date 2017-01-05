package io.intino.legio.natives.bool.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#112#2**/
public class Type_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.bool.BoolParameter self;

	@Override
	public String value() {
		return "Boolean";
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.legio.bool.BoolParameter) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.legio.bool.BoolParameter.class;
	}
}