package io.intino.plugin.annotator.imports;

import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.lang.psi.IdentifierReference;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TaraReferenceImporter {

	private TaraReferenceImporter() {
	}

	@NotNull
	public static List<ImportQuickFix> proposeImportFix(final IdentifierReference node) {
		Identifier element = node.getIdentifierList().get(0);
		List<Node> nodes = IntinoUtil.findRootNode(element, element.getText());
		ArrayList<ImportQuickFix> quickFixes = new ArrayList<>();
		if (nodes.isEmpty()) return Collections.EMPTY_LIST;
		quickFixes.addAll(nodes.stream().map(concept -> new ImportQuickFix((TaraModel) node.getContainingFile(), concept)).collect(Collectors.toList()));
		return quickFixes;
	}
}