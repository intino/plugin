// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import com.intellij.openapi.util.Iconable;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiNamedElement;
import io.intino.magritte.lang.model.Variable;
import org.jetbrains.annotations.Nullable;

public interface TaraVariable extends TaraPsiElement, Variable, Valued, Iconable, Navigatable, PsiNamedElement {

	@Nullable
	TaraBodyValue getBodyValue();

	@Nullable
	TaraDoc getDoc();

	@Nullable
	TaraFlags getFlags();

	@Nullable
	TaraIdentifier getIdentifier();

	@Nullable
	TaraRuleContainer getRuleContainer();

	@Nullable
	TaraSizeRange getSizeRange();

	@Nullable
	TaraValue getValue();

	@Nullable
	TaraVariableType getVariableType();

}
