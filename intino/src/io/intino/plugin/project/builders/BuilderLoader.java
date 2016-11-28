package io.intino.plugin.project.builders;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.moandjiezana.toml.Toml;
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

	}

	private static void registerGroups(ClassLoader classLoader, List<Builder.Group> groups) {

	}

	private static void registerActions(ClassLoader classLoader, List<Builder.Action> actions) {
		final ActionManager manager = ActionManager.getInstance();
		for (Builder.Action action : actions) {
			if (manager.getAction(action.id) != null) continue;
			final AnAction anAction = loadAction(classLoader, action);
			if (anAction != null) {
				manager.registerAction(action.id, anAction);
			}
		}

	}

	private static AnAction loadAction(ClassLoader classLoader, Builder.Action action) {
		try {
			return (AnAction) classLoader.loadClass(action.aClass).newInstance();
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
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
			String anchor;
		}

		private class Group {
			String id;
			String popup;
			String text;
			String aclass;
			String groupId;

		}
	}
}
