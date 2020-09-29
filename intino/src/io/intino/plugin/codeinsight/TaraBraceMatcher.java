package io.intino.plugin.codeinsight;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.lang.psi.TaraTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class TaraBraceMatcher implements PairedBraceMatcher {
	private final BracePair[] pairs;

	public TaraBraceMatcher() {
		pairs = new BracePair[]{
				new BracePair(TaraTypes.LEFT_PARENTHESIS, TaraTypes.RIGHT_PARENTHESIS, false),
				new BracePair(TaraTypes.LEFT_SQUARE, TaraTypes.RIGHT_SQUARE, false),
				new BracePair(TaraTypes.LEFT_CURLY, TaraTypes.RIGHT_CURLY, false),
				new BracePair(TaraTypes.QUOTE_BEGIN, TaraTypes.QUOTE_END, false),
				new BracePair(TaraTypes.EXPRESSION_BEGIN, TaraTypes.EXPRESSION_END, false),
		};
	}

	public BracePair[] getPairs() {
		return Arrays.copyOf(pairs, pairs.length);
	}

	public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType braceType, @Nullable IElementType tokenType) {
		return true;
	}

	public int getCodeConstructStart(final PsiFile file, int openingBraceOffset) {
		return openingBraceOffset;
	}
}