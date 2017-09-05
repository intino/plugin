package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;

import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.roots.DependencyScope.COMPILE;
import static com.intellij.openapi.roots.ModuleRootModificationUtil.addDependency;
import static com.intellij.openapi.roots.OrderRootType.CLASSES;
import static com.intellij.openapi.roots.OrderRootType.SOURCES;
import static io.intino.plugin.project.LibraryConflictResolver.mustAddEntry;

public class LibraryManager {

	private static final String LEGIO = "Legio: ";
	private Module module;
	private final LibraryTable table;

	public LibraryManager(Module module) {
		this.module = module;
		table = module != null ? LibraryTablesRegistrar.getInstance().getLibraryTable(module.getProject()) : null;
	}

	Map<DependencyScope, List<Library>> registerOrGetLibrary(Map<Artifact, DependencyScope> artifacts, Map<Artifact, Artifact> sources) {
		if (table == null) return Collections.emptyMap();
		Map<DependencyScope, List<Library>> registered = new LinkedHashMap<>();
		for (Artifact artifact : artifacts.keySet()) {
			Library library = findLibrary(artifact);
			if (library == null) library = registerLibrary(artifact, sources.get(artifact));
			if (!registered.containsKey(artifacts.get(artifact)))
				registered.put(artifacts.get(artifact), new ArrayList<>());
			registered.get(artifacts.get(artifact)).add(library);
		}
		return registered;
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
		addToModule(libraries, COMPILE);
		return libraries;
	}

	void addToModule(List<Library> libraries, DependencyScope scope) {
		final List<LibraryOrderEntry> registered = Arrays.stream(ModuleRootManager.getInstance(module).getOrderEntries()).
				filter(e -> e instanceof LibraryOrderEntry).map(o -> (LibraryOrderEntry) o).collect(Collectors.toList());
		final List<Library> toRegister = libraries.stream().filter(library -> !isRegistered(registered, library, scope)).collect(Collectors.toList());
		toRegister.forEach(library -> addDependency(module, library, scope, false));
	}

	private List<Library> compileDependenciesOf(Module module) {
		final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
		return Arrays.stream(model.getOrderEntries()).
				filter(o -> o.isValid() && o instanceof LibraryOrderEntry && ((ExportableOrderEntry) o).getScope().equals(COMPILE)).
				map(l -> ((LibraryOrderEntry) l).getLibrary()).collect(Collectors.toList());
	}

	public static void clean(Module module, List<Library> newLibraries) {
		final LibraryTable table = LibraryTablesRegistrar.getInstance().getLibraryTable(module.getProject());
		final ModifiableRootModel rootModel = ApplicationManager.getApplication().runReadAction((Computable<ModifiableRootModel>) () -> ModuleRootManager.getInstance(module).getModifiableModel());
		final List<LibraryOrderEntry> toRemove = collectInvalidLibraries(rootModel, newLibraries);
		toRemove.addAll(collectInvalidLibraries(rootModel));
		removeLibraries(module, rootModel, table, toRemove);
	}

	private static void removeLibraries(Module module, ModifiableRootModel modifiableModel, LibraryTable table, List<LibraryOrderEntry> toRemove) {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed() || !modifiableModel.isWritable()) {
			removeInvalidEntries(modifiableModel, toRemove);
			commit(module, table, toRemove, modifiableModel);
		} else
			application.invokeLater(() -> application.runWriteAction(() -> {
				removeInvalidEntries(modifiableModel, toRemove);
				commit(module, table, toRemove, modifiableModel);
			}));
	}

	private static List<LibraryOrderEntry> collectInvalidLibraries(ModifiableRootModel modifiableModel, List<Library> newLibraries) {
		return Arrays.stream(modifiableModel.getOrderEntries()).
				filter(e -> e instanceof LibraryOrderEntry && (((LibraryOrderEntry) e).getLibrary() == null || !newLibraries.contains(((LibraryOrderEntry) e).getLibrary()))).
				map(l -> (LibraryOrderEntry) l).
				collect(Collectors.toList());
	}

	private static List<LibraryOrderEntry> collectInvalidLibraries(ModifiableRootModel modifiableModel) {
		Map<String, String> entries = new HashMap<>();
		List<LibraryOrderEntry> toRemove = new ArrayList<>();
		final List<LibraryOrderEntry> libraryOrderEntries = Arrays.stream(modifiableModel.getOrderEntries()).
				filter(e -> e instanceof LibraryOrderEntry).map(e -> ((LibraryOrderEntry) e)).collect(Collectors.toList());
		libraryOrderEntries.forEach(e -> {
			final Library l = e.getLibrary();
			if (l == null || l.getName() == null) return;
			if (!l.getName().startsWith(LEGIO)) {
				entries.put(l.getName(), "");
				return;
			}
			final String libraryName = l.getName().substring(0, l.getName().lastIndexOf(":"));
			final String version = l.getName().substring(l.getName().lastIndexOf(":") + 1);
			if (!entries.containsKey(libraryName)) entries.put(libraryName, version);
			else
				toRemove.add(entries.get(libraryName).compareTo(version) > 1 ? e : searchLibrary(libraryOrderEntries, libraryName));
		});
		return toRemove;
	}

	private static LibraryOrderEntry searchLibrary(List<LibraryOrderEntry> libraryOrderEntries, String name) {
		for (LibraryOrderEntry entry : libraryOrderEntries)
			if (entry.getLibrary() != null && entry.getLibrary().getName().startsWith(name + ":"))
				return entry;
		return null;
	}

	@NotNull
	private static ModifiableRootModel removeInvalidEntries(ModifiableRootModel modifiableModel, List<LibraryOrderEntry> toRemove) {
		try {
			for (LibraryOrderEntry entry : toRemove)
				if (entry != null) {
					if (entry.isValid()) modifiableModel.removeOrderEntry(entry);
					else {
						final LibraryOrderEntry libraryOrderEntry = modifiableModel.findLibraryOrderEntry(entry.getLibrary());
						if (libraryOrderEntry != null && libraryOrderEntry.isValid())
							modifiableModel.removeOrderEntry(libraryOrderEntry);
					}

				}
		} catch (Throwable ignored) {
		}
		return modifiableModel;
	}

	private static void commit(Module module, LibraryTable table, List<LibraryOrderEntry> toRemove, ModifiableRootModel modifiableModel) {
		modifiableModel.commit();
		toRemove.stream().filter(library -> !isUsed(module, library.getLibrary())).
				forEach(e -> {
					if (e.getLibrary() != null) table.removeLibrary(e.getLibrary());
				});
	}

	private static boolean isUsed(Module module, Library library) {
		if (library == null) return false;
		final List<Module> others = Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).collect(Collectors.toList());
		for (Module other : others) {
			final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(other).getModifiableModel();
			final OrderEntry orderEntry = Arrays.stream(modifiableModel.getOrderEntries()).
					filter(e -> e instanceof LibraryOrderEntry && library.equals(((LibraryOrderEntry) e).getLibrary())).findFirst().orElse(null);
			if (orderEntry != null) return true;
		}
		return false;
	}

	private Library findLibrary(Artifact artifact) {
		for (Library library : table.getLibraries())
			if (nameOf(artifact).equals(library.getName())) return library;
		return null;
	}

	private Library registerLibrary(Artifact dependency, Artifact sources) {
		final LibraryTable.ModifiableModel tableModel = table.getModifiableModel();
		final Library library = tableModel.createLibrary(nameOf(dependency));
		final Library.ModifiableModel libraryModel = library.getModifiableModel();
		libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(dependency.getFile()), CLASSES);
		if (sources != null) libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(sources.getFile()), SOURCES);
		libraryModel.commit();
		tableModel.commit();
		return library;
	}

	private boolean isRegistered(List<LibraryOrderEntry> registered, Library library, DependencyScope scope) {
		for (LibraryOrderEntry entry : registered)
			if ((library.equals(entry.getLibrary()) && (entry.getScope().equals(scope) || entry.getScope() == COMPILE)) || !mustAddEntry(library, registered))
				return true;
		return false;
	}

	private String nameOf(Artifact dependency) {
		return LEGIO + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
	}

	private String nameOf(String dependency) {
		return LEGIO + dependency;
	}
}
