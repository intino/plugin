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
	private static ResolutionCache cache = null;
	private File resolutionsFile;

	private ResolutionCache(Project project) {
		resolutionsFile = resolutionsFile(project);
		load();
	}

	public static ResolutionCache instance(Project project) {
		if (cache != null) return cache;
		return cache = new ResolutionCache(project);
	}


	public void invalidate() {
		resolutionsFile.delete();
		clear();
	}


	public void invalidate(String dependency) {
		remove(dependency);
	}

	private void load() {
		try {
			if (!resolutionsFile.exists()) return;
			Gson gson = new Gson();
			putAll(gson.fromJson(new String(Files.readAllBytes(resolutionsFile.toPath())), new TypeToken<Map<String, List<DependencyCatalog.Dependency>>>() {
			}.getType()));
		} catch (Exception e) {
			Logger.getInstance(this.getClass()).error(e);
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