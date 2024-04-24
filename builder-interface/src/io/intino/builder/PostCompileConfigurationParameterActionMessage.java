package io.intino.builder;

public class PostCompileConfigurationParameterActionMessage extends PostCompileActionMessage {

	public PostCompileConfigurationParameterActionMessage(String module, String name) {
		super(module, null, ObjectType.CONFIGURATION_PARAMETER, name);
	}
}
