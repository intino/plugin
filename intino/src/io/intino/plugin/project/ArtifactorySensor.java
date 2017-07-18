package io.intino.plugin.project;

import com.intellij.ide.util.PropertiesComponent;
import io.intino.legio.Repository;
import io.intino.legio.Repository.Type;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArtifactorySensor {
	public static final String LANGUAGES_TAG = "tara.dsls";
	public static final String LANGUAGE_TAG = "tara.dsl.";
	public static final String BOXING_TAG = "konos.boxing";
	public static final String GENERATION_TAG = "tara.generation";
	private final PropertiesComponent properties;
	private final ArtifactoryConnector connector;

	ArtifactorySensor(List<Type> types) {
		this.connector = new ArtifactoryConnector(by(types, Repository.Release.class), by(types, Repository.Snapshot.class), by(types, Repository.Language.class));
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
		final List<String> languages = connector.languages();
		if (!languages.isEmpty()) properties.setValues(LANGUAGES_TAG, languages.stream().toArray(String[]::new));
		return languages;
	}

	private void languagesVersions(List<String> languages) {
		for (String language : languages) {
			final List<String> versions = connector.versions(language);
			if (!versions.isEmpty())
				properties.setValues(LANGUAGE_TAG + language, versions.stream().toArray(String[]::new));
		}
	}

	private void boxingVersions() {
		final List<String> versions = connector.boxingVersions();
		if (!versions.isEmpty())
			properties.setValues(BOXING_TAG, versions.stream().toArray(String[]::new));
	}

	private void generationVersions() {
		final List<String> versions = connector.generationVersions();
		if (!versions.isEmpty()) properties.setValues(GENERATION_TAG, versions.stream().toArray(String[]::new));
	}

	private Map<String, String> by(List<Type> types, Class<? extends Type> type) {
		try {

			return types.stream().filter(t -> t.is(type)).collect(Collectors.toMap(Type::url, Type::mavenID));
		} catch (Throwable e) {
		}
		return Collections.emptyMap();
	}
}
