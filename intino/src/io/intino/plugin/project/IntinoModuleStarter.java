package io.intino.plugin.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;
import com.intellij.refactoring.openapi.impl.JavaRenameRefactoringImpl;
import com.intellij.spellchecker.SpellCheckerManager;
import com.intellij.util.Function;
import io.intino.Configuration;
import io.intino.plugin.highlighting.TaraSyntaxHighlighter;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ConfigurationManager;
import io.intino.plugin.project.configuration.MavenConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;


public class IntinoModuleStarter implements ModuleListener, com.intellij.openapi.startup.StartupActivity {

	@Override
	public void runActivity(@NotNull Project project) {
		TaraSyntaxHighlighter.setProject(project);
		addDSLNameToDictionary(project);
		for (Module module : ModuleManager.getInstance(project).getModules()) {
			if (module.isLoaded() && project.isInitialized())
				registerIntinoModule(module);
		}
	}

	private void addDSLNameToDictionary(Project project) {
		for (Module module : ModuleManager.getInstance(project).getModules()) {
			final Configuration conf = IntinoUtil.configurationOf(module);
			if (conf != null && conf.artifact().model() != null && conf.artifact().model().language() != null && conf.artifact().model().language().name() != null)
				SpellCheckerManager.getInstance(project).acceptWordAsCorrect(conf.artifact().model().language().name(), project);
		}
	}

	@Override
	public void moduleAdded(@NotNull Project project, @NotNull Module module) {
		registerIntinoModule(module);
	}

	@Override
	public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
	}

	@Override
	public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
		ConfigurationManager.unregister(module);
	}

	@Override
	public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
		for (Module module : modules) {
			final Configuration conf = IntinoUtil.configurationOf(module);
			if (conf != null && conf.artifact().model() != null && conf.artifact().model().level().isSolution())
				ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
					final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
					progressIndicator.setText("Refactoring java");
					progressIndicator.setIndeterminate(true);
					runRefactor(project, module.getName(), oldNameProvider.fun(module));
				}, "Refactoring java", true, project, null);
		}
	}

	private void registerIntinoModule(@NotNull Module module) {
		if (ConfigurationManager.configurationOf(module) != null) return;
		Configuration configuration = ConfigurationManager.newSuitableProvider(module);
		if (configuration != null) ConfigurationManager.register(module, configuration);
		else {
			configuration = new MavenConfiguration(module);
			if (configuration.isSuitable()) ConfigurationManager.register(module, configuration);
		}
	}

	private void runRefactor(Project project, String newName, String oldName) {
		final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
		final PsiPackage aPackage = psiFacade.findPackage(oldName);
		if (aPackage != null) {
			final JavaRenameRefactoringImpl refactoring = new JavaRenameRefactoringImpl(project, aPackage, newName.toLowerCase(), false, false);
			refactoring.doRefactoring(refactoring.findUsages());
		}
		final File miscDirectory = LanguageManager.getTaraLocalDirectory(project);
		if (!miscDirectory.exists()) return;
		final File[] files = miscDirectory.listFiles();
		if (files == null) return;
		for (File file : files)
			if (file.getName().startsWith(oldName + "."))
				file.renameTo(new File(miscDirectory, newName + "." + FileUtilRt.getExtension(file.getName())));

	}
}