package io.intino.plugin.codeinsight.annotators.imports;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ImportQuickFix implements LocalQuickFix, HighPriorityAction {

	private final String anImport;
	private final SmartPsiElementPointer<TaraModel> file;

	ImportQuickFix(TaraModel fileDestiny, Node nodeToImport) {
		this.file = SmartPointerManager.getInstance(fileDestiny.getProject()).createSmartPsiElementPointer(fileDestiny);
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
		file.getElement().addImport(anImport);
	}


}