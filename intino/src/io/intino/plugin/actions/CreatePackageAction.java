package io.intino.plugin.actions;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateDirectoryOrPackageHandler;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.util.PlatformIcons;

public class CreatePackageAction extends DumbAwareAction {

	private static boolean isEnabled(AnActionEvent e) {
		Project project = e.getData(CommonDataKeys.PROJECT);
		final IdeView ideView = e.getData(LangDataKeys.IDE_VIEW);
		if (project == null || ideView == null)
			return false;
		final PsiDirectory[] directories = ideView.getDirectories();
		return directories.length != 0;
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		final IdeView view = e.getData(LangDataKeys.IDE_VIEW);
		e.getData(LangDataKeys.CONTEXT_COMPONENT).setVisible(true);
		if (view == null) return;
		final Project project = e.getData(CommonDataKeys.PROJECT);
		final PsiDirectory directory = DirectoryChooserUtil.getOrChooseDirectory(view);
		if (directory == null) return;
		CreateDirectoryOrPackageHandler validator = new CreateDirectoryOrPackageHandler(project, directory, false, ".");
		Messages.showInputDialog(project, IdeBundle.message("prompt.enter.new.package.name"), IdeBundle.message("title.new.package"), Messages.getQuestionIcon(), "", validator);
		final PsiFileSystemItem result = validator.getCreatedElement();
		if (result != null) view.selectElement(result);
	}

	@Override
	public void update(AnActionEvent e) {
		boolean enabled = isEnabled(e);
		e.getPresentation().setVisible(enabled);
		e.getPresentation().setEnabled(enabled);
		e.getPresentation().setIcon(PlatformIcons.PACKAGE_ICON);
	}
}