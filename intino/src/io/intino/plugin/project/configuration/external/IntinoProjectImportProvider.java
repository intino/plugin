package io.intino.plugin.project.configuration.external;

import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalProjectImportProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntinoProjectImportProvider extends AbstractExternalProjectImportProvider {

	public IntinoProjectImportProvider() {
		super(new IntinoProjectImportBuilder(), IntinoExternalSystemManager.SYSTEM_ID);
	}

	@Override
	public boolean canImport(@NotNull VirtualFile fileOrDirectory, @Nullable Project project) {
		if (super.canImport(fileOrDirectory, project)) return true;
		return !fileOrDirectory.isDirectory() && isLegioFile(fileOrDirectory);
	}

	@Override
	protected boolean canImportFromFile(VirtualFile file) {
		return file.getName().equals("artifact.legio");
	}

	@NotNull
	@Override
	public String getFileSample() {
		return "<b>Artifact</b> module file (artifact.legio)";
	}

	public static boolean isLegioFile(@NotNull VirtualFile file) {
		return file.getName().equals("artifact.legio");
	}
}