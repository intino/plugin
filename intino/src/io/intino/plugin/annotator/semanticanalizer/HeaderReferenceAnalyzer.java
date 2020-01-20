package io.intino.plugin.annotator.semanticanalizer;

import com.intellij.psi.PsiReference;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.highlighting.TaraSyntaxHighlighter;
import io.intino.plugin.lang.psi.HeaderReference;
import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.messages.MessageProvider;

import java.util.List;

import static io.intino.tara.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class HeaderReferenceAnalyzer extends TaraAnalyzer {

	public static final String MESSAGE = MessageProvider.message("unreached.reference");
	private final HeaderReference reference;

	public HeaderReferenceAnalyzer(HeaderReference reference) {
		this.reference = reference;
	}

	@Override
	public void analyze() {
		List<? extends Identifier> identifierList = reference.getIdentifierList();
		Identifier element = identifierList.get(identifierList.size() - 1);
		PsiReference aReference = element.getReference();
		if (aReference == null || aReference.resolve() == null)
			results.put(element, new TaraAnnotator.AnnotateAndFix(ERROR, MESSAGE, TaraSyntaxHighlighter.UNRESOLVED_ACCESS));
	}
}
