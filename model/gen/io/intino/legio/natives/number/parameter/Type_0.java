package io.intino.legio.natives.number.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#109#2**/
public class Type_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.number.NumberParameter self;

	@Override
	public String value() {
		return "Integer";
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.legio.number.NumberParameter) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.legio.number.NumberParameter.class;
	}
}