package io.intino.plugin.codeinsight.intentions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.NodeContainer;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.semantics.Constraint;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ToBodyParameters extends ParametersIntentionAction {
	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final List<Constraint> allowsOf = IntinoUtil.getConstraintsOf(TaraPsiUtil.getContainerNodeOf(element));
		if (allowsOf == null) return;
		Parameters parameters = getParametersScope(element);
		Map<String, String> parametersData = extractParametersData(parameters.getParameters());
		TaraParameter parameter = TaraPsiUtil.getContainerByType(element, TaraParameter.class);
		if (parameter == null || parameter.name() == null) return;
		NodeContainer container = TaraPsiUtil.getContainerByType(parameter, NodeContainer.class);
		if (container == null) return;
		PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		parametersData.remove(parameter.name());
		final TaraElementFactory factory = TaraElementFactory.getInstance(project);
		final TaraVarInit varInit = (TaraVarInit) factory.createVarInit(parameter.name(), parameter.getValue().getText());
		if (varInit == null) return;
		final boolean body = hasBody(container);
		if (body) addNewLine((PsiElement) container);
		else addNewLineIndent((PsiElement) container);
		((PsiElement) container).add(varInit.copy());
		if (parametersData.isEmpty()) parameters.delete();
		else {
			Parameters explicitParameters = factory.createExplicitParameters(parametersData);
			if (explicitParameters != null) parameters.replace(explicitParameters);
		}
		PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
	}

	private Map<String, String> extractParametersData(List<Parameter> parameters) {
		Map<String, String> map = new LinkedHashMap<>();
		for (Parameter parameter : parameters)
			map.put(parameter.name(), ((Valued) parameter).getValue().getText());
		return map;
	}

	private boolean hasBody(NodeContainer container) {
		return container instanceof TaraNode && ((TaraNode) container).getBody() != null;
	}

	private void addNewLine(PsiElement node) {
		node.add(TaraElementFactory.getInstance(node.getProject()).createBodyNewLine(TaraPsiUtil.getIndentation(node) + 1));
	}

	private void addNewLineIndent(PsiElement container) {
		container.add(TaraElementFactory.getInstance(container.getProject()).createNewLineIndent(TaraPsiUtil.getIndentation(container) + 1));
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		Parameters parametersScope = getParametersScope(element);
		return element.isWritable() && parametersScope != null && !parametersScope.getParameters().isEmpty();
	}

	@NotNull
	public String getText() {
		return "Move to body";
	}
}
