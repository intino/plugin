package io.intino.plugin.project.module;

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.starters.local.*;
import com.intellij.ide.starters.local.wizard.StarterInitialStep;
import com.intellij.ide.starters.local.wizard.StarterLibrariesStep;
import com.intellij.ide.starters.shared.StarterLanguage;
import com.intellij.ide.starters.shared.StarterProjectType;
import com.intellij.ide.starters.shared.StarterSettings;
import com.intellij.ide.starters.shared.StarterTestRunner;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.GitRepositoryInitializer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.dsl.builder.Panel;
import io.intino.Configuration;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.LegioFileCreator;
import io.intino.plugin.project.configuration.MavenConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.moduleRoot;
import static io.intino.plugin.project.configuration.ConfigurationManager.*;
import static java.io.File.separator;

public class NewIntinoModuleBuilder extends StarterModuleBuilder {
	private final List<String> selectedDsls = new ArrayList<>();
	private boolean gorosFramework = false;

	public NewIntinoModuleBuilder() {
		super();
	}


	@Override
	public int getWeight() {
		return 2000;
	}

	@NotNull
	@Override
	public String getBuilderId() {
		return "Intino";
	}

	@NotNull
	@Override
	public String getPresentableName() {
		return "Intino";
	}

	public Icon getBigIcon() {
		return IntinoIcons.LOGO_16;
	}

	@Override
	public Icon getNodeIcon() {
		return getBigIcon();
	}

	@NotNull
	@Override
	public String getDescription() {
		return "Intino project";
	}

	@NotNull
	@Override
	protected List<StarterProjectType> getProjectTypes() {
		return List.of();
	}

	@NotNull
	@Override
	protected List<StarterLanguage> getLanguages() {
		return List.of(StarterSettings.getJAVA_STARTER_LANGUAGE());
	}

	@NotNull
	@Override
	protected StarterPack getStarterPack() {
		return new StarterPack(getBuilderId(),
				List.of(new Starter("intino", "intino", getDependencyConfig("/starters/intino.pom"), Collections.emptyList())));
	}

	@NotNull
	@Override
	public List<Class<? extends ModuleWizardStep>> getIgnoredSteps() {
		return List.of(StarterLibrariesStep.class, ProjectSettingsStep.class);
	}

	@NotNull
	@Override
	protected List<StarterTestRunner> getTestFrameworks() {
		return Collections.emptyList();
	}

	@NotNull
	@Override
	protected List<GeneratorAsset> getAssets(@NotNull Starter starter) {
		return List.of();
	}

	@NotNull
	@Override
	public ModuleType<?> getModuleType() {
		return IntinoModuleType.getModuleType();
	}

	@NotNull
	@Override
	public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
		final ModuleWizardStep[] wizardSteps = super.createWizardSteps(wizardContext, modulesProvider);
		final List<ModuleWizardStep> moduleWizardSteps = new ArrayList<>(Arrays.asList(wizardSteps));
		if (!wizardContext.isCreatingNewProject()) {
			final String suggestedGroup = suggestGroup(modulesProvider);
			if (suggestedGroup != null) getStarterContext().setGroup(suggestedGroup);
			final Path projectFileDirectory = suggestLocation(modulesProvider);
			if (projectFileDirectory != null) wizardContext.setProjectFileDirectory(projectFileDirectory, true);
		}
		return moduleWizardSteps.toArray(new ModuleWizardStep[0]);
	}

	private String suggestGroup(ModulesProvider provider) {
		final Module module = Arrays.stream(provider.getModules()).filter(m -> IntinoUtil.configurationOf(m) instanceof ArtifactLegioConfiguration).findFirst().orElse(null);
		if (module != null) return IntinoUtil.configurationOf(module).artifact().groupId();
		return null;
	}

	private @Nullable Path suggestLocation(ModulesProvider modulesProvider) {
		if (modulesProvider.getModules().length == 0) return null;
		final Module module = modulesProvider.getModules()[0];
		if (module == null) return null;
		final VirtualFile virtualFile = ProjectUtil.guessProjectDir(module.getProject());
		return virtualFile.toNioPath();
	}

	@Override
	protected void setupModule(@NotNull Module module) throws ConfigurationException {
		getStarterContext().setGitIntegration(false);
		getStarterContext().setStarter(getStarterContext().getStarterPack().getStarters().get(0));
		getStarterContext().setStarterDependencyConfig(loadDependencyConfig().get(getStarterContext().getStarter().getId()));
		super.setupModule(module);
	}

	public void setupRootModel(@NotNull ModifiableRootModel rootModel) {
		super.setupRootModel(rootModel);
		final ContentEntry contentEntry = rootModel.getContentEntries()[0];
		final File gen = new File(contentEntry.getFile().getPath(), "gen");
		final File src = new File(contentEntry.getFile().getPath(), "src");
		final File res = new File(contentEntry.getFile().getPath(), "res");
		src.mkdir();
		gen.mkdir();
		res.mkdir();
		final VirtualFile srcVfile = VfsUtil.findFileByIoFile(src, true);
		final VirtualFile genVfile = VfsUtil.findFileByIoFile(gen, true);
		final VirtualFile resVfile = VfsUtil.findFileByIoFile(res, true);
		if (contentEntry.getFile().findChild(IntinoDirectory.INTINO) != null) {
			final VirtualFile intinoVfile = VfsUtil.findFileByIoFile(IntinoDirectory.of(rootModel.getProject()), true);
			if (intinoVfile != null) contentEntry.addExcludeFolder(intinoVfile);
		}
		excludeDirectory(rootModel, contentEntry, ".idea");
		excludeDirectory(rootModel, contentEntry, ".intino");
		contentEntry.addSourceFolder(srcVfile, JavaSourceRootType.SOURCE, JpsJavaExtensionService.getInstance().createSourceRootProperties("", false));
		contentEntry.addSourceFolder(genVfile, JavaSourceRootType.SOURCE, JpsJavaExtensionService.getInstance().createSourceRootProperties("", true));
		contentEntry.addSourceFolder(resVfile, JavaResourceRootType.RESOURCE, JpsJavaExtensionService.getInstance().createResourceRootProperties("", false));
	}

	private void excludeDirectory(@NotNull ModifiableRootModel rootModel, ContentEntry contentEntry, String directory) {
		if (VfsUtil.refreshAndFindChild(contentEntry.getFile(), directory) == null) return;
		VirtualFile baseDirectory = VfsUtil.findFileByIoFile(new File(rootModel.getProject().getBasePath()), true);
		if (baseDirectory != null) {
			final VirtualFile vDirectory = baseDirectory.findChild(directory);
			if (vDirectory != null) contentEntry.addExcludeFolder(vDirectory);
		}
	}

	@NotNull
	@Override
	protected StarterInitialStep createOptionsStep(@NotNull StarterContextProvider contextProvider) {
		return new ModuleOptionsStep(contextProvider);
	}

	@Override
	public @Nullable Module commitModule(@NotNull Project project, @Nullable ModifiableModuleModel model) {
		setName(getStarterContext().getArtifact());
		setContentEntryPath(project.getBasePath() + separator + getName());
		setModuleFilePath(project.getBasePath() + separator + getName() + separator + getName() + ModuleFileType.DOT_DEFAULT_EXTENSION);
		final Module module = super.commitModule(project, model);
		withTask(new Task.Backgroundable(project, " Setting up git repository", false) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				GitRepositoryInitializer.getInstance().initRepository(project, VfsUtil.findFileByIoFile(new File(project.getBasePath()), true), true);
			}
		});
		createIntinoFiles(project, module);
		return module;
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	@NotNull
	@Override
	protected List<String> getFilePathsToOpen() {
		return super.getFilePathsToOpen();//TODO add
	}

	private void createIntinoFiles(@NotNull Project project, @Nullable Module module) {
		if (module == null) return;
		LegioFileCreator legioFileCreator = new LegioFileCreator(module, selectedDsls);
		legioFileCreator.createProjectIfNotExist(module.getProject());
		final VirtualFile file = legioFileCreator.getOrCreateArtifact(getStarterContext().getGroup());
		if (gorosFramework) addModernizationToModule(module);
		if (project.isInitialized()) FileEditorManager.getInstance(project).openFile(file, true);
		else getApplication().invokeLater(() -> FileEditorManager.getInstance(project).openFile(file, true));
		loadConfiguration(module);
	}


	private void addModernizationToModule(@Nullable Module module) {
		try {
			File file = new File(moduleRoot(module), "modernization.goros");
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


	private void loadConfiguration(Module module) {
		final Configuration configuration = register(module, hasExternalProviders() ? newExternalProvider(module) : new MavenConfiguration(module).init());
		configuration.reload();
	}

	private class ModuleOptionsStep extends StarterInitialStep {
		public ModuleOptionsStep(@NotNull StarterContextProvider contextProvider) {
			super(contextProvider);
		}

		@Override
		protected void addFieldsAfter(@NotNull Panel layout) {
			List<String> dsls = new ArrayList<>(dsls());
			dsls.add(0, "");
			layout.row("DSL", row -> {
				row.comboBox(dsls, null).getComponent()
						.addActionListener(actionEvent -> {
							JComboBox source = (JComboBox) actionEvent.getSource();
							String value = source.getSelectedItem().toString();
							selectedDsls.add(value);
						});
				return null;
			});
			layout.row("", row -> {
				row.checkBox("Support Goros framework").getComponent().addActionListener(actionEvent -> gorosFramework = ((JBCheckBox) actionEvent.getSource()).isSelected());
				return null;
			});
		}

		private static Set<String> dsls() {
			ArtifactoryConnector artifactoryConnector = new ArtifactoryConnector(Collections.singletonList(ArtifactoryConnector.repository("intino-maven", Repositories.INTINO_RELEASES)));
			return artifactoryConnector.dsls().stream().map(String::toLowerCase).collect(Collectors.toSet());
		}
	}

}
