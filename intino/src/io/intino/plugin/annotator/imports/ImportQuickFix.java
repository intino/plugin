package io.intino.plugin.annotator.imports;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.structuralsearch.plugin.util.SmartPsiPointer;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ImportQuickFix implements LocalQuickFix, HighPriorityAction {

	private final String anImport;
	private final SmartPsiPointer file;

	ImportQuickFix(TaraModel fileDestiny, Node nodeToImport) {
		this.file = new SmartPsiPointer(fileDestiny);
		String fileName = new File(nodeToImport.file()).getName();
		anImport = fileName.substring(0, fileName.lastIndexOf('.'));
	}

	@NotNull
	public String getText() {
		return "Use '" + anImport + "'";
	}

	@NotNull
	public String getName() {
		return getText();
	}

	@NotNull
	public String getFamilyName() {
		return "Use";
	}


	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
		((TaraModel) file.getElement()).addImport(anImport);
	}


}