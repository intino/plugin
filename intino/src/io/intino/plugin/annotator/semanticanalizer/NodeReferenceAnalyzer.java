package io.intino.plugin.annotator.semanticanalizer;

import com.intellij.psi.PsiElement;
import io.intino.magritte.Checker;
import io.intino.magritte.Language;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.semantics.errorcollector.SemanticException;
import io.intino.magritte.lang.semantics.errorcollector.SemanticFatalException;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.annotator.fix.FixFactory;
import io.intino.plugin.lang.psi.TaraNodeReference;
import io.intino.plugin.lang.psi.impl.TaraUtil;

import static io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class NodeReferenceAnalyzer extends TaraAnalyzer {
	private final TaraNodeReference nodeReference;

	public NodeReferenceAnalyzer(TaraNodeReference nodeReference) {
		this.nodeReference = nodeReference;
	}

	@Override
	public void analyze() {
		try {
			Language language = TaraUtil.getLanguage(nodeReference);
			if (language == null) return;
			new Checker(language).check(nodeReference);
		} catch (SemanticFatalException fatal) {
			for (SemanticException e : fatal.exceptions()) results.put(nodeReference, annotateAndFix(e, nodeReference));
		}
	}

	private TaraAnnotator.AnnotateAndFix annotateAndFix(SemanticException e, Node destiny) {
		return new TaraAnnotator.AnnotateAndFix(ERROR, e.getMessage(), FixFactory.get(e.key(), (PsiElement) destiny));
	}
}
