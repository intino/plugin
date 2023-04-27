package io.intino.plugin.itrules.project.view;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.PsiJavaFile;
import io.intino.plugin.itrules.lang.psi.ItrulesTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ItrulesTreeStructureProvider implements TreeStructureProvider {

	private final Project project;

	public ItrulesTreeStructureProvider(Project project) {
		this.project = project;
	}

	@NotNull
	@Override
	public Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
		if (parent.getValue() instanceof NodeView) return children;
		Collection<AbstractTreeNode<?>> result = new LinkedHashSet<>();
		for (AbstractTreeNode element : children) {
			if (element instanceof PsiDirectoryNode) {
				result.add(element);
				continue;
			}
			ItrulesTemplate itrFile = asItrFile(element);
			if (isJavaClass(element) && isTemplateClass(children, element)) continue;
			if (itrFile == null && (!isJavaClass(element) || !isTemplateClass(children, element)))
				result.add(element);
			else result.add(new NodeView(project, itrFile, settings));
		}
		return result;
	}

	private boolean isJavaClass(AbstractTreeNode<?> element) {
		return element.getValue() instanceof PsiJavaFile;
	}

	private boolean isTemplateClass(Collection<AbstractTreeNode<?>> children, AbstractTreeNode<?> element) {
		PsiJavaFile file = (PsiJavaFile) element.getValue();
		final String javaClassName = FileUtilRt.getNameWithoutExtension(file.getName());
		for (AbstractTreeNode<?> node : children)
			if (asItrFile(node) != null && (((ItrulesTemplate) node.getValue()).getPresentableName() + "Template").equals(javaClassName))
				return true;
		return false;
	}

	private ItrulesTemplate asItrFile(AbstractTreeNode<?> element) {
		ItrulesTemplate model = null;
		if (element.getValue() instanceof ItrulesTemplate)
			model = (ItrulesTemplate) element.getValue();
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
				filter(node -> node.getValue() instanceof NodeView).
				map(node -> (NodeView) node.getValue()).collect(Collectors.toList());
	}

}
