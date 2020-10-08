package io.intino.plugin.actions;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.Configuration;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.actions.utils.TaraTemplates;
import io.intino.plugin.actions.utils.TaraTemplatesFactory;
import io.intino.plugin.file.KonosFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraModelImpl;
import io.intino.plugin.messages.MessageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class CreateBoxFileAction extends JavaCreateTemplateInPackageAction<TaraModelImpl> {


	public CreateBoxFileAction() {
		super("Box File", "Creates a new Box File", IntinoIcons.KONOS_16, true);
	}

	@Override
	protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
		builder.setTitle("Enter name for new Box File");
		builder.addKind("Konos", IntinoIcons.KONOS_16, "Konos");
	}

	@Override
	protected String getActionName(PsiDirectory directory, String newName, String templateName) {
		return "Box File";
	}

	@Override
	protected boolean isAvailable(DataContext dataContext) {
		PsiElement data = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
		return (data instanceof PsiFile || data instanceof PsiDirectory) && super.isAvailable(dataContext);
	}

	@Nullable
	@Override
	protected PsiElement getNavigationElement(@NotNull TaraModelImpl createdElement) {
		return createdElement;
	}

	@Nullable
	@Override
	protected TaraModelImpl doCreate(PsiDirectory directory, String newName, String dsl) throws IncorrectOperationException {
		String template = TaraTemplates.getTemplate("FILE");
		String fileName = newName + "." + KonosFileType.instance().getDefaultExtension();
		PsiFile file = TaraTemplatesFactory.createFromTemplate(directory, newName, fileName, template, true, "DSL", dsl);
		return file instanceof TaraModelImpl ? (TaraModelImpl) file : error(file);
	}

	private TaraModelImpl error(PsiFile file) {
		final String description = file.getFileType().getDescription();
		throw new IncorrectOperationException(MessageProvider.message("tara.file.extension.is.not.mapped.to.tara.file.type", description));
	}

	@Override
	protected void postProcess(TaraModelImpl createdElement, String templateName, Map<String, String> customProperties) {
		super.postProcess(createdElement, templateName, customProperties);
		setCaret(createdElement);
		createdElement.navigate(true);
	}

	private void setCaret(PsiFile file) {
		final PsiDocumentManager instance = PsiDocumentManager.getInstance(file.getProject());
		Document doc = instance.getDocument(file);
		if (doc == null) return;
		instance.commitDocument(doc);
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setIcon(IntinoIcons.KONOS_16);
		final Module module = e.getData(LangDataKeys.MODULE);
		final Configuration configuration = module == null ? null : IntinoUtil.configurationOf(module);
		boolean enabled = configuration != null && configuration.artifact().box() != null;
		e.getPresentation().setVisible(enabled);
		e.getPresentation().setEnabled(enabled);
	}

}