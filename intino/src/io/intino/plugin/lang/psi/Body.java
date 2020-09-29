package io.intino.plugin.lang.psi;

import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.model.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Body extends TaraPsiElement {

	@NotNull
	List<? extends Variable> getVariableList();

	@NotNull
	List<? extends Parameter> getVarInitList();

	@NotNull
	List<? extends Node> getNodeList();

	List<Node> getNodeLinks();

	List<PsiElement> getStatements();

}

