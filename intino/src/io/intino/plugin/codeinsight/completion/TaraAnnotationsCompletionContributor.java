package io.intino.plugin.codeinsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import io.intino.Configuration;
import io.intino.magritte.lang.model.Flags;
import io.intino.magritte.lang.model.Tag;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.module.IntinoModuleType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static io.intino.plugin.lang.psi.TaraTypes.*;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.module.ModuleProvider.moduleOf;


public class TaraAnnotationsCompletionContributor extends CompletionContributor {

	private PsiElementPattern.Capture<PsiElement> afterIs = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new TaraFilters.AfterIsFitFilter()));
	private PsiElementPattern.Capture<PsiElement> afterInto = psiElement().withLanguage(TaraLanguage.INSTANCE)
			.and(new FilterPattern(new TaraFilters.AfterIntoFitFilter()));

	public TaraAnnotationsCompletionContributor() {
		addAfterInto();
		addAfterIs();
	}

	private void addAfterIs() {
		extend(CompletionType.BASIC, afterIs, new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						final Module module = moduleOf(parameters.getOriginalFile());
						if (!IntinoModuleType.isIntino(module)) return;
						addTags(parameters, resultSet);
					}
				}
		);
	}

	private void addAfterInto() {
		extend(CompletionType.BASIC, afterInto, new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						final Module module = moduleOf(parameters.getOriginalFile());
						if (!IntinoModuleType.isIntino(module)) return;
						final Configuration.Artifact.Model.Level level = safe(() -> IntinoUtil.configurationOf(module).artifact().model().level());
						if (level == null || level.isSolution() || level.isProduct()) return;
						addTags(parameters, resultSet);
					}
				}
		);
	}

	private void addTags(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet resultSet) {
		PsiElement annotationContext = getContext(parameters.getPosition());
		if (annotationContext == null) return;
		IElementType elementType = annotationContext.getNode().getElementType();
		if (elementType.equals(IDENTIFIER_KEY) || elementType.equals(METAIDENTIFIER_KEY) || elementType.equals(SUB))
			addNodeTags(resultSet);
		else if (elementType.equals(HAS)) addHasAnnotations(resultSet);
		else addVariableAnnotations(resultSet);
	}

	private void addVariableAnnotations(@NotNull CompletionResultSet resultSet) {
		for (Tag annotation : Flags.forVariable())
			resultSet.addElement(decorate(LookupElementBuilder.create(annotation.name().toLowerCase())));
	}

	private void addHasAnnotations(@NotNull CompletionResultSet resultSet) {
		for (Tag annotation : Flags.forReference())
			resultSet.addElement(decorate(LookupElementBuilder.create(annotation.name().toLowerCase())));
	}

	private void addNodeTags(CompletionResultSet resultSet) {
		for (Tag tag : Flags.forRoot())
			resultSet.addElement(decorate(LookupElementBuilder.create(tag.name().toLowerCase())));
	}

	private LookupElementBuilder decorate(LookupElementBuilder builder) {
		return builder.withTypeText("flag", true).withIcon(IntinoIcons.MODEL_16);
	}

	private PsiElement getContext(PsiElement element) {
		PsiElement context = element.getContext();
		while ((context = context.getPrevSibling()) != null)
			if (isStartingToken(context) || (context.getPrevSibling() != null && isAfterBreakLine(context)))
				return context;
		return null;
	}

	private boolean isStartingToken(PsiElement context) {
		return is(context, VAR) || is(context, HAS) || is(context, SUB) || is(context, METAIDENTIFIER_KEY);
	}

	private boolean isAfterBreakLine(PsiElement context) {
		return is(context.getPrevSibling(), DEDENT) || is(context.getPrevSibling(), NEWLINE) || is(context.getPrevSibling(), NEW_LINE_INDENT) ||
				is(context.getPrevSibling(), IMPORTS) || is(context.getPrevSibling(), DSL_DECLARATION);
	}

	private boolean is(PsiElement context, IElementType type) {
		return context.getNode().getElementType().equals(type);
	}

}