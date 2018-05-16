package io.intino.plugin.build.maven;

import com.intellij.openapi.module.Module;
import io.intino.legio.graph.Artifact;
import io.intino.plugin.project.LegioConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

import static java.io.File.separator;

public class SonarProfileRenderer {
	private static final String PROFILE = "sonarProfile";
	private static final String PROFILES = "profiles";
	private static final String PROPERTIES = "properties";
	private static final String ID = "id";
	private static final String SONAR = "sonar";
	private static final String SONAR_HOST_URL = "sonar.host.url";

	private Document doc;
	private final Artifact.QualityAnalytics analytics;

	public SonarProfileRenderer(Module module) {
		final LegioConfiguration configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		analytics = configuration.graph().artifact().qualityAnalytics();
		loadDoc();
	}

	public void execute() {
		Node profile = sonarProfile();
		if (profile == null) createProfile(analytics.url());
		else setServerURL(analytics.url());
		commit();
	}

	private Node sonarProfile() {
		NodeList nodeList = doc.getElementsByTagName(PROFILE);
		for (int i = 0; i < nodeList.getLength(); i++)
			if (get(nodeList.item(i).getChildNodes(), ID).getTextContent().equals(SONAR)) return nodeList.item(i);
		return null;
	}

	private void setServerURL(String url) {
		doc.getElementsByTagName(SONAR_HOST_URL).item(0).setTextContent(url);
	}

	private void createProfile(String url) {
		NodeList profiles = doc.getElementsByTagName(PROFILES);
		if (profiles.getLength() == 0) {
			Element profilesNode = doc.createElement(PROFILES);
			doc.appendChild(profilesNode);
			profiles = doc.getElementsByTagName(PROFILES);
		}
		Element profileNode = doc.createElement(PROFILE);
		Element id = doc.createElement(ID);
		id.setTextContent(SONAR);
		Element properties = doc.createElement(PROPERTIES);
		Element sonarURL = doc.createElement(SONAR_HOST_URL);
		sonarURL.setTextContent(url);
		properties.appendChild(sonarURL);
		id.setTextContent(SONAR);
		profileNode.appendChild(id);
		profileNode.appendChild(properties);
		profiles.item(0).appendChild(profileNode);
	}

	private void commit() {
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
			e.printStackTrace();
		}
	}

	private Node get(NodeList list, String name) {
		for (int i = 0; i < list.getLength(); i++)
			if (list.item(i).getNodeName().equals(name))
				return list.item(i);
		return list.item(0);
	}

	private static File settingsFile() {
		final File home = new File(System.getProperty("user.home"));
		return new File(home, ".m2" + separator + "settings.xml");
	}

	private void loadDoc() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(settingsFile().getPath());
		} catch (ParserConfigurationException | SAXException | IOException ignored) {
		}
	}
}
