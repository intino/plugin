package io.intino.plugin.itrules.lang.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;
import io.intino.plugin.file.ItrulesFileType;
import io.intino.plugin.itrules.lang.ItrulesIcons;
import io.intino.plugin.itrules.lang.ItrulesLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ItrulesTemplateImpl extends PsiFileBase implements ItrulesTemplate {

    public ItrulesTemplateImpl(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, ItrulesLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ItrulesFileType.instance();
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
                return ItrulesIcons.ICON_13;
            }
        };
    }

    @Nullable
    @Override
    public Icon getIcon(int flags) {
        return ItrulesIcons.ICON_13;
    }

}