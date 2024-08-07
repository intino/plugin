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
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dsl;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.actions.utils.TaraTemplates;
import io.intino.plugin.actions.utils.TaraTemplatesFactory;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraModelImpl;
import io.intino.plugin.messages.MessageProvider;
import io.intino.plugin.project.module.IntinoModuleType;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.intino.plugin.lang.psi.impl.IntinoUtil.isTest;
import static io.intino.plugin.project.Safe.safe;

public class CreateDslFileAction extends JavaCreateTemplateInPackageAction<TaraModelImpl> {

	public CreateDslFileAction() {
		super(MessageProvider.message("new.file.menu.action.text"), MessageProvider.message("new.file.menu.action.description"), IntinoIcons.MODEL_16, true);
	}

	@Override
	protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
		builder.setTitle(MessageProvider.message("new.model.dlg.prompt"));
		final Module module = ModuleProvider.moduleOf(directory);
		if (!IntinoModuleType.isIntino(module))
			throw new IncorrectOperationException(MessageProvider.message("tara.file.error"));
		final Configuration conf = IntinoUtil.configurationOf(module);
		for (Dsl dsl : conf.artifact().dsls())
			if (isTest(directory, module))
				builder.addKind(dsl.outputDsl().name(), IntinoIcons.fileIcon(dsl.name()), dsl.outputDsl().name());
			else builder.addKind(dsl.name(), IntinoIcons.fileIcon(dsl.name()), dsl.name());
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
		final Configuration configuration = IntinoUtil.configurationOf(module);
		return super.isAvailable(dataContext) && IntinoModuleType.isIntino(module) && Boolean.TRUE.equals(safe(() -> !configuration.artifact().dsls().isEmpty()));
	}

	@Nullable
	@Override
	protected PsiElement getNavigationElement(@NotNull TaraModelImpl createdElement) {
		return createdElement;
	}

	@Nullable
	@Override
	protected TaraModelImpl doCreate(PsiDirectory directory, String newName, String templateName) throws IncorrectOperationException {
		String template = TaraTemplates.getTemplate("FILE");
		String fileName = newName + "." + TaraFileType.instance().getDefaultExtension();
		PsiFile file = TaraTemplatesFactory.createFromTemplate(directory, newName, fileName, template, true, "DSL", templateName);
		final Module module = ModuleProvider.moduleOf(directory);
		if (isTest(directory, module)) TestClassCreator.creteTestClass(module, template, newName);
		return file instanceof TaraModelImpl ? (TaraModelImpl) file : error(file);
	}


	private TaraModelImpl error(PsiFile file) {
		final String description = file.getFileType().getDescription();
		throw new IncorrectOperationException(MessageProvider.message("tara.file.extension.is.not.mapped.to.tara.file.type", description));
	}

	@Override
	protected void postProcess(@NotNull TaraModelImpl createdElement, String templateName, Map<String, String> customProperties) {
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