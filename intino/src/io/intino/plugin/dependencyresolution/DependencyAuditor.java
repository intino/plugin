package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.module.Module;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.tara.io.Node;
import io.intino.tara.io.Stash;
import io.intino.tara.io.Variable;
import io.intino.tara.magritte.Predicate;
import io.intino.tara.magritte.stores.FileSystemStore;
import io.intino.tara.magritte.utils.StoreAuditor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.intino.tara.io.Helper.newNode;
import static io.intino.tara.io.Helper.newStash;

public class DependencyAuditor {

	private static final String STASH_NAME = "model#";
	private transient final Module module;
	private StoreAuditor storeAuditor;
	private Stash stash;

	public DependencyAuditor(Module module) {
		this.module = module;
	}

	boolean isModified(io.intino.tara.magritte.Node node) {
		return storeAuditor.isCreated(node);
	}

	public void reload(Stash stash) {
		this.stash = stash;
		customize(stash);
		this.storeAuditor = new StoreAuditor(new FileSystemStore(IntinoDirectory.auditDirectory(module.getProject())) {
			@Override
			public Stash stashFrom(String path) {
				Node importsNode = importsNode(artifactNode(stash));
				List<Node> nodes = importsNode == null ? new ArrayList<>() : new ArrayList<>(importsNode.nodes);
				if (node(stash, "Model") != null) nodes.add(node(stash, "Model", "Level"));
				if (node(stash, "Box") != null) nodes.add(node(stash, "Box"));
				return newStash(stash.language, nodes);
			}
		}, module.getName());
		this.storeAuditor.trace("");
		this.storeAuditor.changeList();
		this.storeAuditor.commit();
	}

	public void reload() {
		reload(this.stash);
	}

	public void invalidate() {
		auditionFile().delete();
	}

	public void invalidate(String nodeName) {
		nodeName = nodeName.replace(STASH_NAME, "");
		if (storeAuditor != null) {
			storeAuditor.removeTrack(STASH_NAME + nodeName);
			storeAuditor.commit();
		}
	}

	private void customize(Stash stash) {
		customizeImports(stash);
		customizeModel(stash);
		customizeBox(stash);
	}

	private void customizeImports(Stash stash) {
		Node artifactNode = artifactNode(stash);
		Node importsNode = importsNode(artifactNode);
		Node dataHubNode = dataHubNode(artifactNode);
		if (dataHubNode != null) updateDatahubDependency(artifactNode, importsNode, dataHubNode);
		if (importsNode == null) return;
		importsNode.name = importsNode.name.replace(Predicate.nameOf(importsNode.name), "") + "imports";
		importsNode.nodes.forEach(n ->
				n.name = STASH_NAME + valueOf("groupId", n.variables) + ":" + valueOf("artifactId", n.variables) + ":" + valueOf("version", n.variables) + ":" + scopeOf(n));
	}

	private void updateDatahubDependency(Node artifactNode, Node importsNode, Node dataHubNode) {
		if (importsNode == null) {
			importsNode = newNode("imports", Collections.singletonList("Artifact$Imports"), Collections.emptyList(), Collections.emptyList());
			artifactNode.nodes.add(importsNode);
		}
		importsNode.nodes.add(newNode(STASH_NAME + "datahub", Arrays.asList("Artifact$Imports$Compile", "Artifact$Imports$Dependency"), dataHubNode.variables, Collections.emptyList()));
	}

	private void customizeModel(Stash stash) {
		Node model = node(stash, "Model", "Level");
		if (model == null) return;
		model.name = STASH_NAME + valueOf("language", model.variables) + ":" + valueOf("version", model.variables) + ":" + valueOf("sdk", model.variables);
	}

	private void customizeBox(Stash stash) {
		Node box = node(stash, "Box");
		if (box == null) return;
		box.name = STASH_NAME + valueOf("language", box.variables) + ":" + valueOf("version", box.variables);
	}

	private Node node(Stash stash, String layer) {
		return artifactNode(stash).nodes.stream().filter(n -> n.layers.get(0).equals("Artifact$" + layer)).findFirst().orElse(null);
	}

	private Node node(Stash stash, String name, String facet) {
		return artifactNode(stash).nodes.stream().filter(n -> n.layers.get(0).equals(facet + "#Artifact$" + name)).findFirst().orElse(null);
	}

	private Node artifactNode(Stash stash) {
		return stash.nodes.get(0);
	}

	private Node dataHubNode(Node node) {
		return node.nodes.stream().filter(n -> n.layers.contains("Artifact$DataHub")).findFirst().orElse(null);
	}

	private String scopeOf(Node node) {
		return node.layers.get(0).replace("Artifact$Imports$", "").toUpperCase();
	}

	private Node importsNode(Node node) {
		return node.nodes.stream().filter(n -> n.layers.contains("Artifact$Imports")).findFirst().orElse(null);
	}

	private String valueOf(String varName, List<Variable> variables) {
		return (String) variables.stream().filter(v -> v.name.equals(varName)).findFirst().get().values.get(0);
	}

	@NotNull
	private File auditionFile() {
		return new File(IntinoDirectory.auditDirectory(module.getProject()), module.getName());
	}
}
