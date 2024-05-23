package io.intino.plugin.itrules.highlighting;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.itrules.lang.psi.ItrulesTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItrulesBraceMatcher implements PairedBraceMatcher {
    private final BracePair[] pairs;

    public ItrulesBraceMatcher() {
        pairs = new BracePair[]{
                new BracePair(ItrulesTypes.LEFT_EXPR, ItrulesTypes.RIGHT_EXPR, false),
        };
    }

    public BracePair[] getPairs() {
        return pairs;
    }

    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType braceType, @Nullable IElementType tokenType) {
        return true;
    }

    public int getCodeConstructStart(final PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}