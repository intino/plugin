package io.intino.plugin.codeinsight.annotators.imports;

import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.lang.psi.IdentifierReference;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaraReferenceImporter {

	private TaraReferenceImporter() {
	}

	@NotNull
	public static List<ImportQuickFix> proposeImportFix(final IdentifierReference node) {
		Identifier element = node.getIdentifierList().get(0);
		List<Mogram> nodes = IntinoUtil.findRootNode(element, element.getText());
		ArrayList<ImportQuickFix> quickFixes = new ArrayList<>();
		if (nodes.isEmpty()) return Collections.emptyList();
		quickFixes.addAll(nodes.stream().map(concept -> new ImportQuickFix((TaraModel) node.getContainingFile(), concept)).toList());
		return quickFixes;
	}
}