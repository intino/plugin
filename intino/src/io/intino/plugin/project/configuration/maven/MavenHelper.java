package io.intino.plugin.project.configuration.maven;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.dsl.Proteo;
import io.intino.tara.dsl.ProteoConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

public class MavenHelper implements MavenTags {

	private final Module module;
	private final MavenProject mavenProject;
	private String path;
	private Document doc;

	public MavenHelper(Module module) {
		this.module = module;
		this.mavenProject = mavenProject(module);
		try {
			path = mavenProject.getPath();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(path);
		} catch (Exception ignored) {
		}
	}

	private static MavenProject mavenProject(Module module) {
		return module == null ? null : MavenProjectsManager.getInstance(module.getProject()).findProject(module);
	}

	public String snapshotRepository() {
		if (doc == null) return null;
		NodeList nodes = doc.getElementsByTagName(REPOSITORY);
		for (int i = 0; i < nodes.getLength(); i++)
			if (isSnapshotRepository(nodes.item(i))) return snapshotURL(nodes.item(i));
		return null;
	}

	private String snapshotURL(Node item) {
		for (int i = 0; i < item.getChildNodes().getLength(); i++) {
			Node child = item.getChildNodes().item(i);
			if (child.getNodeName().equals(URL)) return child.getTextContent();
		}
		return null;
	}

	private boolean isSnapshotRepository(Node item) {
		for (int i = 0; i < item.getChildNodes().getLength(); i++) {
			Node child = item.getChildNodes().item(i);
			if (child.getNodeName().equals(ID)) return child.getTextContent().toLowerCase().contains("snapshot");
		}
		return false;
	}

	boolean hasProteoDependency() {
		NodeList dependencies = doc.getElementsByTagName(DEPENDENCY);
		for (int i = 0; i < dependencies.getLength(); i++)
			if (isMagritteDependency(dependencies.item(i))) return true;
		return false;
	}

	void addProteo() {
		if (doc == null) return;
		Node dependencies = doc.getElementsByTagName(DEPENDENCIES).item(0);
		dependencies.appendChild(createMagritteDependency());
		commit();
	}

	public void version(String version) {
		final Node versionNode = doc.getElementsByTagName(VERSION).item(0);
		versionNode.setTextContent(version);
		commit();
	}

	public void dslVersion(SimpleEntry dsl, String version) {
		final Node dslDependency = findDslDependency(dsl);
		if (dslDependency == null) return;
		for (int i = 0; i < dslDependency.getChildNodes().getLength(); i++) {
			Node child = dslDependency.getChildNodes().item(i);
			if (VERSION.equalsIgnoreCase(child.getNodeName())) child.setTextContent(version);
		}
		commit();
	}


	private Node findDslDependency(SimpleEntry dsl) {
		NodeList dependencies = doc.getElementsByTagName(DEPENDENCY);
		for (int i = 0; i < dependencies.getLength(); i++) {
			final String[] artifactInfo = getArtifactInfo(dependencies.item(i).getChildNodes());
			if (artifactInfo[0].equals(dsl.getKey()) && artifactInfo[1].equals(dsl.getValue()))
				return dependencies.item(i);
		}
		return null;
	}

	public void add(Module newParent) {
		Node dependencies = doc.getElementsByTagName(DEPENDENCIES).item(0);
		Node moduleDependency = createModuleDependency(newParent);
		if (moduleDependency != null) {
			dependencies.appendChild(moduleDependency);
			commit();
		}
	}

	private Node createModuleDependency(Module newParent) {
		Element dependency = doc.createElement(DEPENDENCY);
		MavenProject project = MavenProjectsManager.getInstance(newParent.getProject()).findProject(newParent);
		if (project == null) return null;
		dependency.appendChild(groupId(doc, project.getMavenId().getGroupId()));
		dependency.appendChild(artifactId(doc, project.getMavenId().getArtifactId()));
		dependency.appendChild(type(doc, "bundle"));
		dependency.appendChild(scope(doc));
		dependency.appendChild(version(doc, project.getMavenId().getVersion()));
		return dependency;
	}

	public void remove(Module parent) {
		MavenProjectsManager manager = MavenProjectsManager.getInstance(parent.getProject());
		MavenProject mavenParent = manager.findProject(parent);
		List<MavenArtifact> dependencies;
		if (mavenParent == null || (dependencies = mavenProject.findDependencies(mavenParent.getMavenId())).isEmpty())
			return;
		Node xmlDependencies = doc.getElementsByTagName(DEPENDENCIES).item(0);
		Node xmlParentDependency = findXmlParentDependency(dependencies);
		if (xmlParentDependency != null) xmlDependencies.removeChild(xmlParentDependency);
		commit();
	}

	private boolean isMagritteDependency(Node item) {
		NodeList childNodes = item.getChildNodes();
		String[] artifactInfo = getArtifactInfo(childNodes);
		return Proteo.GROUP_ID.equals(artifactInfo[0]) && Proteo.ARTIFACT_ID.equals(artifactInfo[1]);
	}

	private void commit() {
		try {
			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);
			MavenProjectsManager.getInstance(module.getProject()).forceUpdateAllProjectsOrFindAllAvailablePomFiles();
		} catch (TransformerException ignored) {
		}


	}

	private Node createMagritteDependency() {
		Element dependency = doc.createElement(DEPENDENCY);
		dependency.appendChild(groupId(doc, Proteo.GROUP_ID));
		dependency.appendChild(artifactId(doc, Proteo.ARTIFACT_ID));
		dependency.appendChild(version(doc, PROTEO_VERSION));
		dependency.appendChild(type(doc, PROTEO_TYPE));
		return dependency;
	}

	@NotNull
	private Element groupId(Document doc, String groupId) {
		Element element = doc.createElement(GROUP_ID);
		element.setTextContent(groupId);
		return element;
	}

	@NotNull
	private Element artifactId(Document doc, String artifactId) {
		Element element = doc.createElement(ARTIFACT_ID);
		element.setTextContent(artifactId);
		return element;
	}

	@NotNull
	private Element version(Document doc, String version) {
		Element element = doc.createElement(VERSION);
		element.setTextContent(version);
		return element;
	}

	@NotNull
	private Element type(Document doc, String type) {
		Element element = doc.createElement("type");
		element.setTextContent(type);
		return element;
	}

	@NotNull
	private Element scope(Document doc) {
		Element element = doc.createElement("scope");
		element.setTextContent("");
		return element;
	}

	private Node findXmlParentDependency(List<MavenArtifact> dependencies) {
		NodeList dependency = doc.getElementsByTagName(DEPENDENCY);
		for (int i = 0; i < dependency.getLength(); i++)
			for (MavenArtifact mavenArtifact : dependencies)
				if (isParentModuleDependency(dependency.item(i), mavenArtifact))
					return dependency.item(i);
		return null;
	}

	private boolean isParentModuleDependency(Node item, MavenArtifact mavenArtifact) {
		NodeList childNodes = item.getChildNodes();
		String[] artifactInfo = getArtifactInfo(childNodes);
		return artifactInfo[0].equals(mavenArtifact.getGroupId())
				&& artifactInfo[1].equals(mavenArtifact.getArtifactId())
				&& artifactInfo[2].equals(mavenArtifact.getVersion());
	}

	private String[] getArtifactInfo(NodeList childNodes) {
		String[] artifact = new String[3];
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (GROUP_ID.equalsIgnoreCase(child.getNodeName())) artifact[0] = child.getTextContent();
			else if (ARTIFACT_ID.equalsIgnoreCase(child.getNodeName())) artifact[1] = child.getTextContent();
			else if (VERSION.equalsIgnoreCase(child.getNodeName())) artifact[2] = child.getTextContent();
		}
		return artifact;
	}

	private String dsl() {
		return doc.getElementsByTagName(DSL).item(0) == null ? "" : doc.getElementsByTagName(DSL).item(0).getTextContent();
	}


	public SimpleEntry dslMavenId(Module module, String dsl) {
		if (ProteoConstants.PROTEO.equals(dsl)) return proteoId();
		else if (dsl != null && dsl.equals(dsl()) && importedDSL())
			return fromImportedInfo(dsl);
		else return mavenId(parentModule(module, dsl));
	}

	private boolean importedDSL() {
		return true;//TODO
	}

	private SimpleEntry proteoId() {
		return new SimpleEntry(Proteo.GROUP_ID, Proteo.ARTIFACT_ID);
	}

	private SimpleEntry mavenId(Module module) {
		if (module == null) return new SimpleEntry("", "");
		final MavenProject project = MavenProjectsManager.getInstance(module.getProject()).findProject(module);
		if (project == null) return new SimpleEntry("", "");
		return new SimpleEntry(project.getMavenId().getGroupId(), project.getMavenId().getArtifactId());
	}

	private Module parentModule(Module module, String dsl) {
		for (Module aModule : ModuleManager.getInstance(module.getProject()).getModules()) {
			final Configuration conf = TaraUtil.configurationOf(aModule);
			if (conf != null && (dsl.equals(conf.artifact().model().outLanguage())))
				return aModule;
		}
		return null;
	}

	@NotNull
	private SimpleEntry fromImportedInfo(String dsl) {
		final Map<String, Object> info = LanguageManager.getImportedLanguageInfo(dsl);
		if (info.isEmpty()) return new SimpleEntry("", "");
		return new SimpleEntry(info.get("groupId"), info.get("artifactId"));
	}

}
