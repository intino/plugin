package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.module.Module;
import org.eclipse.aether.graph.Dependency;

import java.util.*;

public class DependencyCatalog {
	static final DependencyCatalog EMPTY = new DependencyCatalog();
	private final Set<Dependency> libraries;
	private final Map<Module, String> moduleDependencies;

	public DependencyCatalog() {
		this.libraries = new LinkedHashSet<>();
		this.moduleDependencies = new LinkedHashMap<>();
	}

	public DependencyCatalog(Collection<Dependency> libraries) {
		this.libraries = new LinkedHashSet<>(libraries);
		this.moduleDependencies = new LinkedHashMap<>();
	}

	public DependencyCatalog addAll(Collection<Dependency> libraries) {
		this.libraries.addAll(libraries);
		return this;
	}

	public DependencyCatalog add(Dependency dependency) {
		libraries.add(dependency);
		return this;
	}

	public DependencyCatalog remove(Dependency dependency) {
		libraries.remove(dependency);
		return this;
	}

	public DependencyCatalog add(Module module, String scope) {
		moduleDependencies.put(module, scope);
		return this;
	}


	public DependencyCatalog removeAll(Collection<Dependency> library) {
		libraries.removeAll(library);
		return this;
	}

	public Map<Module, String> moduleDependencies() {
		return moduleDependencies;
	}

	public List<Dependency> dependencies() {
		return new ArrayList<>(libraries);
	}

	public DependencyCatalog merge(DependencyCatalog catalog) {
		libraries.addAll(catalog.libraries);
		moduleDependencies.putAll(catalog.moduleDependencies);
		return this;
	}

	public enum DependencyScope {
		COMPILE, TEST, RUNTIME, PROVIDED, WEB;

		public String label() {
			return name().substring(0, 1) + name().substring(1).toLowerCase();
		}
	}

}