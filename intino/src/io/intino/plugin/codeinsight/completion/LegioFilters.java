package io.intino.plugin.codeinsight.completion;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import io.intino.legio.Artifact.Boxing;
import io.intino.legio.Artifact.Generation;
import io.intino.legio.level.LevelArtifact.Modeling.Language;
import io.intino.tara.Checker;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticFatalException;
import io.intino.tara.plugin.lang.TaraLanguage;
import io.intino.tara.plugin.lang.psi.TaraModel;
import io.intino.tara.plugin.lang.psi.impl.TaraPsiImplUtil;
import org.jetbrains.annotations.Nullable;
import tara.dsl.Legio;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static io.intino.tara.plugin.lang.psi.impl.TaraPsiImplUtil.getContainerByType;

class LegioFilters {

	static final PsiElementPattern.Capture<PsiElement> inLanguageName = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InLanguageNameFilter()));
	static final PsiElementPattern.Capture<PsiElement> inLanguageVersion = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InLanguageVersionFilter()));
	static final PsiElementPattern.Capture<PsiElement> inBoxingLanguage = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InBoxingLanguageFilter()));
	static final PsiElementPattern.Capture<PsiElement> inBoxingVersion = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InBoxingVersionFilter()));
	static final PsiElementPattern.Capture<PsiElement> inGenerationVersion = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new InGenerationVersionFilter()));


	private static class InLanguageNameFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiImplUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inLanguageNode(node) && inParameter(context, "name");
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
			return isElementAcceptable(element, context) && inLanguageNode(node) && inParameter(context, "version");
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}

	}

	private static class InBoxingLanguageFilter implements ElementFilter {

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiImplUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inBoxingNode(node) && inParameter(context, "language");
		}

		private static boolean inBoxingNode(Node node) {
			return node.type().equals(Boxing.class.getSimpleName()) || node.type().equals(typeName(Boxing.class));
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}

	}

	private static class InBoxingVersionFilter implements ElementFilter {

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiImplUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inBoxingNode(node) && inParameter(context, "version");
		}

		private static boolean inBoxingNode(Node node) {
			return node.type().equals(Boxing.class.getSimpleName()) || node.type().equals(typeName(Boxing.class));
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}

	}

	private static class InGenerationVersionFilter implements ElementFilter {

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final Node node = TaraPsiImplUtil.getContainerNodeOf(context);
			if (node == null) return false;
			check(node);
			return isElementAcceptable(element, context) && inGenerationNode(node) && inParameter(context, "version");
		}

		private static boolean inGenerationNode(Node node) {
			return node.type().equals(Generation.class.getSimpleName()) || node.type().equals(typeName(Generation.class));
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
		return element instanceof PsiElement && context.getParent() != null && file instanceof TaraModel && ((TaraModel) file).dsl().equals(Legio.class.getSimpleName());
	}

	private static boolean inLanguageNode(Node node) {
		return node.type().equals(Language.class.getSimpleName()) || node.type().equals(typeName(Language.class));
	}

	private static boolean inParameter(PsiElement context, String parameterName) {
		final Parameter parameter = getContainerByType(context, Parameter.class);
		return parameter != null && parameter.name().equals(parameterName);
	}
}
