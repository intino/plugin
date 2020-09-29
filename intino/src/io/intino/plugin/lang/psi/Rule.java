package io.intino.plugin.lang.psi;

import com.intellij.pom.Navigatable;

import java.util.List;

public interface Rule extends Navigatable, TaraPsiElement, io.intino.magritte.lang.model.Rule {

	boolean isLambda();

	List<TaraIdentifier> getIdentifierList();
}
