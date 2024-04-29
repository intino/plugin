package io.intino.plugin.codeinsight.annotators.semanticanalizer;

import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.fix.FixFactory;
import io.intino.plugin.lang.psi.TaraMogramReference;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.Checker;
import io.intino.tara.Language;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.semantics.errorcollector.SemanticException;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;

import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class MogramReferenceAnalyzer extends TaraAnalyzer {
	private final TaraMogramReference nodeReference;

	public MogramReferenceAnalyzer(TaraMogramReference nodeReference) {
		this.nodeReference = nodeReference;
	}

	@Override
	public void analyze() {
		try {
			Language language = IntinoUtil.getLanguage(nodeReference);
			if (language == null) return;
			new Checker(language).check(nodeReference);
		} catch (SemanticFatalException fatal) {
			for (SemanticException e : fatal.exceptions()) results.put(nodeReference, annotateAndFix(e, nodeReference));
		}
	}

	private TaraAnnotator.AnnotateAndFix annotateAndFix(SemanticException e, Mogram destiny) {
		return new TaraAnnotator.AnnotateAndFix(ERROR, e.getMessage(), FixFactory.get(e.key(), (PsiElement) destiny));
	}
}
