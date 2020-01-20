package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiFile;
import io.intino.plugin.actions.archetype.ArchetypeRenderer;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;

public class ArchetypeGeneration {
	public void actionPerformed(AnActionEvent e) {
		PsiFile data = e.getData(PSI_FILE);
		if (data == null) return;
		this.withTask(new Task.Backgroundable(e.getProject(), e.getProject().getName() + ": Generating archetypes...", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
			@Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
				for (Module module : ModuleManager.getInstance(e.getProject()).getModules()) {
					if (WebModuleType.is(module, WebModuleTypeBase.getInstance())) continue;
					Configuration configuration = TaraUtil.configurationOf(module);
					if (configuration != null && configuration.artifact().box() != null)
						new ArchetypeRenderer(module, (LegioConfiguration) configuration).render(VfsUtil.virtualToIoFile(data.getVirtualFile()));
				}
			}
		});
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}
}
