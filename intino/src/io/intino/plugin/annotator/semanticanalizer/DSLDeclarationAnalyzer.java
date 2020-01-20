package io.intino.plugin.annotator.semanticanalizer;

import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.annotator.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.annotator.fix.FixFactory;
import io.intino.plugin.lang.psi.TaraDslDeclaration;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.messages.MessageProvider;
import io.intino.tara.Language;

import static io.intino.tara.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class DSLDeclarationAnalyzer extends TaraAnalyzer {

	private static final String PROTEO = "Proteo";
	private static final String MESSAGE = "dsl.not.found";
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
			Language dsl = TaraUtil.getLanguage(file);
			if (dsl == null && !PROTEO.equals(dslName) || !dslName.equals(file.dsl())) {
				results.put(file, new AnnotateAndFix(ERROR, MessageProvider.message(MESSAGE), FixFactory.get(MESSAGE, file)));
			}
		}
	}

	private void findDuplicates() {
		TaraDslDeclaration[] declarations = PsiTreeUtil.getChildrenOfType(file, TaraDslDeclaration.class);
		if (declarations != null && declarations.length > 1)
			for (TaraDslDeclaration declaration : declarations)
				results.put(declaration, new AnnotateAndFix(ERROR, MessageProvider.message("duplicated.dsl.declaration"), FixFactory.get(MESSAGE, file)));
	}
}
