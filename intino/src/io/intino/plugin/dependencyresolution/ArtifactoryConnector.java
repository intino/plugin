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

public class ArtifactoryConnector {
	private static final Logger LOG = Logger.getInstance(ArtifactoryConnector.class.getName());

	private final Map<String, String> languageRepositories;
	private final Map<String, String> releaseRepositories;
	private final String snapshotRepository;

	public ArtifactoryConnector(Map<String, String> releaseRepositories, String snapshotRepository, Map<String, String> languageRepositories) {
		this.releaseRepositories = releaseRepositories;
		this.snapshotRepository = snapshotRepository;
		this.languageRepositories = languageRepositories;
	}

	public List<String> versions(String dsl) throws IOException {
		for (String repo : languageRepositories.keySet()) {
			if (dsl.equals(Proteo.class.getSimpleName()) || dsl.equals(Verso.class.getSimpleName()))
				return proteoVersions();
			URL url = new URL(languageRepositories + "/" + "tara/dsl" + "/" + dsl + "/maven-metadata.xml");
			final String mavenMetadata = new String(read(url.openStream()).toByteArray());
			if (mavenMetadata.isEmpty()) continue;
			return extractVersions(mavenMetadata);
		}
		return Collections.emptyList();
	}

	private List<String> proteoVersions() throws IOException {
		List<String> versions = new ArrayList<>();
		for (String repo : releaseRepositories.keySet()) {
			URL url = new URL(repo + "/" + Proteo.GROUP_ID.replace(".", "/") + "/" + Proteo.ARTIFACT_ID + "/maven-metadata.xml");
			final String mavenMetadata = new String(read(url.openStream()).toByteArray());
			versions.addAll(extractVersions(mavenMetadata));
		}

		URL url = new URL(snapshotRepository + "/" + Proteo.GROUP_ID.replace(".", "/") + "/" + Proteo.ARTIFACT_ID + "/maven-metadata.xml");
		final String mavenMetadata = new String(read(url.openStream()).toByteArray());
		versions.addAll(extractVersions(mavenMetadata));

		return versions;
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
