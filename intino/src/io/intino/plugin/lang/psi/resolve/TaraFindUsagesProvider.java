package io.intino.plugin.lang.psi.resolve;

import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.model.Variable;
import io.intino.plugin.lang.lexer.TaraLexerAdapter;
import io.intino.plugin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerByType;

public class TaraFindUsagesProvider implements FindUsagesProvider {
	private static final String ANONYMOUS = "Anonymous";

	@Nullable
	@Override
	public WordsScanner getWordsScanner() {
		return new DefaultWordsScanner(new TaraLexerAdapter(),
				TokenSet.create(TaraTypes.IDENTIFIER),
				TokenSet.create(TaraTypes.DOC, TaraTypes.DOC_LINE), TokenSet.EMPTY);
	}

	@Override
	public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
		return psiElement instanceof Identifier || psiElement instanceof IdentifierReference;
	}

	@Nullable
	@Override
	public String getHelpId(@NotNull PsiElement psiElement) {
		return HelpID.FIND_OTHER_USAGES;
	}

	@NotNull
	@Override
	public String getType(@NotNull PsiElement element) {
		if (getContainerByType(element, Variable.class) != null) return "variable";
		else if (getContainerByType(element, Parameter.class) != null) return "parameter";
		else if (element.getParent() instanceof Signature) return getContainerByType(element, TaraNode.class).type();
		return "reference";
	}

	@NotNull
	@Override
	public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
		return getDescriptiveName(element);
	}

	@NotNull
	@Override
	public String getDescriptiveName(@NotNull PsiElement element) {
		if (element instanceof TaraNode) {
			String name = ((Node) element).name();
			return name == null ? ANONYMOUS : name;
		} else if (element instanceof Identifier) return element.getText();
		else if (element instanceof TaraModel) return ((TaraModel) element).getName();
		return element.getText();
	}
}