package io.intino.plugin.codeinsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.util.ProcessingContext;
import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.lang.psi.MetaIdentifier;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInsight.lookup.LookupElementBuilder.create;

@SuppressWarnings("Convert2Diamond")
public class TaraNodeCompletionContributor extends CompletionContributor {

	public TaraNodeCompletionContributor() {
		bodyCompletion();
		newLine();
		afterAs();
		afterIdentifier();
		parameterNames();
	}

	private void bodyCompletion() {
		extend(CompletionType.BASIC, TaraFilters.afterNewLineInBody, new BodyCompletionProvider());
	}

	private void newLine() {
		extend(CompletionType.BASIC, TaraFilters.AfterNewLine,
				new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						if (!(parameters.getPosition().getContext() instanceof MetaIdentifier)) return;
						final CompletionUtils completionUtils = new CompletionUtils(parameters, resultSet);
						completionUtils.collectAllowedComponents();
					}
				}
		);
	}

	private void afterAs() {
		extend(CompletionType.BASIC, TaraFilters.afterAs,
				new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						if (!(parameters.getPosition().getContext() instanceof MetaIdentifier)) return;
						final CompletionUtils completionUtils = new CompletionUtils(parameters, resultSet);
						completionUtils.collectAllowedAspects();
					}
				}
		);
	}

	private void afterIdentifier() {
		extend(CompletionType.BASIC, TaraFilters.afterNodeIdentifier,
				new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						resultSet.addElement(create("extends "));
						resultSet.addElement(create("is "));
						resultSet.addElement(create("into "));
					}
				}
		);
	}

	private void parameterNames() {
		extend(CompletionType.BASIC, TaraFilters.inParameterName,
				new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						if (!(parameters.getPosition().getContext() instanceof Identifier)) return;
						final CompletionUtils completionUtils = new CompletionUtils(parameters, resultSet);
						completionUtils.collectSignatureParameters();
					}
				}
		);
	}

}