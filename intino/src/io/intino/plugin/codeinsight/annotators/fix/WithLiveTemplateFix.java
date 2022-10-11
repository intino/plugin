package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WithLiveTemplateFix {

	@Nullable("null means unable to open the editor")
	static Editor positionCursor(@NotNull Project project, @NotNull PsiFile targetFile, @NotNull PsiElement element) {
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

	protected static Editor positionCursorAtBegining(@NotNull Project project, @NotNull PsiFile targetFile, int line) {
		VirtualFile file = targetFile.getVirtualFile();
		if (file == null) {
			file = PsiUtilCore.getVirtualFile(targetFile);
			if (file == null) return null;
		}
		OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, line, 0);
		return FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
	}


	protected static <T extends TemplateContextType> T contextType(Class<T> clazz) {
		return ContainerUtil.findInstance(TemplateContextType.EP_NAME.getExtensions(), clazz);
	}
}