package org.siani.legio.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.jcabi.aether.Aether;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import tara.dsl.ProteoConstants;
import tara.intellij.lang.LanguageManager;
import tara.intellij.project.configuration.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import static org.apache.maven.artifact.Artifact.LATEST_VERSION;

public class LanguageImporter {

	private static final Logger LOG = Logger.getInstance(LanguageImporter.class.getName());

	private Module module;
	private final Configuration configuration;
	private List<String> releaseRepositories;
	private List<String> snapshotRepositories;
	private String languageRepository;

	public LanguageImporter(Module module, Configuration configuration) {
		this.module = module;
		this.configuration = configuration;
		releaseRepositories = configuration.releaseRepositories();
		snapshotRepositories = configuration.snapshotRepositories();
		languageRepository = configuration.languageRepository();
	}

	public String importLanguage(String dsl, String version) {
		try {
			final String versionCode = effectiveVersion(dsl, version);
			downloadLanguage(dsl, versionCode);
			configuration.dslVersion(versionCode);
			reload(dsl, module.getProject());
			return versionCode;
		} catch (IOException e) {
			error(e);
			return null;
		}
	}

	private void downloadLanguage(String name, String version) {
		try {
			if (name.equals(ProteoConstants.PROTEO) || name.equals(ProteoConstants.VERSO)) return;
			final File languagesDirectory = new File(LanguageManager.getLanguagesDirectory().getPath());
			final List<Artifact> jar = new Aether(repository(), languagesDirectory).resolve(new DefaultArtifact(LanguageManager.DSL_GROUP_ID, name, "jar", version), JavaScopes.COMPILE);
		} catch (DependencyResolutionException e) {
			error(e);
		}
	}

	@NotNull
	private List<RemoteRepository> repository() {
		return Collections.singletonList(new RemoteRepository(configuration.languageRepositoryId(), "default", configuration.languageRepository()));
	}

	private String effectiveVersion(String key, String version) throws IOException {
		if (LATEST_VERSION.equals(version)) {
			TreeMap<Long, String> versions = new TreeMap<>();
			new ArtifactoryConnector(releaseRepositories, snapshotRepositories, languageRepository).versions(key).forEach(v -> versions.put(indexOf(v), v));
			return versions.get(versions.lastKey());
		} else return version;
	}

	private Long indexOf(String version) {
		String value = "";
		String[] split = (version.contains("-") ? version.substring(0, version.indexOf("-")) : version).split("\\.");
		int times = split.length - 1;
		if (times == 0) return Long.parseLong(version);
		for (String s : split) {
			if (s.length() < 2) value += new String(new char[2 - s.length()]).replace("\0", "0");
			value += s;
		}
		return Long.parseLong(value);
	}


	private void reload(String fileName, Project project) {
		LanguageManager.reloadLanguage(project, FileUtil.getNameWithoutExtension(fileName));
	}

	private void error(Exception e) {
		Bus.notify(new Notification("Tara Language", "Error connecting with Artifactory.", e.getMessage(), NotificationType.ERROR));
	}
}
