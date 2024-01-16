package io.intino.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import io.intino.Configuration;
import io.intino.plugin.lang.LanguageManager;
import io.intino.tara.dsls.Meta;
import io.intino.tara.dsls.Proteo;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;

import java.util.List;
import java.util.TreeMap;

import static org.apache.maven.artifact.Artifact.LATEST_VERSION;

public class LanguageImporter {
	private final Module module;
	private final Configuration.Artifact.Model model;
	private final MavenDependencyResolver resolver;
	private final List<Configuration.Repository> repositories;


	public LanguageImporter(Module module, Configuration.Artifact.Model model, List<Configuration.Repository> repositories) {
		this.module = module;
		this.model = model;
		this.resolver = new MavenDependencyResolver(new Repositories(this.module).map(repositories));
		this.repositories = repositories;
	}

	void importLanguage() {
		String language = model.language().name();
		final String effectiveVersion = effectiveVersionOf(language, model.language().version());
		final boolean done = downloadLanguage(language, effectiveVersion);
		if (done) {
			model.language().effectiveVersion(effectiveVersion);
			reload(language, module.getProject());
		}
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

	private String effectiveVersionOf(String dsl, String version) {
		if (version.equals(LATEST_VERSION)) {
			TreeMap<Long, String> versions = new TreeMap<>();
			new ArtifactoryConnector(repositories).dslVersions(dsl).forEach(v -> versions.put(indexOf(v), v));
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
