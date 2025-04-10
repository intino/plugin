package io.intino.plugin.project.view;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import io.intino.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

class NodeView extends PsiFileNode implements Navigatable {
	static final DataKey<NodeView> DATA_KEY = DataKey.create("form.array");
	private final PsiFile taraFile;
	private final Icon icon;

	NodeView(Project project, TaraModel psiFile, ViewSettings settings, Icon icon) {
		super(project, psiFile, settings);
		taraFile = psiFile;
		this.icon = icon;
		myName = getName();
	}

	public boolean equals(Object object) {
		return object instanceof NodeView form && taraFile.equals(form.taraFile);
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
			data.setIcon(this.icon);
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
