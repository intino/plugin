package io.intino.plugin.actions.utils;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.ide.fileTemplates.*;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.errorreporting.TaraRuntimeException;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.messages.MessageProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Properties;

public class TaraTemplatesFactory implements FileTemplateGroupDescriptorFactory {

	@NonNls
	private static final Collection<String> TEMPLATES = TaraTemplates.getTemplateValues();
	@NonNls
	private static final String NAME_TEMPLATE_PROPERTY = "NAME";
	private static final String LOW_CASE_NAME_TEMPLATE_PROPERTY = "lowCaseName";

	private TaraTemplatesFactory() {
	}

	public static TaraTemplatesFactory getInstance() {
		return TaraTemplatesFactoryHolder.myInstance;
	}

	public static PsiFile createFromTemplate(@NotNull final PsiDirectory directory,
											 @NotNull final String name,
											 @NotNull String fileName,
											 @NotNull String templateName,
											 boolean allowReformatting,
											 @NonNls String... parameters) throws IncorrectOperationException {
		final FileTemplate template = FileTemplateManager.getDefaultInstance().getJ2eeTemplate(templateName);
		Project project = directory.getProject();
		Properties properties = new Properties(FileTemplateManager.getDefaultInstance().getDefaultProperties());
		properties.setProperty("PROJECT_NAME", project.getName());
		JavaTemplateUtil.setPackageNameAttribute(properties, directory);
		properties.setProperty(NAME_TEMPLATE_PROPERTY, name);
		properties.setProperty(LOW_CASE_NAME_TEMPLATE_PROPERTY, name.substring(0, 1).toLowerCase() + name.substring(1));
		for (int i = 0; i < parameters.length; i += 2)
			properties.setProperty(parameters[i], parameters[i + 1]);
		String text;
		try {
			text = template.getText(properties);
		} catch (Exception e) {
			throw new TaraRuntimeException("Unable to load template for " + FileTemplateManager.getDefaultInstance().internalTemplateToSubject(templateName), e);
		}
		final PsiFileFactory factory = PsiFileFactory.getInstance(project);
		PsiFile file = factory.createFileFromText(fileName, TaraFileType.instance(), text);
		file = (PsiFile) directory.add(file);
		if (file != null && allowReformatting && template.isReformatCode())
			new ReformatCodeProcessor(project, file, null, false).run();
		return file;
	}


	public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
		final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(MessageProvider.message("file.template.group.title.tara"), IntinoIcons.MODEL_16);
		final FileTypeManager fileTypeManager = FileTypeManager.getInstance();
		for (String template : TEMPLATES) {
			group.addTemplate(new FileTemplateDescriptor(template, fileTypeManager.getFileTypeByFileName(template).getIcon()));
		}
		return group;
	}

	private static class TaraTemplatesFactoryHolder {
		private static final TaraTemplatesFactory myInstance = new TaraTemplatesFactory();

		private TaraTemplatesFactoryHolder() {
		}
	}
}