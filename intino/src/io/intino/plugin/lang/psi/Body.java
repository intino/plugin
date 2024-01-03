package io.intino.plugin.lang.psi;

import com.intellij.psi.PsiElement;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Body extends TaraPsiElement {

	@NotNull
	List<? extends Variable> getVariableList();

	@NotNull
	List<? extends Parameter> getVarInitList();

	@NotNull
	List<? extends Mogram> getMogramList();

	List<Mogram> getNodeLinks();

	List<PsiElement> getStatements();

}

