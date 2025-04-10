package io.intino.plugin.itrules.project.view;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import io.intino.plugin.itrules.lang.psi.ItrulesTemplate;

import java.util.Collection;
import java.util.Collections;

class NodeView extends PsiFileNode implements Navigatable {
	static final DataKey<NodeView> DATA_KEY = DataKey.create("form.array");
	private final PsiFile file;

	NodeView(Project project, ItrulesTemplate psiFile, ViewSettings settings) {
		super(project, psiFile, settings);
		file = psiFile;
		myName = getName();
	}

	public boolean equals(Object object) {
		if (object instanceof NodeView) {
			NodeView form = (NodeView) object;
			return file.equals(form.file);
		}
		return false;
	}

	@Override
	public Collection<AbstractTreeNode<?>> getChildrenImpl() {
		return Collections.emptyList();
	}

	public int hashCode() {
		return file.hashCode();
	}

	public String getName() {
		return FileUtil.getNameWithoutExtension(file.getName());
	}
	
	public boolean canNavigateToSource() {
		return file.canNavigateToSource();
	}

	public boolean canNavigate() {
		return file.canNavigate();
	}

	@Override
	protected void updateImpl(PresentationData data) {
		PsiFile value = getValue();
		if (value instanceof ItrulesTemplate) {
			myName = getName();
			data.setPresentableText(((ItrulesTemplate) value).getPresentableName());
		} else data.setPresentableText(value.getName());
		data.setIcon(value.getIcon(Iconable.ICON_FLAG_READ_STATUS));
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
		return file.isValid();
	}

}
