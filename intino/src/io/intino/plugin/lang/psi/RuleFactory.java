package io.intino.plugin.lang.psi;

import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.impl.PsiCustomWordRule;
import io.intino.plugin.lang.psi.impl.TaraIdentifierImpl;
import io.intino.plugin.lang.psi.impl.TaraMetricImpl;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.Tag;
import io.intino.tara.language.model.rules.variable.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RuleFactory {


	@SuppressWarnings("ConstantConditions")
	public static VariableRule<?> createRule(TaraVariable variable) {
		final TaraRule rule = variable.getRuleContainer().getRule();
		if (rule.isLambda() || variable.type().equals(Primitive.OBJECT)) return createLambdaRule(variable.type(), rule);
		else if (variable.type().equals(Primitive.FUNCTION) || variable.flags().contains(Tag.Reactive))
			return new NativeRule(rule.getText(), "", Collections.emptyList());
		else if (variable.type().equals(Primitive.OBJECT))
			return new NativeObjectRule(rule.getText());
		else return new PsiCustomWordRule(rule.getText(), variable);
	}

	@Nullable
	private static VariableRule<?> createLambdaRule(Primitive type, TaraRule rule) {
		final List<PsiElement> parameters = Arrays.asList(rule.getChildren());
		return switch (type) {
			case DOUBLE -> createDoubleRule(rule);
			case INTEGER -> createIntegerRule(rule);
			case STRING -> {
				final String value = valueOf(parameters, Collections.singletonList(StringValue.class));
				yield new StringRule(value.isEmpty() ? "" : value.substring(1, value.length() - 1));
			}
			case RESOURCE -> new FileRule(valuesOf(parameters));
			case FUNCTION -> new NativeRule(parameters.get(0).getText(), "", Collections.emptyList());
			case WORD -> new WordRule(valuesOf(parameters));
			case OBJECT -> new NativeObjectRule(valuesOf(parameters).get(0));
			default ->
//			case REFERENCE: TODO
					null;
		};
	}

	private static VariableRule createIntegerRule(TaraRule rule) {
		return new IntegerRule(minOf(rule.getRange()).intValue(), maxOf(rule.getRange()).intValue(), valueOf(Arrays.asList(rule.getChildren()), Arrays.asList(TaraMetricImpl.class, TaraIdentifierImpl.class)));
	}

	private static VariableRule createDoubleRule(TaraRule rule) {
		return new DoubleRule(minOf(rule.getRange()), maxOf(rule.getRange()), valueOf(Arrays.asList(rule.getChildren()), Arrays.asList(TaraMetricImpl.class, TaraIdentifierImpl.class)));
	}

	private static Double minOf(TaraRange range) {
		if (range == null) return Double.NEGATIVE_INFINITY;
		final String min = range.getChildren()[0].getText();
		return min.equals("*") ? Double.NEGATIVE_INFINITY : Double.parseDouble(min);
	}

	private static Double maxOf(TaraRange range) {
		if (range == null) return Double.POSITIVE_INFINITY;
		final String max = range.getChildren()[range.getChildren().length - 1].getText();
		return max.equals("*") ? Double.POSITIVE_INFINITY : Double.parseDouble(max);
	}

	private static List<String> valuesOf(List<PsiElement> parameters) {
		return parameters.stream().map(PsiElement::getText).toList();
	}

	private static String valueOf(List<PsiElement> parameters, List<Class<? extends PsiElement>> classes) {
		PsiElement value = parameters.stream().filter(e -> classes.contains(e.getClass())).findFirst().orElse(null);
		return value == null ? "" : value.getText();
	}

}
