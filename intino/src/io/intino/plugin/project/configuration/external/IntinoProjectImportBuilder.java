package io.intino.plugin.project.configuration.external;

import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsTree;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;

public class IntinoProjectImportBuilder extends ProjectImportBuilder<Object> {
	private Parameters myParameters;

	private static class Parameters {
		private Project myProjectToUpdate;

		private Path myImportRootDirectory;
		private VirtualFile myImportProjectFile;
		private List<VirtualFile> myFiles;

		private MavenProjectsTree myMavenProjectTree;
		private List<MavenProject> mySelectedProjects;

		private boolean myOpenModulesConfigurator;
	}

	@Override
	public @NotNull
	@Nls(capitalization = Nls.Capitalization.Sentence) String getName() {
		return "Intino";
	}

	@Override
	public Icon getIcon() {
		return IntinoIcons.ICON_16;
	}

	@Override
	public boolean isMarked(Object element) {
		return false;
	}

	@Override
	public boolean isSuitableSdkType(SdkTypeId sdkType) {
		return sdkType == JavaSdk.getInstance();
	}

	@Override
	public void setOpenProjectSettingsAfter(boolean on) {
		getParameters().myOpenModulesConfigurator = on;
	}

	private Parameters getParameters() {
		if (myParameters == null) myParameters = new Parameters();
		return myParameters;
	}

	@Override
	public @Nullable List<Module> commit(Project project, ModifiableModuleModel model, ModulesProvider modulesProvider, ModifiableArtifactModel artifactModel) {
		System.out.println("ssss");
		return null;
	}
}
