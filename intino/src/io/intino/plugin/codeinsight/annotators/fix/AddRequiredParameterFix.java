package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.codeinsight.livetemplates.TaraTemplateContext;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraFacetApply;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.TaraRuleContainer;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Parametrized;
import io.intino.tara.language.semantics.Constraint;
import io.intino.tara.language.semantics.constraints.parameter.ReferenceParameter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.intino.tara.language.model.Primitive.*;

class AddRequiredParameterFix extends WithLiveTemplateFix implements IntentionAction {

	@SafeFieldForPreview
	private Parametrized parametrized;

	public AddRequiredParameterFix(PsiElement element) {
		try {
			this.parametrized = element instanceof Mogram ? (Parametrized) element : (Parametrized) TaraPsiUtil.getContainerOf(element);
		} catch (Throwable e) {
			this.parametrized = null;
		}
	}



	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Add required parameters";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		return file.isValid();
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		if (this.parametrized == null) return;
		List<Constraint.Parameter> requires = findConstraints().stream().
				filter(constraint -> constraint instanceof Constraint.Parameter && ((Constraint.Parameter) constraint).size().isRequired()).
				map(constraint -> (Constraint.Parameter) constraint).toList();
		filterPresentParameters(requires);
		ApplicationManager.getApplication().invokeAndWait(() -> createLiveTemplateFor(requires, file, editor));
//		cleanSignature(findAnchor(requires));
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private List<Constraint> findConstraints() {
		final Mogram node = (Mogram) this.parametrized;
		final List<Constraint> constraintsOf = new ArrayList<>(IntinoUtil.constraintsOf(node));
		List<Constraint> aspectConstraints = new ArrayList<>();
		final List<String> aspects = aspectTypes(node);
		for (Constraint c : constraintsOf) {
			if (c instanceof Constraint.Facet && aspects.contains(((Constraint.Facet) c).type())) {
				aspectConstraints.addAll(((Constraint.Facet) c).constraints());
			}
		}
		constraintsOf.addAll(aspectConstraints);
		return constraintsOf;
	}

//	private void cleanSignature(PsiElement anchor) {
//		if (hasParameters(anchor)) return;
//		final TaraFacetApply facet = TaraPsiImplUtil.getContainerByType(anchor, TaraFacetApply.class);
//		final TaraNode Mogram = TaraPsiImplUtil.getContainerByType(anchor, TaraMogram.class);
//		if (facet != null && facet.getParameters() != null) facet.getParameters().delete();
//		else if (node.getSignature().getParameters() != null) ((TaraNode) parametrized).getSignature().getParameters().delete();
//
//	}

	private List<String> aspectTypes(Mogram node) {
		return node.resolve().secondaryTypes().stream().map(s -> s.substring(0, s.indexOf(":"))).toList();
	}

	private void createLiveTemplateFor(List<Constraint.Parameter> requires, PsiFile file, Editor editor) {
		IdeDocumentHistory.getInstance(file.getProject()).includeCurrentPlaceAsChangePlace();
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		final PsiElement anchor = ApplicationManager.getApplication().runWriteAction((Computable<PsiElement>) () -> findAnchor(requires));
		final Editor parameterEditor = positionCursor(file.getProject(), file, anchor);
		if (parameterEditor == null) return;
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(parameterEditor.getDocument());
		TemplateManager.getInstance(file.getProject()).startTemplate(parameterEditor, createTemplate(anchor, requires, file));
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(parameterEditor.getDocument());
	}

	private PsiElement findAnchor(List<Constraint.Parameter> requires) {
		return requires.isEmpty() || requires.get(0).facet().isEmpty() ? findAnchor((TaraMogram) parametrized) : findAnchor((TaraFacetApply) ((Mogram) parametrized).appliedFacets().stream().filter(f -> f.type().equals(requires.get(0).facet())).findFirst().get());
	}

	private PsiElement findAnchor(TaraMogram node) {
		if (!hasParameters(node)) {
			final PsiElement emptyParameters = TaraElementFactory.getInstance(node.getProject()).createEmptyParameters();
			return node.getSignature().addAfter(emptyParameters, anchor(node)).getFirstChild();
		} else {
			final List<Parameter> parameters = node.getSignature().getParameters().getParameters();
			return (PsiElement) parameters.get(parameters.size() - 1);
		}
	}

	private PsiElement anchor(TaraMogram node) {
		return node.getSignature().getMetaIdentifier() != null && node.getSignature().getMetaIdentifier().getNextSibling() instanceof TaraRuleContainer ?
				node.getSignature().getMetaIdentifier().getNextSibling() :
				node.getSignature().getMetaIdentifier();
	}

	private PsiElement findAnchor(TaraFacetApply apply) {
		if (!hasParameters(apply)) {
			if (apply.getParameters() != null && apply.getParameters().getParameters().isEmpty())
				return apply.getParameters().getFirstChild();
			final PsiElement emptyParameters = TaraElementFactory.getInstance(apply.getProject()).createEmptyParameters();
			return apply.addAfter(emptyParameters, apply.getMetaIdentifier()).getFirstChild();
		} else {
			final List<Parameter> parameters = apply.getParameters().getParameters();
			return (PsiElement) parameters.get(parameters.size() - 1);
		}
	}

	private boolean hasParameters(PsiElement element) {
		final TaraFacetApply aspectApply = TaraPsiUtil.getContainerByType(element, TaraFacetApply.class);
		return aspectApply != null ? hasParameters(aspectApply) : hasParameters(TaraPsiUtil.getContainerByType(element, TaraMogram.class));
	}

	private boolean hasParameters(TaraFacetApply apply) {
		return apply.getParameters() != null && !apply.getParameters().getParameters().isEmpty();
	}

	private boolean hasParameters(TaraMogram node) {
		return node.getSignature().getParameters() != null && !node.getSignature().getParameters().getParameters().isEmpty();
	}

	private Template createTemplate(PsiElement anchor, List<Constraint.Parameter> requires, PsiFile file) {
		final Template template = TemplateManager.getInstance(file.getProject()).createTemplate("var", "Tara", createTemplateText(anchor, requires));
		addVariables(template, requires);
		((TemplateImpl) template).getTemplateContext().setEnabled(contextType(TaraTemplateContext.class), true);
		return template;
	}

	private void addVariables(Template template, List<Constraint.Parameter> requires) {
		for (int i = 0; i < requires.size(); i++)
			template.addVariable("VALUE" + i, "", '"' + defaultValue(requires, i), true);
	}

	@NotNull
	private String defaultValue(List<Constraint.Parameter> requires, int i) {
		final Constraint.Parameter parameter = requires.get(i);
		if (parameter instanceof ReferenceParameter) return "empty ";
		return (mustBeQuoted(parameter) ? "\\\"\\\"" : "") + '"';
	}

	private boolean mustBeQuoted(Constraint.Parameter parameter) {
		return DATE.equals(parameter.type()) || STRING.equals(parameter.type()) || TIME.equals(parameter.type()) || RESOURCE.equals(parameter.type());
	}

	private String createTemplateText(PsiElement anchor, List<Constraint.Parameter> requires) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < requires.size(); i++)
			text.append(", ").append(requires.get(i).name()).append(" = ").append("$VALUE").append(i).append("$");
		return !hasParameters(anchor) && !text.isEmpty() ? text.substring(2) : text.toString();
	}

	private void filterPresentParameters(List<Constraint.Parameter> requires) {
		for (Parameter parameter : parametrized.parameters()) {
			Constraint.Parameter require = findInConstraints(requires, parameter.name());
			if (require != null) requires.remove(require);
		}
	}

	@Nullable
	private Constraint.Parameter findInConstraints(List<Constraint.Parameter> constraints, String name) {
		for (Constraint.Parameter require : constraints) if (require.name().equals(name)) return require;
		return null;
	}

	@Override
	public boolean startInWriteAction() {
		return false;
	}
}
