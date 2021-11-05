package io.intino.plugin.dependencyresolution;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class DependencyCatalog {
	static final DependencyCatalog EMPTY = new DependencyCatalog();
	private Set<Dependency> libraries;

	public DependencyCatalog() {
		this.libraries = new LinkedHashSet<>();
	}

	public DependencyCatalog(Collection<Dependency> libraries) {
		this.libraries = new LinkedHashSet<>(libraries);
	}

	public DependencyCatalog addAll(Collection<Dependency> libraries) {
		this.libraries.addAll(libraries);
		return this;
	}

	public DependencyCatalog add(Dependency dependency) {
		libraries.add(dependency);
		return this;
	}

	public DependencyCatalog remove(Dependency library) {
		libraries.remove(library);
		return this;
	}

	public DependencyCatalog removeAll(Collection<Dependency> library) {
		libraries.removeAll(library);
		return this;
	}

	public List<Dependency> dependencies() {
		return new ArrayList<>(libraries);
	}

	public DependencyCatalog merge(DependencyCatalog catalog) {
		libraries.addAll(catalog.libraries);
		return this;
	}

	public enum DependencyScope {
		COMPILE, TEST, RUNTIME, PROVIDED, WEB;

		public String label() {
			return name().substring(0, 1) + name().substring(1).toLowerCase();
		}
	}

	public static class Dependency {
		String identifier;
		File jar;
		boolean sources;
		String groupId;
		String artifactId;
		String classifier;
		String version;
		DependencyScope scope;
		String moduleReference;

		public Dependency(String identifier, String module) {
			this(identifier, null, false);
			toModule(module);
		}

		public Dependency(String identifier, File jar) {
			this(identifier, jar, false);
		}

		public Dependency(String identifier, File jar, boolean sources) {
			this.identifier = identifier;
			this.jar = jar;
			this.sources = sources;
			String[] split = mavenId().split(":");
			this.groupId = split[0];
			this.artifactId = split[1];
			this.classifier = split.length == 5 ? split[2] : "";
			this.version = split.length == 5 ? split[3] : split[2];
			this.scope = calculateScope();
		}

		public Dependency sources(boolean sources) {
			this.sources = sources;
			return this;
		}

		public String identifier() {
			return identifier;
		}

		public File jar() {
			return jar;
		}

		public String classifier() {
			return classifier;
		}

		public DependencyScope scope() {
			return scope;
		}

		@NotNull
		public String mavenId() {
			return identifier.substring(0, identifier.lastIndexOf(":"));
		}

		@NotNull
		public String groupArtifactAndScope() {
			return identifier.replace(":" + version() + ":", ":");
		}

		@NotNull
		public String version() {
			return version;
		}

		public String groupId() {
			return groupId;
		}

		public String artifactId() {
			return artifactId;
		}

		public boolean isToModule() {
			return moduleReference != null;
		}

		private void toModule(String module) {
			this.moduleReference = module;
		}

		@Override
		public String toString() {
			return identifier;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Dependency dependency = (Dependency) o;
			return identifier.equals(dependency.identifier);
		}

		@Override
		public int hashCode() {
			return Objects.hash(identifier);
		}

		@NotNull
		private DependencyScope calculateScope() {
			return DependencyScope.valueOf(identifier.substring(identifier.lastIndexOf(":") + 1).toUpperCase());
		}
	}
}