package io.intino.plugin.codeinsight.completion;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import io.intino.Configuration.Artifact.Dependency;
import io.intino.Configuration.Artifact.Dsl;
import io.intino.plugin.lang.psi.StringValue;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.Checker;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;
import org.jetbrains.annotations.Nullable;
import tara.dsl.Legio;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static io.intino.plugin.lang.TaraLanguage.INSTANCE;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerByType;

class LegioFilters {
	static final PsiElementPattern.Capture<PsiElement> inDslName = psiElement().withLanguage(INSTANCE)
			.and(new FilterPattern(new InDslNameFilter()));
	static final PsiElementPattern.Capture<PsiElement> inDslVersion = psiElement().withLanguage(INSTANCE)
			.and(new FilterPattern(new InDslVersionFilter()));
	static final PsiElementPattern.Capture<PsiElement> inDslBuilderVersion = psiElement().withLanguage(INSTANCE)
			.and(new FilterPattern(new InBuilderVersionFilter()));
	static final PsiElementPattern.Capture<PsiElement> inDependencyVersion = psiElement().withLanguage(INSTANCE)
			.and(new FilterPattern(new InDependencyVersionFilter()));

	private static void check(Mogram node) {
		try {
			new Checker(new Legio()).check(node);
		} catch (SemanticFatalException ignored) {
		}
	}

	private static String typeName(Class<?> aClass) {
		return aClass.getCanonicalName().replace(aClass.getPackage().getName() + ".Configuration.", "");
	}

	private static boolean isElementAcceptable(Object element, PsiElement context) {
		final PsiFile file = context.getContainingFile().getOriginalFile();
		return element instanceof PsiElement && context.getParent() != null && file instanceof TaraModel && Legio.class.getSimpleName().equals(((TaraModel) file).dsl());
	}

	private static boolean isNode(Mogram node, Class aClass) {
		String type = node.type();
		type = type.contains(".") ? type.substring(type.lastIndexOf(".") + 1) : type;
		return type.equals(aClass.getSimpleName()) || type.equals(typeName(aClass));
	}

	private static boolean inDependencyNode(Mogram node) {
		String type = node.type().replace(":", "");
		type = type.substring(type.lastIndexOf(".") + 1);
		return is(type, Dependency.Compile.class) || is(type, Dependency.Test.class) || is(type, Dependency.Provided.class) || is(type, Dependency.Runtime.class);
	}

	private static boolean is(String type, Class<?> aClass) {
		return type.equals(aClass.getSimpleName()) || type.equals(typeName(aClass));
	}

	private static boolean inParameter(PsiElement context, String parameterName) {
		final StringValue value = getContainerByType(context, StringValue.class);
		if (value == null) return false;
		final Parameter parameter = getContainerByType(value, Parameter.class);
		return parameter != null && parameter.name().equals(parameterName);
	}

	private static class InDslNameFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Mogram mogram = TaraPsiUtil.getContainerNodeOf(context);
			if (mogram == null) return false;
			check(mogram);
			return isElementAcceptable(element, context) && isNode(mogram, Dsl.class) && inParameter(context, "name");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class InDslVersionFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Mogram mogram = TaraPsiUtil.getContainerNodeOf(context);
			if (mogram == null) return false;
			check(mogram);
			return isElementAcceptable(element, context) && isNode(mogram, Dsl.class) && inParameter(context, "version");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class InBuilderVersionFilter implements ElementFilter {

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Mogram mogram = TaraPsiUtil.getContainerNodeOf(context);
			if (mogram == null) return false;
			check(mogram);
			return isElementAcceptable(element, context) && isNode(mogram, Dsl.Builder.class) && isNode(mogram.container(), Dsl.class) && inParameter(context, "version");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class InDependencyVersionFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Mogram node = TaraPsiUtil.getContainerNodeOf(context);
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