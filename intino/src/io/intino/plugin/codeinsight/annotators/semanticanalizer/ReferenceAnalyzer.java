package io.intino.plugin.codeinsight.annotators.semanticanalizer;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.CommonProblemDescriptorImpl;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ex.ProblemDescriptorImpl;
import com.intellij.codeInspection.ex.QuickFixWrapper;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ExternallyAnnotated;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.fix.CreateClassFromMethodReferenceFix;
import io.intino.plugin.codeinsight.annotators.fix.CreateMetricClassIntention;
import io.intino.plugin.codeinsight.annotators.fix.CreateVariableRuleClassIntention;
import io.intino.plugin.codeinsight.annotators.imports.AlternativesForReferenceFix;
import io.intino.plugin.codeinsight.annotators.imports.CreateNodeQuickFix;
import io.intino.plugin.codeinsight.annotators.imports.ImportQuickFix;
import io.intino.plugin.codeinsight.annotators.imports.TaraReferenceImporter;
import io.intino.plugin.codeinsight.languageinjection.CreateFunctionInterfaceIntention;
import io.intino.plugin.highlighting.TaraSyntaxHighlighter;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.resolve.MethodReferenceSolver;
import io.intino.plugin.lang.psi.resolve.OutDefinedReferenceSolver;
import io.intino.plugin.lang.psi.resolve.TaraMogramReferenceSolver;
import io.intino.plugin.messages.MessageProvider;
import io.intino.tara.Language;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;
import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.INSTANCE;
import static java.util.Collections.singletonList;

public class ReferenceAnalyzer extends TaraAnalyzer {

	private static final String MESSAGE = "unreached.reference";
	private final IdentifierReference reference;

	public ReferenceAnalyzer(IdentifierReference reference) {
		this.reference = reference;
	}

	@Override
	public void analyze() {
		List<? extends Identifier> identifierList = reference.getIdentifierList();
		Identifier element = identifierList.get(identifierList.size() - 1);
		PsiReference aReference = element.getReference();
		if (aReference == null) return;
		final PsiElement resolve = aReference.resolve();
		if (resolve != null) return;
		if (isInstanceReference() && aReference instanceof TaraMogramReferenceSolver)
			results.put(reference, new AnnotateAndFix(INSTANCE, MessageProvider.message("node.reference")));
		else if (TaraPsiUtil.contextOf(reference, TaraVariableType.class) == null || !isConceptReference())
			setError(aReference, element);
	}

	private boolean isConceptReference() {
		final Language language = IntinoUtil.getLanguage(reference);
		return language != null && language.types(reference.getText()) != null;
	}

	private boolean isInstanceReference() {
		final Language language = IntinoUtil.getLanguage(reference);
		return language != null && language.instances().containsKey(reference.getText());
	}

	private void setError(PsiReference aReference, Identifier element) {
		if (aReference instanceof TaraMogramReferenceSolver) createNodeError(element);
		else if (aReference instanceof MethodReferenceSolver) createMethodReferenceError(element);
		else if (aReference instanceof OutDefinedReferenceSolver) createOutDefinedReferenceError(element);
		else createGeneralError(element);
	}

	private void createGeneralError(Identifier element) {
		results.put(element, new AnnotateAndFix(ERROR, MessageProvider.message(MESSAGE), TaraSyntaxHighlighter.UNRESOLVED_ACCESS));
	}

	private void createNodeError(Identifier element) {
		results.put(element, new AnnotateAndFix(ERROR, MessageProvider.message(MESSAGE), TaraSyntaxHighlighter.UNRESOLVED_ACCESS, createNodeReferenceFixes(element)));
	}

	private void createMethodReferenceError(Identifier element) {
		results.put(element, new AnnotateAndFix(ERROR, MessageProvider.message(MESSAGE), TaraSyntaxHighlighter.UNRESOLVED_ACCESS, createMethodReferenceFixes(element)));
	}

	private void createOutDefinedReferenceError(Identifier element) {
		Variable variable = TaraPsiUtil.getContainerByType(element, Variable.class);
		if (variable == null) return;
		Rule rule = TaraPsiUtil.getContainerByType(element, Rule.class);
		if (rule == null)
			results.put(element, new AnnotateAndFix(ERROR, MessageProvider.message("error.link.to.rule"), TaraSyntaxHighlighter.UNRESOLVED_ACCESS));
		else
			results.put(element, new AnnotateAndFix(ERROR, MessageProvider.message("error.link.to.rule"), TaraSyntaxHighlighter.UNRESOLVED_ACCESS, collectFixes(variable, rule)));
	}

	private IntentionAction[] collectFixes(Variable variable, Rule rule) {
		if (variable == null) return new IntentionAction[0];
		if (Primitive.FUNCTION.equals(variable.type()))
			return new IntentionAction[]{new CreateFunctionInterfaceIntention(variable)};
		if (Primitive.WORD.equals(variable.type()))
			return new IntentionAction[]{new CreateVariableRuleClassIntention(rule)};
		return new IntentionAction[]{new CreateVariableRuleClassIntention(rule), new CreateMetricClassIntention(rule)};
	}

	private IntentionAction[] createNodeReferenceFixes(Identifier element) {
		ArrayList<LocalQuickFix> fixes = new ArrayList<>(createImportFixes(element));
		List<IntentionAction> actions = fixes.stream().map(fix -> toIntention(element, fix.getName(), fix)).collect(Collectors.toList());
		actions.addAll(alternativesForReferenceFix(element));
		actions.addAll(createNewElementFix(element));
		return actions.toArray(new IntentionAction[0]);
	}


	private IntentionAction[] createMethodReferenceFixes(Identifier element) {
		List<IntentionAction> actions = new ArrayList<>(createMethodFix(element));
		return actions.toArray(new IntentionAction[0]);
	}

	private List<CreateNodeQuickFix> createNewElementFix(Identifier element) {
		Mogram mogram = TaraPsiUtil.getContainerNodeOf(element);
		if (mogram != null)
			return singletonList(new CreateNodeQuickFix(element.getText(), (TaraModel) element.getContainingFile()));
		return Collections.emptyList();
	}


	private List<IntentionAction> createMethodFix(Identifier element) {
		Valued valued = TaraPsiUtil.getContainerByType(element, Valued.class);
		if (valued != null) return getFix(element, valued);
		return Collections.emptyList();
	}

	@NotNull
	private List<IntentionAction> getFix(Identifier element, Valued valued) {
		return singletonList(new CreateClassFromMethodReferenceFix(valued));
	}

	private List<AlternativesForReferenceFix> alternativesForReferenceFix(Identifier element) {
		Mogram mogram = TaraPsiUtil.getContainerNodeOf(element);
		return mogram != null ? singletonList(new AlternativesForReferenceFix(element)) : Collections.emptyList();
	}

	private IntentionAction toIntention(PsiElement node, String message, LocalQuickFix fix) {
		return toIntention(node, getAnnotationRange(node), message, fix);
	}

	private static TextRange getAnnotationRange(@NotNull PsiElement startElement) {
		return startElement instanceof ExternallyAnnotated
				? ((ExternallyAnnotated) startElement).getAnnotationRegion()
				: startElement.getTextRange();
	}

	private IntentionAction toIntention(PsiElement element, TextRange range, String message, LocalQuickFix fix) {
		LocalQuickFix[] quickFixes = {fix};
		CommonProblemDescriptorImpl descriptor = new ProblemDescriptorImpl(element, element, message,
				quickFixes, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, null, true);
		return QuickFixWrapper.wrap((ProblemDescriptor) descriptor, 0);
	}

	private List<ImportQuickFix> createImportFixes(Identifier node) {
		final PsiFile file = InjectedLanguageManager.getInstance(node.getProject()).getTopLevelFile(node);
		if (!(file instanceof TaraModel)) return Collections.emptyList();
		return TaraReferenceImporter.proposeImportFix((IdentifierReference) node.getParent());
	}
}
