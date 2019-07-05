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
import java.util.List;

import static io.intino.tara.io.Helper.newStash;

public class DependencyAuditor {

	public static final String STASH_NAME = "model#";
	private transient final Module module;
	private StoreAuditor storeAuditor;

	public DependencyAuditor(Module module) {
		this.module = module;
	}

	boolean isModified(io.intino.tara.magritte.Node node) {
		return storeAuditor.isCreated(node);
	}

	public void stash(Stash stash) {
		customize(stash);
		this.storeAuditor = new StoreAuditor(new FileSystemStore(IntinoDirectory.of(module.getProject())) {
			@Override
			public Stash stashFrom(String path) {
				List<Node> nodes = importsNode(artifactNode(stash)).nodes;
				if (node(stash, "Model") != null) nodes.add(node(stash, "Model", "Level"));
				if (node(stash, "Box") != null) nodes.add(node(stash, "Box"));
				return newStash(stash.language, nodes);
			}
		}, module.getName());
		this.storeAuditor.trace("");
		this.storeAuditor.changeList();
		this.storeAuditor.commit();
	}


	private void customize(Stash stash) {
		customizeImports(stash);
		customizeModel(stash);
		customizeBox(stash);
	}

	private void customizeImports(Stash stash) {
		Node node = importsNode(artifactNode(stash));
		if (node == null) return;
		node.name = node.name.replace(Predicate.nameOf(node.name), "") + "imports";
		node.nodes.forEach(n ->
				n.name = STASH_NAME + valueOf("groupId", n.variables) + ":" + valueOf("artifactId", n.variables) + ":" + valueOf("version", n.variables) + ":" + scopeOf(n));
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

	private Node node(Stash stash, String facet) {
		return artifactNode(stash).nodes.stream().filter(n -> n.facets.get(0).equals("Artifact$" + facet)).findFirst().orElse(null);
	}

	private Node node(Stash stash, String name, String facet) {
		return artifactNode(stash).nodes.stream().filter(n -> n.facets.get(0).equals(facet + "#Artifact$" + name)).findFirst().orElse(null);
	}

	private Node artifactNode(Stash stash) {
		return stash.nodes.get(0);
	}

	private String scopeOf(Node node) {
		return node.facets.get(0).replace("Artifact$Imports$", "").toUpperCase();
	}

	private Node importsNode(Node node) {
		return node.nodes.stream().filter(n -> n.facets.contains("Artifact$Imports")).findFirst().orElse(null);
	}

	private String valueOf(String varName, List<Variable> variables) {
		return (String) variables.stream().filter(v -> v.name.equals(varName)).findFirst().get().values.get(0);
	}


	public void invalidate() {
		auditionFile().delete();
	}

	public void invalidate(String nodeName) {
		if (storeAuditor != null) {
			storeAuditor.removeTrack(STASH_NAME + nodeName);
			storeAuditor.commit();
		}
	}

	@NotNull
	private File auditionFile() {
		return new File(IntinoDirectory.of(module.getProject()), module.getName());
	}
}
