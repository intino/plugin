package io.intino.plugin.codeinsight.spelling;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.spellchecker.tokenizer.TokenizerBase;
import io.intino.plugin.lang.psi.Identifier;
import org.jetbrains.annotations.NotNull;

public class TaraSpellcheckerStrategy extends SpellcheckingStrategy {

	private final Tokenizer<Identifier> myIdentifierTokenizer = TokenizerBase.create(PlainTextSplitter.getInstance());

	@NotNull
	@Override
	public Tokenizer getTokenizer(PsiElement element) {
		if (element instanceof Identifier) return myIdentifierTokenizer;
		return super.getTokenizer(element);
	}
}
