package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import edu.emory.mathcs.backport.java.util.Collections;
import io.intino.Configuration;
import io.intino.plugin.dependencyresolution.DependencyCatalog.Dependency;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.dependencyresolution.IntinoLibrary.INTINO;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

class ModuleDependencyResolver {
	DependencyCatalog resolveDependencyWith(Module dependency, String scope) {
		return resolveDependencyWith(dependency, Collections.emptyList(), scope);
	}

	DependencyCatalog resolveDependencyWith(Module dependency, List<Configuration.Artifact.Dependency.Exclude> excludes, String scope) {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return resolveDependencies(dependency, excludes, scope);
		else
			return application.runReadAction((Computable<DependencyCatalog>) () -> resolveDependencies(dependency, excludes, scope));
	}

	private DependencyCatalog resolveDependencies(Module dependency, List<Configuration.Artifact.Dependency.Exclude> excludes, String scope) {
		final List<String> excludeQns = excludes.stream().map(e -> e.groupId() + ":" + e.artifactId()).collect(toList());
		DependencyCatalog catalog = new DependencyCatalog();
		catalog.add(dependencyFrom((LegioConfiguration) IntinoUtil.configurationOf(dependency), scope));
		librariesOf(dependency).stream().filter(l -> l.getName() != null && l.getFiles(OrderRootType.CLASSES).length >= 1).forEach(l -> catalog.add(dependencyFrom(l, scope)));
		moduleDependenciesOf(dependency).stream().map(IntinoUtil::configurationOf).filter(c -> c instanceof LegioConfiguration).map(c -> (LegioConfiguration) c).forEach(c -> catalog.add(dependencyFrom(c, scope)));
		return catalog.removeAll(catalog.dependencies().stream().filter(d -> excludeQns.contains(d.groupId() + ":" + d.artifactId())).collect(Collectors.toList()));
	}

	@NotNull
	private Dependency dependencyFrom(LegioConfiguration c, String scope) {
		LegioArtifact artifact = c.artifact();
		return new Dependency(artifact.groupId() + ":" + artifact.name() + ":" + artifact.version() + ":" + DependencyCatalog.DependencyScope.valueOf(scope.toUpperCase()), c.module().getName());
	}

	@NotNull
	private Dependency dependencyFrom(Library library, String scope) {
		return new Dependency(requireNonNull(library.getName()).replace(INTINO, "") + ":" + scope, new File(library.getFiles(OrderRootType.CLASSES)[0].getPath().replace("!", "")), library.getFiles(OrderRootType.SOURCES).length > 0);
	}

	private List<Library> librariesOf(Module module) {
		final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
		return Arrays.stream(model.getOrderEntries()).filter(o -> o.isValid() && o instanceof LibraryOrderEntry && ((ExportableOrderEntry) o).getScope().equals(DependencyScope.COMPILE) && ((LibraryOrderEntry) o).getLibraryName() != null && ((LibraryOrderEntry) o).getLibraryName().startsWith(INTINO)).map(l -> ((LibraryOrderEntry) l).getLibrary()).collect(toList());
	}

	private List<Module> moduleDependenciesOf(Module moduleDependency) {
		final ModifiableRootModel model = ModuleRootManager.getInstance(moduleDependency).getModifiableModel();
		return Arrays.stream(model.getOrderEntries()).filter(o -> o.isValid() && o instanceof ModuleOrderEntry).map(o1 -> ((ModuleOrderEntry) o1).getModule()).collect(toList());
	}
}