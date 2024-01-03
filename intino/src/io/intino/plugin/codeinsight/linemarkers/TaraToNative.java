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
import io.intino.plugin.lang.psi.Rule;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.List;

public class TaraToNative extends JavaLineMarkerProvider {

	private final MarkerType markerType = new MarkerType("", element -> {
		if (!(element instanceof Rule)) return null;
		PsiElement reference = ReferenceManager.resolveRule((Rule) element);
		String start = "Native code declared in ";
		@NonNls String pattern;
		if (reference == null) return null;
		pattern = reference.getNavigationElement().getContainingFile().getName();
		return GutterTooltipHelper.getTooltipText(List.of(reference), start, false, pattern);
	}, new LineMarkerNavigator() {
		@Override
		public void browse(MouseEvent e, PsiElement element) {
			Rule rule = TaraPsiUtil.getContainerByType(element, Rule.class);
			if (rule == null) return;
			if (DumbService.isDumb(element.getProject())) {
				DumbService.getInstance(element.getProject()).showDumbModeNotification("Navigation to implementation classes is not possible during index update");
				return;
			}
			NavigatablePsiElement reference = (NavigatablePsiElement) ReferenceManager.resolveRule(rule);
			if (reference == null) return;
			String title = DaemonBundle.message("navigation.title.overrider.method", element.getText(), 1);
			MethodCellRenderer renderer = new MethodCellRenderer(false);
			PsiElementListNavigator.openTargets(e, new NavigatablePsiElement[]{reference}, title, "Overriding Methods of " + (reference.getName()), renderer);
		}
	}
	);

	@Override
	public LineMarkerInfo<?> getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Rule))
			return super.getLineMarkerInfo(element);
		PsiElement reference = ReferenceManager.resolveRule((Rule) element);
		if (reference != null) {
			final MarkerType type = markerType;
			final PsiElement leaf = leafOf(element);
			return new LineMarkerInfo<>(leaf, element.getTextRange(), AllIcons.Gutter.ImplementedMethod, type.getTooltip(),
					type.getNavigationHandler(), GutterIconRenderer.Alignment.LEFT, leaf::getText);
		} else return super.getLineMarkerInfo(element);
	}

	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}
}