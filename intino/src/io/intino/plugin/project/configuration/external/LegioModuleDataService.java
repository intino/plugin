package io.intino.plugin.project.configuration.external;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.externalSystem.JavaModuleData;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.IdeModelsProvider;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractModuleDataService;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;

public class LegioModuleDataService extends AbstractProjectDataService<JavaModuleData, Project> {

	@Override
	public @NotNull Key<JavaModuleData> getTargetDataKey() {
		return JavaModuleData.KEY;
	}

	@Override
	public void importData(@NotNull Collection<? extends DataNode<JavaModuleData>> toImport, @Nullable ProjectData projectData, @NotNull Project project, @NotNull IdeModifiableModelsProvider modelsProvider) {
		Intrinsics.checkNotNullParameter(toImport, "toImport");
		Intrinsics.checkNotNullParameter(project, "project");
		Intrinsics.checkNotNullParameter(modelsProvider, "modelsProvider");
		if (projectData != null) {
			Iterator var5 = toImport.iterator();

			while (var5.hasNext()) {
				DataNode javaModuleNode = (DataNode) var5.next();
				DataNode var10000 = javaModuleNode.getParent(ModuleData.class);
				if (var10000 != null) {
					DataNode moduleNode = var10000;
					Module var10 = (Module) moduleNode.getUserData(AbstractModuleDataService.MODULE_KEY);
					if (var10 != null) {
						Module module = var10;
						Object var11 = javaModuleNode.getData();
						Intrinsics.checkNotNullExpressionValue(var11, "javaModuleNode.data");
						JavaModuleData javaModuleData = (JavaModuleData) var11;
						this.importTargetBytecodeVersion(module, javaModuleData);
					}
				}
			}
		}
	}


	private void importTargetBytecodeVersion(Module module, JavaModuleData javaModuleData) {
		CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(module.getProject());
		String projectTargetBytecodeVersion = compilerConfiguration.getProjectBytecodeTarget();
		String targetBytecodeVersion = javaModuleData.getTargetBytecodeVersion();
		compilerConfiguration.setBytecodeTargetLevel(module, Intrinsics.areEqual(targetBytecodeVersion, projectTargetBytecodeVersion) ? null : targetBytecodeVersion);
	}


	@Override
	public @NotNull Computable<Collection<Project>> computeOrphanData(@NotNull Collection<? extends DataNode<JavaModuleData>> toImport, @NotNull ProjectData projectData, @NotNull Project project, @NotNull IdeModifiableModelsProvider modelsProvider) {
		return super.computeOrphanData(toImport, projectData, project, modelsProvider);
	}

	@Override
	public void postProcess(@NotNull Collection<? extends DataNode<JavaModuleData>> toImport, @Nullable ProjectData projectData, @NotNull Project project, @NotNull IdeModifiableModelsProvider modelsProvider) {
		super.postProcess(toImport, projectData, project, modelsProvider);
	}

	@Override
	public void onSuccessImport(@NotNull Collection<DataNode<JavaModuleData>> imported, @Nullable ProjectData projectData, @NotNull Project project, @NotNull IdeModelsProvider modelsProvider) {
		super.onSuccessImport(imported, projectData, project, modelsProvider);
	}

	@Override
	public void onFailureImport(Project project) {
		super.onFailureImport(project);
	}
}
