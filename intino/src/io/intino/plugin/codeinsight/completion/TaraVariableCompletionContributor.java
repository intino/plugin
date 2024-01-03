package io.intino.plugin.codeinsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.util.ProcessingContext;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.PsiCustomWordRule;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.Variable;
import io.intino.tara.language.model.rules.Suggestion;
import io.intino.tara.language.model.rules.variable.WordRule;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInsight.lookup.LookupElementBuilder.create;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.tara.language.model.Primitive.*;


public class TaraVariableCompletionContributor extends CompletionContributor {

	public TaraVariableCompletionContributor() {
		extend(CompletionType.BASIC, psiElement()
				.withLanguage(TaraLanguage.INSTANCE)
				.and(new FilterPattern(new AfterVarFitFilter())),
				new CompletionProvider<>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   @NotNull ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						for (Primitive primitive : Primitive.getPrimitives())
							resultSet.addElement(create(primitive.getName().toLowerCase() + (mustHaveContract(primitive) ? ":" :
									" ")).withTypeText(Primitive.class.getSimpleName()));
					}
				}
		);

		extend(CompletionType.BASIC, TaraFilters.afterEquals,
				new CompletionProvider<>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   @NotNull ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						final Valued valued = TaraPsiUtil.contextOf(parameters.getOriginalPosition(), Valued.class);
						if (valued == null) return;
						if (valued instanceof Variable && WORD.equals(valued.type())) {
							if (valued.rule() instanceof WordRule)
								((WordRule) valued.rule()).words().forEach(w -> resultSet.addElement(create(w)));
							else
								((PsiCustomWordRule) valued.rule()).words().forEach(w -> resultSet.addElement(create(w)));
						} else if (valued instanceof Parameter && REFERENCE.equals(valued.type()) && !(parameters.getPosition().getParent() instanceof StringValue))
							resultSet.addElement(create("empty"));
						else if (BOOLEAN.equals(valued.type())) {
							resultSet.addElement(create("true"));
							resultSet.addElement(create("false"));
						} else if (STRING.equals(valued.type()) && valued.rule() instanceof Suggestion)
							((Suggestion) valued.rule()).suggestedValues().forEach(v -> resultSet.addElement(create("\"" + v + "\"")));
					}
				}
		);
	}

	private boolean mustHaveContract(Primitive primitive) {
		return FUNCTION.equals(primitive);
	}

	private static class AfterVarFitFilter implements ElementFilter {
		public boolean isAcceptable(Object element, PsiElement context) {
			if (element instanceof PsiElement && isInAttribute(context)) {
				PsiElement parent = getVariableType(context);
				if (parent == null) return false;
				if (parent.getPrevSibling() == null || parent.getPrevSibling().getPrevSibling() == null) return false;

				final ASTNode ctxPreviousNode = parent.getPrevSibling().getPrevSibling().getNode();
				return TaraTypes.VAR.equals(ctxPreviousNode.getElementType());
			}
			return false;
		}

		private boolean isInAttribute(PsiElement context) {
			PsiElement parent = context.getParent();
			if (parent instanceof TaraModel) {
				final ASTNode ctxPreviousNode = safe(() -> context.getPrevSibling().getPrevSibling().getNode());
				return ctxPreviousNode != null && TaraTypes.VAR.equals(ctxPreviousNode.getElementType());
			} else while (parent != null && !(parent instanceof Mogram)) {
				if (parent instanceof Variable) return true;
				parent = parent.getParent();
			}
			return false;
		}

		TaraVariableType getVariableType(PsiElement element) {
			PsiElement parent = element.getParent();
			while (parent != null && !(parent instanceof Mogram)) {
				if (parent instanceof TaraVariableType) return (TaraVariableType) parent;
				parent = parent.getParent();
			}
			return null;
		}

		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}
	}
}