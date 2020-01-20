package io.intino.plugin.project.builders;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.intino.plugin.lang.LanguageManager;
import io.intino.tara.Language;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfaceBuilderLoader {
	private static final Logger LOG = Logger.getInstance(InterfaceBuilderLoader.class.getName());
	private static Map<String, ClassLoader> loadedVersions = new HashMap<>();
	private static Map<Project, String> versionsByProject = new HashMap<>();
	public static String minimunVersion = "1.0.0";

	private InterfaceBuilderLoader() {
	}

	static {
		try {
			minimunVersion = IOUtils.readLines(InterfaceBuilderLoader.class.getResourceAsStream("/minimum_box.info"), Charset.defaultCharset()).get(0);
		} catch (IOException ignored) {
		}
	}

	static boolean isLoaded(Project project, String version) {
		return version != null && version.equalsIgnoreCase(versionsByProject.get(project)) && areClassesLoaded(version);
	}

	private static boolean areClassesLoaded(String version) {
		return loadedVersions.containsKey(version);
	}

	public static boolean exists(String version) {
		return loadedVersions.get(version) != null;
	}

	static void load(Project module, File[] libraries, String version) {
		try {
			if (version.compareTo(minimunVersion) < 0) return;
			if (isLoaded(module, version)) return;
			final ClassLoader classLoader = areClassesLoaded(version) ? loadedVersions.get(version) : createClassLoader(libraries);
			if (classLoader == null) return;
			Manifest manifest = Manifest.load(classLoader);
			if (manifest == null) return;
			registerBuilder(version, classLoader, manifest);
			addLanguage(module, classLoader);
			loadedVersions.put(version, classLoader);
			versionsByProject.put(module, version);
		} catch (RuntimeException | Error e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private static void registerBuilder(String version, ClassLoader classLoader, Manifest manifest) {
		if (!areClassesLoaded(version)) registerActions(classLoader, manifest.actions, version);
	}

	private static void addLanguage(Project project, ClassLoader classLoader) {
		final Language language = loadLanguage(classLoader);
		if (language != null) LanguageManager.registerAuxiliar(project, language);
	}

	private static Language loadLanguage(ClassLoader classLoader) {
		try {
			return (Language) classLoader.loadClass(LanguageManager.DSL_GROUP_ID + ".Konos").getConstructors()[0].newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InvocationTargetException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	private static void registerActions(ClassLoader classLoader, List<Manifest.Action> actions, String version) {
		final ActionManager manager = ActionManager.getInstance();
		for (Manifest.Action action : actions) {
			if (manager.getAction(action.id + version) != null) {
				LOG.debug("action already registered: " + action.id + version);
				continue;
			}
			final AnAction anAction = loadAction(classLoader, action);
			if (anAction != null) {
				if (action.shortcut != null)
					anAction.registerCustomShortcutSet(CustomShortcutSet.fromString(action.shortcut), null);
				manager.registerAction(action.id + version, anAction);
				if (!manager.isGroup(action.groupId)) continue;
				if (action.relativeToAction != null)
					((DefaultActionGroup) manager.getAction(action.groupId)).add(anAction, new Constraints(anchorOf(action.anchor), action.relativeToAction));
				else
					((DefaultActionGroup) manager.getAction(action.groupId)).add(anAction, new Constraints(anchorOf(action.anchor), null));
			} else LOG.error("action is null: " + action.id);
		}
	}

	private static Anchor anchorOf(String anchor) {
		if (anchor.equals("first")) return Anchor.FIRST;
		if (anchor.equals("last")) return Anchor.LAST;
		return null;
	}

	private static AnAction loadAction(ClassLoader classLoader, Manifest.Action action) {
		try {
			return (AnAction) classLoader.loadClass(action.aClass).getDeclaredConstructors()[0].newInstance();
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	private static ClassLoader createClassLoader(File[] libraries) {
		return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () ->
				new URLClassLoader(Arrays.stream(libraries).map(InterfaceBuilderLoader::toURL).toArray(URL[]::new), InterfaceBuilderLoader.class.getClassLoader()));
	}

	private static URL toURL(File l) {
		try {
			return l.toURI().toURL();
		} catch (MalformedURLException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	public static class Manifest {
		List<Action> actions;
		Map<String, String> dependencies;

		static Manifest load(ClassLoader classLoader) {
			InputStream stream = classLoader.getResourceAsStream("manifest.json");
			if (stream != null) return new Gson().fromJson(new InputStreamReader(stream), Manifest.class);
			return null;
		}

		static class Action {
			String id;
			String aClass;
			String groupId;
			String shortcut;
			String anchor;
			String relativeToAction;
		}

	}
}
