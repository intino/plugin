package io.intino.legio.plugin.dependencyresolution;

import io.intino.legio.plugin.project.LegioConfiguration;

import java.io.IOException;
import java.util.TreeMap;

import static org.apache.maven.artifact.Artifact.LATEST_VERSION;

public class LegioUtil {


	public static String effectiveVersionOf(String dsl, String version, LegioConfiguration configuration) {
		if (version.equals(LATEST_VERSION))
			try {
				TreeMap<Long, String> versions = new TreeMap<>();
				new ArtifactoryConnector(configuration.releaseRepositories(), configuration.snapshotRepositories(), configuration.languageRepository()).versions(dsl).forEach(v -> versions.put(indexOf(v), v));
				return versions.get(versions.lastKey());
			} catch (IOException e) {
				return LATEST_VERSION;
			}
		return version;
	}

	private static Long indexOf(String version) {
		String value = "";
		String[] split = (version.contains("-") ? version.substring(0, version.indexOf("-")) : version).split("\\.");
		int times = split.length - 1;
		if (times == 0) return Long.parseLong(version);
		for (String s : split) {
			if (s.length() < 2) value += new String(new char[2 - s.length()]).replace("\0", "0");
			value += s;
		}
		return Long.parseLong(value);
	}
}
