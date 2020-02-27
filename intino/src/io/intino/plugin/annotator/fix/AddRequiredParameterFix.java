package io.intino.plugin.annotator.fix;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.model.Parametrized;
import io.intino.magritte.lang.semantics.Constraint;
import io.intino.magritte.lang.semantics.constraints.parameter.ReferenceParameter;
import io.intino.plugin.codeinsight.livetemplates.TaraTemplateContext;
import io.intino.plugin.lang.psi.TaraAspectApply;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.TaraRuleContainer;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.magritte.lang.model.Primitive.*;

class AddRequiredParameterFix extends WithLiveTemplateFix implements IntentionAction {

	private Parametrized parametrized;

	public AddRequiredParameterFix(PsiElement element) {
		try {
			this.parametrized = element instanceof Node ? (Parametrized) element : (Parametrized) TaraPsiUtil.getContainerOf(element);
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
				map(constraint -> (Constraint.Parameter) constraint).collect(Collectors.toList());
		filterPresentParameters(requires);
//		cleanSignature(findAnchor(requires));
		createLiveTemplateFor(requires, file, editor);
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private List<Constraint> findConstraints() {
		final Node node = (Node) this.parametrized;
		final List<Constraint> constraintsOf = new ArrayList<>(TaraUtil.getConstraintsOf(node));
		List<Constraint> aspectConstraints = new ArrayList<>();
		final List<String> aspects = aspectTypes(node);
		for (Constraint c : constraintsOf) {
			if (c instanceof Constraint.Aspect && aspects.contains(((Constraint.Aspect) c).type())) {
				aspectConstraints.addAll(((Constraint.Aspect) c).constraints());
			}
		}
		constraintsOf.addAll(aspectConstraints);
		return constraintsOf;
	}

//	private void cleanSignature(PsiElement anchor) {
//		if (hasParameters(anchor)) return;
//		final TaraFacetApply facet = TaraPsiImplUtil.getContainerByType(anchor, TaraFacetApply.class);
//		final TaraNode node = TaraPsiImplUtil.getContainerByType(anchor, TaraNode.class);
//		if (facet != null && facet.getParameters() != null) facet.getParameters().delete();
//		else if (node.getSignature().getParameters() != null) ((TaraNode) parametrized).getSignature().getParameters().delete();
//
//	}

	private List<String> aspectTypes(Node node) {
		return node.resolve().secondaryTypes().stream().map(s -> s.substring(0, s.indexOf(":"))).collect(Collectors.toList());
	}

	private void createLiveTemplateFor(List<Constraint.Parameter> requires, PsiFile file, Editor editor) {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		IdeDocumentHistory.getInstance(file.getProject()).includeCurrentPlaceAsChangePlace();
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		final PsiElement anchor = findAnchor(requires);
		final Editor parameterEditor = positionCursor(file.getProject(), file, anchor);
		if (parameterEditor == null) return;
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(parameterEditor.getDocument());
		TemplateManager.getInstance(file.getProject()).startTemplate(parameterEditor, createTemplate(anchor, requires, file));
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(parameterEditor.getDocument());
	}

	private PsiElement findAnchor(List<Constraint.Parameter> requires) {
		return requires.isEmpty() || requires.get(0).aspect().isEmpty() ? findAnchor((TaraNode) parametrized) : findAnchor((TaraAspectApply) ((Node) parametrized).appliedAspects().stream().filter(f -> f.type().equals(requires.get(0).aspect())).findFirst().get());
	}

	private PsiElement findAnchor(TaraNode node) {
		if (!hasParameters(node)) {
			final PsiElement emptyParameters = TaraElementFactory.getInstance(node.getProject()).createEmptyParameters();
			return node.getSignature().addAfter(emptyParameters, anchor(node)).getFirstChild();
		} else {
			final List<Parameter> parameters = node.getSignature().getParameters().getParameters();
			return (PsiElement) parameters.get(parameters.size() - 1);
		}
	}

	private PsiElement anchor(TaraNode node) {
		return node.getSignature().getMetaIdentifier() != null && node.getSignature().getMetaIdentifier().getNextSibling() instanceof TaraRuleContainer ?
				node.getSignature().getMetaIdentifier().getNextSibling() :
				node.getSignature().getMetaIdentifier();
	}

	private PsiElement findAnchor(TaraAspectApply apply) {
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
		final TaraAspectApply aspectApply = TaraPsiUtil.getContainerByType(element, TaraAspectApply.class);
		return aspectApply != null ? hasParameters(aspectApply) : hasParameters(TaraPsiUtil.getContainerByType(element, TaraNode.class));
	}

	private boolean hasParameters(TaraAspectApply apply) {
		return apply.getParameters() != null && !apply.getParameters().getParameters().isEmpty();
	}

	private boolean hasParameters(TaraNode node) {
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
		return !hasParameters(anchor) && (text.length() > 0) ? text.substring(2) : text.toString();
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
		return true;
	}
}
