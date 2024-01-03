package io.intino.plugin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramRoot;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TaraModel extends MogramRoot, PsiFile {

	@NotNull
	PsiElement addNode(@NotNull Mogram mogram) throws IncorrectOperationException;

	Mogram addNode(String identifier);

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
