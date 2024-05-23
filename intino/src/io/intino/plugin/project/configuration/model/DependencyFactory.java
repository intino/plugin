package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;

import java.util.List;

public class DependencyFactory {

	public static Configuration.Artifact.Dependency.Compile createCompile(String groupId, String artifactId, String version) {
		return new Configuration.Artifact.Dependency.Compile() {
			@Override
			public String groupId() {
				return groupId;
			}

			@Override
			public String artifactId() {
				return artifactId;
			}

			@Override
			public String version() {
				return version;
			}

			@Override
			public void version(String newVersion) {

			}

			@Override
			public String scope() {
				return "Compile";
			}

			@Override
			public List<Exclude> excludes() {
				return null;
			}

			@Override
			public String effectiveVersion() {
				return null;
			}

			@Override
			public void effectiveVersion(String version) {

			}

			@Override
			public boolean transitive() {
				return false;
			}


			@Override
			public boolean toModule() {
				return false;
			}

			@Override
			public void toModule(boolean toModule) {

			}

			@Override
			public Configuration root() {
				return null;
			}

			@Override
			public Configuration.ConfigurationNode owner() {
				return null;
			}
		};
	}

}
