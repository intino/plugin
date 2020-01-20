package io.intino.plugin.annotator.fix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiElement;
import io.intino.plugin.errorreporting.TaraRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixFactory {

	private static Map<String, Class<? extends IntentionAction>[]> fixes = new HashMap<>();

	static {
		fixes.put("reject.type.not.exists", new Class[]{RemoveElementFix.class});
		fixes.put("duplicated.dsl.declaration", new Class[]{ConfigureModuleFix.class});
		fixes.put("required.terminal.variable.redefine", new Class[]{RedefineFix.class});
		fixes.put("required.parameter.in.context", new Class[]{AddRequiredParameterFix.class});
		fixes.put("required.type.in.context", new Class[]{AddRequiredElementFix.class});
		fixes.put("reject.instance.reference.variable", new Class[]{RemoveElementFix.class});
		fixes.put("warning.node.name.starts.uppercase", new Class[]{ToLowerCaseInstanceFix.class});
		fixes.put("reject.duplicate.variable", new Class[]{RemoveElementFix.class});
		fixes.put("reject.duplicate.entries", new Class[]{RemoveElementFix.class});
		fixes.put("reject.duplicated.facet", new Class[]{RemoveElementFix.class});
		fixes.put("reject.sub.of.instance", new Class[]{RemoveElementFix.class});
		fixes.put("reject.other.parameter.in.context", new Class[]{RemoveElementFix.class});
		fixes.put("reject.native.signature.not.found", new Class[]{NavigateToInterfaceFix.class});
		fixes.put("reject.nonexisting.variable.rule", new Class[]{AddNativeRuleNameFix.class});
		fixes.put("warning.variable.name.starts.uppercase", new Class[]{LowerCaseVariableFix.class});
		fixes.put("reject.number.parameter.with.erroneous.metric", new Class[]{AddMetricFix.class});
		fixes.put("reject.node.with.required.aspect.not.found", new Class[]{AddRequiredAspectFix.class});
	}

	private FixFactory() {
	}

	public static IntentionAction[] get(String key, PsiElement element, String... parameters) {
		Class<? extends IntentionAction>[] classes = fixes.get(key);
		if (classes == null) return IntentionAction.EMPTY_ARRAY;
		return instanceFixes(classes, element, parameters);
	}

	private static IntentionAction[] instanceFixes(Class<? extends IntentionAction>[] classes, PsiElement element, String[] parameters) {
		try {
			List<IntentionAction> actions = new ArrayList<>();
			for (Class<? extends IntentionAction> aClass : classes) {
				IntentionAction intentionAction = aClass.getDeclaredConstructors()[0].getParameters().length == 2 ?
						aClass.getConstructor(PsiElement.class, String[].class).newInstance(element, parameters) :
						aClass.getConstructor(PsiElement.class).newInstance(element);
				actions.add(intentionAction);
			}
			return actions.toArray(new IntentionAction[0]);
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			List<String> classNames = new ArrayList<>();
			for (Class<? extends IntentionAction> aClass : classes) classNames.add(aClass.getSimpleName());
			throw new TaraRuntimeException("Fix couldn't be instantiated: " + String.join(", ", classNames) + ". " + e.getMessage(), e);
		}
	}

}
