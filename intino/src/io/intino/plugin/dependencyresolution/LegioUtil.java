package io.intino.plugin.dependencyresolution;

import io.intino.plugin.project.LegioConfiguration;

import java.util.TreeMap;

import static org.apache.maven.artifact.Artifact.LATEST_VERSION;

public class LegioUtil {


	public static String effectiveVersionOf(String dsl, String version, LegioConfiguration configuration) {
		if (version.equals(LATEST_VERSION)) {
			TreeMap<Long, String> versions = new TreeMap<>();
			new ArtifactoryConnector(configuration.languageRepositories()).versions(dsl).forEach(v -> versions.put(indexOf(v), v));
			return versions.isEmpty() ? LATEST_VERSION : versions.get(versions.lastKey());
		}
		return version;
	}

	private static Long indexOf(String version) {
		StringBuilder value = new StringBuilder();
		String[] split = (version.contains("-") ? version.substring(0, version.indexOf("-")) : version).split("\\.");
		int times = split.length - 1;
		if (times == 0) return Long.parseLong(version);
		for (String s : split) {
			if (s.length() < 2) value.append(new String(new char[2 - s.length()]).replace("\0", "0"));
			value.append(s);
		}
		return Long.parseLong(value.toString());
	}
}
