package io.intino.plugin.project.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;
import com.intellij.refactoring.openapi.impl.JavaRenameRefactoringImpl;
import com.intellij.spellchecker.SpellCheckerManager;
import com.intellij.util.Function;
import io.intino.Configuration;
import io.intino.plugin.highlighting.TaraSyntaxHighlighter;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.ConfigurationManager;
import io.intino.plugin.project.configuration.MavenConfiguration;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.intellij.openapi.util.io.FileUtilRt.getExtension;
import static io.intino.plugin.project.Safe.safeList;

public class IntinoModuleStarter implements ModuleListener, ProjectActivity {

	@Nullable
	@Override
	public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
		TaraSyntaxHighlighter.setProject(project);
		addDSLNameToDictionary(project);
		for (Module module : ModuleManager.getInstance(project).getModules()) {
			if (module.isLoaded() && project.isInitialized())
				registerIntinoModule(module);
		}
		return continuation;
	}


	private void addDSLNameToDictionary(Project project) {
		SpellCheckerManager checkerManager = SpellCheckerManager.getInstance(project);
		for (Module module : ModuleManager.getInstance(project).getModules())
			safeList(() -> IntinoUtil.configurationOf(module).artifact().dsls()).stream()
					.map(Configuration.Artifact.Dsl::name)
					.filter(Objects::nonNull)
					.forEach(name -> checkerManager.acceptWordAsCorrect(name, project));
	}

	@Override
	public void modulesAdded(@NotNull Project project, @NotNull List<? extends Module> modules) {
		modules.forEach(this::registerIntinoModule);
	}

	@Override
	public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
		ConfigurationManager.unregister(module);
	}

	@Override
	public void modulesRenamed(@NotNull Project project, @NotNull List<? extends Module> modules, @NotNull Function<? super Module, String> oldNameProvider) {
		for (Module module : modules) {
			final Configuration conf = IntinoUtil.configurationOf(module);
			if (!(conf instanceof ArtifactLegioConfiguration)) return;
			ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
				final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
				progressIndicator.setText("Refactoring java");
				progressIndicator.setIndeterminate(true);
				runRefactor(project, conf, module.getName(), oldNameProvider.fun(module));
			}, "Refactoring Java", true, project, null);
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

	private void runRefactor(Project project, Configuration conf, String newName, String oldName) {
		String oldPackage = conf.artifact().code().generationPackage();
		if (oldPackage == null) return;
		conf.artifact().name(newName);
		final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
		final PsiPackage aPackage = psiFacade.findPackage(oldPackage);
		if (aPackage != null) {
			final JavaRenameRefactoringImpl refactoring = new JavaRenameRefactoringImpl(project, aPackage, newName.toLowerCase(), false, false);
			refactoring.doRefactoring(refactoring.findUsages());
		}
		final File taraDir = LanguageManager.getTaraLocalDirectory(project);
		if (!taraDir.exists()) return;
		final File[] files = taraDir.listFiles();
		if (files == null) return;
		Arrays.stream(files)
				.filter(file -> file.getName().startsWith(oldName + "."))
				.findFirst()
				.ifPresent(file -> file.renameTo(new File(taraDir, newName + "." + getExtension(file.getName()))));
	}
}
