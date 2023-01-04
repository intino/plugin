package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.codeinsight.livetemplates.TaraTemplateContext;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedefineFix implements IntentionAction {
	private final Node node;
	private final String[] parameters;

	public RedefineFix(PsiElement element, String... parameters) {
		this.node = element instanceof Node ? (Node) element : TaraPsiUtil.getContainerNodeOf(element);
		this.parameters = parameters;
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Redefine Variable";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		return file.isValid();
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		IdeDocumentHistory.getInstance(project).includeCurrentPlaceAsChangePlace();
		PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		final Editor bodyEditor = positionCursor(project, file, addLineSeparator(((TaraNode) node)));
		if (bodyEditor == null) return;
		TemplateManager.getInstance(project).startTemplate(bodyEditor, createTemplate(file));
		PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(bodyEditor.getDocument());
	}

	private PsiElement addLineSeparator(TaraNode node) {
		final PsiElement newLineIndent = TaraElementFactory.getInstance(node.getProject()).createBodyNewLine(TaraPsiUtil.getIndentation(node) + 1);
		return node.add(newLineIndent);
	}

	private Template createTemplate(PsiFile file) {
		final Template template = TemplateManager.getInstance(file.getProject()).createTemplate("var", "Tara", "var $TYPE$ $NAME$");
		template.addVariable("TYPE", "", '"' + parameters[1] + '"', true);
		template.addVariable("NAME", "", '"' + parameters[0] + '"', false);
		((TemplateImpl) template).getTemplateContext().setEnabled(contextType(), true);
		return template;
	}

	private static <T extends TemplateContextType> TaraTemplateContext contextType() {
		return new TaraTemplateContext();
	}


	@Nullable("null means unable to open the editor")
	protected static Editor positionCursor(@NotNull Project project, @NotNull PsiFile targetFile, @NotNull PsiElement element) {
		TextRange range = element.getTextRange();
		int textOffset = range.getEndOffset();
		VirtualFile file = targetFile.getVirtualFile();
		if (file == null) {
			file = PsiUtilCore.getVirtualFile(element);
			if (file == null) return null;
		}
		OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, textOffset);
		return FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
	}

	@Override
	public boolean startInWriteAction() {
		return false;
	}
}
