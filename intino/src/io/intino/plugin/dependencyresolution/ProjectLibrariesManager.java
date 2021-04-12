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
			if (library == null) return;
			Library.ModifiableModel model = library.getModifiableModel();
			registerSources(dependency, model);
			model.commit();
		} else application.invokeLater(() -> application.runWriteAction(() -> {
			Library.ModifiableModel model = table.findLibrary(dependency).getModifiableModel();
			registerSources(dependency, model);
			model.commit();
		}));

	}

	private void registerCatalog(DependencyCatalog catalog) {
		Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> {
				catalog.dependencies().stream().filter(d -> !d.isToModule() && table.findLibrary(d) == null).forEach(this::registerClasses);
				if (table.model().isChanged()) table.model().commit();
			});
		else application.invokeLater(() -> application.runWriteAction(() -> {
			catalog.dependencies().stream().filter(d -> !d.isToModule() && table.findLibrary(d) == null).forEach(this::registerClasses);
			if (table.model().isChanged()) table.model().commit();
		}));
	}

	private void registerClasses(Dependency dependency) {
		final LibraryTable.ModifiableModel tableModel = table.model();
		Library library = tableModel.getLibraryByName(table.nameOf(dependency));
		if (library == null) library = tableModel.createLibrary(table.nameOf(dependency));
		final Library.ModifiableModel libraryModel = library.getModifiableModel();
		File jar = new File(dependency.jar.getPath()).getAbsoluteFile();
		libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(jar), CLASSES);
		registerSources(dependency, libraryModel);
		libraryModel.commit();
	}

	private void registerSources(Dependency dependency, Library.ModifiableModel libraryModel) {
		File libraryRoot = sourcesFile(dependency);
		if (libraryRoot.exists()) libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(libraryRoot), SOURCES);
	}

	private File sourcesFile(Dependency dependency) {
		return dependency.jar.getName().endsWith("-sources.jar") ? dependency.jar : new File(dependency.jar.getAbsolutePath().replace(".jar", "-sources.jar"));
	}
}