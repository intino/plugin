package io.intino.legio.natives.real.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#105#2**/
public class Type_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.real.RealParameter self;

	@Override
	public String value() {
		return "Double";
	}

	@Override
	public void self(Layer context) {
		self = (io.intino.legio.real.RealParameter) context;
	}

	@Override
	public Class<? extends Layer> selfClass() {
		return io.intino.legio.real.RealParameter.class;
	}
}