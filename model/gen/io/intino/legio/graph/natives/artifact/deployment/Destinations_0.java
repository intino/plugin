package io.intino.legio.graph.natives.artifact.deployment;

import java.util.List;
import io.intino.legio.graph.Destination;
import java.util.ArrayList;

/**Artifact.Deployment#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#77#2**/
public class Destinations_0 implements io.intino.legio.graph.functions.Destinations, io.intino.tara.magritte.Function {
	private io.intino.legio.graph.Artifact.Deployment self;

	@Override
	public List<Destination> destinations() {
		return java.util.Arrays.asList(self.dev(), self.pro());
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.legio.graph.Artifact.Deployment) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.legio.graph.Artifact.Deployment.class;
	}
}