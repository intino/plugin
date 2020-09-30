package io.intino.plugin.frameworks;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModelListener;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportProvider;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPanel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GorosSupportProvider extends FrameworkSupportInModuleProvider {
	@NotNull
	@Override
	public FrameworkTypeEx getFrameworkType() {
		return GorosFrameworkType.getFrameworkType();
	}

	@Override
	public boolean isEnabledForModuleType(@NotNull ModuleType moduleType) {
		return moduleType instanceof JavaModuleType;
	}

	@Override
	public boolean isSupportAlreadyAdded(@NotNull Module module, @NotNull FacetsProvider facetsProvider) {
		return modernizationFile(module).exists();
	}

	private File modernizationFile(Module module) {
		return new File(IntinoUtil.moduleRoot(module), "modernization.goros");
	}

	@NotNull
	@Override
	public FrameworkSupportInModuleConfigurable createConfigurable(@NotNull FrameworkSupportModel model) {
		return new GorosSupportConfigurable(model);
	}

	private void addSupport(final Module module,
							final ModifiableRootModel rootModel) {
		if (rootModel.getProject().isInitialized()) addModernizationToModule(module);
		else startWithModernization(module);
	}

	private void startWithModernization(final Module module) {
		StartupManager.getInstance(module.getProject()).registerPostStartupActivity(() -> addModernizationToModule(module));
	}

	private void addModernizationToModule(final Module module) {
		try {
			File file = modernizationFile(module);
			file.getParentFile().mkdirs();
			Files.writeString(file.toPath(), ("<modernization>\n" +
					"\t<platform></platform>\n" +
					"\t<model></model>\n" +
					"\t<project name=\"$project\" package=\"\" directory=\"\"/>\n" +
					"\t<module name=\"$module\"/>\n" +
					"\t<definitions excluded=\"\"/>\n" +
					"</modernization>").replace("$project", module.getProject().getName()).replace("$module", module.getName()));
			VirtualFile vFile = VfsUtil.findFileByIoFile(file, true);
			if (vFile != null) vFile.refresh(true, false);
		} catch (IOException ignored) {

		}
	}


	private class GorosSupportConfigurable extends FrameworkSupportInModuleConfigurable implements FrameworkSupportModelListener {
		private final JPanel myMainPanel;

		private GorosSupportConfigurable(FrameworkSupportModel model) {
			model.addFrameworkListener(this);
			this.myMainPanel = new JBPanel<>();
		}


		@Override
		public void frameworkSelected(@NotNull FrameworkSupportProvider frameworkSupportProvider) {
		}

		@Override
		public void frameworkUnselected(@NotNull FrameworkSupportProvider frameworkSupportProvider) {
		}

		@Override
		public void wizardStepUpdated() {
		}

		@Override
		public void addSupport(@NotNull Module module,
							   @NotNull ModifiableRootModel rootModel,
							   @NotNull ModifiableModelsProvider modifiableModelsProvider) {
			GorosSupportProvider.this.addSupport(module, rootModel);
		}

		@Nullable
		@Override
		public JComponent createComponent() {
			return myMainPanel;
		}
	}
}
