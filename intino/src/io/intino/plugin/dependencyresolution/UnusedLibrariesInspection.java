package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.Computable;

import java.util.Arrays;
import java.util.List;

public class UnusedLibrariesInspection {
	private final Project project;

	public UnusedLibrariesInspection(Project project) {
		this.project = project;
	}

	public void cleanUp() {
		IntinoLibrary intinoLibrary = new IntinoLibrary(project);
		LibraryTable.ModifiableModel model = intinoLibrary.model();
		List<Library> toRemove = intinoLibrary.libraries().stream().filter(library -> !isUsed(library)).toList();
		if (toRemove.isEmpty()) return;
		Application application = ApplicationManager.getApplication();
		application.runWriteAction(() -> {
			toRemove.forEach(model::removeLibrary);
			model.commit();
		});
	}

	private boolean isUsed(Library library) {
		return Arrays.stream(ModuleManager.getInstance(project).getModules()).anyMatch(module -> contains(module, library));
	}

	private boolean contains(Module module, Library lib) {
		return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
			ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
			return Arrays.stream(model.getOrderEntries()).anyMatch(entry -> entry instanceof LibraryOrderEntry && lib.equals(((LibraryOrderEntry) entry).getLibrary()));
		});
	}
}
