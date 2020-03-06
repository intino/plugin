package io.intino.plugin.project;

import com.intellij.ide.util.PropertiesComponent;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArtifactorySensor {
	public static final String LANGUAGES_TAG = "tara.dsls";
	public static final String LANGUAGE_TAG = "tara.dsl.";
	public static final String BOXING_TAG = "konos.boxing";
	public static final String DEPENDENCY_TAG = "dependency.library";
	public static final String GENERATION_TAG = "tara.generation";
	private final PropertiesComponent properties;
	private final ArtifactoryConnector languageConnector;
	private final ArtifactoryConnector connector;

	public ArtifactorySensor(List<Repository> repositories) {
		this.languageConnector = new ArtifactoryConnector(by(repositories, Repository.Language.class));
		this.connector = new ArtifactoryConnector(by(repositories, Repository.Release.class, Repository.Snapshot.class));
		this.properties = PropertiesComponent.getInstance();
	}

	void update() {
		final List<String> languages = languages();
		languagesVersions(languages);
		boxingVersions();
		generationVersions();
	}

	@NotNull
	private List<String> languages() {
		final List<String> languages = languageConnector.languages();
		if (!languages.isEmpty()) properties.setValues(LANGUAGES_TAG, languages.toArray(new String[0]));
		return languages;
	}

	private void languagesVersions(List<String> languages) {
		for (String language : languages) {
			final List<String> versions = languageConnector.dslVersions(language);
			if (!versions.isEmpty())
				properties.setValues(LANGUAGE_TAG + language, versions.toArray(new String[0]));
		}
	}

	private void boxingVersions() {
		final List<String> versions = languageConnector.boxingVersions();
		if (!versions.isEmpty())
			properties.setValues(BOXING_TAG, versions.toArray(new String[0]));
	}

	private void generationVersions() {
		final List<String> versions = languageConnector.modelBuilderVersions();
		if (!versions.isEmpty()) properties.setValues(GENERATION_TAG, versions.toArray(new String[0]));
	}

	public List<String> dependencyVersions(String artifact) {
		return connector.versions(artifact);
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
