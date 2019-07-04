package io.intino.plugin.dependencyresolution;

import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import gherkin.deps.com.google.gson.Gson;
import io.intino.plugin.dependencyresolution.DependencyCatalog.Dependency;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.tara.io.Node;
import io.intino.tara.io.Stash;
import io.intino.tara.io.Variable;
import io.intino.tara.magritte.Predicate;
import io.intino.tara.magritte.stores.FileSystemStore;
import io.intino.tara.magritte.utils.StoreAuditor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.tara.io.Helper.newStash;

public class DependencyAuditor extends HashMap<String, List<Dependency>> {

	private transient final Module module;
	private StoreAuditor storeAuditor;

	public DependencyAuditor(Module module) {
		this.module = module;
		load();
	}

	boolean isModified(io.intino.tara.magritte.Node node) {
		return storeAuditor.isCreated(node);
	}

	public void stash(Stash stash) {
		customize(stash);
		this.storeAuditor = new StoreAuditor(new FileSystemStore(IntinoDirectory.of(module.getProject())) {
			@Override
			public Stash stashFrom(String path) {
				return newStash(stash.language, dependencyNodesOf(stash.nodes));
			}
		}, module.getName());
		this.storeAuditor.trace("");
		this.storeAuditor.changeList();
		this.storeAuditor.commit();
	}


	private void customize(Stash stash) {
		Node node = importsNode(stash.nodes.get(0));
		if (node == null) return;
		node.name = node.name.replace(Predicate.nameOf(node.name), "") + "imports";
		dependencyNodesOf(stash.nodes).forEach(n ->
				n.name = node.name + "$" + valueOf("groupId", n.variables) + ":" + valueOf("artifactId", n.variables) + ":" + valueOf("version", n.variables) + ":" + scopeOf(n));
	}

	private String scopeOf(Node node) {
		return node.facets.get(0).replace("Artifact$Imports$", "");
	}

	private Node importsNode(Node node) {
		return node.nodes.stream().filter(n -> n.facets.contains("Artifact$Imports")).findFirst().orElse(null);
	}

	private List<Node> dependencyNodesOf(List<Node> nodes) {
		List<Node> nodeList = new ArrayList<>();
		for (Node node : nodes) {
			if (!node.nodes.isEmpty()) nodeList.addAll(dependencyNodesOf(node.nodes));
			if (node.facets.stream().anyMatch(f -> f.startsWith("Artifact$Imports$"))) nodeList.add(node);
		}
		return nodeList;
	}

	private String valueOf(String varName, List<Variable> variables) {
		return (String) variables.stream().filter(v -> v.name.equals(varName)).findFirst().get().values.get(0);
	}

	private void load() {
		try {
			if (!resolutionsFile().toFile().exists()) return;
			Gson gson = new Gson();
			putAll(gson.fromJson(new String(Files.readAllBytes(resolutionsFile())), new TypeToken<Map<String, List<Dependency>>>() {
			}.getType()));
		} catch (Exception e) {
			Logger.getInstance(this.getClass()).error(e);
			resolutionsFile().toFile().delete();
		}
	}

	void save() {
		try {

			Files.write(resolutionsFile(), new Gson().toJson(new HashMap<>(this)).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			Logger.getInstance(this.getClass()).error(e);
		}
	}

	private Path resolutionsFile() {
		return new File(IntinoDirectory.of(module.getProject()), module.getName() + "_dependencies.json").toPath();
	}

	public void invalidate() {
		auditionFile().delete();
		resolutionsFile().toFile().delete();

	}

	@NotNull
	private File auditionFile() {
		return new File(IntinoDirectory.of(module.getProject()), module.getName());
	}
}
