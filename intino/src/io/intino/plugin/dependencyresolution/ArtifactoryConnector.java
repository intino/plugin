package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import io.intino.tara.dsl.Proteo;
import io.intino.tara.dsl.Verso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.plugin.dependencyresolution.TaraBuilderResolver.TARA_BUILDER_REPOSITORY;
import static io.intino.plugin.project.builders.InterfaceBuilderManager.INTINO_RELEASES;

public class ArtifactoryConnector {
	private static final Logger LOG = Logger.getInstance(ArtifactoryConnector.class.getName());

	private Map<String, String> snapshotRepositories;
	private final Map<String, String> languageRepositories;
	private final Map<String, String> releaseRepositories;

	public ArtifactoryConnector(Map<String, String> releaseRepositories, Map<String, String> snapshotRepositories, Map<String, String> languageRepositories) {
		this.releaseRepositories = releaseRepositories;
		this.snapshotRepositories = snapshotRepositories;
		this.languageRepositories = languageRepositories;
	}

	public List<String> languages() {
		List<String> langs = new ArrayList<>();
		try {
			for (String repo : languageRepositories.keySet()) {
				URL url = new URL(repo + "/" + "tara/dsl" + "/");
				final String result = new String(read(url.openStream()).toByteArray());
				if (result.isEmpty()) continue;
				langs.addAll(extractLanguages(result));
			}
			langs.add("Verso");
			langs.add("Proteo");
			return langs;
		} catch (IOException ignored) {
			System.out.println(ignored.getMessage());
		}
		return Collections.emptyList();
	}

	private List<String> extractLanguages(String result) {
		result = result.substring(result.indexOf("<pre><a"), result.lastIndexOf("</pre"));
		final List<String> languages = new ArrayList<>(Arrays.asList(result.split("\n")));
		languages.remove(0);
		return languages.stream().map(l -> l.substring(l.indexOf("\">") + 2, l.indexOf("/<"))).collect(Collectors.toList());
	}

	public List<String> versions(String dsl) {
		try {
			for (String repo : languageRepositories.keySet()) {
				if (dsl.equals(Proteo.class.getSimpleName()) || dsl.equals(Verso.class.getSimpleName()))
					return proteoVersions();
				URL url = new URL(repo + "/" + "tara/dsl" + "/" + dsl + "/maven-metadata.xml");
				final String mavenMetadata = new String(read(url.openStream()).toByteArray());
				if (mavenMetadata.isEmpty()) continue;
				return extractVersions(mavenMetadata);
			}
		} catch (IOException ignored) {
		}
		return Collections.emptyList();
	}

	public List<String> boxingVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/" + "io/intino/konos/builder/maven-metadata.xml");
			return extractVersions(new String(read(url.openStream()).toByteArray()));
		} catch (IOException ignored) {
			return Collections.emptyList();
		}
	}

	public List<String> generationVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/" + "io/intino/tara/builder/maven-metadata.xml");
			return extractVersions(new String(read(url.openStream()).toByteArray()));
		} catch (IOException ignored) {
			return Collections.emptyList();
		}
	}

	private List<String> proteoVersions() throws IOException {
		URL url = new URL(TARA_BUILDER_REPOSITORY + "/" + Proteo.GROUP_ID.replace(".", "/") + "/" + Proteo.ARTIFACT_ID + "/maven-metadata.xml");
		final String mavenMetadata = new String(read(url.openStream()).toByteArray());
		return extractVersions(mavenMetadata);
	}

	private List<String> extractVersions(String metadata) {
		if (!metadata.contains("<versions>")) return Collections.emptyList();
		metadata = metadata.substring(metadata.indexOf("<versions>")).substring("<versions>".length() + 1);
		metadata = metadata.substring(0, metadata.indexOf("</versions>"));
		metadata = metadata.replace("<version>", "").replace("</version>", "");
		return Arrays.stream(metadata.trim().split("\n")).map(String::trim).collect(Collectors.toList());
	}

	private ByteArrayOutputStream read(InputStream stream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
			int n;
			while ((n = stream.read(byteChunk)) > 0)
				baos.write(byteChunk, 0, n);
		} finally {
			stream.close();
		}
		return baos;
	}
}
