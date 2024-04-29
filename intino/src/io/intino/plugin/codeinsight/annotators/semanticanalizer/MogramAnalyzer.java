package io.intino.plugin.codeinsight.annotators.semanticanalizer;

import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.fix.DeprecatedErrorFixFactory;
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

import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.WARNING;

public class MogramAnalyzer extends TaraAnalyzer {
	private final Mogram mogram;

	public MogramAnalyzer(Mogram mogram) {
		this.mogram = mogram;
	}

	@Override
	public void analyze() {
		try {
			Language language = IntinoUtil.getLanguage((PsiElement) mogram);
			if (language == null) return;
			mogram.resolve();
			new Checker(language).check(mogram);
			String description = language.doc(mogram.type()).description();
			if (description.toLowerCase().startsWith("deprecated"))
				results.put(((TaraMogram) mogram).getSignature(), new AnnotateAndFix(WARNING, mogram.doc(), DeprecatedErrorFixFactory.get("deprecated.mogram", (PsiElement) mogram)));
		} catch (SemanticFatalException fatal) {
			for (SemanticException e : fatal.exceptions())
				for (PsiElement origin : e.origin() != null ? cast(e.origin()) : Collections.singletonList((TaraMogram) mogram)) {
					if (origin instanceof TaraMogram) origin = ((TaraMogram) origin).getSignature();
					else if (origin instanceof MogramRoot) return;
					else if (origin instanceof Facet) origin = ((TaraFacetApply) origin).getMetaIdentifier();
					results.put(origin, annotateAndFix(e, origin));
				}
		}
	}

	private AnnotateAndFix annotateAndFix(SemanticException e, PsiElement destination) {
		return new AnnotateAndFix(e.level(), e.getMessage(), FixFactory.get(e.key(), destination, e.getParameters()));
	}

	private List<PsiElement> cast(Element[] elements) {
		List<PsiElement> list = new ArrayList<>();
		for (Element element : elements) list.add((PsiElement) element);
		return list;
	}
}
