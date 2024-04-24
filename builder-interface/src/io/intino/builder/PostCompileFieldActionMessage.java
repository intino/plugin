package io.intino.builder;

import java.io.File;

import static io.intino.builder.BuildConstants.SEPARATOR;

public class PostCompileFieldActionMessage extends PostCompileActionMessage {
	private final String type;
	private final boolean isStatic;
	private final String modifier;

	public PostCompileFieldActionMessage(String module, File file, String modifier, boolean isStatic, String type, String name) {
		super(module, file, ObjectType.FIELD, name);
		this.type = type;
		this.isStatic = isStatic;
		this.modifier = modifier;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public String type() {
		return type;
	}

	public String modifier() {
		return modifier;
	}

	@Override
	public String toString() {
		return super.toString() + SEPARATOR + modifier + SEPARATOR + isStatic + SEPARATOR + type;
	}
}
