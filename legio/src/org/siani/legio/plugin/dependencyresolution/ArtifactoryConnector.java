package org.siani.legio.plugin.dependencyresolution;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import tara.dsl.ProteoConstants;
import tara.intellij.settings.TaraSettings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.lang.String.valueOf;
import static java.nio.channels.Channels.newChannel;
import static tara.dsl.ProteoConstants.PROTEO;
import static tara.dsl.ProteoConstants.PROTEO_GROUP_ID;
import static tara.intellij.lang.LanguageManager.LANGUAGE_EXTENSION;

public class ArtifactoryConnector {
	private static final Logger LOG = Logger.getInstance(ArtifactoryConnector.class.getName());

	private static final String SECURE_SOURCE = "https://artifactory.siani.es/artifactory/languages-release/";
	private static final String SOURCE = "http://artifactory.siani.es/artifactory/languages-release/";
	private static final String SOURCE_API = "http://artifactory.siani.es/artifactory/api/storage/languages-release/";
	private static final String LIBS_SOURCE_API = "http://artifactory.siani.es/artifactory/api/storage/libs-release-local/";
	private TaraSettings settings;
	private String snapshotRepository;

	public ArtifactoryConnector(TaraSettings settings, String snapshotRepository) {
		this.settings = settings;
		this.snapshotRepository = snapshotRepository != null ? snapshotRepository.replace("artifactory/", "artifactory/api/storage/") + "-local/" : null;
	}

	public ArtifactoryConnector() {
	}

	public File get(File destiny, String name, String version) throws IOException {
		destiny.getParentFile().mkdirs();
		final FileOutputStream stream = new FileOutputStream(destiny);
		stream.getChannel().transferFrom(newChannel(new URL(getUrl(dslName(name, version))).openStream()), 0, Long.MAX_VALUE);
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
		if (dsl.equals(PROTEO)) return proteoVersions();
		URL url = new URL(getApiUrl(dsl + "/"));
		final JsonObject o = responseFrom(url);
		return extractUris(o);
	}

	private List<String> proteoVersions() throws IOException {
		List<String> versions = new ArrayList<>();
		if (snapshotRepository != null) {
			URL url = new URL(snapshotRepository + PROTEO_GROUP_ID.replace(".", "/") + "/" + ProteoConstants.PROTEO_ARTIFACT_ID);
			final JsonObject o = responseFrom(url);
			versions.addAll(extractUris(o));
		}
		URL url = new URL(LIBS_SOURCE_API + PROTEO_GROUP_ID.replace(".", "/") + "/" + ProteoConstants.PROTEO_ARTIFACT_ID);
		final JsonObject o = responseFrom(url);
		versions.addAll(extractUris(o));
		return versions;
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
	private String getUrl(String path) {
		return SOURCE + path;
	}

	@NotNull
	private String getSecureUrl(String path) {
		return SECURE_SOURCE + path;
	}

	@NotNull
	private String getApiUrl(String path) {
		return SOURCE_API + path;
	}

}
