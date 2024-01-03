package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VfsUtil;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;

import java.io.File;

import static com.intellij.openapi.roots.OrderRootType.CLASSES;
import static com.intellij.openapi.roots.OrderRootType.SOURCES;
import static io.intino.plugin.dependencyresolution.IntinoLibrary.libraryLabelOf;

@SuppressWarnings("Duplicates")
public class ProjectLibrariesManager {
	private final IntinoLibrary table;

	public ProjectLibrariesManager(Project project) {
		this.table = new IntinoLibrary(project);
	}

	public void register(DependencyCatalog catalog) {
		final Application application = ApplicationManager.getApplication();
		application.runWriteAction(() -> registerCatalog(catalog));
	}

	public void registerSources(Artifact artifact) {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed()) {
			Library library = table.findLibrary(artifact);
			if (library == null) return;
			Library.ModifiableModel model = library.getModifiableModel();
			registerSources(artifact, model);
			model.commit();
		} else application.invokeLater(() -> application.runWriteAction(() -> {
			Library.ModifiableModel model = table.findLibrary(artifact).getModifiableModel();
			registerSources(artifact, model);
			model.commit();
		}));

	}

	private void registerCatalog(DependencyCatalog catalog) {
		Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed()) {
			catalog.dependencies().stream().filter(d -> table.findLibrary(d.getArtifact()) == null).forEach(this::registerClasses);
			if (table.model().isChanged()) table.model().commit();
		} else application.invokeLater(() -> application.runWriteAction(() -> {
			catalog.dependencies().stream().filter(d -> table.findLibrary(d.getArtifact()) == null).forEach(this::registerClasses);
			if (table.model().isChanged()) table.model().commit();
		}));
	}

	private void registerClasses(Dependency dependency) {
		final LibraryTable.ModifiableModel tableModel = table.model();
		Library library = tableModel.getLibraryByName(libraryLabelOf(dependency.getArtifact()));
		if (library == null) library = tableModel.createLibrary(libraryLabelOf(dependency.getArtifact()));
		final Library.ModifiableModel libraryModel = library.getModifiableModel();
		File jar = dependency.getArtifact().getFile().getAbsoluteFile();
		libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(jar), CLASSES);
		registerSources(dependency.getArtifact(), libraryModel);
		libraryModel.commit();
	}

	private void registerSources(Artifact dependency, Library.ModifiableModel libraryModel) {
		File libraryRoot = sourcesFile(dependency);
		if (libraryRoot.exists()) libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(libraryRoot), SOURCES);
	}

	private File sourcesFile(Artifact dependency) {
		return dependency.getFile().getName().endsWith("-sources.jar") ?
				dependency.getFile() :
				new File(dependency.getFile().getAbsolutePath().replace(".jar", "-sources.jar"));
	}
}