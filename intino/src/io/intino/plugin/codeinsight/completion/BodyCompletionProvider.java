package io.intino.plugin.codeinsight.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import io.intino.magritte.Checker;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.semantics.errorcollector.SemanticFatalException;
import io.intino.plugin.lang.psi.MetaIdentifier;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInsight.lookup.LookupElementBuilder.create;
import static io.intino.magritte.lang.model.Tag.Instance;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerNodeOf;

class BodyCompletionProvider extends CompletionProvider<CompletionParameters> {


	BodyCompletionProvider() {
	}

	public void addCompletions(@NotNull CompletionParameters parameters,
							   ProcessingContext context,
							   @NotNull CompletionResultSet resultSet) {
		if (!(parameters.getPosition().getContext() instanceof MetaIdentifier)) return;
		final CompletionUtils completionUtils = new CompletionUtils(parameters, resultSet);
		completionUtils.collectAllowedComponents();
		completionUtils.collectBodyParameters();
		if (!isDeclaration(getContainerNodeOf(parameters.getPosition().getContext()))) addKeywords(resultSet);
	}

	private boolean isDeclaration(Node node) {
		final Node container = check((PsiElement) node);
		return container != null && (node.is(Instance) || container.is(Instance));
	}

	private Node check(PsiElement node) {
		Checker checker = new Checker(IntinoUtil.getLanguage(node));
		final Node container = getContainerNodeOf(node);
		if (container == null) return null;
		try {
			checker.check(container);
		} catch (SemanticFatalException ignored) {
		}
		return container;
	}

	private void addKeywords(CompletionResultSet resultSet) {
		resultSet.addElement(create("has "));
		resultSet.addElement(create("sub "));
		resultSet.addElement(create("var "));
	}
}
