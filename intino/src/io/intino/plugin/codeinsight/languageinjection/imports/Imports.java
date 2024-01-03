package io.intino.plugin.codeinsight.languageinjection.imports;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.project.Project;
import io.intino.plugin.lang.LanguageManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Imports {

	private static Map<String, Map<String, Set<String>>> imports;
	private File taraDirectory;

	public Imports(Project project) {
		taraDirectory = LanguageManager.getTaraLocalDirectory(project);
		imports = loadImports();
	}

	private Map<String, Map<String, Set<String>>> loadImports() {
		if (taraDirectory == null) return Collections.emptyMap();
		final File[] files = taraDirectory.listFiles((dir, name) -> name.endsWith(LanguageManager.JSON));
		Gson gson = new Gson();
		Map<String, Map<String, Set<String>>> imports = new HashMap<>();
		if (files == null) return imports;
		try {
			for (File file : files) {
				imports.put(file.getName().toLowerCase(), gson.fromJson(new FileReader(file), new TypeToken<Map<String, Set<String>>>() {
				}.getType()));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return imports;
	}

	public Map<String, Map<String, Set<String>>> get() {
		return imports;
	}

	public Map<String, Set<String>> get(String module) {
		return imports.get(module.toLowerCase());
	}

	public void save(String fileName, String qn, Set<String> newImports) {
		final String file = fileName.toLowerCase();
		if (!imports.containsKey(file)) imports.put(file, new HashMap<>());
		imports.get(file).put(qn, newImports == null ? Collections.emptySet() : newImports);
		save(file);
	}

	private void save(String fileName) {
		try {
			final File file = new File(taraDirectory, fileName.toLowerCase());
			file.getParentFile().mkdirs();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Files.write(file.toPath(), gson.toJson(imports.get(fileName.toLowerCase())).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void refactor(String module, String old, String newQn) {
		final Map<String, Set<String>> map = imports.get(module.toLowerCase() + LanguageManager.JSON);
		if (map == null) return;
		Map<String, String> qnMap = new HashMap<>();
		final List<String> collect = map.keySet().stream().filter(qn -> qn.startsWith(old)).toList();
		collect.forEach(k -> qnMap.put(k, k.replaceFirst(old, newQn)));
		for (String key : qnMap.keySet()) {
			map.put(qnMap.get(key), map.get(key));
			map.remove(key);
		}
		save(module.toLowerCase() + LanguageManager.JSON);
	}
}
