package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import io.intino.plugin.dependencyresolution.DependencyCatalog.Dependency;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static io.intino.plugin.dependencyresolution.DependencyCatalog.DependencyScope.COMPILE;
import static io.intino.plugin.dependencyresolution.IntinoLibrary.INTINO;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

class ModuleDependencyResolver {
	DependencyCatalog resolveDependencyTo(Module dependency) {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return resolveDependencies(dependency);
		else return application.runReadAction((Computable<DependencyCatalog>) () -> resolveDependencies(dependency));
	}

	private DependencyCatalog resolveDependencies(Module dependency) {
		DependencyCatalog catalog = new DependencyCatalog();
		librariesOf(dependency).stream().filter(l -> l.getName() != null && l.getFiles(OrderRootType.CLASSES).length >= 1).forEach(l -> catalog.add(dependencyFrom(l)));
		moduleDependenciesOf(dependency).stream().
				map(TaraUtil::configurationOf).
				filter(c -> c instanceof LegioConfiguration).
				map(c -> (LegioConfiguration) c).
				forEach(c -> catalog.add(dependencyFrom(c)));
		return catalog;
	}

	@NotNull
	private Dependency dependencyFrom(LegioConfiguration c) {
		return new Dependency(c.groupId() + ":" + c.artifactId() + ":" + c.version() + ":" + COMPILE, c.module().getName());
	}

	@NotNull
	private Dependency dependencyFrom(Library library) {
		return new Dependency(requireNonNull(library.getName()).replace(INTINO, "") + ":" + COMPILE,
				new File(library.getFiles(OrderRootType.CLASSES)[0].getPath().replace("!", "")),
				library.getFiles(OrderRootType.SOURCES).length > 0);
	}

	private List<Library> librariesOf(Module module) {
		final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
		return Arrays.stream(model.getOrderEntries()).
				filter(o -> o.isValid() && o instanceof LibraryOrderEntry &&
						((ExportableOrderEntry) o).getScope().equals(DependencyScope.COMPILE) &&
						((LibraryOrderEntry) o).getLibraryName() != null &&
						((LibraryOrderEntry) o).getLibraryName().startsWith(INTINO)).
				map(l -> ((LibraryOrderEntry) l).getLibrary()).collect(toList());
	}

	private List<Module> moduleDependenciesOf(Module moduleDependency) {
		final ModifiableRootModel model = ModuleRootManager.getInstance(moduleDependency).getModifiableModel();
		return Arrays.stream(model.getOrderEntries()).
				filter(o -> o.isValid() && o instanceof ModuleOrderEntry).
				map(o1 -> ((ModuleOrderEntry) o1).getModule()).collect(toList());
	}
}