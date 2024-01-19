package io.intino.plugin.build;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;

import java.io.File;

import static com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction;

public abstract class PostCompileAction {
	public enum FinishStatus {
		NothingDone, RequiresReload, Done, Error
	}

	protected final Module module;

	public PostCompileAction(Module module) {
		this.module = module;
	}

	public abstract FinishStatus execute();

	protected PsiClass findClass(File file) {
		VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, true);
		if (virtualFile == null) return null;
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return getClass(virtualFile);
		return application.<PsiClass>runReadAction(() -> getClass(virtualFile));
	}

	private PsiClass getClass(VirtualFile virtualFile) {
		if (module == null) return null;
		PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(virtualFile);
		return ((PsiJavaFile) psiFile).getClasses()[0];
	}

	protected <T> T read(Computable<T> t) {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return t.compute();
		return application.runReadAction(t);
	}

	protected void write(Runnable t) {
		if (ApplicationManager.getApplication().isWriteAccessAllowed()) t.run();
		else runWriteCommandAction(module.getProject(), t);
	}
}
