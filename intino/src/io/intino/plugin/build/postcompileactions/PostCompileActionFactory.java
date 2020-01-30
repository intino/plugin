package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import io.intino.plugin.build.PostCompileAction;

import java.util.List;

import static io.intino.plugin.build.postcompileactions.PostCompileActionFactory.ObjectType.*;

public class PostCompileActionFactory {

	public enum ObjectType {
		FIELD, METHOD, CONFIGURATION_PARAMETER, MODULE
	}

	public static PostCompileAction get(Module module, String type, List<String> parameters) {
		if (type.equals(FIELD.name())) return new FieldCreationAction(module, parameters);
		if (type.equals(METHOD.name())) return new MethodCreationAction(module, parameters);
		if (type.equals(CONFIGURATION_PARAMETER.name()))
			return new ConfigurationParameterCreationAction(module, parameters);
		if (type.equals(MODULE.name())) return new ModuleCreationAction(module, parameters);
		return null;
	}
}
