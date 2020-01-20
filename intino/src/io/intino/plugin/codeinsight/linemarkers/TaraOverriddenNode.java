package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class TaraOverriddenNode extends JavaLineMarkerProvider {

	private final MarkerType markerType = new MarkerType("TaraOverridenNode", element -> {
		if (!Node.class.isInstance(element)) return null;
		PsiElement reference = (PsiElement) getOverriddenNode((Node) element);
		String start = "Overrides element in";
		@NonNls String pattern;
		if (reference == null) return null;
		pattern = reference.getNavigationElement().getContainingFile().getName();
		return GutterIconTooltipHelper.composeText(new PsiElement[]{reference}, start, pattern);
	}, new LineMarkerNavigator() {
		@Override
		public void browse(MouseEvent e, PsiElement element) {
			if (!Node.class.isInstance(element)) return;
			if (DumbService.isDumb(element.getProject())) {
				DumbService.getInstance(element.getProject()).showDumbModeNotification("Navigation to implementation classes is not possible during index update");
				return;
			}
			NavigatablePsiElement reference = (NavigatablePsiElement) getOverriddenNode((Node) element);
			if (reference == null) return;
			String title = DaemonBundle.message("navigation.title.overrider.method", element.getText(), 1);
			MethodCellRenderer renderer = new MethodCellRenderer(false);
			PsiElementListNavigator.openTargets(e, new NavigatablePsiElement[]{reference}, title, "Overrides element " + (reference.getName()), renderer);
		}
	});

	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Node)) return super.getLineMarkerInfo(element);
		Node node = (Node) element;
		if (isOverridden(node)) {
			final Icon icon = AllIcons.Gutter.OverridingMethod;
			final MarkerType type = markerType;
			return new LineMarkerInfo(leafOf(element), element.getTextRange(), icon, type.getTooltip(),
					type.getNavigationHandler(), GutterIconRenderer.Alignment.LEFT);
		} else return super.getLineMarkerInfo(element);
	}

	private Node getOverriddenNode(Node inner) {
		Node node = TaraPsiUtil.getContainerNodeOf((PsiElement) inner);
		if (node == null) return null;
		Node parent = node.parent();
		while (parent != null) {
			for (Node parentVar : parent.components())
				if (isOverridden(inner, parentVar))
					return parentVar;
			parent = parent.parent();
		}
		return null;
	}

	private boolean isOverridden(Node node) {
		return getOverriddenNode(node) != null;
	}

	private boolean isOverridden(Node node, Node parentNode) {
		return parentNode.type().equals(node.type()) && parentNode.name() != null && parentNode.name().equals(node.name());
	}


	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}
}
