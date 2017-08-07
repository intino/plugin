package io.intino.plugin.dependencyresolution;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyLogger {

	private File file;
	private Map<String, List<String>> dependencies = new HashMap<>();
	private static DependencyLogger logger;

	public static DependencyLogger instance() {
		if (logger == null) logger = new DependencyLogger();
		return logger;
	}

	private DependencyLogger() {
	}

	public void add(String dependency, List<String> resolutions) {
		dependencies.put(dependency, resolutions);
	}

	public Map<String, List<String>> dependencyTree() {
		return dependencies;
	}
}
