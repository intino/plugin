package io.intino.plugin.project.builders;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.moandjiezana.toml.Toml;
import io.intino.plugin.IntinoIcons;
import io.intino.tara.Language;
import io.intino.tara.plugin.lang.LanguageManager;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuilderLoader {
	private static final String KONOS = "konos";
	private static final Logger LOG = Logger.getInstance(BuilderLoader.class.getName());
	private static Map<String, ClassLoader> loadedVersions = new HashMap<>();
	private static Map<Project, String> versionsByProject = new HashMap<>();

	private BuilderLoader() {
	}

	static boolean isLoaded(Project module, String version) {
		return version.equalsIgnoreCase(versionsByProject.get(module)) && areClassesLoaded(version);
	}

	private static boolean areClassesLoaded(String version) {
		return loadedVersions.containsKey(version);
	}

	public static boolean exists(String version) {
		return loadedVersions.get(version) != null;
	}

	static void load(Project module, File[] libraries, String version) {
		try {
			if (isLoaded(module, version)) return;
			final ClassLoader classLoader = areClassesLoaded(version) ? loadedVersions.get(version) : createClassLoader(libraries);
			if (classLoader == null) return;
			Builder builder = Builder.from(classLoader.getResourceAsStream(KONOS + ".toml"));
			if (builder == null) return;
			registerBuilder(version, classLoader, builder);
			addLanguage(module, classLoader);
			loadedVersions.put(version, classLoader);
			versionsByProject.put(module, version);
		} catch (RuntimeException | Error e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private static void registerBuilder(String version, ClassLoader classLoader, Builder builder) {
		if (!areClassesLoaded(version)) {
			registerGroups(classLoader, builder.groups);
			registerActions(classLoader, builder.actions, version);
		}
	}

	private static void addLanguage(Project project, ClassLoader classLoader) {
		final Language language = loadLanguage(classLoader);
		if (language != null) LanguageManager.registerAuxiliar(project, language);
	}

	private static Language loadLanguage(ClassLoader classLoader) {
		try {
			return (Language) classLoader.loadClass(LanguageManager.DSL_GROUP_ID + ".Konos").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	private static void registerGroups(ClassLoader classLoader, List<Builder.Group> groups) {
		final ActionManager manager = ActionManager.getInstance();
		for (Builder.Group group : groups) {
			if (manager.getAction(group.id) != null) {
				LOG.debug("group already registered: " + group.id);
				continue;
			}
			final ActionGroup actionGroup = loadGroup(classLoader, group);
			if (actionGroup != null) {
				actionGroup.getTemplatePresentation().setIcon(IntinoIcons.KONOS_16);
				actionGroup.getTemplatePresentation().setText("Konos");
				actionGroup.getTemplatePresentation().setDescription("Konos Actions");
				actionGroup.getTemplatePresentation().setEnabled(true);
				actionGroup.setPopup(true);
				manager.registerAction(group.id, actionGroup);
				((DefaultActionGroup) manager.getAction(group.groupId)).add(actionGroup, Constraints.LAST);
			} else LOG.error("group is null: " + group.id);
		}
	}

	private static void registerActions(ClassLoader classLoader, List<Builder.Action> actions, String version) {
		final ActionManager manager = ActionManager.getInstance();
		for (Builder.Action action : actions) {
			if (manager.getAction(action.id + version) != null) {
				LOG.debug("action already registered: " + action.id + version);
				continue;
			}
			final AnAction anAction = loadAction(classLoader, action);
			if (anAction != null) {
				if (action.shortcut != null)
					anAction.registerCustomShortcutSet(CustomShortcutSet.fromString(action.shortcut), null);
				manager.registerAction(action.id + version, anAction);
				if (action.relativeToAction != null)
					((DefaultActionGroup) manager.getAction(action.groupId)).add(anAction, new Constraints(null, action.relativeToAction));
				else ((DefaultActionGroup) manager.getAction(action.groupId)).add(anAction);
			} else LOG.error("action is null: " + action.id);
		}
	}

	private static AnAction loadAction(ClassLoader classLoader, Builder.Action action) {
		try {
			return (AnAction) classLoader.loadClass(action.aClass).newInstance();
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			LOG.error(e.getMessage());
			return null;
		}
	}

	private static ActionGroup loadGroup(ClassLoader classLoader, Builder.Group group) {
		try {
			return (ActionGroup) classLoader.loadClass(group.aClass).newInstance();
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			LOG.error(e.getMessage());
			return null;
		}
	}

	private static ClassLoader createClassLoader(File[] libraries) {
		return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () ->
				new URLClassLoader(Arrays.stream(libraries).map(BuilderLoader::toURL).toArray(URL[]::new), BuilderLoader.class.getClassLoader()));
	}

	private static URL toURL(File l) {
		try {
			return l.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}


	public static class Builder {
		String name;
		String version;
		String changeNotes;
		String gulpFileTemplate;
		String packageJsonFileTemplate;

		List<Action> actions;
		List<Group> groups;

		public String gulpFileTemplate() {
			return gulpFileTemplate;
		}

		public String packageJsonFileTemplate() {
			return packageJsonFileTemplate;
		}

		static Builder from(InputStream manifest) {
			return manifest == null ? null : new Toml().read(manifest).to(Builder.class);
		}

		static class Action {
			String id;
			String aClass;
			String groupId;
			String shortcut;
			String anchor;
			String relativeToAction;
		}

		private class Group {
			String id;
			String popup;
			String text;
			String aClass;
			String groupId;

		}
	}
}
