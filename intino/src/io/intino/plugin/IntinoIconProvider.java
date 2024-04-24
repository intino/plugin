package io.intino.plugin;

import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class IntinoIconProvider extends IconProvider {

	@Override
	public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
		return icon(element);
	}

	@Nullable
	public static Icon icon(@NotNull PsiElement element) {
		PsiFile containingFile = element.getContainingFile();
		if (containingFile == null) return null;
		if (LegioFileType.instance().equals(containingFile.getFileType())) return LegioFileType.instance().getIcon();
		return TaraFileType.instance().equals(containingFile.getFileType()) ? IntinoIcons.fileIcon(IntinoUtil.dslOf(element)) : null;
	}


}
