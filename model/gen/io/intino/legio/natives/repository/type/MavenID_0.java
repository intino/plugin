package io.intino.legio.natives.repository.type;

import io.intino.legio.Repository;

/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#97#2**/
public class MavenID_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.Repository.Type self;

	@Override
	public String value() {
		return self.ownerAs(Repository.class).identifier();
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.legio.Repository.Type) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.legio.Repository.Type.class;
	}
}