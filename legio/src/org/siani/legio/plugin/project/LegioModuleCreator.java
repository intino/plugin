package org.siani.legio.plugin.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import org.siani.legio.plugin.file.LegioFileType;
import org.siani.itrules.model.Frame;

import java.io.File;

class LegioModuleCreator {
	private final Module module;
	private final PsiFileFactory fileFactory;
	private final PsiManager psiManager;

	LegioModuleCreator(Module module) {
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
			return psiManager.findFile(ioFile);
		}
		return fileFactory.createFileFromText(destiny.getAbsolutePath(), LegioFileType.instance(), legio);
	}
}
