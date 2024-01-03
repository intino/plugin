package io.intino.plugin.project;

import com.intellij.ide.util.PropertiesComponent;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArtifactorySensor {
	public static final String Languages = "tara.dsls";
	public static final String LanguageLibrary = "tara.dsl.";
	public static final String BoxBuilder = "konos.builder";
	public static final String ModelBuilder = "model.builder";
	private final PropertiesComponent properties;
	private final ArtifactoryConnector artifactory;
	private final String modelSdk;

	public ArtifactorySensor(List<Repository> repositories, String modelSdk) {
		this.artifactory = new ArtifactoryConnector(by(repositories));
		this.modelSdk = modelSdk;
		this.properties = PropertiesComponent.getInstance();
	}

	public void update(String language) {
		if (language != null) languageVersions(language);
		updateBuilders();
	}

	private void updateBuilders() {
		boxBuilderVersions();
		modelBuilderVersions();
	}

	private void languageVersions(String language) {
		final List<String> versions = artifactory.dslVersions(language);
		if (!versions.isEmpty()) properties.setList(LanguageLibrary + language, versions);
	}

	private void boxBuilderVersions() {
		final List<String> versions = artifactory.boxBuilderVersions();
		if (!versions.isEmpty()) properties.setList(BoxBuilder, versions);
	}

	private void modelBuilderVersions() {
		final List<String> versions = artifactory.modelBuilderVersions(modelSdk);
		if (!versions.isEmpty()) properties.setList(ModelBuilder, versions);
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
		return Arrays.stream(types).anyMatch(type -> type.isInstance(r));
	}
}
