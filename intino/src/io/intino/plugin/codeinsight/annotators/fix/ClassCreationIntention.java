package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.ide.util.DirectoryUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import io.intino.plugin.errorreporting.TaraRuntimeException;

import java.util.Collection;

public abstract class ClassCreationIntention extends PsiElementBaseIntentionAction {
	protected static final String RULES_PACKAGE = ".rules";
	private static final String SRC = "src";
	private static final String GEN = "gen";
	protected static final String DOT = ".";

	PsiDirectory findDestination(PsiFile file, final PsiDirectoryImpl srcDirectory, final String destinyName) {
		PsiDirectory subdirectory = srcDirectory.findSubdirectory(destinyName);
		if (subdirectory != null) return subdirectory;
		final PsiDirectory[] destiny = createPath(file, srcDirectory, destinyName);
		return destiny[0];
	}

	private PsiDirectory[] createPath(final PsiFile file, final PsiDirectoryImpl srcDirectory, final String destinyName) {
		final PsiDirectory[] destiny = new PsiDirectory[1];
		WriteCommandAction.writeCommandAction(file.getProject(), file).run(() -> {
			destiny[0] = srcDirectory;
			for (String subDirName : destinyName.split("\\.")) {
				if (destiny[0] == null) break;
				destiny[0] = destiny[0].findSubdirectory(subDirName) == null ?
						DirectoryUtil.createSubdirectories(subDirName, destiny[0], ".") :
						destiny[0].findSubdirectory(subDirName);
			}
		});
		return destiny;
	}

	VirtualFile getSrcDirectory(Collection<VirtualFile> virtualFiles) {
		for (VirtualFile file : virtualFiles)
			if (file.isDirectory() && SRC.equals(file.getName())) return file;
		throw new TaraRuntimeException("src directory not found");
	}

	VirtualFile getGenDirectory(Collection<VirtualFile> virtualFiles) {
		for (VirtualFile file : virtualFiles)
			if (file.isDirectory() && GEN.equals(file.getName())) return file;
		throw new TaraRuntimeException("gen directory not found");
	}
}