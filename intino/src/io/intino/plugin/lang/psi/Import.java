package io.intino.plugin.lang.psi;

import com.intellij.openapi.util.Iconable;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;

public interface Import extends Navigatable, Iconable, TaraPsiElement {

	@NotNull
	TaraHeaderReference getHeaderReference();

}
