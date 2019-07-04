package io.intino.plugin.dependencyresolution;

import io.intino.plugin.dependencyresolution.DependencyCatalog.Dependency;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DependencyConflictResolver {

	public DependencyConflictResolver() {
	}

	public void resolve(DependencyCatalog catalog) {
		while (true) if (!resolveScopeConflicts(catalog, catalog.dependencies())) break;
		resolveVersionConflicts(catalog);
	}

	private boolean resolveScopeConflicts(DependencyCatalog catalog, List<Dependency> dependencies) {
		for (Dependency dep : dependencies) {
			for (Dependency conflict : findConflicts(dependencies, dep))
				if (dep.scope.compareTo(conflict.scope) < 0) {
					catalog.remove(conflict);
					return true;
				}
		}
		return false;
	}

	private void resolveVersionConflicts(DependencyCatalog catalog) {
		Map<String, String> dependencies = catalog.dependencies().stream().
				collect(Collectors.toMap(Dependency::groupArtifactAndScope, Dependency::version, (a, b) -> a, LinkedHashMap::new));
		catalog.removeAll(catalog.dependencies().stream().
				filter(dependency -> !dependency.version.equals(dependencies.get(dependency.groupArtifactAndScope()))).collect(Collectors.toList()));
	}

	private List<Dependency> findConflicts(Collection<Dependency> dependencies, Dependency dependency) {
		String mavenId = dependency.mavenId();
		return dependencies.stream().filter(d -> d.identifier.startsWith(mavenId) && !d.identifier.equals(dependency.identifier)).collect(Collectors.toList());
	}


}