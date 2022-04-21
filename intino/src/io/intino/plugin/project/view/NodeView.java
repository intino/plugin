package io.intino.plugin.project.view;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import io.intino.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

class NodeView extends PsiFileNode implements Navigatable {
	static final DataKey<NodeView> DATA_KEY = DataKey.create("form.array");
	@SuppressWarnings("deprecation")
	public static final TextAttributesKey ERROR = createTextAttributesKey("ERROR",
			new TextAttributes(null, null, JBColor.RED, EffectType.WAVE_UNDERSCORE, Font.PLAIN));
	private final PsiFile taraFile;

	NodeView(Project project, TaraModel psiFile, ViewSettings settings) {
		super(project, psiFile, settings);
		taraFile = psiFile;
		myName = getName();
	}

	public boolean equals(Object object) {
		if (object instanceof NodeView) {
			NodeView form = (NodeView) object;
			return taraFile.equals(form.taraFile);
		}
		return false;
	}

	@Override
	public Collection<AbstractTreeNode<?>> getChildrenImpl() {
		return Collections.emptyList();
	}

	public int hashCode() {
		return taraFile.hashCode();
	}

	public String getName() {
		return FileUtil.getNameWithoutExtension(taraFile.getName());
	}

	public void navigate(boolean requestFocus) {
		taraFile.navigate(requestFocus);
	}

	public boolean canNavigateToSource() {
		return taraFile.canNavigateToSource();
	}

	public boolean canNavigate() {
		return taraFile.canNavigate();
	}

	@Override
	protected void updateImpl(@NotNull PresentationData data) {
		PsiFile value = getValue();
		if (value instanceof TaraModel) {
			myName = getName();
			data.setPresentableText(((TaraModel) value).getPresentableName());
			final Icon icon = value.getIcon(Iconable.ICON_FLAG_READ_STATUS);
			data.setIcon("Legio".equalsIgnoreCase(((TaraModel) value).dsl()) ?
					icon : new RowIcon(icon, PlatformIcons.PACKAGE_LOCAL_ICON));
		} else data.setPresentableText(value.getName());
		VirtualFile file = getVirtualFile();
		if (file != null && file.is(VFileProperty.SYMLINK)) {
			String target = file.getCanonicalPath();
			if (target == null) {
				data.setAttributesKey(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);
				data.setTooltip("Broken link");
			} else data.setTooltip(FileUtil.toSystemDependentName(target));
		}
	}

	public boolean isValid() {
		return taraFile.isValid();
	}

}
