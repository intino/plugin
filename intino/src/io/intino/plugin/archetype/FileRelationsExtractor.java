package io.intino.plugin.archetype;

import io.intino.plugin.IntinoException;
import io.intino.plugin.archetype.lang.antlr.ArchetypeGrammar;
import io.intino.plugin.archetype.lang.antlr.ArchetypeParser;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.plugin.project.Safe.safe;

public class FileRelationsExtractor {

	private ArchetypeGrammar.RootContext root;

	public FileRelationsExtractor(File archetypeFile) {
		try {
			root = new ArchetypeParser(archetypeFile).parse();
		} catch (IntinoException e) {
			return;
		}
	}

	public Map<String, List<String>> sharedDirectoriesWithOwner(String owner) {
		if (root == null) return Collections.emptyMap();
		return extract(root.node(), owner, null, "");
	}

	private Map<String, List<String>> extract(List<ArchetypeGrammar.NodeContext> nodes, String searchOwner, String parentOwner, String contextDirectory) {
		Map<String, List<String>> directories = new HashMap<>();
		for (ArchetypeGrammar.NodeContext node : nodes) {
			if (node.start.toString().equals("*")) continue;
			String currentDirectory = contextDirectory(node, contextDirectory);
			String scopeOwner = (parentOwner == null) ? safe(() -> node.declaration().ownerAndConsumer().IDENTIFIER().toString(), null) : parentOwner;
			if (node.declaration().parameters() == null &&
					node.declaration().splitted() == null &&
					node.declaration().WITH() == null &&
					node.declaration().ownerAndConsumer().uses() != null &&
					scopeOwner.equals(searchOwner)) {
				directories.put(currentDirectory, node.declaration().ownerAndConsumer().uses().IDENTIFIER().stream().map(Object::toString).collect(Collectors.toList()));
			}
			if (node.body() != null)
				directories.putAll(extract(node.body().node(), searchOwner, scopeOwner, currentDirectory));
		}
		return directories;
	}

	private String contextDirectory(ArchetypeGrammar.NodeContext node, String contextDirectory) {
		ArchetypeGrammar.DeclarationContext declaration = node.declaration();
		return (contextDirectory.isEmpty() ? "" : contextDirectory + File.separator) + (declaration.IN() != null ? declaration.LABEL(0).toString().replace("\"", "") : declaration.IDENTIFIER().toString());
	}
}