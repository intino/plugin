package io.intino.plugin.codeinsight.intentions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.Parameters;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.Constraint;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ExplicitToImplicitParameters extends ParametersIntentionAction {

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final List<Constraint> constraints = TaraUtil.getConstraintsOf(TaraPsiUtil.getContainerNodeOf(element));
		if (constraints == null) return;
		Parameters parameters = getParametersScope(element);
		Map<Integer, String> implicit = extractParametersData(parameters, constraints);
		if (implicit.size() != parameters.getParameters().size()) return;
		parameters.replace(TaraElementFactory.getInstance(project).createParameters(sort(implicit)));
	}

	private String[] sort(Map<Integer, String> map) {
		List<String> values = new ArrayList<>();
		List<Integer> sortedKeys = new ArrayList(map.keySet());
		Collections.sort(sortedKeys);
		values.addAll(sortedKeys.stream().map(map::get).collect(Collectors.toList()));
		return values.toArray(new String[values.size()]);
	}

	@SuppressWarnings("ConstantConditions")
	private Map<Integer, String> extractParametersData(Parameters parameters, List<Constraint> constraints) {
		Map<Integer, String> result = new HashMap<>();
		final List<Constraint.Parameter> parameterConstraints = filterParameterConstraints(parameters.isInFacet() != null ? TaraUtil.getConstraintsOf(parameters.isInFacet()) : constraints);
		for (Parameter parameter : parameters.getParameters()) {
			final Constraint.Parameter constraint = findCorrespondingAllow(parameterConstraints, parameter.name());
			if (constraint != null) result.put(constraint.position(), ((Valued) parameter).getValue().getText());
		}
		return result;
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		PsiElement parametersScope = getParametersScope(element);
		return element.isWritable() && parametersScope != null && ((Parameters) parametersScope).areExplicit();
	}

	@NotNull
	public String getText() {
		return "To implicit parameters";
	}
}
