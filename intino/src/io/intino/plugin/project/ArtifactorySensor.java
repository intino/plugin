package io.intino.plugin.project;

import com.intellij.ide.util.PropertiesComponent;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArtifactorySensor {
	public static final String Languages = "tara.dsls";
	public static final String LanguageLibrary = "tara.dsl.";
	public static final String BoxBuilder = "konos.builder";
	public static final String ModelBuilder = "model.builder";
	private static boolean buildersUpdated = false;
	private final PropertiesComponent properties;
	private final ArtifactoryConnector modelBuilderConnector;

	public ArtifactorySensor(List<Repository> repositories) {
		this.modelBuilderConnector = new ArtifactoryConnector(by(repositories));
		this.properties = PropertiesComponent.getInstance();
	}

	void update(String language) {
		if (language != null) languageVersions(language);
		updateBuilders();
	}

	private void updateBuilders() {
		if (buildersUpdated) return;
		boxBuilderVersions();
		modelBuilderVersions();
		buildersUpdated = true;
	}

	private void languageVersions(String language) {
		final List<String> versions = modelBuilderConnector.dslVersions(language);
		if (!versions.isEmpty())
			properties.setValues(LanguageLibrary + language, versions.toArray(new String[0]));
	}

	private void boxBuilderVersions() {
		final List<String> versions = modelBuilderConnector.boxBuilderVersions();
		if (!versions.isEmpty())
			properties.setValues(BoxBuilder, versions.toArray(new String[0]));
	}

	private void modelBuilderVersions() {
		final List<String> versions = modelBuilderConnector.modelBuilderVersions();
		if (!versions.isEmpty()) properties.setValues(ModelBuilder, versions.toArray(new String[0]));
	}

	@SafeVarargs
	private List<Repository> by(List<Repository> repositories, Class<? extends Repository>... types) {
		try {
			return repositories.stream().filter(r -> isType(r, types)).collect(Collectors.toList());
		} catch (Throwable ignored) {
		}
		return new ArrayList<>();
	}

	private boolean isType(Repository r, Class<? extends Repository>[] types) {
		return Arrays.stream(types).anyMatch(type -> type.isInstance(r));
	}
}
