package io.intino.plugin.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.ThrowableComputable;
import io.intino.Configuration;
import io.intino.plugin.actions.dialog.UpdateVersionDialog;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class AllModuleDependencyPropagator {
	private final Map<Module, LegioConfiguration> modules;
	private final Project project;

	public AllModuleDependencyPropagator(List<Module> modules) {
		this.project = modules.get(0).getProject();
		this.modules = modules.stream().filter(module -> IntinoUtil.configurationOf(module) instanceof LegioConfiguration).collect(Collectors.toMap(m -> m, m -> (LegioConfiguration) IntinoUtil.configurationOf(m)));
	}

	public void execute() {
		if (modules.isEmpty()) return;
		Map<String, String> newVersions = askForNewVersions();
		if (newVersions.isEmpty()) return;
		for (String library : newVersions.keySet())
			for (LegioConfiguration configuration : modules.values()) {
				Configuration.Artifact.Dependency dependency = dependency(configuration, library.split(":"));
				if (dependency != null && !dependency.version().equals(newVersions.get(library)))
					dependency.version(newVersions.get(library));
			}
		modules.values().forEach(LegioConfiguration::reload);
	}

	private Configuration.Artifact.Dependency dependency(LegioConfiguration configuration, String[] library) {
		return configuration.artifact().dependencies().stream().filter(d -> d.groupId().equals(library[0]) && d.artifactId().equals(library[1])).findFirst().orElse(null);
	}

	private Map<String, String> askForNewVersions() {
		final Map<String, String>[] response = new Map[]{new HashMap<>()};
		Application application = ApplicationManager.getApplication();
		try {
			Map<String, List<String>> libraries = ProgressManager.getInstance().
					runProcessWithProgressSynchronously(((ThrowableComputable<Map<String, List<String>>, Exception>) this::downloadLibraryUpdates),
							"Calculating Project Updates", true, project);

			if (libraries.isEmpty()) {
				Notifications.Bus.notify(new Notification("Tara Language", "Dependency update", "The project " + project.getName() + " is  already updated", NotificationType.INFORMATION));
				return Collections.emptyMap();
			}
			application.invokeAndWait(() -> {
				UpdateVersionDialog dialog = new UpdateVersionDialog(project, "Update Versions of all modules", libraries);
				dialog.show();
				if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) response[0] = dialog.newVersions();
				else response[0] = Collections.emptyMap();
			});
		} catch (Exception ignored) {
		}
		return response[0];
	}

	private Map<String, List<String>> downloadLibraryUpdates() {
		Map<String, List<String>> cache = new HashMap<>();
		Map<String, List<String>> map = new LinkedHashMap<>();
		for (Map.Entry<Module, LegioConfiguration> entry : modules.entrySet()) {
			ArtifactoryConnector connector = new ArtifactoryConnector(entry.getValue().repositories());
			entry.getValue().artifact().dependencies().stream().map(Configuration.Artifact.Dependency::identifier).map(identifier -> identifier.split(":")).forEach(split -> {
				List<String> versions;
				String artifactId = split[0] + ":" + split[1];
				if (cache.containsKey(artifactId)) versions = cache.get(artifactId);
				else {
					versions = connector.versions(artifactId);
					cache.put(artifactId, versions);
				}
				List<String> higherVersions = filter(versions, split[2]);
				if (!versions.isEmpty() && higherVersions.size() > 1) map.put(artifactId, higherVersions);
			});
		}
		return map;
	}

	private List<String> filter(List<String> versions, String current) {
		return versions.stream().filter(v -> v.equals(current) || v.compareTo(current) > 0).collect(Collectors.toList());
	}


}
