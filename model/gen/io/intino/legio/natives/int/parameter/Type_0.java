package io.intino.legio.natives.int.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#108#2**/
public class Type_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.int.IntParameter self;

	@Override
	public String value() {
		return "Integer";
	}

	@Override
	public void self(Layer context) {
		self = (io.intino.legio.int.IntParameter) context;
	}

	@Override
	public Class<? extends Layer> selfClass() {
		return io.intino.legio.int.IntParameter.class;
	}
}