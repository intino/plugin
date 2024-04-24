package io.intino.builder;

public class PostCompileConfigurationMainActionMessage extends PostCompileActionMessage {

	public PostCompileConfigurationMainActionMessage(String module, String qualifiedName) {
		super(module, null, ObjectType.MAIN_CLASS, qualifiedName);
	}
}
