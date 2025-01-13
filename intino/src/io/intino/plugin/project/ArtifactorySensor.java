package io.intino.plugin.project;

import com.intellij.ide.util.PropertiesComponent;
import io.intino.Configuration;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArtifactorySensor {
	public static final String Languages = "tara.dsls";
	public static final String LanguageLibrary = "tara.dsl.";
	private final PropertiesComponent properties;
	private final ArtifactoryConnector artifactory;

	public ArtifactorySensor(List<Repository> repositories) {
		this.artifactory = new ArtifactoryConnector(by(repositories));
		this.properties = PropertiesComponent.getInstance();
	}

	public void update(Configuration.Artifact.Dsl dsl) {
		String name = dsl.name();
		update(name);
	}

	public void update(String dsl) {
		List<String> versions = artifactory.dslVersions(dsl);
		if (versions.isEmpty()) versions = artifactory.dslVersions(dsl.toLowerCase());
		properties.setList(LanguageLibrary + dsl, versions);
	}


	@SafeVarargs
	private List<Repository> by(List<Repository> repositories, Class<? extends Repository>... types) {
		try {
			return repositories.stream().filter(r -> isType(r, types)).toList();
		} catch (Throwable ignored) {
		}
		return new ArrayList<>();
	}

	private boolean isType(Repository r, Class<? extends Repository>[] types) {
		return types.length == 0 || Arrays.stream(types).anyMatch(type -> type.isInstance(r));
	}
}
