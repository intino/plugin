package io.intino.builder;

import java.io.File;

public abstract class PostCompileActionMessage {
	protected final String module;
	protected final File file;
	protected final ObjectType objectType;
	protected final String name;

	public enum ObjectType {
		FIELD, METHOD, CONFIGURATION_PARAMETER, CONFIGURATION_DEPENDENCY, MODULE, MAIN_CLASS, ARTIFACT_BUILD
	}

	public PostCompileActionMessage(String module, File file, ObjectType objectType, String name) {
		this.module = module;
		this.file = file;
		this.objectType = objectType;
		this.name = name;
	}

	@Override
	public String toString() {
		return module + BuildConstants.SEPARATOR + objectType.name() + BuildConstants.SEPARATOR + file + BuildConstants.SEPARATOR + name;
	}
}
