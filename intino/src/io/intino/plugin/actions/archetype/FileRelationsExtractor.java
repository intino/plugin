package io.intino.plugin.actions.archetype;

import io.intino.plugin.IntinoException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileRelationsExtractor {

	private ArchetypeGrammar.RootContext root;

	public FileRelationsExtractor(File archetypeFile) {
		try {
			root = new ArchetypeParser(archetypeFile).parse();
		} catch (IntinoException e) {
			return;
		}
	}

	public Map<String, String> sharedDirectoriesWithOwner(String owner) {
		Map<String, String> directories = new HashMap<>();
		directories.putAll(extract(root.node(), owner, ""));
		return directories;
	}

	private Map<String, String> extract(List<ArchetypeGrammar.NodeContext> nodes, String owner, String contextDirectory) {
		for (ArchetypeGrammar.NodeContext node : nodes) {
			if (node.start.toString().equals("*")) continue;
			if (node.declaration().parameters() == null &&
					node.declaration().splitted() == null &&
					node.declaration().WITH() == null &&
					node.declaration().ownerAndUses().OWNER().toString().equals(owner)) {
				node.declaration().ownerAndUses().uses().IDENTIFIER().stream().map(Object::toString).forEach(i -> {

				});
			}
			extract(node.body().node(), owner, contextDirectory(node, contextDirectory));
		}
		return null;
	}

	private String contextDirectory(ArchetypeGrammar.NodeContext node, String contextDirectory) {
		ArchetypeGrammar.DeclarationContext declaration = node.declaration();
		return contextDirectory + File.separator + (declaration.IN() != null ? declaration.LABEL(0).toString() : declaration.IDENTIFIER().toString());
	}
}
