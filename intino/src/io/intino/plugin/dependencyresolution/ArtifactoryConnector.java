package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.dsl.Meta;
import io.intino.tara.dsl.Proteo;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.project.builders.InterfaceBuilderManager.INTINO_RELEASES;
import static io.intino.plugin.project.builders.ModelBuilderManager.TARA_BUILDER_REPOSITORY;

public class ArtifactoryConnector {
	private static final Logger LOG = Logger.getInstance(ArtifactoryConnector.class.getName());

	private final List<Configuration.Repository> repositories;

	public ArtifactoryConnector(List<Configuration.Repository> repositories) {
		this.repositories = repositories;
		this.repositories.add(mavenRepository());
	}

	@NotNull
	private Configuration.Repository.Release mavenRepository() {
		return new Configuration.Repository.Release() {
			@Override
			public Configuration root() {
				return null;
			}

			@Override
			public Configuration.ConfigurationNode owner() {
				return null;
			}

			@Override
			public String identifier() {
				return "maven2";
			}

			@Override
			public String url() {
				return "https://repo.maven.apache.org/maven2/";
			}
		};
	}

	public List<String> languages() {
		List<String> langs = new ArrayList<>();
		try {
			for (Configuration.Repository repo : repositories) {
				URL url = new URL(repo.url() + "/" + "tara/dsl" + "/");
				final String result = new String(read(connect(url)).toByteArray());
				if (result.isEmpty()) continue;
				langs.addAll(extractLanguages(result));
			}
			langs.add("Verso");
			langs.add("Proteo");
			return langs;
		} catch (Throwable ignored) {
			return Collections.emptyList();
		}
	}

	public List<String> dslVersions(String dsl) {
		try {
			if (dsl.equals(Proteo.class.getSimpleName()) || dsl.equals(Meta.class.getSimpleName()))
				return proteoVersions();
			for (Configuration.Repository repo : repositories) {
				URL url = new URL(repo + "/" + "tara/dsl" + "/" + dsl + "/maven-metadata.xml");
				final String mavenMetadata = new String(read(connect(url)).toByteArray());
				if (!mavenMetadata.isEmpty()) return extractVersions(mavenMetadata);
			}
		} catch (Throwable ignored) {
		}
		return Collections.emptyList();
	}

	public List<String> versions(String artifact) {
		try {
			for (Configuration.Repository repo : repositories) {
				URL url = new URL(repo.url() + "/" + artifact.replace(":", "/").replace(".", "/") + "/maven-metadata.xml");
				final String mavenMetadata = new String(read(connect(url)).toByteArray());
				if (!mavenMetadata.isEmpty()) return extractVersions(mavenMetadata);
			}
		} catch (Throwable ignored) {
		}
		return Collections.emptyList();
	}

	private InputStream connect(URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			return connection.getInputStream();
		} catch (Throwable e) {
			return null;
		}
	}

	private List<String> extractLanguages(String result) {
		if (result == null || result.isEmpty() || !result.contains("<pre><a")) return Collections.emptyList();
		result = result.substring(result.indexOf("<pre><a"), result.lastIndexOf("</pre"));
		final List<String> languages = new ArrayList<>(Arrays.asList(result.split("\n")));
		languages.remove(0);
		return languages.stream().map(l -> l.substring(l.indexOf("\">") + 2, l.indexOf("/<"))).collect(Collectors.toList());
	}

	public List<String> boxingVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/" + "io/intino/konos/builder/maven-metadata.xml");
			return extractVersions(new String(read(connect(url)).toByteArray()));
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public List<String> generationVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/" + "io/intino/tara/builder/maven-metadata.xml");
			return extractVersions(new String(read(connect(url)).toByteArray()));
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	private List<String> proteoVersions() throws Throwable {
		URL url = new URL(TARA_BUILDER_REPOSITORY + "/" + Proteo.GROUP_ID.replace(".", "/") + "/" + Proteo.ARTIFACT_ID + "/maven-metadata.xml");
		final String mavenMetadata = new String(read(connect(url)).toByteArray());
		return extractVersions(mavenMetadata);
	}

	private List<String> extractVersions(String metadata) {
		if (!metadata.contains("<versions>")) return Collections.emptyList();
		metadata = metadata.substring(metadata.indexOf("<versions>")).substring("<versions>".length() + 1);
		metadata = metadata.substring(0, metadata.indexOf("</versions>"));
		metadata = metadata.replace("<version>", "").replace("</version>", "");
		return Arrays.stream(metadata.trim().split("\n")).map(String::trim).collect(Collectors.toList());
	}

	private ByteArrayOutputStream read(InputStream stream) throws Throwable {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (stream == null) return baos;
		try {
			byte[] byteChunk = new byte[4096];
			int n;
			while ((n = stream.read(byteChunk)) > 0)
				baos.write(byteChunk, 0, n);
		} finally {
			stream.close();
		}
		return baos;
	}
}
