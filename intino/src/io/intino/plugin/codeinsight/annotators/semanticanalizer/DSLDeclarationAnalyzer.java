package io.intino.plugin.codeinsight.annotators.semanticanalizer;

import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.fix.FixFactory;
import io.intino.plugin.lang.psi.TaraDslDeclaration;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.Language;

import static io.intino.plugin.messages.MessageProvider.message;
import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class DSLDeclarationAnalyzer extends TaraAnalyzer {
	private final TaraModel file;

	public DSLDeclarationAnalyzer(TaraModel file) {
		this.file = file;
	}

	@Override
	public void analyze() {
		if (!hasErrors()) analyzeDslExistence();
	}

	private void analyzeDslExistence() {
		checkDslExistence(this.file.dsl());
		if (hasErrors()) return;
		findDuplicates();
	}

	private void checkDslExistence(String dslName) {
		if (dslName != null && !dslName.isEmpty()) {
			Language dsl = IntinoUtil.getLanguage(file);
			if (dsl == null || !dslName.equals(file.dsl()))
				results.put(file.getFirstChild(), new AnnotateAndFix(ERROR, message("dsl.not.found"), FixFactory.get("dsl.not.found", file)));
		}
	}

	private void findDuplicates() {
		TaraDslDeclaration[] declarations = PsiTreeUtil.getChildrenOfType(file, TaraDslDeclaration.class);
		if (declarations != null && declarations.length > 1)
			for (TaraDslDeclaration declaration : declarations)
				results.put(declaration, new AnnotateAndFix(ERROR, message("duplicated.dsl.declaration"), FixFactory.get("dsl.not.found", file)));
	}
}
