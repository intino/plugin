package io.intino.legio.natives.integer.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#97#2**/
public class Type_0 implements tara.magritte.Expression<String> {
	private io.intino.legio.integer.IntegerParameter self;

	@Override
	public String value() {
		return "Integer";
	}

	@Override
	public void self(tara.magritte.Layer context) {
		self = (io.intino.legio.integer.IntegerParameter) context;
	}

	@Override
	public Class<? extends tara.magritte.Layer> selfClass() {
		return io.intino.legio.integer.IntegerParameter.class;
	}
}