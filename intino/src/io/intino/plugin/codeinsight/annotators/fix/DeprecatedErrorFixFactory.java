package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.fix.deprecated.ChangeByDslDeclarationFix;
import io.intino.plugin.errorreporting.TaraRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeprecatedErrorFixFactory {

	private static final Map<String, Class<? extends IntentionAction>[]> fixes = new HashMap<>();

	static {
		fixes.put("deprecated.mogram", new Class[]{ChangeByDslDeclarationFix.class});
	}

	private DeprecatedErrorFixFactory() {
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
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
				 IllegalAccessException e) {
			List<String> classNames = new ArrayList<>();
			for (Class<? extends IntentionAction> aClass : classes) classNames.add(aClass.getSimpleName());
			throw new TaraRuntimeException("Fix couldn't be instantiated: " + String.join(", ", classNames) + ". " + e.getMessage(), e);
		}
	}
}
