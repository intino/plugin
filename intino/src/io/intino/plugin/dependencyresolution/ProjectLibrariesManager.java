package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VfsUtil;
import io.intino.plugin.dependencyresolution.DependencyCatalog.Dependency;

import java.io.File;

import static com.intellij.openapi.roots.OrderRootType.CLASSES;
import static com.intellij.openapi.roots.OrderRootType.SOURCES;

@SuppressWarnings("Duplicates")
public class ProjectLibrariesManager {
	private final IntinoLibrary table;

	public ProjectLibrariesManager(Project project) {
		this.table = new IntinoLibrary(project);
	}

	public void register(DependencyCatalog catalog) {
		if (!table.isValid()) return;
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed()) registerCatalog(catalog);
		else application.invokeLater(() -> application.runWriteAction(() -> registerCatalog(catalog)));
	}

	public void registerSources(Dependency dependency) {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed()) {
			Library library = table.findLibrary(dependency);
			registerSources(dependency, library.getModifiableModel());
		} else application.invokeLater(() -> application.runWriteAction(() -> {
			Library library = table.findLibrary(dependency);
			registerSources(dependency, library.getModifiableModel());
		}));

	}

	private void registerCatalog(DependencyCatalog catalog) {
		catalog.dependencies().stream().filter(d -> !d.isToModule() && table.findLibrary(d) == null).forEach(this::registerLibraryFor);
	}

	private void registerLibraryFor(Dependency dependency) {
		final LibraryTable.ModifiableModel tableModel = table.model();
		final Library.ModifiableModel libraryModel = tableModel.createLibrary(table.nameOf(dependency)).getModifiableModel();
		libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(dependency.jar), CLASSES);
		registerSources(dependency, libraryModel);
		libraryModel.commit();
		tableModel.commit();
	}

	private void registerSources(Dependency dependency, Library.ModifiableModel libraryModel) {
		File libraryRoot = sourcesFile(dependency);
		if (libraryRoot.exists()) libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(libraryRoot), SOURCES);
	}

	private File sourcesFile(Dependency dependency) {
		return new File(dependency.jar.getAbsolutePath().replace(".jar", "-sources.jar"));
	}
}