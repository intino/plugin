package io.intino;

import java.util.List;

public interface ProjectConfiguration {

	default ProjectConfiguration init() {
		return null;
	}

	default boolean isSuitable() {
		return false;
	}

	default boolean isReloading() {
		return false;
	}

	default void reload() {
	}

	Project project();


	interface Project extends ConfigurationNode {

		String name();

		void name(String newName);

		String description();

		String url();

		Scm scm();

		List<Developer> developers();

		List<Configuration.Server> servers();

		List<Configuration.Repository> repositories();

		interface Developer {
			String name();

			String email();

			String organization();

			String organizationUrl();
		}

		interface Scm {
			String url();

			String connection();

			String developerConnection();

			String tag();
		}
	}

	interface ConfigurationNode {
		ProjectConfiguration root();

		ConfigurationNode owner();
	}
}