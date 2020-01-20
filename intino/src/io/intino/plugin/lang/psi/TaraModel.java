package io.intino.plugin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.NodeRoot;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TaraModel extends NodeRoot, PsiFile {

	@NotNull
	PsiElement addNode(@NotNull Node node) throws IncorrectOperationException;

	Node addNode(String identifier);

	Import addImport(String reference);

	@NotNull
	String getPresentableName();

	String dsl();

	@NotNull
	List<Import> getImports();

	TaraDslDeclaration getDSLDeclaration();

	void updateDSL(String dsl);

	@Override
	default void stashNodeName(String name) {
	}
}
