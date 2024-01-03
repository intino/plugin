package io.intino.plugin.codeinsight.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.documentation.TaraDocumentationProvider;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.TaraVariable;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramContainer;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class EditElementDocumentation extends PsiElementBaseIntentionAction {
	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final File docFile = TaraDocumentationProvider.getDocumentationFile(element);
		createIfNecessary(docFile);
		Map<String, String> docMap = TaraDocumentationProvider.extractDocumentationFrom(docFile);
		if (docMap == null) docMap = new HashMap<>();
		final String qn = createQn(element);
		DialogBuilder builder = new DialogBuilder(project);
		final String text = createDialog(builder, docMap.get(qn));
		docMap.put(qn, text);
		TaraDocumentationProvider.saveDocumentation(docMap, docFile);
	}

	public void createIfNecessary(File docFile) {
		if (!docFile.exists()) try {
			docFile.createNewFile();
		} catch (IOException e) {
			Notifications.Bus.notify(new Notification("Intino", "Intino", "Error creating documentation file", NotificationType.ERROR), null);
		}
	}

	public String createDialog(DialogBuilder builder, String content) {
		Application application = ApplicationManager.getApplication();
		AtomicReference<String> text = new AtomicReference<>();
		application.invokeAndWait(() -> {
			builder.setTitle("Edit Documentation");
			final JTextArea textArea = new JTextArea(content);
			textArea.setSize(600, 400);
			textArea.setMinimumSize(new Dimension(600, 400));
			builder.setCenterPanel(textArea);
			builder.setPreferredFocusComponent(textArea);
			builder.resizable(false);
			builder.removeAllActions();
			builder.showModal(true);
			text.set(textArea.getText());
		});
		return text.get();
	}

	private String createQn(PsiElement element) {
		final Variable variable = TaraPsiUtil.getContainerByType(element, Variable.class);
		if (variable != null) return createVariableQn(variable);
		final Mogram mogram = TaraPsiUtil.getContainerByType(element, Mogram.class);
		return mogram != null ? createNodeQn(mogram) : "";

	}

	private String createNodeQn(Mogram node) {
		return node.qualifiedName();
	}

	private String createVariableQn(Variable variable) {
		return variable.container().qualifiedName() + "." + variable.name();
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		return element.getLanguage().equals(TaraLanguage.INSTANCE) && canBeDocumented(TaraPsiUtil.getContainerByType(element, Identifier.class));
	}

	private boolean canBeDocumented(Identifier identifier) {
		if (identifier == null) return false;
		final TaraVariable variable = TaraPsiUtil.getContainerByType(identifier, TaraVariable.class);
		if (variable != null) return identifier.equals(variable.getIdentifier());
		final MogramContainer nodeContainer = TaraPsiUtil.getContainerByType(identifier, MogramContainer.class);
		if (nodeContainer != null) {
			if (nodeContainer instanceof TaraMogram)
				return identifier.equals(((TaraMogram) nodeContainer).getSignature().getIdentifier());
		}
		return false;
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@NotNull
	@Override
	public String getText() {
		return "Edit documentation";
	}
}
