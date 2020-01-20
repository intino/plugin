package io.intino.plugin.project.module;

import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.importSources.DetectedContentRoot;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.util.io.FileUtilRt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TaraProjectStructureDetector extends ProjectStructureDetector {

	private static final Logger LOG = Logger.getInstance("#TaraProjectStructureDetector");


	@NotNull
	@Override
	public DirectoryProcessingResult detectRoots(@NotNull File dir,
												 @NotNull File[] children,
												 @NotNull File base,
												 @NotNull List<DetectedProjectRoot> result) {
		LOG.info("Detecting roots under " + dir);
		for (File child : children) {
			final String name = child.getName();
			if (FileUtilRt.extensionEquals(name, "tara")) {
				LOG.info("Found Tara file " + child.getPath());
				result.add(new DetectedContentRoot(dir, "Tara", JavaModuleType.getModuleType(), JavaModuleType.getModuleType()));
				return DirectoryProcessingResult.SKIP_CHILDREN;
			}
		}
		return DirectoryProcessingResult.PROCESS_CHILDREN;
	}

	@Override
	public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
									  @NotNull ProjectDescriptor projectDescriptor,
									  @NotNull ProjectFromSourcesBuilder builder) {
		builder.setupModulesByContentRoots(projectDescriptor, roots);
	}

	@Override
	public List<ModuleWizardStep> createWizardSteps(ProjectFromSourcesBuilder builder, ProjectDescriptor projectDescriptor, javax.swing.Icon stepIcon) {
		return Collections.emptyList();
	}
}
