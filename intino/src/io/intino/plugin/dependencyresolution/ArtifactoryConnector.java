package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.intino.Configuration;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ArtifactoryConnector {
	public static final String MAVEN_URL = "https://repo1.maven.org/maven2/";
	private static final Logger LOG = Logger.getInstance(ArtifactoryConnector.class.getName());
	private final List<Configuration.Repository> repositories;
	private final List<ArtifactoryCredential> credentials;

	public ArtifactoryConnector(Configuration.Repository repository) {
		this(List.of(repository));
	}
	public ArtifactoryConnector(List<Configuration.Repository> repositories) {
		this.repositories = new ArrayList<>(repositories);
		this.repositories.add(mavenRepository());
		credentials = new ArrayList<>();
	}

	public ArtifactoryConnector(Project project, List<Configuration.Repository> repositories) {
		this.repositories = new ArrayList<>(repositories);
		this.repositories.add(mavenRepository());
		if (project != null) credentials = IntinoSettings.getInstance(project).artifactories();
		else credentials = new ArrayList<>();
	}

	@NotNull
	private Configuration.Repository.Release mavenRepository() {
		return repository("maven2","https://repo.maven.apache.org/maven2/");
	}

	@NotNull
	public static Configuration.Repository.Release repository(String identifier, String url) {
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
				return identifier;
			}

			@Override
			public String url() {
				return url;
			}

			@Override
			public String user() {
				return null;
			}

			@Override
			public String password() {
				return null;
			}

			@Override
			public UpdatePolicy updatePolicy() {
				return UpdatePolicy.Daily;
			}
		};
	}

	public List<String> dsls() {
		List<String> dsls = new ArrayList<>();
		try {
			for (Configuration.Repository repo : repositories) {
				URL url = new URL(repo.url() + "/" + "tara/dsl" + "/");
				final String result = new String(read(connect(url)));
				if (result.isEmpty()) continue;
				dsls.addAll(extractLanguages(result));
			}
			return dsls;
		} catch (Throwable ignored) {
			return Collections.emptyList();
		}
	}

	public List<String> dslVersions(String dsl) {
		try {
			for (Configuration.Repository repo : repositories) {
				URL url = new URL(repo.url() + "/" + "tara/dsl" + "/" + dsl + "/maven-metadata.xml");
				final String mavenMetadata = read(connect(url));
				if (!mavenMetadata.isEmpty()) return extractVersions(mavenMetadata);
			}
		} catch (Throwable e) {
			LOG.info(e);
		}
		return Collections.emptyList();
	}

	public List<String> versions(String artifact) {
		try {
			for (Configuration.Repository repo : repositories) {
				String spec = repo.url() + (repo.url().endsWith("/") ? "" : "/") + artifact.replace(":", "/").replace(".", "/") + "/maven-metadata.xml";
				URL url = new URL(spec);
				final String mavenMetadata = read(connect(repo.identifier(), url));
				if (!mavenMetadata.isEmpty()) return extractVersions(mavenMetadata);
			}
		} catch (Throwable ignored) {
		}
		return Collections.emptyList();
	}

	private InputStream connect(String mavenId, URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			ArtifactoryCredential credential = credentials.stream().filter(r -> r.serverId.equalsIgnoreCase(mavenId)).findFirst().orElse(null);
			if (credential != null) {
				String auth = credential.username + ":" + credential.password;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
				String authHeaderValue = "Basic " + new String(encodedAuth);
				connection.setRequestProperty("Authorization", authHeaderValue);
				return connection.getInputStream();
			}
			return connect(url);
		} catch (Throwable e) {
			return null;
		}
	}

	private InputStream connect(URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			return connection.getInputStream();
		} catch (Throwable e) {
			return InputStream.nullInputStream();
		}
	}

	private List<String> extractLanguages(String result) {
		if (result == null || !result.contains("<pre><a")) return Collections.emptyList();
		result = result.substring(result.indexOf("<pre><a"), result.lastIndexOf("</pre"));
		final List<String> languages = new ArrayList<>(Arrays.asList(result.split("\n")));
		languages.remove(0);
		return languages.stream().map(l -> l.substring(l.indexOf("\">") + 2, l.indexOf("/<"))).toList();
	}

	private List<String> extractVersions(String metadata) {
		if (!metadata.contains("<versions>")) return Collections.emptyList();
		metadata = metadata.substring(metadata.indexOf("<versions>")).substring("<versions>".length() + 1);
		metadata = metadata.substring(0, metadata.indexOf("</versions>"));
		metadata = metadata.replace("<version>", "").replace("</version>", "");
		return Arrays.stream(metadata.trim().split("\n")).map(String::trim).toList();
	}

	private String read(InputStream stream) throws Throwable {
		if (stream == null) return "";
		byte[] bytes = stream.readAllBytes();
		stream.close();
		return new String(bytes);

	}
}
