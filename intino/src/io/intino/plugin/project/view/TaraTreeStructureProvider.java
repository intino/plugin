package io.intino.plugin.project.view;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.PsiJavaFile;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class TaraTreeStructureProvider implements com.intellij.ide.projectView.TreeStructureProvider {
	private final Project project;

	public TaraTreeStructureProvider(Project project) {
		this.project = project;
	}

	@NotNull
	public Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
		if (parent.getValue() instanceof NodeView) return children;
		Collection<AbstractTreeNode<?>> result = new LinkedHashSet<>();
		for (AbstractTreeNode<?> element : children) {
			if (element instanceof PsiDirectoryNode) {
				result.add(element);
				continue;
			}
			TaraModel taraModel = asTaraFile(element);
			if (isJavaClass(element) && isMethodObjectClass(children, element)) continue;
			if (taraModel == null && (!isJavaClass(element) || !isMethodObjectClass(children, element)))
				result.add(element);
			else result.add(new NodeView(project, taraModel, settings, iconOf(taraModel)));
		}
		return result;
	}

	private Icon iconOf(TaraModel taraModel) {
		if (taraModel.getFileType().equals(LegioFileType.instance())) return LegioFileType.instance().getIcon();
		return IntinoIcons.fileIcon(IntinoUtil.dslOf(taraModel));
	}

	private boolean isJavaClass(AbstractTreeNode<?> element) {
		return element.getValue() instanceof PsiJavaFile;
	}

	private boolean isMethodObjectClass(Collection<AbstractTreeNode<?>> children, AbstractTreeNode<?> element) {
		PsiJavaFile file = (PsiJavaFile) element.getValue();
		final String javaClassName = FileUtilRt.getNameWithoutExtension(file.getName());
		return children.stream()
				.anyMatch(node -> asTaraFile(node) != null && ((TaraModel) node.getValue()).getPresentableName().equals(javaClassName));
	}

	private TaraModel asTaraFile(AbstractTreeNode<?> element) {
		TaraModel model = null;
		if (element.getValue() instanceof TaraModel)
			model = (TaraModel) element.getValue();
		return model;
	}

	@Override
	public @Nullable Object getData(@NotNull Collection<? extends AbstractTreeNode<?>> selected, @NotNull String dataId) {
		if (NodeView.DATA_KEY.is(dataId)) {
			List<NodeView> result = getNodeTreeViews(selected);
			if (!result.isEmpty()) return result.toArray(new NodeView[0]);
		}
		return null;
	}

	private List<NodeView> getNodeTreeViews(Collection<? extends AbstractTreeNode<?>> selected) {
		return selected.stream().
				filter(mogram -> mogram.getValue() instanceof NodeView).
				map(mogram -> (NodeView) mogram.getValue()).toList();
	}
}
