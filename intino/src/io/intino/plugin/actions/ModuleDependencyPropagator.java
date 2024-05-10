package io.intino.plugin.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.ThrowableComputable;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dsl;
import io.intino.plugin.actions.dialog.UpdateVersionDialog;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.project.configuration.Version.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

import static io.intino.plugin.lang.psi.impl.IntinoUtil.configurationOf;
import static java.util.stream.Collectors.toMap;

public class ModuleDependencyPropagator {
	private static final Logger LOG = Logger.getInstance(ModuleDependencyPropagator.class.getName());
	private final Module module;
	private final Configuration configuration;
	private Level change = null;

	public ModuleDependencyPropagator(Module module, Configuration configuration) {
		this.module = module;
		this.configuration = configuration;
	}

	public Level execute() {
		if (module == null) return null;
		Map<String, String> newVersions = askForNewVersions();
		change = null;
		if (newVersions.isEmpty()) return null;
		for (String library : newVersions.keySet()) {
			try {
				propagateChange(library, newVersions.get(library));
			} catch (Throwable e) {
				LOG.error(e);
			}
		}
		configurationOf(module).reload();
		return change;
	}

	private void propagateChange(String library, String newVersion) {
		if (!library.contains(":")) {
			Dsl dsl = configuration.artifact().dsl(library);
			if (dsl != null) updateDsl(dsl, newVersion);
		} else {
			String[] identifier = library.split(":");
			Configuration.Artifact.Dependency dependency = dependency(identifier);
			if (dependency == null) return;
			if (!dependency.version().equals(newVersion)) {
				change = calculateChange(change, newVersion, dependency.version());
				dependency.version(newVersion);
			}
		}
	}

	private void updateDsl(Dsl dsl, String newVersion) {
		change = calculateChange(change, newVersion, dsl.version());
		if (!newVersion.equals(dsl.version())) dsl.version(newVersion);
	}

	private Level calculateChange(Level currentChangeLevel, String newVersion, String oldVersion) {
		Level changeLevel = calculateChangeLevel(oldVersion, newVersion);
		return currentChangeLevel == null ? changeLevel :
				Level.values()[Math.max(currentChangeLevel.ordinal(), changeLevel.ordinal())];
	}

	private Level calculateChangeLevel(String oldVersion, String newVersion) {
		return versionOf(oldVersion).distanceTo(versionOf(newVersion));
	}

	private Configuration.Artifact.Dependency dependency(String[] library) {
		return configuration.artifact().dependencies().stream()
				.filter(d -> d.groupId().equals(library[0]) && d.artifactId().equals(library[1]))
				.findFirst()
				.orElse(null);
	}

	private Map<String, String> askForNewVersions() {
		var response = new Map[]{new HashMap<>()};
		Application application = ApplicationManager.getApplication();
		try {
			Map<String, List<String>> libraries = ProgressManager.getInstance().
					runProcessWithProgressSynchronously(((ThrowableComputable<Map<String, List<String>>, Exception>) this::loadLibraryUpdates),
							"Calculating Module Updates", true, this.module.getProject());
			if (libraries.values().stream().noneMatch(v -> v.size() > 1)) {
				Notifications.Bus.notify(new Notification("Intino", "Dependency update", "The module " + module.getName() + " is  already updated", NotificationType.INFORMATION));
				return Collections.emptyMap();
			}
			application.invokeAndWait(() -> {
				UpdateVersionDialog dialog = new UpdateVersionDialog(module.getProject(), "Update Versions of module", possibleUpdates(libraries));
				dialog.show();
				if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) response[0] = dialog.newVersions();
				else response[0] = Collections.emptyMap();
			});
		} catch (Exception e) {
			LOG.error(e);
		}
		return response[0];
	}

	@NotNull
	private static Map<String, List<String>> possibleUpdates(Map<String, List<String>> libraries) {
		return libraries.entrySet().stream()
				.filter(e -> e.getValue().size() > 1)
				.collect(toMap(Entry::getKey, Entry::getValue));
	}

	private Map<String, List<String>> loadLibraryUpdates() {
		var connector = new ArtifactoryConnector(module.getProject(), configuration.repositories());
		Map<String, List<String>> map = new LinkedHashMap<>();
		configuration.artifact().dsls().forEach(dsl -> dslVersions(map, connector, dsl));
		configuration.artifact().dependencies().forEach(d -> {
			String[] split = d.identifier().split(":");
			List<String> versions = connector.versions(split[0] + ":" + split[1]);
			if (!versions.isEmpty()) map.put(d.identifier(), filter(versions, split[2]));
		});
		return map;
	}

	private void dslVersions(Map<String, List<String>> map, ArtifactoryConnector connector, Configuration.Artifact.Dsl dsl) {
		List<String> dslVersions = connector.dslVersions(dsl.name());
		if (!dslVersions.isEmpty() && !dsl.version().equals(dslVersions.get(dslVersions.size() - 1)))
			map.put(dsl.name(), dslVersions);
	}

	private List<String> filter(List<String> versions, String current) {
		Version currentVersion = versionOf(current);
		return versions.stream().map(ModuleDependencyPropagator::versionOf).filter(Objects::nonNull).filter(v -> v.equals(currentVersion) || v.compareTo(currentVersion) > 0).map(Version::get).toList();
	}

	@Nullable
	private static Version versionOf(String current) {
		try {
			return new Version(current);
		} catch (Exception e) {
			return null;
		}
	}
}
