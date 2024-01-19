package io.intino.plugin;

import java.io.File;

import static io.intino.plugin.BuildConstants.SEPARATOR;

public class MavenBuildActionMessage extends PostCompileActionMessage {

	private final String phase;

	public MavenBuildActionMessage(String module, File file, String coors, String phase) {
		super(module, file, ObjectType.INVOKE_MAVEN, coors);
		this.phase = phase;
	}

	public String toString() {
		return super.toString() + SEPARATOR + phase;
	}
}