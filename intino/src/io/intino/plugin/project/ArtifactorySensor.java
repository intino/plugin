package io.intino.plugin.project;

import com.intellij.ide.util.PropertiesComponent;
import io.intino.legio.graph.Repository;
import io.intino.legio.graph.Repository.Type;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ArtifactorySensor {
	public static final String LANGUAGES_TAG = "tara.dsls";
	public static final String LANGUAGE_TAG = "tara.dsl.";
	public static final String BOXING_TAG = "konos.boxing";
	public static final String DEPENDENCY_TAG = "dependency.library";
	public static final String GENERATION_TAG = "tara.generation";
	private final PropertiesComponent properties;
	private final ArtifactoryConnector languageConnectors;
	private final ArtifactoryConnector dependencyConnectors;

	public ArtifactorySensor(List<Type> repositories) {
		this.languageConnectors = new ArtifactoryConnector(by(repositories, Repository.Language.class));
		this.dependencyConnectors = new ArtifactoryConnector(by(repositories, Repository.Release.class, Repository.Snapshot.class));
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
		final List<String> languages = languageConnectors.languages();
		if (!languages.isEmpty()) properties.setValues(LANGUAGES_TAG, languages.toArray(new String[0]));
		return languages;
	}

	private void languagesVersions(List<String> languages) {
		for (String language : languages) {
			final List<String> versions = languageConnectors.dslVersions(language);
			if (!versions.isEmpty())
				properties.setValues(LANGUAGE_TAG + language, versions.toArray(new String[0]));
		}
	}

	private void boxingVersions() {
		final List<String> versions = languageConnectors.boxingVersions();
		if (!versions.isEmpty())
			properties.setValues(BOXING_TAG, versions.toArray(new String[0]));
	}

	private void generationVersions() {
		final List<String> versions = languageConnectors.generationVersions();
		if (!versions.isEmpty()) properties.setValues(GENERATION_TAG, versions.toArray(new String[0]));
	}

	public List<String> dependencyVersions(String artifact) {
		return dependencyConnectors.versions(artifact);
	}

	@SafeVarargs
	private final Map<String, String> by(List<Type> types, Class<? extends Type>... repositories) {
		try {
			return new HashMap<>(types.stream().filter(t -> Arrays.stream(repositories).anyMatch(t::i$)).collect(Collectors.toMap(Type::url, Type::mavenID)));
		} catch (Throwable ignored) {
		}
		return new HashMap<>();
	}
}
