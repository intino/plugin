package io.intino.builder;


public class PostCompileDependantWebModuleActionMessage extends PostCompileActionMessage {
	public PostCompileDependantWebModuleActionMessage(String module, String serviceName) {
		super(module, null, ObjectType.MODULE, serviceName);
	}
}
