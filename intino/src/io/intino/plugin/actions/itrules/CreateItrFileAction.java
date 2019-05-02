package io.intino.plugin.actions.itrules;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.itrules.lang.ItrulesIcons;
import io.intino.plugin.itrules.lang.file.ItrulesFileType;
import io.intino.plugin.itrules.lang.psi.ItrulesTemplateImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CreateItrFileAction extends JavaCreateTemplateInPackageAction<ItrulesTemplateImpl> {

	public CreateItrFileAction() {
		super("ItRules Template", "Creates new Itrules Model", ItrulesIcons.ICON_13, true);
	}

	@Override
	protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
		builder.setTitle("Enter name for the new Itrules Template");
		builder.addKind("itr template", ItrulesIcons.ICON_13, "");
	}

	@Override
	protected String getActionName(PsiDirectory directory, String newName, String templateName) {
		return "ItRules Template";
	}

	@Nullable
	@Override
	protected PsiElement getNavigationElement(@NotNull ItrulesTemplateImpl template) {
		return template;
	}

	@Override
	protected boolean isAvailable(DataContext dataContext) {
		PsiElement data = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
		if (!(data instanceof PsiFile || data instanceof PsiDirectory)) return false;
		return super.isAvailable(dataContext);
	}

	@Nullable
	@Override
	protected ItrulesTemplateImpl doCreate(PsiDirectory directory, String newName, String templateName) throws IncorrectOperationException {
		final PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());
		String fileName = newName + "." + ItrulesFileType.INSTANCE.getDefaultExtension();
		PsiFile file = factory.createFileFromText(fileName, ItrulesFileType.INSTANCE, "def type()" +
				"\n\t" +
				"\n\t" +
				"\nend");
		directory.add(file);
		return (ItrulesTemplateImpl) file;
	}

	@Override
	protected void postProcess(ItrulesTemplateImpl createdElement, String templateName, Map<String, String> customProperties) {
		super.postProcess(createdElement, templateName, customProperties);
		setCaret(createdElement);
		if (createdElement.canNavigate()) createdElement.navigate(true);
	}

	private void setCaret(PsiFile file) {
		final PsiDocumentManager instance = PsiDocumentManager.getInstance(file.getProject());
		Document doc = instance.getDocument(file);
		if (doc == null) return;
		instance.commitDocument(doc);
	}
}