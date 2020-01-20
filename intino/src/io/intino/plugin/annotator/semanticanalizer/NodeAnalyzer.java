package io.intino.plugin.annotator.semanticanalizer;

import com.intellij.psi.PsiElement;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.annotator.fix.FixFactory;
import io.intino.plugin.lang.psi.TaraAspectApply;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.Checker;
import io.intino.tara.Language;
import io.intino.tara.lang.model.Aspect;
import io.intino.tara.lang.model.Element;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.NodeRoot;
import io.intino.tara.lang.semantics.errorcollector.SemanticException;
import io.intino.tara.lang.semantics.errorcollector.SemanticFatalException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeAnalyzer extends TaraAnalyzer {

	private Node node;

	public NodeAnalyzer(Node node) {
		this.node = node;
	}

	@Override
	public void analyze() {
		try {
			Language language = TaraUtil.getLanguage((PsiElement) node);
			if (language == null) return;
			new Checker(language).check(node);
		} catch (SemanticFatalException fatal) {
			for (SemanticException e : fatal.exceptions()) {
				List<PsiElement> origins = e.origin() != null ? cast(e.origin()) : Collections.singletonList((TaraNode) node);
				for (PsiElement origin : origins) {
					if (origin instanceof TaraNode) origin = ((TaraNode) origin).getSignature();
					else if (origin instanceof NodeRoot) return;
					else if (origin instanceof Aspect) origin = ((TaraAspectApply) origin).getMetaIdentifier();
					results.put(origin, annotateAndFix(e, origin));
				}
			}
		}
	}

	private TaraAnnotator.AnnotateAndFix annotateAndFix(SemanticException e, PsiElement destiny) {
		return new TaraAnnotator.AnnotateAndFix(e.level(), e.getMessage(), FixFactory.get(e.key(), destiny, e.getParameters()));
	}

	private List<PsiElement> cast(Element[] elements) {
		List<PsiElement> list = new ArrayList<>();
		for (Element element : elements) list.add((PsiElement) element);
		return list;
	}
}
