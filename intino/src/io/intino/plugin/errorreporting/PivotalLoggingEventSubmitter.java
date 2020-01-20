package io.intino.plugin.errorreporting;

import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class PivotalLoggingEventSubmitter {

	private Logger logger = Logger.getInstance(PivotalLoggingEventSubmitter.class);

	private static final String TRACKER = "www.pivotaltracker.com/services/v5/projects/";
	private static final String TRACKER_URL = "https://" + TRACKER + "/";
	private static final String COMMENTS = "/comments";
	private static final Logger LOG = Logger.getInstance(PivotalLoggingEventSubmitter.class.getName());
	private static final String PLUGIN_ID = "plugin.id";
	private static final String USER = "user";
	private static final String PLUGIN_VERSION = "plugin.version";
	private static final String PLUGIN_NAME = "plugin.name";
	private static final String REPORT_ADDITIONAL_INFO = "report.additionalInfo";
	private static final String REPORT_DESCRIPTION = "report.description";
	private static final String REPORT_TITLE = "report.title";
	private static final String REPORT_TYPE = "report.type";
	private Properties properties;
	private final String token;
	private final String url;

	public PivotalLoggingEventSubmitter(Properties properties, String project, String token) {
		this.properties = properties;
		this.token = token;
		url = TRACKER_URL + project + "/stories";
	}

	public void submit() {
		try {
			PivotalStory story = new PivotalStory();
			String response = createStory(story);
			addInfo(story, new JsonParser().parse(response));
			addCommentary(story);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private String createStory(PivotalStory story) throws IOException {
		HttpURLConnection connection = createConnection("POST", url);
		sendStory(connection, story);
		checkResponse(connection);
		return getResponse(connection);
	}

	private void addCommentary(PivotalStory story) throws IOException {
		HttpURLConnection connection = createConnection("POST", story.url + COMMENTS);
		addComments(connection, story);
		checkResponse(connection);
	}

	private void addComments(HttpURLConnection connection, PivotalStory story) throws IOException {
		if (story.comment == null) return;
		final OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), Charset.defaultCharset());
		osw.write("{\"text\":\"" + story.comment + "\"}");
		osw.close();
	}

	private void addInfo(PivotalStory story, JsonElement element) {
		JsonObject jobject = element.getAsJsonObject();
		story.id = jobject.get("id").getAsInt();
		story.url = url + "/" + story.id;
	}

	private String getResponse(HttpURLConnection connection) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.defaultCharset()));
		StringBuilder builder = new StringBuilder();
		String output;
		while ((output = reader.readLine()) != null)
			builder.append(output);
		reader.close();
		return builder.toString();
	}

	private void checkResponse(HttpURLConnection connection) throws IOException {
		int responseCode = connection.getResponseCode();
		if (responseCode != 200)
			logger.warn("Tracker server answered: " + responseCode + ". " + connection.getResponseMessage() + "\n");
	}

	private void sendStory(HttpURLConnection connection, PivotalStory pivotalStory) throws IOException {
		final OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), Charset.defaultCharset());
		osw.write(pivotalStory.asJson().toString());
		osw.close();
	}

	private HttpURLConnection createConnection(String method, String url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("X-TrackerToken", token);
		connection.setRequestMethod(method);
		connection.connect();
		return connection;
	}

	static class SubmitException extends Exception {
		public SubmitException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private class PivotalStory {
		int id;
		String name = buildName();
		String description = buildDescription(PivotalLoggingEventSubmitter.this.properties.get(REPORT_DESCRIPTION).toString());
		String storyType = getReportType();
		List<String> labels = Collections.singletonList(PivotalLoggingEventSubmitter.this.properties.get(PLUGIN_NAME).toString());
		String currentState = "unstarted";
		String url;
		String comment = (String) properties.get(REPORT_ADDITIONAL_INFO);

		private String buildName() {
			Object title = properties.get(REPORT_TITLE);
			return "Error " + ErrorNameFactory.next() + " in plugin v." + properties.get(PLUGIN_VERSION).toString().trim() + (title != null ? ": " + title.toString() : "");
		}

		private String buildDescription(String description) {
			StringBuilder builder = new StringBuilder();
			builder.append(PLUGIN_ID).append(": ").append(properties.get(PLUGIN_ID)).append("\n");
			builder.append(USER).append(": ").append(System.getProperty("user.name")).append("\n");
			builder.append(PLUGIN_NAME).append(": ").append(properties.get(PLUGIN_NAME)).append("\n");
			builder.append(PLUGIN_VERSION).append(": ").append(properties.get(PLUGIN_VERSION).toString().trim()).append("\n");
			return builder.append("````\n").append(description).append("````\n").toString();
		}

		String getReportType() {
			Object reportType = properties.get(REPORT_TYPE);
			return reportType != null ? reportType.toString().replace("apunt", "feature") : "bug";
		}

		JsonElement asJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("name", new JsonPrimitive(name));
			jsonObject.add("current_state", new JsonPrimitive(currentState));
			jsonObject.add("story_type", new JsonPrimitive(storyType));
			jsonObject.add("labels", new Gson().toJsonTree(labels));
			jsonObject.add("description", new JsonPrimitive(description));
			return jsonObject;
		}
	}

}
