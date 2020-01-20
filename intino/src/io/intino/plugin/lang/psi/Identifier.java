package io.intino.plugin.lang.psi;

import com.intellij.openapi.util.Iconable;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;

public interface Identifier extends Navigatable, Iconable, TaraPsiElement, PsiNamedElement, PsiNameIdentifierOwner {

}
