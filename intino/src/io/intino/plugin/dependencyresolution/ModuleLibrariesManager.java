package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import io.intino.plugin.dependencyresolution.DependencyCatalog.Dependency;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.intellij.openapi.roots.ModuleRootModificationUtil.addDependency;
import static io.intino.plugin.dependencyresolution.DependencyCatalog.DependencyScope.COMPILE;
import static io.intino.plugin.dependencyresolution.IntinoLibrary.INTINO;

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
		catalog.dependencies().forEach(dependency -> {
			if (isAlreadyAdded(dependency)) return;
			if (dependency.isToModule()) addModuleDependency(dependency);
			else addLibrary(dependency);
		});
	}

	private boolean isAlreadyAdded(Dependency dependency) {
		if (dependency.isToModule()) return isDependantModule(dependency.moduleReference);
		return isDependantLibrary(dependency);
	}

	@NotNull
	private boolean isDependantModule(String moduleReference) {
		return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
			ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
			return Arrays.stream(model.getModuleDependencies()).anyMatch(moduleDependency -> moduleDependency.getName().equals(moduleReference));
		});
	}

	@NotNull
	private boolean isDependantLibrary(Dependency dependency) {
		String label = INTINO + dependency.mavenId();
		return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
			ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
			return Arrays.stream(model.getOrderEntries()).anyMatch(entry -> entry instanceof LibraryOrderEntry && label.equals(((LibraryOrderEntry) entry).getLibraryName()));
		});
	}

	private void addModuleDependency(Dependency dependency) {
		Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).
				filter(m -> m.getName().equals(dependency.moduleReference)).findFirst().
				ifPresent(m -> addDependency(module, m, DependencyScope.valueOf(dependency.scope.name()), false));

	}

	private void addLibrary(Dependency dependency) {
		Library library = table.findLibrary(dependency);
		if (library != null) addDependency(module, library, DependencyScope.valueOf(dependency.scope.name()), false);
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
		for (OrderEntry orderEntry : model.getOrderEntries())
			if (!(orderEntry instanceof ModuleSourceOrderEntry) && !(orderEntry instanceof JdkOrderEntry) && (!orderEntry.isValid() || !isSuitable(orderEntry, catalog)))
				model.removeOrderEntry(orderEntry);
		return model;
	}

	private boolean isSuitable(OrderEntry e, DependencyCatalog catalog) {
		String library = identifierOf(e);
		return library != null && catalog.dependencies().stream().anyMatch(dependency -> dependency.identifier.equals(library));
	}

	private String identifierOf(OrderEntry e) {
		if (e instanceof LibraryOrderEntry) {
			Library library = ((LibraryOrderEntry) e).getLibrary();
			if (library == null || library.getName() == null) return null;
			String name = library.getName().replace(INTINO, "");
			return name + ":" + ((LibraryOrderEntry) e).getScope().name();
		} else if (e instanceof ModuleOrderEntry) {
			Module module = ((ModuleOrderEntry) e).getModule();
			Configuration configuration = TaraUtil.configurationOf(module);
			return configuration.groupId() + ":" + configuration.artifactId() + ":" + configuration.version() + ":" + COMPILE;
		}
		return null;
	}

}
