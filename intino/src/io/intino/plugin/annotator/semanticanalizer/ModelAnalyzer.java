package io.intino.plugin.annotator.semanticanalizer;

import com.intellij.psi.PsiElement;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.annotator.fix.FixFactory;
import io.intino.plugin.errorreporting.TaraRuntimeException;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.Checker;
import io.intino.tara.Language;
import io.intino.tara.lang.model.Element;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.NodeRoot;
import io.intino.tara.lang.semantics.errorcollector.SemanticException;
import io.intino.tara.lang.semantics.errorcollector.SemanticFatalException;

import java.util.ArrayList;
import java.util.List;

import static io.intino.tara.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class ModelAnalyzer extends TaraAnalyzer {
	private TaraModel model;

	public ModelAnalyzer(TaraModel model) {
		this.model = model;
	}

	@Override
	public void analyze() {
		try {
			Language language = TaraUtil.getLanguage(model);
			if (language == null) return;
			new Checker(language).check(model);
		} catch (SemanticFatalException fatal) {
			for (SemanticException e : fatal.exceptions()) {
				if (e.origin() == null) throw new TaraRuntimeException("origin = null: " + e.getMessage(), e);
				List<PsiElement> origins = cast(e.origin());
				for (PsiElement origin : origins)
					if (origin instanceof Node && !(origin instanceof NodeRoot)) {
						origin = ((TaraNode) origin).getSignature();
						results.put(origin, annotateAndFix(e, origin));
					}
			}
		}
	}

	private List<PsiElement> cast(Element[] elements) {
		List<PsiElement> list = new ArrayList<>();
		for (Element element : elements) list.add((PsiElement) element);
		return list;
	}

	private TaraAnnotator.AnnotateAndFix annotateAndFix(SemanticException e, PsiElement destiny) {
		return new TaraAnnotator.AnnotateAndFix(ERROR, e.getMessage(), FixFactory.get(e.key(), destiny));
	}
}
