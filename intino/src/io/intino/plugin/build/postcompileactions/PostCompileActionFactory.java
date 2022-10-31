package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.builders.BoxBuilderManager;
import io.intino.plugin.project.configuration.Version;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.intino.konos.compiler.shared.PostCompileActionMessage.ObjectType.*;
import static io.intino.plugin.project.Safe.safe;

public class PostCompileActionFactory {


	public static PostCompileAction get(Module module, String type, List<String> parameters) {
		Configuration configuration = IntinoUtil.configurationOf(module);
		Version version = version(configuration);
		if (version == null || version.compareTo(minVersion()) < 0) return null;
		if (type.equals(FIELD.name())) return new FieldCreationAction(module, parameters);
		if (type.equals(METHOD.name())) return new MethodCreationAction(module, parameters);
		if (type.equals(CONFIGURATION_PARAMETER.name()))
			return new ConfigurationParameterCreationAction(module, parameters);
		if (type.equals(CONFIGURATION_DEPENDENCY.name()))
			return new ConfigurationDependencyCreationAction(module, parameters);
		if (type.equals(MODULE.name())) return new ModuleCreationAction(module, parameters);
		if (type.equals(MAIN_CLASS.name())) return new MainClassCreationAction(module, parameters);
		return null;
	}

	@NotNull
	private static Version minVersion() {
		try {
			return new Version(BoxBuilderManager.MinimumVersion);
		} catch (IntinoException e) {
			return null;
		}
	}

	@NotNull
	private static Version version(Configuration configuration) {
		try {
			return new Version(safe(() -> configuration.artifact().box().version()));
		} catch (IntinoException e) {
			return null;
		}
	}
}
