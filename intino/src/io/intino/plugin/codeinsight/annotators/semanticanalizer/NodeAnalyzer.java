package io.intino.plugin.codeinsight.annotators.semanticanalizer;

import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.fix.FixFactory;
import io.intino.plugin.lang.psi.TaraFacetApply;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.Checker;
import io.intino.tara.Language;
import io.intino.tara.language.model.Element;
import io.intino.tara.language.model.Facet;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramRoot;
import io.intino.tara.language.semantics.errorcollector.SemanticException;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeAnalyzer extends TaraAnalyzer {
	private final Mogram node;

	public NodeAnalyzer(Mogram node) {
		this.node = node;
	}

	@Override
	public void analyze() {
		try {
			Language language = IntinoUtil.getLanguage((PsiElement) node);
			if (language == null) return;
			node.resolve();
			new Checker(language).check(node);
		} catch (SemanticFatalException fatal) {
			for (SemanticException e : fatal.exceptions()) {
				List<PsiElement> origins = e.origin() != null ? cast(e.origin()) : Collections.singletonList((TaraMogram) node);
				for (PsiElement origin : origins) {
					if (origin instanceof TaraMogram) origin = ((TaraMogram) origin).getSignature();
					else if (origin instanceof MogramRoot) return;
					else if (origin instanceof Facet) origin = ((TaraFacetApply) origin).getMetaIdentifier();
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
