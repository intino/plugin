package io.intino.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dsl;
import io.intino.plugin.archetype.Formatters;
import io.intino.plugin.lang.LanguageManager;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TreeMap;

import static org.apache.maven.artifact.Artifact.LATEST_VERSION;
import static org.eclipse.aether.util.artifact.JavaScopes.COMPILE;

public class DslImporter {
	private final Module module;
	private final MavenDependencyResolver resolver;
	private final List<Configuration.Repository> repositories;

	public DslImporter(Module module, List<Configuration.Repository> repositories) {
		this.module = module;
		this.resolver = new MavenDependencyResolver(new Repositories(this.module).map(repositories));
		this.repositories = repositories;
	}

	List<Dependency> importDsl(Dsl dsl) {
		String dslName = dsl.artifactId();
		if (dslName == null || dsl.version() == null) return null;
		final DependencyResult result = downloadLanguage(dsl);
		if (result != null) {
			List<Dependency> dependencies = MavenDependencyResolver.dependenciesFrom(result, false);
			dsl.effectiveVersion(dependencies.get(0).getArtifact().getBaseVersion());
			reload(dslName, module.getProject());
			return dependencies;
		}
		return null;
	}

	private DependencyResult downloadLanguage(Dsl dsl) {
		try {
			return resolver.resolve(new DefaultArtifact(dsl.groupId(), dsl.artifactId(), "jar", effectiveVersionOf(dsl)), COMPILE);
		} catch (DependencyResolutionException e) {
			try {
				return resolver.resolve(new DefaultArtifact(dsl.groupId(), legacyMode(dsl.artifactId()), "jar", effectiveVersionOf(dsl)), COMPILE);
			} catch (DependencyResolutionException e2) {
				error(e2);
				return null;
			}
		}
	}

	private static @NotNull String legacyMode(String dsl) {
		return dsl.toLowerCase().equals(dsl) ? Formatters.firstUpperCase(dsl) : dsl.toLowerCase();
	}

	private void reload(String fileName, Project project) {
		LanguageManager.reloadLanguage(project, FileUtil.getNameWithoutExtension(fileName));
	}

	private void error(Exception e) {
		Bus.notify(new Notification("Intino", "Error connecting with Artifactory.", e.getMessage(), NotificationType.ERROR));
	}

	private String effectiveVersionOf(Dsl dsl) {
		String version = dsl.effectiveVersion();
		if (LATEST_VERSION.equals(version)) {
			TreeMap<Long, String> versions = new TreeMap<>();
			new ArtifactoryConnector(repositories).dslVersions(dsl.name()).forEach(v -> versions.put(indexOf(v), v));
			return versions.isEmpty() ? LATEST_VERSION : versions.get(versions.lastKey());
		}
		return version;
	}

	private Long indexOf(String version) {
		StringBuilder value = new StringBuilder();
		String[] split = (version.contains("-") ? version.substring(0, version.indexOf("-")) : version).split("\\.");
		int times = split.length - 1;
		if (times == 0) return Long.parseLong(version);
		for (String s : split) {
			if (s.length() < 2) value.append(new String(new char[2 - s.length()]).replace("\0", "0"));
			value.append(s);
		}
		return Long.parseLong(value.toString());
	}
}
