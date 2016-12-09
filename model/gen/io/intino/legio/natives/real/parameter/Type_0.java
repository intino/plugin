package io.intino.legio.natives.real.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#94#2**/
public class Type_0 implements tara.magritte.Expression<String> {
	private io.intino.legio.real.RealParameter self;

	@Override
	public String value() {
		return "Double";
	}

	@Override
	public void self(tara.magritte.Layer context) {
		self = (io.intino.legio.real.RealParameter) context;
	}

	@Override
	public Class<? extends tara.magritte.Layer> selfClass() {
		return io.intino.legio.real.RealParameter.class;
	}
}