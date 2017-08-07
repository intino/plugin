package io.intino.legio.graph.natives.artifact.webimports.webartifact;



/**#/Users/oroncal/workspace/intino/model/src/io/intino/legio/Main.tara#36#3**/
public class Identifier_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.legio.graph.Artifact.WebImports.WebArtifact self;

	@Override
	public String value() {
		return self.groupId() + ":" + self.artifactId() + ":" + self.version();
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.legio.graph.Artifact.WebImports.WebArtifact) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.legio.graph.Artifact.WebImports.WebArtifact.class;
	}
}