package io.intino.plugin.settings;

import com.intellij.openapi.diagnostic.Logger;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.io.File.separator;

class ArtifactoryCredentialsManager {
	private static final Logger logger = Logger.getInstance(ArtifactoryCredentialsManager.class);

	private static final String USERNAME = "username";
	private static final String ID = "id";
	private static final String SETTINGS = "settings";
	private static final String PASSWORD = "password";
	private static final String SERVER = "server";
	private static final String SERVERS = "servers";
	private Document doc = null;

	ArtifactoryCredentialsManager() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(settingsFile().getPath());
		} catch (ParserConfigurationException | SAXException | IOException ignored) {
		}
	}

	private static void commit(Document doc) {
		try {
			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(settingsFile());
			transformer.transform(source, result);
		} catch (TransformerException e) {
			logger.error(e);
		}
	}

	private static void createSettingsFile(List<ArtifactoryCredential> credentials) {
		FrameBuilder builder = new FrameBuilder("artifactory");
		builder.add("server", credentials.stream().
				map(credential -> new FrameBuilder("server").add("name", credential.serverId).add(USERNAME, credential.username).add(PASSWORD, credential.password).toFrame()).toArray(Frame[]::new));
		write(new ArtifactorySettingsTemplate().render(builder));
	}

	private static void write(String settings) {
		try {
			Files.write(settingsFile().toPath(), settings.getBytes());
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private static File settingsFile() {
		final File home = new File(System.getProperty("user.home"));
		return new File(home, ".m2" + separator + "settings.xml");
	}

	List<io.intino.plugin.settings.ArtifactoryCredential> loadCredentials() {
		List<io.intino.plugin.settings.ArtifactoryCredential> artifactories = new ArrayList<>();
		final List<Node> servers = servers();
		for (Node server : servers) {
			NodeList list = server.getChildNodes();
			artifactories.add(new io.intino.plugin.settings.ArtifactoryCredential(get(list, ID).getTextContent(), get(list, USERNAME).getTextContent(), get(list, PASSWORD).getTextContent()));
		}
		return artifactories;
	}

	void saveCredentials(List<io.intino.plugin.settings.ArtifactoryCredential> credentials) {
		if (settingsFile().exists()) setCredentials(credentials);
		else createSettingsFile(credentials);
	}

	private Node get(NodeList list, String name) {
		for (int i = 0; i < list.getLength(); i++)
			if (list.item(i).getNodeName().equals(name))
				return list.item(i);
		return list.item(0);
	}

	private List<Node> servers() {
		return doc != null ? asList(doc.getElementsByTagName(SERVER)) : Collections.emptyList();
	}

	private void setCredentials(List<io.intino.plugin.settings.ArtifactoryCredential> credentials) {
		removeServers();
		for (io.intino.plugin.settings.ArtifactoryCredential credential : credentials)
			addCredentials(createServer(credential.serverId), credential.username, credential.password);
		for (ArtifactoryCredential credential : credentials)
			addCredentials(createServer(credential.serverId + "-snapshot"), credential.username, credential.password);
		commit(doc);
	}

	private void removeServers() {
		final NodeList elementsByTagName = doc.getElementsByTagName(SERVERS);
		if (elementsByTagName.getLength() == 0) return;
		elementsByTagName.item(0).getParentNode().removeChild(elementsByTagName.item(0));
	}

	private void addCredentials(Node server, String user, String password) {
		get(server.getChildNodes(), USERNAME).setTextContent(user);
		get(server.getChildNodes(), PASSWORD).setTextContent(password);
	}

	private Node createServer(String name) {
		final NodeList servers = doc.getElementsByTagName(SERVERS);
		Node serversNode = (servers.getLength() > 0) ? servers.item(0) : createServers();
		Element serverNode = doc.createElement(SERVER);
		Element serverId = doc.createElement(ID);
		serverId.setTextContent(name);
		Element userNode = doc.createElement(USERNAME);
		Element passwordNode = doc.createElement(PASSWORD);
		Element configurationNode = createConfiguration();
		serverNode.appendChild(serverId);
		serverNode.appendChild(userNode);
		serverNode.appendChild(passwordNode);
		serverNode.appendChild(doc.importNode(configurationNode, true));
		return serversNode.appendChild(serverNode);
	}

	private Element createConfiguration() {
		try {
			return DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.parse(new ByteArrayInputStream(configurationText().getBytes()))
					.getDocumentElement();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			logger.error(e);
		}
		return null;
	}

	private String configurationText() {
		return "\t<configuration>\n" +
				"\t\t<timeout>5000</timeout>\n" +
				"\t\t<httpConfiguration>\n" +
				"\t\t\t<all>\n" +
				"\t\t\t\t<connectionTimeout>5000</connectionTimeout>\n" +
				"\t\t\t\t<readTimeout>5000</readTimeout>\n" +
				"\t\t\t</all>\n" +
				"\t\t</httpConfiguration>\n" +
				"\t</configuration>";
	}

	private Node createServers() {
		return doc.getElementsByTagName(SETTINGS).item(0).appendChild(doc.createElement(SERVERS));
	}

	private List<Node> asList(NodeList elements) {
		List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < elements.getLength(); i++) nodes.add(elements.item(i));
		return nodes;
	}
}
