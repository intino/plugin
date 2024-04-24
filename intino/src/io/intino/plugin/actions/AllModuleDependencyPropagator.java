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
import io.intino.plugin.IntinoException;
import io.intino.plugin.actions.dialog.UpdateVersionDialog;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;

import java.util.*;
import java.util.stream.Collectors;


public class AllModuleDependencyPropagator {
	private final Map<Module, ArtifactLegioConfiguration> modules;
	private final Project project;

	public AllModuleDependencyPropagator(List<Module> modules) {
		this.project = modules.get(0).getProject();
		this.modules = modules.stream().filter(module -> IntinoUtil.configurationOf(module) instanceof ArtifactLegioConfiguration).collect(Collectors.toMap(m -> m, m -> (ArtifactLegioConfiguration) IntinoUtil.configurationOf(m)));
	}

	public Map<ArtifactLegioConfiguration, Version.Level> execute() {
		if (modules.isEmpty()) return null;
		Map<String, String> newVersions = askForNewVersions();
		if (newVersions.isEmpty()) return Collections.emptyMap();
		Map<ArtifactLegioConfiguration, Version.Level> changes = new HashMap<>();
		for (String library : newVersions.keySet())
			for (ArtifactLegioConfiguration configuration : modules.values()) {
				String[] coors = library.split(":");
				Configuration.Artifact.Dependency dependency = dependency(configuration, coors);
				String newVersion = newVersions.get(library);
				if (dependency == null) updateDsl(configuration, newVersions, library);
				else if (!dependency.version().equals(newVersion)) {
					String oldVersion = dependency.version();
					dependency.version(newVersion);
					calculateChange(changes, configuration, newVersion, oldVersion);
				}
			}
		changes.keySet().forEach(ArtifactLegioConfiguration::reload);
		return changes;
	}

	private void calculateChange(Map<ArtifactLegioConfiguration, Version.Level> changes, ArtifactLegioConfiguration configuration, String newVersion, String oldVersion) {
		Version.Level changeLevel = calculateChangeLevel(oldVersion, newVersion);
		if (!changes.containsKey(configuration)) changes.put(configuration, changeLevel);
		else if (changes.get(configuration).ordinal() < changeLevel.ordinal())
			changes.put(configuration, changeLevel);
	}

	private Version.Level calculateChangeLevel(String oldVersion, String newVersion) {
		try {
			return new Version(oldVersion).distanceTo(new Version(newVersion));
		} catch (IntinoException e) {
			return Version.Level.Minor;
		}
	}

	private void updateDsl(ArtifactLegioConfiguration configuration, Map<String, String> newVersions, String library) {
		configuration.artifact().dsls().stream()
				.filter(dsl -> dsl.name().equalsIgnoreCase(library) && !newVersions.get(library).equals(dsl.version()))
				.forEach(dsl -> dsl.version(newVersions.get(library)));
	}

	private Configuration.Artifact.Dependency dependency(ArtifactLegioConfiguration configuration, String[] library) {
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
				Notifications.Bus.notify(new Notification("Intino", "Dependency update", "The project " + project.getName() + " is  already updated", NotificationType.INFORMATION));
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
		for (Map.Entry<Module, ArtifactLegioConfiguration> entry : modules.entrySet()) {
			ArtifactoryConnector connector = new ArtifactoryConnector(project, entry.getValue().repositories());
			entry.getValue().artifact().dsls().forEach(dsl -> dslVersions(map, connector, dsl));
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

	private void dslVersions(Map<String, List<String>> map, ArtifactoryConnector connector, Configuration.Artifact.Dsl dsl) {
		List<String> dslVersions = connector.dslVersions(dsl.name());
		if (!dsl.version().equals(dslVersions.get(dslVersions.size() - 1)))
			map.put(dsl.name(), dslVersions);
	}

	private List<String> filter(List<String> versions, String current) {
		try {
			Version currentVersion = new Version(current);
			List<String> list = new ArrayList<>();
			for (String v : versions)
				if (new Version(v).equals(currentVersion) || v.compareTo(current) > 0) list.add(v);
			return list;
		} catch (IntinoException e) {
			return versions;
		}
	}
}
