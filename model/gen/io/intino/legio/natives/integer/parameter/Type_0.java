package io.intino.legio.natives.integer.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#108#2**/
public class Type_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.integer.IntegerParameter self;

	@Override
	public String value() {
		return "Integer";
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.legio.integer.IntegerParameter) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.legio.integer.IntegerParameter.class;
	}
}