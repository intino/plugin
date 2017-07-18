package io.intino.plugin.codeinsight.completion;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import io.intino.legio.Artifact.Box;
import io.intino.legio.level.LevelArtifact.Model;
import io.intino.tara.Checker;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticFatalException;
import io.intino.tara.plugin.lang.TaraLanguage;
import io.intino.tara.plugin.lang.psi.StringValue;
import io.intino.tara.plugin.lang.psi.TaraModel;
import io.intino.tara.plugin.lang.psi.impl.TaraPsiImplUtil;
import org.jetbrains.annotations.Nullable;
import tara.dsl.Legio;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static io.intino.tara.plugin.lang.psi.impl.TaraPsiImplUtil.getContainerByType;

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


	private static class InLanguageNameFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiImplUtil.getContainerNodeOf(context);
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
			final Node node = TaraPsiImplUtil.getContainerNodeOf(context);
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

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiImplUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inBoxNode(node) && inParameter(context, "language");
		}

		private static boolean inBoxNode(Node node) {
			return node.type().equals(Box.class.getSimpleName()) || node.type().equals(typeName(Box.class));
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}

	}

	private static class InBoxVersionFilter implements ElementFilter {

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiImplUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inBoxNode(node) &&
					(inParameter(context, "version") || inParameter(context, "sdk"));
		}

		private static boolean inBoxNode(Node node) {
			return node.type().equals(Box.class.getSimpleName()) || node.type().equals(typeName(Box.class));
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}

	}

	private static class InSDKVersionFilter implements ElementFilter {

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiImplUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inModelNode(node) && inParameter(context, "sdk");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}

	}

	private static void check(Node node) {
		try {
			new Checker(new Legio()).check(node);
		} catch (SemanticFatalException ignored) {
		}
	}

	private static String typeName(Class aClass) {
		return aClass.getCanonicalName().replace(aClass.getPackage().getName() + ".", "");
	}

	private static boolean isElementAcceptable(Object element, PsiElement context) {
		final PsiFile file = context.getContainingFile().getOriginalFile();
		return element instanceof PsiElement && context.getParent() != null && file instanceof TaraModel && Legio.class.getSimpleName().equals(((TaraModel) file).dsl());
	}

	private static boolean inModelNode(Node node) {
		final String type = node.type().replace(":", "");
		return type.equals(Model.class.getSimpleName()) || type.equals(typeName(Model.class));
	}

	private static boolean inParameter(PsiElement context, String parameterName) {
		final StringValue value = getContainerByType(context, StringValue.class);
		if (value == null) return false;
		final Parameter parameter = getContainerByType(value, Parameter.class);
		return parameter != null && parameter.name().equals(parameterName);
	}
}
