package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import io.intino.Configuration.Artifact.Dependency.Exclude;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static io.intino.plugin.dependencyresolution.IntinoLibrary.INTINO;
import static io.intino.plugin.dependencyresolution.IntinoLibrary.artifactOf;
import static java.util.stream.Collectors.toList;

class ModuleDependencyResolver {
	DependencyCatalog resolveDependencyWith(Module dependency, String scope) {
		return resolveDependencyWith(dependency, scope, List.of());
	}

	DependencyCatalog resolveDependencyWith(Module dependency, String scope, List<Exclude> excludes) {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return resolveDependencies(dependency, excludes, scope);
		else
			return application.runReadAction((Computable<DependencyCatalog>) () -> resolveDependencies(dependency, excludes, scope));
	}

	private DependencyCatalog resolveDependencies(Module dependency, List<Exclude> excludes, String scope) {
		final List<String> excludeQns = excludes.stream().map(e -> e.groupId() + ":" + e.artifactId()).toList();
		DependencyCatalog catalog = new DependencyCatalog();
		catalog.add(dependency, scope);
		librariesOf(dependency).stream().filter(l -> l.getName() != null && l.getFiles(OrderRootType.CLASSES).length >= 1).forEach(l -> catalog.add(dependencyFrom(l, scope)));
		moduleDependenciesOf(dependency).forEach(m -> catalog.add(m, scope));
		return catalog.removeAll(catalog.dependencies().stream().filter(d -> excludeQns.contains(d.getArtifact().getGroupId() + ":" + d.getArtifact().getArtifactId())).toList());
	}


	@NotNull
	private Dependency dependencyFrom(Library library, String scope) {
		Artifact artifact = artifactOf(library, scope);
		artifact.setFile(new File(library.getFiles(OrderRootType.CLASSES)[0].getPath().replace("!", "")));
		return new Dependency(artifact, scope);
	}

	private List<Library> librariesOf(Module module) {
		final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
		return Arrays.stream(model.getOrderEntries()).filter(o -> o.isValid() && o instanceof LibraryOrderEntry && ((ExportableOrderEntry) o).getScope().equals(DependencyScope.COMPILE) && ((LibraryOrderEntry) o).getLibraryName() != null && ((LibraryOrderEntry) o).getLibraryName().startsWith(INTINO)).map(l -> ((LibraryOrderEntry) l).getLibrary()).collect(toList());
	}

	private List<Module> moduleDependenciesOf(Module moduleDependency) {
		final ModifiableRootModel model = ModuleRootManager.getInstance(moduleDependency).getModifiableModel();
		return Arrays.stream(model.getOrderEntries())
				.filter(o -> o.isValid() && o instanceof ModuleOrderEntry)
				.map(o1 -> ((ModuleOrderEntry) o1).getModule())
				.toList();
	}
}