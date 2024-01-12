package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import io.intino.Configuration;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.eclipse.aether.graph.Dependency;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.intellij.openapi.roots.ModuleRootModificationUtil.addDependency;
import static io.intino.plugin.dependencyresolution.IntinoLibrary.INTINO;
import static io.intino.plugin.dependencyresolution.IntinoLibrary.libraryIdentifierOf;

@SuppressWarnings("Duplicates")
public class ModuleLibrariesManager {
	private final Module module;
	private final IntinoLibrary table;

	public ModuleLibrariesManager(Module module) {
		this.module = module;
		this.table = new IntinoLibrary(module.getProject());
	}

	public void merge(DependencyCatalog catalog) {
		removeInvalidDependencies(catalog);
		addNewDependencies(catalog);
	}

	private void addNewDependencies(DependencyCatalog catalog) {
		catalog.dependencies().stream()
				.filter(d -> !isAlreadyAdded(d))
				.forEach(this::addLibrary);
		catalog.moduleDependencies().entrySet().stream()
				.filter(e -> !isAlreadyAdded(e.getKey()))
				.forEach(e -> addDependency(module, e.getKey(), DependencyScope.valueOf(e.getValue()), false));
	}

	public boolean isAlreadyAdded(Module moduleReference) {
		return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
			ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
			return Arrays.asList(model.getModuleDependencies()).contains(moduleReference);
		});
	}

	public boolean isAlreadyAdded(Dependency dependency) {
		String label = IntinoLibrary.libraryLabelOf(dependency.getArtifact());
		Module moduleDependency = moduleOf(libraryIdentifierOf(dependency.getArtifact()));
		return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
			ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
			return Arrays.stream(model.getOrderEntries())
					.anyMatch(entry -> asLibrary(entry, label) || asModuleDependency(entry, moduleDependency));
		});
	}

	private boolean asModuleDependency(OrderEntry entry, Module moduleDependency) {
		return (entry instanceof ModuleOrderEntry m) && Objects.equals(m.getModule(), moduleDependency);
	}

	private static boolean asLibrary(OrderEntry entry, String label) {
		return entry instanceof LibraryOrderEntry && label.equals(((LibraryOrderEntry) entry).getLibraryName());
	}

	private Module moduleOf(String identifier) {
		String[] names = identifier.split(":");
		for (Module m : Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).filter(module -> !module.equals(this.module)).toList()) {
			final Configuration configuration = IntinoUtil.configurationOf(m);
			if (!(configuration instanceof ArtifactLegioConfiguration)) continue;
			Configuration.Artifact artifact = configuration.artifact();
			if (names[0].equals(artifact.groupId().toLowerCase()) && names[1].equals(artifact.name().toLowerCase()) && names[2].equalsIgnoreCase(artifact.version()))
				return m;
		}
		return null;
	}

	private void addLibrary(Dependency dependency) {
		Library library = table.findLibrary(dependency.getArtifact());
		if (library != null)
			addDependency(module, library, DependencyScope.valueOf(dependency.getScope().toUpperCase()), false);
	}

	private void removeInvalidDependencies(DependencyCatalog catalog) {
		final ModifiableRootModel rootModel = ApplicationManager.getApplication().runReadAction((Computable<ModifiableRootModel>) () -> ModuleRootManager.getInstance(module).getModifiableModel());
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed() || !rootModel.isWritable())
			removeInvalidEntries(rootModel, catalog).commit();
		else
			application.invokeLater(() -> application.runWriteAction(() -> removeInvalidEntries(rootModel, catalog).commit()));
	}

	private ModifiableRootModel removeInvalidEntries(ModifiableRootModel model, DependencyCatalog catalog) {
		Arrays.stream(model.getOrderEntries())
				.filter(entry -> !(entry instanceof ModuleSourceOrderEntry) && !(entry instanceof JdkOrderEntry))
				.filter(entry -> !entry.isValid() || !isSuitable(entry, catalog))
				.forEach(model::removeOrderEntry);
		return model;
	}

	private boolean isSuitable(OrderEntry e, DependencyCatalog catalog) {
		String library = identifierOf(e);
		String scope = scope(e);
		return library != null && (match(catalog.dependencies(), library, scope) || match(catalog.moduleDependencies(), library, scope));
	}

	private boolean match(Map<Module, String> modules, String library, String scope) {
		return modules.entrySet().stream().anyMatch(e -> {
			Configuration.Artifact artifact = IntinoUtil.configurationOf(e.getKey()).artifact();
			String identifier = String.join(":", artifact.groupId(), artifact.name(), artifact.version());
			return identifier.equals(library) && scope.equalsIgnoreCase(e.getValue());
		});
	}

	private static boolean match(List<Dependency> dependencies, String library, String scope) {
		return dependencies.stream()
				.anyMatch(dependency -> libraryIdentifierOf(dependency.getArtifact()).equals(library) && scope.equalsIgnoreCase(dependency.getScope()));
	}


	private String identifierOf(OrderEntry e) {
		if (e instanceof LibraryOrderEntry) {
			Library library = ((LibraryOrderEntry) e).getLibrary();
			if (library == null || library.getName() == null) return null;
			return library.getName().replace(INTINO, "");
		} else if (e instanceof ModuleOrderEntry) {
			Module module = ((ModuleOrderEntry) e).getModule();
			Configuration configuration = IntinoUtil.configurationOf(module);
			if (configuration == null) return null;
			Configuration.Artifact artifact = configuration.artifact();
			return artifact.groupId() + ":" + artifact.name() + ":" + artifact.version();
		}
		return null;
	}

	private String scope(OrderEntry e) {
		if (e instanceof ModuleOrderEntry) return ((ModuleOrderEntry) e).getScope().name();
		if (e instanceof LibraryOrderEntry) return ((LibraryOrderEntry) e).getScope().name();
		return null;
	}
}
