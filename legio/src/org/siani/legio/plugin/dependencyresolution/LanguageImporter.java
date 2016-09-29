package org.siani.legio.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import tara.intellij.lang.LanguageManager;
import tara.intellij.project.configuration.Configuration;
import tara.intellij.settings.TaraSettings;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import static org.apache.maven.artifact.Artifact.LATEST_VERSION;
import static tara.dsl.ProteoConstants.PROTEO;
import static tara.dsl.ProteoConstants.VERSO;

public class LanguageImporter {

	private static final Logger LOG = Logger.getInstance(LanguageImporter.class.getName());
	private final TaraSettings settings;

	private Module module;
	private final Configuration configuration;

	public LanguageImporter(Module module, Configuration configuration) {
		this.module = module;
		this.configuration = configuration;
		settings = TaraSettings.getSafeInstance(module.getProject());
	}

	public String importLanguage(String dsl, String version) {
		try {
			if (!PROTEO.equals(dsl) && !VERSO.equals(dsl)) return "";
			final List<String> repositories = configuration.repositories();
			final String versionCode = getVersion(dsl, version, repositories);
			downloadLanguage(dsl, versionCode, repositories);
			configuration.dslVersion(versionCode);
			reload(dsl, module.getProject());
			return versionCode;
		} catch (IOException e) {
			error(e);
			return null;
		}
	}

	private File downloadLanguage(String name, String version, List<String> repositories) {
		try {
			final File taraDirectory = new File(LanguageManager.getTaraDirectory().getPath());
			File dslFile = new File(new File(taraDirectory, name + File.separator + version), name + "-" + version + ".jar");
			for (String repository : repositories)
				new ArtifactoryConnector(settings, repository).get(dslFile, name, version);
			return dslFile;
		} catch (IOException e) {
			error(e);
			return null;
		}
	}

	private String getVersion(String key, String version, List<String> repositories) throws IOException {
		if (LATEST_VERSION.equals(version)) {
			TreeMap<Long, String> versions = new TreeMap<>();
			for (String repository : repositories)
				new ArtifactoryConnector(settings, repository).versions(key).forEach(v -> versions.put(indexOf(v), v));
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

	private void error(IOException e) {
		Bus.notify(new Notification("Tara Language", "Error connecting with Artifactory.", e.getMessage(), NotificationType.ERROR));
	}
}
