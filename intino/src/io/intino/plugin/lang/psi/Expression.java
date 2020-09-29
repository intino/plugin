package io.intino.plugin.lang.psi;

import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

public interface Expression extends MultilineValue, NavigatablePsiElement, TaraPsiElement, PsiLanguageInjectionHost {

	boolean isValidHost();

	@NotNull
	LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper();

	String getValue();

	PsiLanguageInjectionHost updateText(@NotNull String text);
}
