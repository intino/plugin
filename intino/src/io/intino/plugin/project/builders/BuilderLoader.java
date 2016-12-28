package io.intino.plugin.project.builders;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.moandjiezana.toml.Toml;
import io.intino.plugin.IntinoIcons;
import tara.Language;
import io.intino.tara.plugin.lang.LanguageManager;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuilderLoader {
	private static final Logger LOG = Logger.getInstance(BuilderLoader.class.getName());
	private static List<String> loadedVersions = new ArrayList<>();

	private BuilderLoader() {
	}

	static Builder load(String name, File[] library, String version) {
		try {
			if (loadedVersions.contains(library[0].getAbsolutePath())) return null;
			final ClassLoader classLoader = createClassLoader(library);
			if (classLoader == null) return null;
			Builder builder = Builder.from(classLoader.getResourceAsStream(name.toLowerCase() + ".toml"));
			if (builder == null) return null;
			registerGroups(classLoader, builder.groups);
			registerActions(classLoader, builder.actions, version);
			addLanguage(classLoader);
			loadedVersions.add(library[0].getAbsolutePath());
			return builder;
		} catch (RuntimeException | Error e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	private static void addLanguage(ClassLoader classLoader) {
		final Language language = loadLanguage(classLoader);
		if (language != null) LanguageManager.register(language);
	}

	private static Language loadLanguage(ClassLoader classLoader) {
		try {
			return (Language) classLoader.loadClass(LanguageManager.DSL_GROUP_ID + ".Pandora").newInstance();
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
				actionGroup.getTemplatePresentation().setIcon(IntinoIcons.PANDORA_16);
				actionGroup.getTemplatePresentation().setText("Pandora");
				actionGroup.getTemplatePresentation().setDescription("Pandora Actions");
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
				if (action.shortcut != null) anAction.registerCustomShortcutSet(CustomShortcutSet.fromString(action.shortcut), null);
				manager.registerAction(action.id + version, anAction);
				((DefaultActionGroup) manager.getAction(action.groupId)).add(anAction);
			} else LOG.error("action is null: " + action.id);
		}

	}

	private synchronized static AnAction loadAction(ClassLoader classLoader, Builder.Action action) {
		try {
			return (AnAction) classLoader.loadClass(action.aClass).newInstance();
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			LOG.error(e.toString() + ": " + e.getMessage());
			return null;
		}
	}

	private synchronized static ActionGroup loadGroup(ClassLoader classLoader, Builder.Group group) {
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
			LOG.error("Malformed URL");
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
