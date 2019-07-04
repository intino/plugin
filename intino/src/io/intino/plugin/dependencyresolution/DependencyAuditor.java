package io.intino.plugin.dependencyresolution;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
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
				List<Node> nodes = importsNode(artifactNode(stash)).nodes;
				if (node(stash, "Model") != null) nodes.add(node(stash, "Model"));
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
				n.name = node.name + "$" + valueOf("groupId", n.variables) + ":" + valueOf("artifactId", n.variables) + ":" + valueOf("version", n.variables) + ":" + scopeOf(n));
	}

	private void customizeModel(Stash stash) {
		Node model = node(stash, "Model");
		if (model == null) return;
		model.name = artifactNode(stash).name + "$" + valueOf("language", model.variables) + ":" + valueOf("version", model.variables) + ":" + valueOf("sdk", model.variables);
	}

	private void customizeBox(Stash stash) {
		Node box = node(stash, "Box");
		if (box == null) return;
		box.name = artifactNode(stash).name + "$" + valueOf("language", box.variables) + ":" + valueOf("version", box.variables);
	}

	private Node node(Stash stash, String facet) {
		return artifactNode(stash).nodes.stream().filter(n -> n.facets.get(0).equals("Artifact$" + facet)).findFirst().orElse(null);
	}

	private Node artifactNode(Stash stash) {
		return stash.nodes.get(0);
	}

	private String scopeOf(Node node) {
		return node.facets.get(0).replace("Artifact$Imports$", "");
	}

	private Node importsNode(Node node) {
		return node.nodes.stream().filter(n -> n.facets.contains("Artifact$Imports")).findFirst().orElse(null);
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
