package org.siani.legio.plugin.dependencyresolution;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import static tara.dsl.ProteoConstants.*;
import static tara.intellij.lang.LanguageManager.LANGUAGE_EXTENSION;

public class ArtifactoryConnector {
	private static final Logger LOG = Logger.getInstance(ArtifactoryConnector.class.getName());

	private static final String SECURE_SOURCE = "https://artifactory.siani.es/artifactory/languages-release/";
	private static final String PUBLISH_API = "http://artifactory.siani.es/artifactory/api/storage/languages-release/";
	private static final String LIB_RELEASE_LOCAL = "http://artifactory.siani.es/artifactory/api/storage/libs-release-local/";
	private TaraSettings settings;
	private final List<String> release;
	private final List<String> snapshot;
	private final List<String> language;

	public ArtifactoryConnector(TaraSettings settings, List<String> release, List<String> snapshot, List<String> language) {
		this.settings = settings;
		this.release = release;
		this.snapshot = snapshot;
		this.language = language;
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
		if (dsl.equals(PROTEO) || dsl.equals(VERSO)) return proteoVersions();
		URL url = new URL(getApiUrl(dsl + "/"));
		return extractUris(responseFrom(url));
	}

	private List<String> proteoVersions() throws IOException {
		List<String> versions = new ArrayList<>();
		for (String s : release) {
			URL url = new URL(LIB_RELEASE_LOCAL + PROTEO_GROUP_ID.replace(".", "/") + "/" + ProteoConstants.PROTEO_ARTIFACT_ID);
			versions.addAll(extractVersions(responseFrom(url)));
		}
		if (!snapshot.isEmpty()) {
			URL url = new URL(snapshot.get(0) + PROTEO_GROUP_ID.replace(".", "/") + "/" + ProteoConstants.PROTEO_ARTIFACT_ID);
			versions.addAll(extractVersions(responseFrom(url)));
		}
		return versions;
	}

	private List<? extends String> extractVersions(JsonObject versions) {
		List<String> list = new ArrayList<>();
		JsonArray array = versions.get("result").getAsJsonArray();
		for (JsonElement element : array) {
			list.add(element.toString());
		}
		return list;
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
	private String getUrl(String path) {
		return SOURCE + path;
	}

	@NotNull
	private String getSecureUrl(String path) {
		return SECURE_SOURCE + path;
	}

	@NotNull
	private String getApiUrl(String path) {
		return PUBLISH_API + path;
	}

}
