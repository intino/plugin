package io.intino.plugin.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.ThrowableComputable;
import io.intino.Configuration;
import io.intino.plugin.actions.dialog.UpdateVersionDialog;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;

import java.util.*;
import java.util.stream.Collectors;

import static io.intino.plugin.lang.psi.impl.IntinoUtil.configurationOf;

public class ModuleDependencyPropagator {
	private final Module module;
	private final Configuration configuration;

	public ModuleDependencyPropagator(Module module, Configuration configuration) {
		this.module = module;
		this.configuration = configuration;
	}

	public void execute() {
		if (module == null) return;
		Map<String, String> newVersions = askForNewVersions();
		if (newVersions.isEmpty()) return;
		for (String library : newVersions.keySet()) {
			Configuration.Artifact.Dependency dependency = dependency(library.split(":"));
			if (!dependency.version().equals(newVersions.get(library))) dependency.version(newVersions.get(library));
		}
		configurationOf(module).reload();
	}

	private Configuration.Artifact.Dependency dependency(String[] library) {
		return configuration.artifact().dependencies().stream().filter(d -> d.groupId().equals(library[0]) && d.artifactId().equals(library[1])).findFirst().orElse(null);
	}

	private Map<String, String> askForNewVersions() {
		final Map<String, String>[] response = new Map[]{new HashMap<>()};
		Application application = ApplicationManager.getApplication();
		try {
			Map<String, List<String>> libraries = ProgressManager.getInstance().
					runProcessWithProgressSynchronously(((ThrowableComputable<Map<String, List<String>>, Exception>) this::loadLibraryUpdates),
							"Calculating Module Updates", true, this.module.getProject());
			if (libraries.values().stream().noneMatch(v -> v.size() > 1)) {
				Notifications.Bus.notify(new Notification("Tara Language", "Dependency update", "The module " + module.getName() + " is  already updated", NotificationType.INFORMATION));
				return Collections.emptyMap();
			}
			application.invokeAndWait(() -> {
				UpdateVersionDialog dialog = new UpdateVersionDialog(module.getProject(), "Update Versions of module", libraries.entrySet().stream().filter(e -> e.getValue().size() > 1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
				dialog.show();
				if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) response[0] = dialog.newVersions();
				else response[0] = Collections.emptyMap();
			});
		} catch (Exception ignored) {
		}
		return response[0];
	}

	private Map<String, List<String>> loadLibraryUpdates() {
		ArtifactoryConnector connector = new ArtifactoryConnector(configuration.repositories());
		Map<String, List<String>> map = new LinkedHashMap<>();
		configuration.artifact().dependencies().forEach(d -> {
			String[] split = d.identifier().split(":");
			List<String> versions = connector.versions(split[0] + ":" + split[1]);
			if (!versions.isEmpty()) map.put(d.identifier(), filter(versions, split[2]));
		});
		return map;
	}

	private List<String> filter(List<String> versions, String current) {
		return versions.stream().filter(v -> v.equals(current) || v.compareTo(current) > 0).collect(Collectors.toList());
	}
}
