package io.intino.plugin.archetype.lang.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.archetype.lang.antlr.ArchetypeLanguage;
import io.intino.plugin.file.ArchetypeFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ArchetypeTemplateImpl extends PsiFileBase implements ArchetypeTemplate {

	public ArchetypeTemplateImpl(@NotNull FileViewProvider viewProvider) {
		super(viewProvider, ArchetypeLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public FileType getFileType() {
		return ArchetypeFileType.instance();
	}

	@Override
	public String toString() {
		return getPresentableName();
	}

	@NotNull
	public String getPresentableName() {
		return getName().contains(".") ? getName().substring(0, getName().lastIndexOf(".")) : getName();
	}

	@Override
	public ItemPresentation getPresentation() {
		return new ItemPresentation() {
			@Override
			public String getPresentableText() {
				return getName().substring(0, getName().lastIndexOf("."));
			}

			@Override
			public String getLocationString() {
				final PsiDirectory psiDirectory = getParent();
				if (psiDirectory != null) {
					return psiDirectory.getVirtualFile().getPresentableUrl();
				}
				return null;
			}

			@Override
			public Icon getIcon(final boolean open) {
				return IntinoIcons.ARCHETYPE_16;
			}
		};
	}

	@Nullable
	@Override
	public Icon getIcon(int flags) {
		return IntinoIcons.ARCHETYPE_16;
	}
}