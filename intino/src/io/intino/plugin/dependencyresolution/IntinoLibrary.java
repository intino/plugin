package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTable.ModifiableModel;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;

import java.util.Arrays;
import java.util.List;

public class IntinoLibrary {
	public static final String INTINO = "Intino: ";
	private final LibraryTable table;
	private final ModifiableModel modifiableModel;

	IntinoLibrary(Project project) {
		this.table = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
		this.modifiableModel = table.getModifiableModel();

	}

	Library findLibrary(DependencyCatalog.Dependency dependency) {
		for (Library library : table.getLibraries())
			if (nameOf(dependency).equals(library.getName())) return library;
		return null;
	}

	String nameOf(DependencyCatalog.Dependency dependency) {
		return INTINO + dependency.groupId() + ":" + dependency.artifactId() + ":" + dependency.version();
	}

	List<Library> libraries() {
		return Arrays.asList(table.getLibraries());
	}

	ModifiableModel model() {
		return modifiableModel;
	}

	boolean isValid() {
		return table != null;
	}
}
