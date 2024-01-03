package io.intino.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import io.intino.Configuration;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.tara.dsls.Meta;
import io.intino.tara.dsls.Proteo;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;

import java.util.TreeMap;

import static org.apache.maven.artifact.Artifact.LATEST_VERSION;

public class LanguageImporter {
	private final Configuration configuration;
	private final Module module;
	private final MavenDependencyResolver resolver;

	LanguageImporter(Module module, Configuration configuration) {
		this.module = module;
		this.configuration = configuration;
		this.resolver = new MavenDependencyResolver(new Repositories(this.module).map(configuration.repositories()));
	}

	String importLanguage(String dsl, String version) {
		final String effectiveVersion = effectiveVersionOf(dsl, version, (ArtifactLegioConfiguration) configuration);
		final boolean done = downloadLanguage(dsl, effectiveVersion);
		if (done) {
			configuration.artifact().model().language().effectiveVersion(effectiveVersion);
			reload(dsl, module.getProject());
		}
		return effectiveVersion;
	}

	private boolean downloadLanguage(String name, String version) {
		if (Proteo.class.getSimpleName().equalsIgnoreCase(name) || Meta.class.getSimpleName().equals(name))
			return true;
		try {
			resolver.resolve(new DefaultArtifact(LanguageManager.DSL_GROUP_ID, name, "jar", version), JavaScopes.COMPILE);
			return true;
		} catch (DependencyResolutionException e) {
			try {
				resolver.resolve(new DefaultArtifact(LanguageManager.DSL_GROUP_ID, name.toLowerCase(), "jar", version), JavaScopes.COMPILE);
				return true;
			} catch (DependencyResolutionException e2) {
				error(e2);
				return false;
			}
		}
	}

	private void reload(String fileName, Project project) {
		LanguageManager.reloadLanguage(project, FileUtil.getNameWithoutExtension(fileName));
	}

	private void error(Exception e) {
		Bus.notify(new Notification("Intino", "Error connecting with Artifactory.", e.getMessage(), NotificationType.ERROR));
	}

	private String effectiveVersionOf(String dsl, String version, ArtifactLegioConfiguration configuration) {
		if (version.equals(LATEST_VERSION)) {
			TreeMap<Long, String> versions = new TreeMap<>();
			new ArtifactoryConnector(configuration.repositories()).dslVersions(dsl).forEach(v -> versions.put(indexOf(v), v));
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
