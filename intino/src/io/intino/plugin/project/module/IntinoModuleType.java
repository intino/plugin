package io.intino.plugin.project.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.project.configuration.LegioFileCreator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class IntinoModuleType extends ModuleType<NewIntinoModuleBuilder> {

	@SuppressWarnings("WeakerAccess")
	public static final String INTINO_MODULE = "INTINO_MODULE";

	public IntinoModuleType() {
		super(INTINO_MODULE);
	}

	public static ModuleType<?> getModuleType() {
		ModuleType<?> moduleType = ModuleTypeManager.getInstance().findByID(INTINO_MODULE);
		return moduleType != null ? moduleType : ModuleType.EMPTY;
	}

	public static boolean isIntino(Module module) {
		return module != null && !module.isDisposed() && (isIntinoModule(module) || ModuleType.is(module, getModuleType()));
	}

	private static boolean isIntinoModule(Module module) {
		return new LegioFileCreator(module).legioFile().exists();
	}

	@NotNull
	@Override
	public NewIntinoModuleBuilder createModuleBuilder() {
		return new NewIntinoModuleBuilder();
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
	public Icon getNodeIcon(boolean isOpened) {
		return IntinoIcons.MODEL_16;
	}
}
