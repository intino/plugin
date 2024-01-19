package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import io.intino.plugin.build.PostCompileAction;

import java.util.List;

import static io.intino.konos.compiler.shared.PostCompileActionMessage.ObjectType.*;
import static io.intino.plugin.PostCompileActionMessage.ObjectType.INVOKE_MAVEN;

public class PostCompileActionFactory {
	public static PostCompileAction get(Module module, String type, List<String> parameters) {
		if (type.equals(FIELD.name())) return new FieldCreationAction(module, parameters);
		if (type.equals(METHOD.name())) return new MethodCreationAction(module, parameters);
		if (type.equals(CONFIGURATION_PARAMETER.name()))
			return new ConfigurationParameterCreationAction(module, parameters);
		if (type.equals(CONFIGURATION_DEPENDENCY.name()))
			return new ConfigurationDependencyCreationAction(module, parameters);
		if (type.equals(MODULE.name())) return new ModuleCreationAction(module, parameters);
		if (type.equals(MAIN_CLASS.name())) return new MainClassCreationAction(module, parameters);
		if (type.equals(INVOKE_MAVEN.name())) return new MavenInvokeAction(module, parameters);
		return null;
	}
}
