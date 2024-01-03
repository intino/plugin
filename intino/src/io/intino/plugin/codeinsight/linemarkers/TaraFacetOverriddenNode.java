package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Facet;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.List;

import static com.intellij.icons.AllIcons.Gutter.OverridenMethod;

public class TaraFacetOverriddenNode extends JavaLineMarkerProvider {

	private final MarkerType markerType = new MarkerType("", element -> {
		if (!(element instanceof Mogram)) return null;
		TaraMogram reference = getOverriddenNode((Mogram) element);
		@NonNls String pattern;
		if (reference == null) return null;
		pattern = reference.getNavigationElement().getContainingFile().getName();
		return GutterTooltipHelper.getTooltipText(List.of(reference), "Node overridden by facet in ", false, pattern);
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
			PsiElementListNavigator.openTargets(e, new NavigatablePsiElement[]{reference}, title, "Overridden Mogram of " + (reference.getName()), renderer);
		}
	}
	);

	@Override
	public LineMarkerInfo<?> getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Mogram mogram) || !(TaraPsiUtil.getContainerOf(element) instanceof Facet))
			return super.getLineMarkerInfo(element);
		if (isOverridden(mogram)) {
			final MarkerType type = markerType;
			return new LineMarkerInfo<>(element, element.getTextRange(), OverridenMethod, type.getTooltip(), type.getNavigationHandler(), GutterIconRenderer.Alignment.LEFT, element::getText);
		} else return super.getLineMarkerInfo(element);
	}

	private TaraMogram getOverriddenNode(Mogram inner) {
		Mogram container = TaraPsiUtil.getContainerNodeOf((PsiElement) inner);
		if (container == null) return null;
		return (TaraMogram) container.components().stream().filter(containerNode -> isOverridden(inner, containerNode)).findFirst().orElse(null);
	}

	private boolean isOverridden(Mogram node) {
		return getOverriddenNode(node) != null;
	}

	private boolean isOverridden(Mogram node, Mogram parentNode) {
		return parentNode.type().equals(node.type()) && ((parentNode.name() == null && node.name() == null) || (parentNode.name() != null && parentNode.name().equals(node.name())));
	}
}
