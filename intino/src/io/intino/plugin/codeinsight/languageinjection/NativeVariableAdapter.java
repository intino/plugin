package io.intino.plugin.codeinsight.languageinjection;

import com.intellij.openapi.module.Module;
import io.intino.itrules.Adapter;
import io.intino.itrules.FrameBuilderContext;
import io.intino.plugin.lang.psi.TaraVariable;
import io.intino.tara.Language;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.Tag;
import io.intino.tara.language.model.Variable;

class NativeVariableAdapter implements Adapter<Variable> {

	private NativeFormatter formatter;

	NativeVariableAdapter(Module module, String generatedLanguage, Language language) {
		formatter = new NativeFormatter(module, generatedLanguage, language);
	}

	@Override
	public void adapt(Variable source, FrameBuilderContext context) {
		if (source.type() == null) return;
		context.add(source.type().getName());
		for (Tag tag : source.flags()) context.add(tag.name().toLowerCase());
		createFrame(context, source);
	}

	private void createFrame(FrameBuilderContext context, final Variable variable) {
		if (variable.name() == null || variable.values() == null || variable.values().isEmpty() || !(variable.values().get(0) instanceof Primitive.Expression))
			return;
		final Primitive.Expression body = (Primitive.Expression) variable.values().get(0);
		if (Primitive.FUNCTION.equals(variable.type()))
			formatter.fillFrameForNativeVariable(context, variable, ((TaraVariable) variable).getBodyValue() != null);
		else
			formatter.fillFrameExpressionVariable(context, variable, body.get(), ((TaraVariable) variable).getBodyValue() != null);
	}
}