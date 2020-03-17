package io.intino.plugin.dependencyresolution;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.intino.plugin.project.IntinoDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolutionCache extends HashMap<String, List<DependencyCatalog.Dependency>> {
	private static Map<String, ResolutionCache> cache = new HashMap<>();
	private File resolutionsFile;

	private ResolutionCache(Project project) {
		resolutionsFile = resolutionsFile(project);
		load();
	}

	public static ResolutionCache instance(Project project) {
		if (!cache.containsKey(project.getName())) cache.put(project.getName(), new ResolutionCache(project));
		return cache.get(project.getName());
	}

	public static void invalidate(String dependency) {
		for (ResolutionCache value : cache.values()) {
			value.remove(dependency);
			value.save();
		}
	}

	public void invalidate() {
		resolutionsFile.delete();
		clear();
	}

	private void load() {
		try {
			if (!resolutionsFile.exists()) return;
			Gson gson = new Gson();
			String json = new String(Files.readAllBytes(resolutionsFile.toPath()));
			Map<? extends String, ? extends List<DependencyCatalog.Dependency>> map = gson.fromJson(json, new TypeToken<Map<String, List<DependencyCatalog.Dependency>>>() {
			}.getType());
			if (map != null) putAll(map);
		} catch (Exception e) {
			resolutionsFile.delete();
		}
	}

	synchronized void save() {
		try {

			Files.write(resolutionsFile.toPath(), new Gson().toJson(new HashMap<>(this)).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			Logger.getInstance(this.getClass()).error(e);
		}
	}

	private File resolutionsFile(Project project) {
		return new File(IntinoDirectory.of(project), "resolution.cache");
	}
}
