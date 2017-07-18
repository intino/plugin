package io.intino.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.jcabi.aether.Aether;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.dsl.Proteo;
import io.intino.tara.dsl.Verso;
import io.intino.tara.plugin.lang.LanguageManager;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class LanguageImporter {

	private static final Logger LOG = Logger.getInstance(LanguageImporter.class.getName());

	private Module module;
	private final Configuration configuration;

	LanguageImporter(Module module, Configuration configuration) {
		this.module = module;
		this.configuration = configuration;
	}

	void importLanguage(String dsl, String version) {
		final String effectiveVersion = LegioUtil.effectiveVersionOf(dsl, version, (LegioConfiguration) configuration);
		final boolean done = downloadLanguage(dsl, effectiveVersion);
		if (done) {
			configuration.language(d -> d.name().equals(dsl)).version(effectiveVersion);
			reload(dsl, module.getProject());
		}
	}

	private boolean downloadLanguage(String name, String version) {
		try {
			if (name.equalsIgnoreCase(Proteo.class.getSimpleName()) || name.equals(Verso.class.getSimpleName()))
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
		return configuration.languageRepositories().entrySet().stream().map((e) -> new RemoteRepository(e.getValue(), "default", e.getKey())).collect(Collectors.toList());
	}

	private void reload(String fileName, Project project) {
		LanguageManager.reloadLanguage(project, FileUtil.getNameWithoutExtension(fileName));
	}

	private void error(Exception e) {
		Bus.notify(new Notification("Tara Language", "Error connecting with Artifactory.", e.getMessage(), NotificationType.ERROR));
	}
}
