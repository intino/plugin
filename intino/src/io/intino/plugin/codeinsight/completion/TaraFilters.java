package io.intino.plugin.codeinsight.completion;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.Nullable;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static io.intino.plugin.lang.psi.TaraTypes.*;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerNodeOf;


class TaraFilters {

	static final PsiElementPattern.Capture<PsiElement> AfterNewLine = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new AfterNewLinePrimalFilter()));
	static final PsiElementPattern.Capture<PsiElement> afterAs = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new AfterAsFilter()))
			.and(new FilterPattern(new InFacetFilter()));
	static final PsiElementPattern.Capture<PsiElement> afterNewLineInBody = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new AfterNewLineInBodyFilter())).andNot(afterAs);
	static final PsiElementPattern.Capture<PsiElement> afterEquals = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new AfterEqualsFilter()));
	static final PsiElementPattern.Capture<PsiElement> afterNodeIdentifier = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new AfterElementTypeFitFilter(IDENTIFIER_KEY)));
	static final PsiElementPattern.Capture<PsiElement> inParameterName = psiElement().withLanguage(TaraLanguage.INSTANCE).and(new FilterPattern(new InParameters())).andNot(afterEquals);

	private TaraFilters() {
	}

	private static boolean in(PsiElement context, Class<? extends PsiElement> container) {
		PsiElement parent = context.getParent();
		while (parent != null) {
			if (container.isInstance(parent)) return true;
			parent = parent.getParent();
		}
		return false;
	}

	private static boolean inAnnotations(PsiElement context) {
		PsiElement parent = context.getParent();
		while (parent != null) {
			if (parent instanceof TaraAnnotations) return true;
			parent = parent.getParent();
		}

		return false;
	}

	private static boolean previousNewLine(PsiElement context) {
		return context.getPrevSibling() != null && (is(context, TaraTypes.NEWLINE) || is(context, TaraTypes.IMPORTS) || is(context, TaraTypes.DSL_DECLARATION));
	}

	private static boolean previousNewLineIndent(PsiElement context) {
		return context.getPrevSibling() != null && is(context, TaraTypes.NEW_LINE_INDENT);
	}

	private static boolean is(PsiElement context, IElementType type) {
		return context.getPrevSibling() != null && context.getPrevSibling().getNode() != null && type.equals(context.getPrevSibling().getNode().getElementType());
	}

	private static class AfterElementTypeFitFilter implements ElementFilter {
		IElementType type;

		private AfterElementTypeFitFilter(IElementType type) {
			this.type = type;
		}

		public boolean isAcceptable(Object element, PsiElement context) {
			if (context == null) return false;
			PsiElement prevSibling = context.getPrevSibling() != null ? context.getPrevSibling() : context.getParent().getPrevSibling();
			if (prevSibling != null && prevSibling.getPrevSibling() != null) {
				PsiElement prevPrevSibling = prevSibling.getPrevSibling();
				if (element instanceof PsiElement) {
					if (prevSibling.getNode().getElementType() == TokenType.WHITE_SPACE && type.equals(prevPrevSibling.getNode().getElementType()))
						return true;
					else return type.equals(prevSibling.getNode().getElementType());
				}
			}
			return false;
		}

		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class AfterNewLineInBodyFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			return isElementAcceptable(element, context) && inBody(context) && afterNewLine(context);
		}

		private boolean afterNewLine(PsiElement context) {
			return context.getPrevSibling() == null && TaraFilters.in(context, Signature.class);
		}

		private boolean inBody(PsiElement context) {
			return context.getParent() instanceof MetaIdentifier && TaraFilters.in(context, Body.class) && !inAnnotations(context);
		}

		private boolean isElementAcceptable(Object element, PsiElement context) {
			return (element instanceof PsiElement) && context != null && context.getParent() != null;
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class AfterAsFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			return !isNotAcceptable(element, context) && TaraFilters.in(context, Signature.class) && afterAs(context);
		}

		private boolean afterAs(PsiElement context) {
			final PsiElement parent = context.getOriginalElement().getParent().getParent();
			return parent != null && hasPreviousAs(parent);
		}

		private boolean hasPreviousAs(PsiElement parent) {
			PsiElement prevSibling = parent.getPrevSibling();
			while (prevSibling != null) {
				if (is(prevSibling, TaraTypes.AS)) return true;
				prevSibling = prevSibling.getPrevSibling();
			}
			return false;
		}

		private boolean isNotAcceptable(Object element, PsiElement context) {
			return !(element instanceof PsiElement) || context == null || context.getParent() == null;
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class AfterEqualsFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			final PsiElement realContext = TaraPsiUtil.contextOf(context, Value.class);
			return isCandidate(element, realContext) && realContext.getPrevSibling().getPrevSibling() != null && isPreviousEquals(realContext);
		}

		private boolean isPreviousEquals(PsiElement context) {
			return TaraTypes.EQUALS.equals(context.getPrevSibling().getPrevSibling().getNode().getElementType());
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class AfterNewLinePrimalFilter implements ElementFilter {

		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			if (!(element instanceof PsiElement) || context == null || context.getParent() == null) return false;
			if (context.getParent() instanceof MetaIdentifier && !new AfterAsFilter().isAcceptable(element, context) && !inAnnotations(context)) {
				PsiElement contextOf = (PsiElement) getContainerNodeOf(context);
				if (contextOf == null || contextOf.getPrevSibling() == null) return false;
				return !TaraFilters.in(context, Body.class) && (previousNewLine(contextOf) || previousNewLineIndent(contextOf));
			}
			return false;
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static boolean isCandidate(Object element, @Nullable PsiElement context) {
		return element instanceof PsiElement && context != null && context.getPrevSibling() != null;
	}

	private static class InFacetFilter implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			return acceptableParent(element, context) && !(facet(context) && getContainerNodeOf(context) == null);
		}

		private boolean facet(PsiElement context) {
			return context.getParent() instanceof MetaIdentifier && !inAnnotations(context);
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static class InParameters implements ElementFilter {
		@Override
		public boolean isAcceptable(Object element, @Nullable PsiElement context) {
			return acceptableParent(element, context) && (parameter(context) && getContainerNodeOf(context) != null);
		}

		private boolean parameter(PsiElement context) {
			return in(context, TaraParameter.class);
		}

		@Override
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	static class AfterIsFitFilter implements ElementFilter {

		public boolean isAcceptable(Object element, PsiElement context) {
			if (context == null) return false;
			PsiElement ctx = (context.getPrevSibling() != null) ? context : context.getParent();
			while (ctx.getPrevSibling() != null && !IDENTIFIER_KEY.equals(ctx.getPrevSibling().getNode().getElementType())) {
				if (IS.equals(ctx.getNode().getElementType())) return true;
				ctx = ctx.getPrevSibling();
			}
			ctx = ctx.getParent();
			while (ctx != null && !(ctx instanceof Mogram)) {
				if (ctx instanceof TaraFlags) return true;
				ctx = ctx.getParent();
			}
			return false;
		}

		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}

	}

	static class AfterIntoFitFilter implements ElementFilter {

		public boolean isAcceptable(Object element, PsiElement context) {
			if (context == null) return false;
			PsiElement ctx = (context.getPrevSibling() != null) ? context : context.getParent();
			while (ctx.getPrevSibling() != null && !IDENTIFIER_KEY.equals(ctx.getPrevSibling().getNode().getElementType())) {
				if (INTO.equals(ctx.getNode().getElementType())) return true;
				ctx = ctx.getPrevSibling();
			}
			ctx = ctx.getParent();
			while (ctx != null && !(ctx instanceof Mogram)) {
				if (ctx instanceof TaraAnnotations) return true;
				ctx = ctx.getParent();
			}
			return false;
		}

		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}

	private static boolean acceptableParent(Object element, @Nullable PsiElement context) {
		return element instanceof PsiElement && context != null && context.getParent() != null;
	}
}
