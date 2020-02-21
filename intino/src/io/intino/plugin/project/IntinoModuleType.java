package io.intino.plugin.project;

import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class IntinoModuleType extends JavaModuleType {

	@SuppressWarnings("WeakerAccess")
	public static final String INTINO_MODULE_OPTION_NAME = "io.intino.tara.isIntinoModule";
	public static final String TARA_MODULE_OPTION_NAME = "io.intino.tara.isTaraModule";
	private static final String TARA_MODULE = "TARA_MODULE";

	public IntinoModuleType(@NonNls String id) {
		super(id);
	}

	public IntinoModuleType() {
		this(TARA_MODULE);
	}

	public static boolean isIntino(Module module) {
		return module != null && !module.isDisposed() && (isIntinoModule(module) || ModuleType.is(module, ModuleTypeManager.getInstance().findByID(TARA_MODULE)));
	}

	private static boolean isIntinoModule(Module module) {
		return "true".equals(module.getOptionValue(TARA_MODULE_OPTION_NAME)) || "true".equals(module.getOptionValue(INTINO_MODULE_OPTION_NAME));
	}

	@NotNull
	@Override
	public String getName() {
		return "Intino";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "Intino module";
	}

	@NotNull
	@Override
	public Icon getNodeIcon(@Deprecated boolean isOpened) {
		return IntinoIcons.MODEL_16;
	}
}
