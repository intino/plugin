package io.intino.plugin.stash;

import com.intellij.openapi.vfs.VirtualFile;
import io.intino.tara.io.Concept;
import io.intino.tara.io.Node;
import io.intino.tara.io.Stash;
import io.intino.tara.io.Variable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.intino.tara.io.StashDeserializer.stashFrom;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

class StashToTara {

	private StringBuilder builder = new StringBuilder();

	static Path createTara(VirtualFile stash, File destiny) throws IOException {
		destiny.deleteOnExit();
		final String stashText = taraFrom(stashFrom(new File(stash.getPath())));
		Files.write(destiny.toPath(), stashText.getBytes(StandardCharsets.UTF_8));
		return destiny.toPath();
	}

	static String taraFrom(Stash stash) {
		if (stash == null) return "Stash cannot be opened";
		return new StashToTara().execute(stash);
	}

	private String execute(Stash stash) {
		writeDsl(stash);
		writeContentRules(stash.contentRules, -1, stash.concepts);
		writeConcepts(stash, -1);
		writeNodes(stash.nodes, -1);
		return builder.toString();
	}

	private void writeConcepts(Stash stash, int level) {
		List<String> types = stash.contentRules.stream().map(r -> r.type).collect(toList());
		writeContentRules(stash.concepts.stream()
				.filter(c -> !isComponent(c))
				.filter(c -> !types.contains(c.name))
				.map(c -> new Concept.Content(c.name, 0, 0)).collect(toList()), level, stash.concepts);
	}

	private boolean isComponent(Concept c) {
		return !c.name.contains("#") && c.name.contains("$");
	}

	private void writeContentRules(List<Concept.Content> contentRules, int level, List<Concept> directory) {
		contentRules.forEach(c -> writeConcept(c, level + 1, directory));
	}

	private void writeConcept(Concept.Content contentRules, int level, List<Concept> directory) {
		newLine(level);
		writeHeader(contentRules, conceptOf(contentRules.type, directory));
		writeVariables(conceptOf(contentRules.type, directory).variables, level);
		writeParameters(conceptOf(contentRules.type, directory).parameters, level);
		writeContentRules(conceptOf(contentRules.type, directory), level, directory);
		writeNodes(conceptOf(contentRules.type, directory).nodes, level);
		if (level == 0) newLine(0);
	}

	private void writeContentRules(Concept concept, int level, List<Concept> directory) {
		concept.contentRules.stream()
				.filter(r -> !r.type.startsWith(concept.name + "$"))
				.forEach(r -> {
					newLine(level + 1);
					write("has", cardinalityOf(r), r.type.replace("$", "."));
				});
		writeContentRules(concept.contentRules.stream()
				.filter(r -> r.type.startsWith(concept.name) && !r.type.equals(concept.name)).collect(toList()), level, directory);
	}

	private void writeHeader(Concept.Content rule, Concept concept) {
		write(coreType(concept), cardinalityOf(rule), simpleName(concept.name));
		if (concept.parent != null) write(" extends " + concept.parent);
		if (concept.name.contains("#")) write(" on " + concept.name.split("#")[1].replace("$", "."));
		if (concept.types.size() > 1) {
			write(" as ");
			range(1, concept.types.size()).forEach(i -> write(concept.types.get(i), ";"));
		}
	}

	private String coreType(Concept concept) {
		return concept.types.get(0).startsWith("MetaAspect") ? "MetaAspect" :
				concept.types.get(0).startsWith("Aspect") ? "Aspect" : simpleName(concept.types.get(0));
	}

	private void writeNodes(List<? extends Node> instances, int level) {
		instances.forEach(i -> writeNode(i, level + 1));
	}

	private void writeDsl(Stash stash) {
		write("dsl ", stash.language);
		newLine(0);
	}

	private void writeNode(Node node, int level) {
		newLine(level);
		writeCore(node, level);
		if (level == 0) newLine(0);
	}

	private void newLine(int level) {
		addNewLine();
		addTabs(level);
	}

	private void writeCore(Node node, int level) {
		write(simpleName(node.layers.get(0)), " ", simpleName(node.name));
		writeAspects(node);
		writeVariables(node.variables, level);
		writeNodes(node.nodes, level);
	}

	private void writeAspects(Node node) {
		if (node.layers.size() > 1) write(" as");
		node.layers.stream().filter(a -> a.contains("#")).map(facet -> " " + facet.split("#")[0]).forEach(this::write);
	}

	private void writeVariables(List<Variable> variables, int level) {
		variables.forEach(v -> {
			addNewLine();
			addTabs(level + 1);
			write("var " + v.getClass().getSimpleName().toLowerCase() + " " + v.name, " = ");
			write(v);
		});
	}

	private void writeParameters(List<Variable> parameters, int level) {
		parameters.forEach(p -> {
			addNewLine();
			addTabs(level + 1);
			write(p.name, " = ");
			write(p);
		});
	}

	private void write(Variable variable) {
		if (variable instanceof Variable.Integer) format(variable);
		else if (variable instanceof Variable.Long) format(variable);
		else if (variable instanceof Variable.Double) format(variable);
		else if (variable instanceof Variable.Boolean) format(variable);
		else if (variable instanceof Variable.String) formatWithQuotes(variable);
		else if (variable instanceof Variable.Resource) formatWithQuotes(variable);
		else if (variable instanceof Variable.Reference) format(variable);
		else if (variable instanceof Variable.Word) format(variable);
		else if (variable instanceof Variable.Function) format(variable);
		else if (variable instanceof Variable.Object) format(variable);
		else if (variable instanceof Variable.Date) formatWithQuotes(variable);
		else if (variable instanceof Variable.Instant) formatWithQuotes(variable);
		else if (variable instanceof Variable.Time) formatWithQuotes(variable);
	}

	private void format(Variable variable) {
		if (variable instanceof Variable.Object) write("@reference ");
		variable.values.forEach(v -> write(v, " "));
	}

	private void formatWithQuotes(Variable variable) {
		variable.values.forEach(v -> write("\"", v.toString(), "\" "));
	}

	private void addNewLine() {
		write("\n");
	}

	private void addTabs(int level) {
		range(0, level).forEach(i -> write("\t"));
	}

	private String simpleName(String name) {
		if (name == null) return "";
		String shortName = name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : name;
		shortName = shortName.contains("#") ? shortName.substring(shortName.lastIndexOf("#") + 1) : shortName;
		shortName = shortName.contains("$") ? shortName.substring(shortName.lastIndexOf("$") + 1) : shortName;
		return shortName;
	}

	@NotNull
	private String cardinalityOf(Concept.Content rules) {
		if (rules.min == 0 && (rules.max == 0 || rules.max == Integer.MAX_VALUE)) return " ";
		return ":{" + rules.min + ".." + (rules.max == Integer.MAX_VALUE ? "*" : rules.max) + "} ";
	}

	public void write(Object... content) {
		for (Object o : content) builder.append(o);
	}

	private Concept conceptOf(String type, List<Concept> directory) {
		return directory.stream().filter(c -> c.name.equals(type)).findFirst().orElse(null);
	}
}
