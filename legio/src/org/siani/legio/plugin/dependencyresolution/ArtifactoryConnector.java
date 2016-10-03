package org.siani.legio.plugin.dependencyresolution;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import tara.intellij.settings.TaraSettings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static java.nio.channels.Channels.newChannel;
import static tara.dsl.ProteoConstants.*;
import static tara.intellij.lang.LanguageManager.LANGUAGE_EXTENSION;

public class ArtifactoryConnector {
	private static final Logger LOG = Logger.getInstance(ArtifactoryConnector.class.getName());

	private static final String SECURE_SOURCE = "https://artifactory.siani.es/artifactory/languages-release/";
	private static final String PUBLISH_API = "http://artifactory.siani.es/artifactory/api/storage/languages-release/";
	private TaraSettings settings;
	private final String languageRepository;
	private final List<String> releaseRepositories;
	private final List<String> snapshotRepositories;

	public ArtifactoryConnector(TaraSettings settings, List<String> releaseRepositories, List<String> snapshotRepositories, String languageRepository) {
		this.settings = settings;
		this.releaseRepositories = releaseRepositories;
		this.snapshotRepositories = snapshotRepositories;
		this.languageRepository = languageRepository;
	}

	public File get(File destiny, String name, String version) throws IOException {
		destiny.getParentFile().mkdirs();
		final FileOutputStream stream = new FileOutputStream(destiny);
		stream.getChannel().transferFrom(newChannel(new URL(dslName(name, version)).openStream()), 0, Long.MAX_VALUE);
		stream.close();
		return destiny;
	}

	public int put(File dsl, String name, String version) throws IOException {
		return put(new URL(getSecureUrl(dslName(name, version))), dsl);
	}

	private String dslName(String name, String version) {
		return name + "/" + version + "/" + name + "-" + version + LANGUAGE_EXTENSION;
	}

	public List<String> versions(String dsl) throws IOException {
		if (dsl.equals(PROTEO) || dsl.equals(VERSO)) return proteoVersions();
		URL url = new URL(getApiUrl(dsl + "/"));
		return extractUris(responseFrom(url));
	}

	private List<String> proteoVersions() throws IOException {
		List<String> versions = new ArrayList<>();
		for (String repo : releaseRepositories) {
			URL url = new URL(repo + "/" + PROTEO_GROUP_ID.replace(".", "/") + "/" + PROTEO_ARTIFACT_ID + "/maven-metadata.xml");
			final String mavenMetadata = new String(read(url.openStream()).toByteArray());
			versions.addAll(extractVersions(mavenMetadata));
		}
		for (String repo : snapshotRepositories) {
			URL url = new URL(repo + "/" + PROTEO_GROUP_ID.replace(".", "/") + "/" + PROTEO_ARTIFACT_ID + "/maven-metadata.xml");
			final String mavenMetadata = new String(read(url.openStream()).toByteArray());
			versions.addAll(extractVersions(mavenMetadata));
		}
		return versions;
	}

	private List<String> extractVersions(String metadata) {
		metadata = metadata.substring(metadata.indexOf("<versions>")).substring("<versions>".length() + 1);
		metadata = metadata.substring(0, metadata.indexOf("</versions>"));
		metadata = metadata.replace("<version>", "").replace("</version>", "");
		return Arrays.stream(metadata.trim().split("\n")).map(String::trim).collect(Collectors.toList());
	}

	private JsonObject responseFrom(URL url) throws IOException {
		String input = readResponse(new BufferedReader(new InputStreamReader(url.openStream())));
		return new Gson().fromJson(input, JsonObject.class);
	}

	public List<String> languages() throws IOException {
		URL url = new URL(getApiUrl(""));
		final JsonObject o = responseFrom(url);
		return extractUris(o);
	}

	private int put(URL url, File origin) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		String userpass = settings.userName() + ":" + settings.password();
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
		connection.setRequestProperty("Authorization", basicAuth);
		connection.setRequestMethod("PUT");
		connection.setRequestProperty("Content-Type", "multipart/form-data");
		connection.setRequestProperty("Content-Length", valueOf(origin.length()));
		try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
			wr.write(Files.readAllBytes(origin.toPath()));
		}
		connection.getOutputStream().flush();
		return connection.getResponseCode();
	}

	private List<String> extractUris(JsonObject o) {
		List<String> uris = new ArrayList<>();
		o.get("children").getAsJsonArray().forEach(c -> uris.add(c.getAsJsonObject().get("uri").getAsString().substring(1)));
		uris.remove("maven-metadata.xml");
		return uris;
	}

	private String readResponse(BufferedReader reader) {
		StringBuilder everything = new StringBuilder();
		try {
			String line;
			while ((line = reader.readLine()) != null) everything.append(line);
			reader.close();
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		return everything.toString();
	}

	@NotNull
	private String toPublish(String uri) {
		return uri.replace("artifactory/", "artifactory/api/storage/") + "-local/";
	}

	@NotNull
	private String getSecureUrl(String path) {
		return SECURE_SOURCE + path;
	}


	@NotNull
	private String getApiUrl(String path) {
		return PUBLISH_API + path;
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
