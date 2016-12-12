package io.intino.legio.natives.text.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#102#2**/
public class Type_0 implements tara.magritte.Expression<String> {
	private io.intino.legio.text.TextParameter self;

	@Override
	public String value() {
		return "String";
	}

	@Override
	public void self(tara.magritte.Layer context) {
		self = (io.intino.legio.text.TextParameter) context;
	}

	@Override
	public Class<? extends tara.magritte.Layer> selfClass() {
		return io.intino.legio.text.TextParameter.class;
	}
}