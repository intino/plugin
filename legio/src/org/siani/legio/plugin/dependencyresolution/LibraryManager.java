package org.siani.legio.plugin.dependencyresolution;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VfsUtil;
import org.sonatype.aether.artifact.Artifact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.openapi.roots.DependencyScope.COMPILE;
import static com.intellij.openapi.roots.DependencyScope.TEST;
import static com.intellij.openapi.roots.ModuleRootModificationUtil.addDependency;
import static com.intellij.openapi.roots.OrderRootType.CLASSES;

public class LibraryManager {

	private static final String LEGIO = "Legio: ";
	private Module module;
	private final LibraryTable table;

	LibraryManager(Module module) {
		this.module = module;
		table = LibraryTablesRegistrar.getInstance().getLibraryTable(module.getProject());

	}


	List<Library> registerOrGetLibrary(List<Artifact> artifacts) {
		List<Library> result = new ArrayList<>();
		for (Artifact artifact : artifacts) {
			Library library = findLibrary(table, artifact);
			if (library == null) library = registerLibrary(artifact);
			result.add(library);
		}
		return result;
	}

	void removeOldLibraries(List<Library> libraries) {
		List<Library> toRemove = new ArrayList<>();
		for (Library library : table.getLibraries()) if (!libraries.contains(library)) toRemove.add(library);
		toRemove.forEach(table::removeLibrary);
		final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
		final LibraryTable moduleLibraryTable = modifiableModel.getModuleLibraryTable();
		final List<Library> moduleLibraries = Arrays.asList(moduleLibraryTable.getLibraries());
		toRemove.stream().filter(moduleLibraries::contains).forEach(l -> moduleLibraryTable.getModifiableModel().removeLibrary(l));
		moduleLibraryTable.getModifiableModel().commit();
		modifiableModel.commit();
	}

	void addToModule(List<Library> libraries, boolean test) {
		final List<LibraryOrderEntry> registered = Arrays.stream(ModuleRootManager.getInstance(module).getOrderEntries()).filter(e -> e instanceof LibraryOrderEntry).map(o -> (LibraryOrderEntry) o).collect(Collectors.toList());
		libraries.stream().filter(library -> !isRegistered(registered, library)).forEach(library -> addDependency(module, library, test ? TEST : COMPILE, false));
	}

	void removeOldVersionsOf(List<Library> libraries) {
		//TODO
	}

	private Library registerLibrary(Artifact dependency) {
		final LibraryTable.ModifiableModel tableModel = table.getModifiableModel();
		final Library library = tableModel.createLibrary(nameOf(dependency));
		final Library.ModifiableModel libraryModel = library.getModifiableModel();
		libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(dependency.getFile()), CLASSES);
		libraryModel.commit();
		tableModel.commit();
		return library;
	}

	private boolean isRegistered(List<LibraryOrderEntry> registered, Library library) {
		for (LibraryOrderEntry entry : registered)
			if (library.equals(entry.getLibrary())) return true;
		return false;
	}

	private Library findLibrary(LibraryTable libraryTable, Artifact artifact) {
		for (Library library : libraryTable.getLibraries())
			if (nameOf(artifact).equals(library.getName())) return library;
		return null;
	}

	private String nameOf(Artifact dependency) {
		return LEGIO + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
	}
}
