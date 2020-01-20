package io.intino.plugin.codeinsight.languageinjection;

import com.intellij.openapi.module.Module;
import io.intino.itrules.Adapter;
import io.intino.itrules.FrameBuilderContext;
import io.intino.plugin.lang.psi.Expression;
import io.intino.plugin.lang.psi.TaraVarInit;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.Language;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.model.Primitive;
import io.intino.tara.lang.semantics.Constraint;

import static io.intino.tara.lang.model.Primitive.FUNCTION;

class NativeParameterAdapter implements Adapter<Parameter> {

	private final NativeFormatter formatter;

	NativeParameterAdapter(Module module, String generatedLanguage, Language language) {
		this.formatter = new NativeFormatter(module, generatedLanguage, language);
	}

	@Override
	public void adapt(Parameter source, FrameBuilderContext context) {
		if (source.type() == null) return;
		context.add(source.type().getName());
		source.flags().stream().map(tag -> tag.name().toLowerCase()).forEach(context::add);
		final Constraint.Parameter constraint = TaraUtil.parameterConstraintOf(source);
		if (constraint != null)
			constraint.flags().stream().map(tag -> tag.name().toLowerCase()).forEach(context::add);
		createFrame(context, source);
	}

	private void createFrame(FrameBuilderContext context, final Parameter parameter) {
		createNativeFrame(context, parameter);
	}

	private void createNativeFrame(FrameBuilderContext context, Parameter parameter) {
		if (parameter.values() == null || parameter.values().isEmpty() || !(parameter.values().get(0) instanceof Primitive.Expression))
			return;
		final Expression expression = ((Valued) parameter).getBodyValue() != null ? ((Valued) parameter).getBodyValue().getExpression() : ((Valued) parameter).getValue().getExpressionList().get(0);
		if (expression == null) return;
		String value = expression.getValue();
		if (FUNCTION.equals(parameter.type()))
			formatter.fillFrameForFunctionParameter(context, parameter, value, parameter instanceof TaraVarInit && ((TaraVarInit) parameter).getBodyValue() != null);
		else
			formatter.fillFrameExpressionParameter(context, parameter, value, parameter instanceof TaraVarInit && ((TaraVarInit) parameter).getBodyValue() != null);
	}
}