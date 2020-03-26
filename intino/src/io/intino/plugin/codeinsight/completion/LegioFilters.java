package io.intino.plugin.codeinsight.completion;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import io.intino.Configuration.Artifact.Box;
import io.intino.Configuration.Artifact.Dependency;
import io.intino.Configuration.Artifact.Model;
import io.intino.magritte.Checker;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.semantics.errorcollector.SemanticFatalException;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.psi.StringValue;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.Nullable;
import tara.dsl.Legio;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerByType;

class LegioFilters {
	static final PsiElementPattern.Capture<PsiElement> inModelLanguage = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InLanguageNameFilter()));
	static final PsiElementPattern.Capture<PsiElement> inLanguageVersion = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InLanguageVersionFilter()));
	static final PsiElementPattern.Capture<PsiElement> inBoxLanguage = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InBoxLanguageFilter()));
	static final PsiElementPattern.Capture<PsiElement> inBoxVersion = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InBoxVersionFilter()));
	static final PsiElementPattern.Capture<PsiElement> inSDKVersion = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InSDKVersionFilter()));
	static final PsiElementPattern.Capture<PsiElement> inDependencyVersion = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InDependencyVersionFilter()));

	private static void check(Node node) {
		try {
			new Checker(new Legio()).check(node);
		} catch (SemanticFatalException ignored) {
		}
	}

	private static String typeName(Class aClass) {
		return aClass.getCanonicalName().replace(aClass.getPackage().getName() + ".Configuration.", "");
	}

	private static boolean isElementAcceptable(Object element, PsiElement context) {
		final PsiFile file = context.getContainingFile().getOriginalFile();
		return element instanceof PsiElement && context.getParent() != null && file instanceof TaraModel && Legio.class.getSimpleName().equals(((TaraModel) file).dsl());
	}

	private static boolean inModelNode(Node node) {
		final String type = node.type().replace(":", "");
		return type.equals(Model.class.getSimpleName()) || type.equals(typeName(Model.class));
	}

	private static boolean inDependencyNode(Node node) {
		String type = node.type().replace(":", "");
		type = type.substring(type.lastIndexOf(".") + 1);
		return is(type, Dependency.Compile.class) || is(type, Dependency.Test.class) || is(type, Dependency.Provided.class) || is(type, Dependency.Runtime.class);
	}

	private static boolean is(String type, Class aClass) {
		return type.equals(aClass.getSimpleName()) || type.equals(typeName(aClass));
	}

	private static boolean inParameter(PsiElement context, String parameterName) {
		final StringValue value = getContainerByType(context, StringValue.class);
		if (value == null) return false;
		final Parameter parameter = getContainerByType(value, Parameter.class);
		return parameter != null && parameter.name().equals(parameterName);
	}

	private static class InLanguageNameFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inModelNode(node) && inParameter(context, "language");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class InLanguageVersionFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inModelNode(node) && inParameter(context, "version");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class InBoxLanguageFilter implements ElementFilter {

		private static boolean inBoxNode(Node node) {
			return node.type().equals(Box.class.getSimpleName()) || node.type().equals(typeName(Box.class));
		}

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inBoxNode(node) && inParameter(context, "language");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class InBoxVersionFilter implements ElementFilter {

		private static boolean inBoxNode(Node node) {
			return node.type().equals(Box.class.getSimpleName()) || node.type().equals(typeName(Box.class));
		}

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inBoxNode(node) &&
					(inParameter(context, "version") || inParameter(context, "sdk"));
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class InSDKVersionFilter implements ElementFilter {

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inModelNode(node) && inParameter(context, "sdk");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class InDependencyVersionFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inDependencyNode(node) && inParameter(context, "version");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}
}