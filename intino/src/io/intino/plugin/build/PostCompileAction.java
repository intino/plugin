package io.intino.plugin.build;

import com.intellij.openapi.module.Module;

public abstract class PostCompileAction {
	protected final Module module;

	public PostCompileAction(Module module) {
		this.module = module;
	}

	public abstract void execute();


}
