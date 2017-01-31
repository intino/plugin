package io.intino.legio.natives.text.parameter;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#114#2**/
public class Type_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.text.TextParameter self;

	@Override
	public String value() {
		return "String";
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.legio.text.TextParameter) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.legio.text.TextParameter.class;
	}
}