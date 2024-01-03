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
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.List;

public class TaraOverriddenNode extends JavaLineMarkerProvider {

	private final MarkerType markerType = new MarkerType("TaraOverridenNode", element -> {
		if (!(element instanceof Mogram)) return null;
		PsiElement reference = (PsiElement) getOverriddenNode((Mogram) element);
		@NonNls String pattern;
		if (reference == null) return null;
		pattern = reference.getNavigationElement().getContainingFile().getName();
		return GutterTooltipHelper.getTooltipText(List.of(reference), "Overrides element in", false, pattern);
	}, new LineMarkerNavigator() {
		@Override
		public void browse(MouseEvent e, PsiElement element) {
			if (!(element instanceof Mogram)) return;
			if (DumbService.isDumb(element.getProject())) {
				DumbService.getInstance(element.getProject()).showDumbModeNotification("Navigation to implementation classes is not possible during index update");
				return;
			}
			NavigatablePsiElement reference = (NavigatablePsiElement) getOverriddenNode((Mogram) element);
			if (reference == null) return;
			String title = DaemonBundle.message("navigation.title.overrider.method", element.getText(), 1);
			MethodCellRenderer renderer = new MethodCellRenderer(false);
			PsiElementListNavigator.openTargets(e, new NavigatablePsiElement[]{reference}, title, "Overrides element " + (reference.getName()), renderer);
		}
	});

	@Override
	public LineMarkerInfo<?> getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Mogram mogram)) return super.getLineMarkerInfo(element);
		if (isOverridden(mogram)) {
			final MarkerType type = markerType;
			final PsiElement leaf = leafOf(element);
			return new LineMarkerInfo<>(leaf, element.getTextRange(), AllIcons.Gutter.OverridingMethod, type.getTooltip(), type.getNavigationHandler(), GutterIconRenderer.Alignment.LEFT, leaf::getText);
		} else return super.getLineMarkerInfo(element);
	}

	private Mogram getOverriddenNode(Mogram inner) {
		Mogram mogram = TaraPsiUtil.getContainerNodeOf((PsiElement) inner);
		if (mogram == null) return null;
		Mogram parent = mogram.parent();
		while (parent != null) {
			for (Mogram parentVar : parent.components())
				if (isOverridden(inner, parentVar)) return parentVar;
			parent = parent.parent();
		}
		return null;
	}

	private boolean isOverridden(Mogram node) {
		return getOverriddenNode(node) != null;
	}

	private boolean isOverridden(Mogram node, Mogram parentNode) {
		return parentNode.type().equals(node.type()) && parentNode.name() != null && parentNode.name().equals(node.name());
	}


	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}
}
