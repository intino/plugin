package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	public LibraryManager(Module module) {
		this.module = module;
		table = module != null ? LibraryTablesRegistrar.getInstance().getLibraryTable(module.getProject()) : null;
	}

	public Library findLibrary(String artifact) {
		if (table == null) return null;
		final String dependency = nameOf(artifact);
		for (Library library : table.getLibraries())
			if (dependency.equals(library.getName())) return library;
		return null;
	}

	List<Library> registerOrGetLibrary(List<Artifact> artifacts) {
		if (table == null) return Collections.emptyList();
		List<Library> result = new ArrayList<>();
		for (Artifact artifact : artifacts) {
			Library library = findLibrary(artifact);
			if (library == null) library = registerLibrary(artifact);
			result.add(library);
		}
		return result;
	}

	List<Library> resolveAsModuleDependency(Module moduleDependency) {
		if (table == null) return Collections.emptyList();
		final ModuleRootManager manager = ModuleRootManager.getInstance(this.module);
		final ModifiableRootModel modifiableModel = manager.getModifiableModel();
		if (!manager.isDependsOn(moduleDependency)) {
			modifiableModel.addModuleOrderEntry(moduleDependency);
			modifiableModel.commit();
		}
		final List<Library> libraries = compileDependenciesOf(moduleDependency);
		addToModule(libraries, false);
		return libraries;
	}

	void addToModule(List<Library> libraries, boolean test) {
		final List<LibraryOrderEntry> registered = Arrays.stream(ModuleRootManager.getInstance(module).getOrderEntries()).
				filter(e -> e instanceof LibraryOrderEntry).map(o -> (LibraryOrderEntry) o).collect(Collectors.toList());
		final List<Library> toRegister = libraries.stream().filter(library -> !isRegistered(registered, library)).collect(Collectors.toList());
		toRegister.forEach(library -> addDependency(module, library, test ? TEST : COMPILE, false));
	}

	private List<Library> compileDependenciesOf(Module module) {
		final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
		return Arrays.stream(model.getOrderEntries()).filter(o -> o.isValid() && o instanceof LibraryOrderEntry).map(l -> ((LibraryOrderEntry) l).getLibrary()).collect(Collectors.toList());
	}

	public static void removeOldLibraries(Module module, List<Library> libraries) {
		final LibraryTable table = LibraryTablesRegistrar.getInstance().getLibraryTable(module.getProject());
		List<Library> toRemove = new ArrayList<>();
		for (Library library : table.getLibraries())
			if (!libraries.contains(library)) toRemove.add(library);
		if (toRemove.isEmpty()) return;
		final Application application = ApplicationManager.getApplication();
		final ModifiableRootModel modifiableModel = application.isReadAccessAllowed() ?
				removeInvalidEntries(module, toRemove) :
				application.runReadAction((Computable<ModifiableRootModel>) () -> removeInvalidEntries(module, toRemove));
		if (application.isWriteAccessAllowed()) commit(module, table, toRemove, modifiableModel);
		else application.invokeLater(() -> application.runWriteAction(() -> commit(module, table, toRemove, modifiableModel)));
	}

	@NotNull
	private static ModifiableRootModel removeInvalidEntries(Module module, List<Library> toRemove) {
		final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
		final List<LibraryOrderEntry> invalidEntries = Arrays.stream(modifiableModel.getOrderEntries()).
				filter(e -> e instanceof LibraryOrderEntry && toRemove.contains(((LibraryOrderEntry) e).getLibrary())).
				map(l -> (LibraryOrderEntry) l).
				collect(Collectors.toList());
		invalidEntries.forEach(modifiableModel::removeOrderEntry);
		return modifiableModel;
	}

	private static boolean isUsedByOthers(Module module, Library library) {
		final List<Module> others = Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).filter(m -> !m.equals(module)).collect(Collectors.toList());
		for (Module other : others) {
			final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(other).getModifiableModel();
			final OrderEntry orderEntry = Arrays.stream(modifiableModel.getOrderEntries()).filter(e -> e instanceof LibraryOrderEntry && library.equals(((LibraryOrderEntry) e).getLibrary())).findFirst().orElse(null);
			if (orderEntry != null) return true;
		}
		return false;
	}

	private static void commit(Module module, LibraryTable table, List<Library> toRemove, ModifiableRootModel modifiableModel) {
		modifiableModel.commit();
		toRemove.stream().
				filter(library -> !isUsedByOthers(module, library)).
				forEach(table::removeLibrary);
	}

	private Library findLibrary(Artifact artifact) {
		for (Library library : table.getLibraries())
			if (nameOf(artifact).equals(library.getName())) return library;
		return null;
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

	private String nameOf(Artifact dependency) {
		return LEGIO + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
	}

	private String nameOf(String dependency) {
		return LEGIO + dependency;
	}
}