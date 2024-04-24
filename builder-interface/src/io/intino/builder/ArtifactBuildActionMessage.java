package io.intino.builder;

import java.io.File;

import static io.intino.builder.BuildConstants.SEPARATOR;
import static io.intino.builder.PostCompileActionMessage.ObjectType.ARTIFACT_BUILD;

public class ArtifactBuildActionMessage extends PostCompileActionMessage {
	private final String phase;

	public ArtifactBuildActionMessage(String module, File file, String coors, String phase) {
		super(module, file, ARTIFACT_BUILD, coors);
		this.phase = phase;
	}

	public String toString() {
		return super.toString() + SEPARATOR + phase;
	}
}