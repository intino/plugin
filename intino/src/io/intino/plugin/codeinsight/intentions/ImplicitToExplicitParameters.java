package io.intino.plugin.codeinsight.intentions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.semantics.Constraint;
import io.intino.plugin.lang.psi.Parameters;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImplicitToExplicitParameters extends ParametersIntentionAction {
	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final List<Constraint> allowsOf = IntinoUtil.getConstraintsOf(TaraPsiUtil.getContainerNodeOf(element));
		if (allowsOf == null) return;
		Parameters parameters = getParametersScope(element);
		Map<String, String> explicit = extractParametersData(parameters.getParameters());
		if (explicit.size() != parameters.getParameters().size()) return;
		parameters.replace(TaraElementFactory.getInstance(project).createExplicitParameters(explicit));
	}

	private Map<String, String> extractParametersData(List<Parameter> parameters) {
		Map<String, String> map = new LinkedHashMap<>();
		for (Parameter parameter : parameters)
			map.put(parameter.name(), ((Valued) parameter).getValue().getText());
		return map;
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		Parameters parametersScope = getParametersScope(element);
		return element.isWritable() && parametersScope != null && !parametersScope.getParameters().isEmpty() && !parametersScope.areExplicit();
	}

	@NotNull
	public String getText() {
		return "To explicit parameters";
	}
}
