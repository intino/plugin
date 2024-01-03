package io.intino.plugin.codeinsight.annotators.semanticanalizer;

import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.fix.FixFactory;
import io.intino.plugin.errorreporting.TaraRuntimeException;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.Checker;
import io.intino.tara.Language;
import io.intino.tara.language.model.Element;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramRoot;
import io.intino.tara.language.semantics.errorcollector.SemanticException;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;

import java.util.ArrayList;
import java.util.List;

import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class ModelAnalyzer extends TaraAnalyzer {
	private final TaraModel model;

	public ModelAnalyzer(TaraModel model) {
		this.model = model;
	}

	@Override
	public void analyze() {
		try {
			Language language = IntinoUtil.getLanguage(model);
			if (language == null) return;
			new Checker(language).check(model);
		} catch (SemanticFatalException fatal) {
			for (SemanticException e : fatal.exceptions()) {
				if (e.origin() == null) throw new TaraRuntimeException("origin = null: " + e.getMessage(), e);
				List<PsiElement> origins = cast(e.origin());
				for (PsiElement origin : origins)
					if (origin instanceof Mogram && !(origin instanceof MogramRoot)) {
						origin = ((TaraMogram) origin).getSignature();
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
