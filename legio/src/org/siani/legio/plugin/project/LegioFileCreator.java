package org.siani.legio.plugin.project;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import org.siani.itrules.model.Frame;
import org.siani.legio.plugin.file.LegioFileType;

import java.io.File;

class LegioFileCreator {
	private final Module module;
	private final PsiFileFactory fileFactory;
	private final PsiManager psiManager;

	LegioFileCreator(Module module) {
		this.module = module;
		this.fileFactory = PsiFileFactory.getInstance(module.getProject());
		psiManager = PsiManager.getInstance(module.getProject());
	}

	PsiFile create() {
		final String legio = LegioTemplate.create().format(new Frame().addTypes("legio").addSlot("name", module.getName()));
		final File destiny = new File(new File(module.getModuleFilePath()).getParent(), "configuration.legio");
		if (destiny.exists()) {
			final VirtualFile ioFile = VfsUtil.findFileByIoFile(destiny, true);
			if (ioFile == null) return null;
			Application application = ApplicationManager.getApplication();
			return application.isReadAccessAllowed() ? psiManager.findFile(ioFile) : inReadAction(ioFile, application);
		}
		return fileFactory.createFileFromText(destiny.getAbsolutePath(), LegioFileType.instance(), legio);
	}

	private PsiFile inReadAction(final VirtualFile ioFile, Application application) {
		return application.runReadAction(new Computable<PsiFile>() {
			@Override
			public PsiFile compute() {
				return psiManager.findFile(ioFile);
			}
		});
	}
}
