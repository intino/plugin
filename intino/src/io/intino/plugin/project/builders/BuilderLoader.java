package io.intino.plugin.project.builders;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.moandjiezana.toml.Toml;
import io.intino.plugin.IntinoIcons;
import tara.Language;
import tara.intellij.lang.LanguageManager;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

class BuilderLoader {
	private static final Logger LOG = Logger.getInstance(BuilderLoader.class.getName());

	private BuilderLoader() {
	}

	static void load(String name, File[] library) {
		try {
			final ClassLoader classLoader = createClassLoader(library);
			if (classLoader == null) return;
			Builder builder = Builder.from(classLoader.getResourceAsStream(name.toLowerCase() + ".toml"));
			if (builder == null) return;
			unregisterActions(builder.actions);
			registerGroups(classLoader, builder.groups);
			registerActions(classLoader, builder.actions);
			addLanguage(classLoader);
		} catch (RuntimeException | Error e) {
			LOG.error(e.getMessage(), e);
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
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	private static void unregisterActions(List<Builder.Action> actions) {
		final ActionManager manager = ActionManager.getInstance();
		for (Builder.Action action : actions) if (manager.getAction(action.id) != null) manager.unregisterAction(action.id);
		System.gc();

	}

	private static void registerGroups(ClassLoader classLoader, List<Builder.Group> groups) {
		final ActionManager manager = ActionManager.getInstance();
		for (Builder.Group group : groups) {
			if (manager.getAction(group.id) != null) {
				LOG.debug("group already registered: " + group.id);
				continue;
			}
			final ActionGroup anAction = loadGroup(classLoader, group);

			if (anAction != null) {
				anAction.getTemplatePresentation().setIcon(IntinoIcons.PANDORA_16);
				anAction.getTemplatePresentation().setText("Pandora");
				anAction.getTemplatePresentation().setDescription("Pandora Actions");
				anAction.getTemplatePresentation().setEnabled(true);
				anAction.setPopup(true);
				manager.registerAction(group.id, anAction);
				((DefaultActionGroup) manager.getAction(group.groupId)).add(anAction, Constraints.LAST);
			} else LOG.error("group is null: " + group.id);
		}

	}

	private static void registerActions(ClassLoader classLoader, List<Builder.Action> actions) {
		final ActionManager manager = ActionManager.getInstance();
		for (Builder.Action action : actions) {
			if (manager.getAction(action.id) != null) {
				LOG.debug("action already registered: " + action.id);
				continue;
			}
			final AnAction anAction = loadAction(classLoader, action);
			if (anAction != null) {
				manager.registerAction(action.id, anAction);
				((DefaultActionGroup) manager.getAction(action.groupId)).add(anAction);
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


	static class Builder {
		String name;
		String version;
		String changeNotes;

		List<Action> actions;
		List<Group> groups;

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
