package io.intino.plugin.actions;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.actions.utils.TaraTemplates;
import io.intino.tara.plugin.actions.utils.TaraTemplatesFactory;
import io.intino.tara.plugin.lang.TaraIcons;
import io.intino.tara.plugin.lang.file.TaraFileType;
import io.intino.tara.plugin.lang.psi.impl.TaraModelImpl;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.messages.MessageProvider;
import io.intino.tara.plugin.project.TaraModuleType;
import io.intino.tara.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.intino.tara.plugin.lang.psi.impl.TaraUtil.isTest;

public class CreateModelFileAction extends JavaCreateTemplateInPackageAction<TaraModelImpl> {

	public CreateModelFileAction() {
		super(MessageProvider.message("new.file.menu.action.text"), MessageProvider.message("new.file.menu.action.description"), TaraIcons.ICON_16, true);
	}

	@Override
	protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
		builder.setTitle(MessageProvider.message("new.model.dlg.prompt"));
		final Module module = ModuleProvider.moduleOf(directory);
		if (!TaraModuleType.isTara(module))
			throw new IncorrectOperationException(MessageProvider.message("tara.file.error"));
		final Configuration conf = TaraUtil.configurationOf(module);
		if (isTest(directory, module)) builder.addKind(conf.outLanguage(), TaraIcons.MODEL_16, conf.outLanguage());
		else for (Configuration.LanguageLibrary languageLibrary : conf.languages())
			if (!languageLibrary.name().isEmpty())
				builder.addKind(languageLibrary.name(), TaraIcons.MODEL_16, languageLibrary.name());
	}

	@Override
	protected String getActionName(PsiDirectory directory, String newName, String templateName) {
		return MessageProvider.message("new.file.menu.action.text");
	}

	@Override
	protected boolean isAvailable(DataContext dataContext) {
		PsiElement data = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
		if (!(data instanceof PsiFile || data instanceof PsiDirectory)) return false;
		Module module = ModuleProvider.moduleOf(data);
		final Configuration configuration = TaraUtil.configurationOf(module);
		return super.isAvailable(dataContext) && TaraModuleType.isTara(module) && configuration != null && !configuration.languages().isEmpty();
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
		String fileName = newName + "." + TaraFileType.instance().getDefaultExtension();
		PsiFile file = TaraTemplatesFactory.createFromTemplate(directory, newName, fileName, template, true, "DSL", dsl);
		final Module module = ModuleProvider.moduleOf(directory);
		if (isTest(directory, module)) TestClassCreator.creteTestClass(module, dsl, newName);
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
}