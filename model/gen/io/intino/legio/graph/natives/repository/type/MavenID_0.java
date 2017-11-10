package io.intino.legio.graph.natives.repository.type;

import io.intino.legio.graph.Repository;

/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#103#2**/
public class MavenID_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.graph.Repository.Type self;

	@Override
	public String value() {
		return self.core$().ownerAs(Repository.class).identifier();
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.legio.graph.Repository.Type) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.legio.graph.Repository.Type.class;
	}
}