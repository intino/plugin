package io.intino.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.jcabi.aether.Aether;
import io.intino.Configuration;
import io.intino.Configuration.Repository;
import io.intino.magritte.dsl.Meta;
import io.intino.magritte.dsl.Proteo;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.apache.maven.artifact.Artifact.LATEST_VERSION;

public class LanguageImporter {
	private final Configuration configuration;
	private Module module;
	private final List<Repository> repositories;

	LanguageImporter(Module module, Configuration configuration) {
		this.module = module;
		this.configuration = configuration;
		this.repositories = languageRepositories(configuration);
	}

	String importLanguage(String dsl, String version) {
		final String effectiveVersion = effectiveVersionOf(dsl, version, (LegioConfiguration) configuration);
		final boolean done = downloadLanguage(dsl, effectiveVersion);
		if (done) {
			configuration.artifact().model().language().effectiveVersion(effectiveVersion);
			reload(dsl, module.getProject());
		}
		return effectiveVersion;

	}

	private boolean downloadLanguage(String name, String version) {
		try {
			if (Proteo.class.getSimpleName().equalsIgnoreCase(name) || Meta.class.getSimpleName().equals(name))
				return true;
			final File languagesDirectory = new File(LanguageManager.getLanguagesDirectory().getPath());
			new Aether(repositories(), languagesDirectory).resolve(new DefaultArtifact(LanguageManager.DSL_GROUP_ID, name, "jar", version), JavaScopes.COMPILE);
			return true;
		} catch (DependencyResolutionException e) {
			error(e);
			return false;
		}
	}

	@NotNull
	private List<RemoteRepository> repositories() {
		return repositories.stream().map(r -> new RemoteRepository(r.identifier(), "default", r.url())).collect(Collectors.toList());
	}

	private void reload(String fileName, Project project) {
		LanguageManager.reloadLanguage(project, FileUtil.getNameWithoutExtension(fileName));
	}

	private void error(Exception e) {
		Bus.notify(new Notification("Tara Language", "Error connecting with Artifactory.", e.getMessage(), NotificationType.ERROR));
	}

	private String effectiveVersionOf(String dsl, String version, LegioConfiguration configuration) {
		if (version.equals(LATEST_VERSION)) {
			TreeMap<Long, String> versions = new TreeMap<>();
			new ArtifactoryConnector(repositories).dslVersions(dsl).forEach(v -> versions.put(indexOf(v), v));
			return versions.isEmpty() ? LATEST_VERSION : versions.get(versions.lastKey());
		}
		return version;
	}

	@NotNull
	private List<Repository> languageRepositories(Configuration configuration) {
		return configuration.repositories().stream().filter(r -> r instanceof Repository.Language).collect(Collectors.toList());
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
