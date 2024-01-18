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
import io.intino.plugin.actions.dialog.UpdateVersionDialog;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.project.builders.BoxBuilderManager;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.project.configuration.model.LegioBox;
import io.intino.plugin.project.configuration.model.LegioModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

import static io.intino.plugin.lang.psi.impl.IntinoUtil.configurationOf;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.builders.BoxBuilderManager.ARTIFACT_ID;
import static io.intino.plugin.project.builders.BoxBuilderManager.GROUP_ID;
import static java.util.stream.Collectors.toMap;

public class ModuleDependencyPropagator {
	private static final Logger LOG = Logger.getInstance(ModuleDependencyPropagator.class.getName());
	private final Module module;
	private final Configuration configuration;
	private Version.Level change = null;

	public ModuleDependencyPropagator(Module module, Configuration configuration) {
		this.module = module;
		this.configuration = configuration;
	}

	public Version.Level execute() {
		if (module == null) return null;
		Map<String, String> newVersions = askForNewVersions();
		change = null;
		if (newVersions.isEmpty()) return null;
		for (String library : newVersions.keySet()) {
			try {
				String[] identifier = library.split(":");
				if (identifier.length < 3) continue;
				propagateChange(library, identifier, newVersions);
			} catch (Throwable e) {
				LOG.error(e);
			}
		}
		configurationOf(module).reload();
		return change;
	}

	private void propagateChange(String library, String[] identifier, Map<String, String> newVersions) {
		Configuration.Artifact.Dependency dependency = dependency(identifier);
		if (dependency == null) {
			if (configuration.artifact().box() != null && GROUP_ID.equals(identifier[0]) && ARTIFACT_ID.equals(identifier[1])) {
				change = calculateChange(change, identifier[2], configuration.artifact().box().version());
				updateBoxBuilder(newVersions, library);
			} else {
				Configuration.Artifact.Model model = configuration.artifact().model();
				if (model != null) updateModel(library, identifier, newVersions, model);
			}
		} else if (!dependency.version().equals(newVersions.get(library))) {
			change = calculateChange(change, newVersions.get(library), dependency.version());
			dependency.version(newVersions.get(library));
		}
	}

	private void updateModel(String library, String[] identifier, Map<String, String> newVersions, Configuration.Artifact.Model model) {
		String[] sdk = model.sdk().split(":");
		if (sdk[0].equals(identifier[0]) && sdk[1].equals(identifier[1])) {
			change = calculateChange(change, identifier[2], model.sdkVersion());
			updateModelBuilder(newVersions, library);
		} else {
			String[] frameworkCoors = new LanguageResolver(module, model, model.language().effectiveVersion(), configuration.repositories()).frameworkCoors().split(":");
			if (frameworkCoors[0].equals(identifier[0]) && frameworkCoors[1].equals(identifier[1]))
				updateModelFramework(newVersions, library);
		}
	}

	private void updateBoxBuilder(Map<String, String> newVersions, String library) {
		if (!newVersions.get(library).equals(configuration.artifact().box().version()))
			((LegioBox) configuration.artifact().box()).version(newVersions.get(library));
	}

	private void updateModelFramework(Map<String, String> newVersions, String library) {
		if (!newVersions.get(library).equals(configuration.artifact().model().language().version()))
			((LegioModel) configuration.artifact().model()).language().version(newVersions.get(library));
	}

	private void updateModelBuilder(Map<String, String> newVersions, String library) {
		if (!newVersions.get(library).equals(configuration.artifact().model().sdkVersion()))
			((LegioModel) configuration.artifact().model()).sdkVersion(newVersions.get(library));
	}

	private Version.Level calculateChange(Version.Level currentChangeLevel, String newVersion, String oldVersion) {
		Version.Level changeLevel = calculateChangeLevel(oldVersion, newVersion);
		return currentChangeLevel == null || currentChangeLevel.ordinal() < changeLevel.ordinal() ? changeLevel : currentChangeLevel;
	}

	private Version.Level calculateChangeLevel(String oldVersion, String newVersion) {
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
		ArtifactoryConnector connector = new ArtifactoryConnector(module.getProject(), configuration.repositories());
		Map<String, List<String>> map = new LinkedHashMap<>();
		Configuration.Artifact.Box box = configuration.artifact().box();
		if (box != null) {
			List<String> boxVersions = connector.boxBuilderVersions();
			if (!box.version().equals(boxVersions.get(boxVersions.size() - 1)))
				map.put(GROUP_ID + ":" + BoxBuilderManager.ARTIFACT_ID + ":" + box.version(), boxVersions);
		}
		Configuration.Artifact.Model model = configuration.artifact().model();
		if (model != null) {
			String builder = safe(() -> configuration.artifact().model().sdk());
			List<String> modelBuilderVersions = connector.modelBuilderVersions(builder);
			if (!model.sdkVersion().equals(modelBuilderVersions.get(modelBuilderVersions.size() - 1)))
				map.put(builder + ":" + model.sdkVersion(), modelBuilderVersions);
			String frameworkCoors = new LanguageResolver(module, model, model.language().effectiveVersion(), configuration.repositories()).frameworkCoors();
			String[] parts = frameworkCoors.split(":");
			List<String> versions = connector.versions(parts[0] + ":" + parts[1]);
			if (!versions.isEmpty()) map.put(frameworkCoors, filter(versions, parts[2]));
		}
		configuration.artifact().dependencies().forEach(d -> {
			String[] split = d.identifier().split(":");
			List<String> versions = connector.versions(split[0] + ":" + split[1]);
			if (!versions.isEmpty()) map.put(d.identifier(), filter(versions, split[2]));
		});
		return map;
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
