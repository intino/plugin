package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;

import static com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment.LEFT;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.getOverriddenVariable;

public class TaraOverriddenVariable extends JavaLineMarkerProvider {

	private final MarkerType markerType = new MarkerType("TaraOverridenVariable", element -> {
		if (!(element instanceof Variable)) return null;
		PsiElement reference = getOverriddenVariable((Variable) element);
		String start = "Overrides variable in ";
		@NonNls String pattern;
		if (reference == null) return null;
		pattern = reference.getNavigationElement().getContainingFile().getName();
		return GutterTooltipHelper.getTooltipText(List.of(reference), start, false, pattern);
	}, new LineMarkerNavigator() {
		@Override
		public void browse(MouseEvent e, PsiElement element) {
			if (!(element instanceof Variable)) return;
			if (DumbService.isDumb(element.getProject())) {
				DumbService.getInstance(element.getProject()).showDumbModeNotification("Navigation to implementation classes is not possible during index update");
				return;
			}
			NavigatablePsiElement reference = (NavigatablePsiElement) getOverriddenVariable((Variable) element);
			if (reference == null) return;
			String title = DaemonBundle.message("navigation.title.overrider.method", element.getText(), 1);
			MethodCellRenderer renderer = new MethodCellRenderer(false);
			PsiElementListNavigator.openTargets(e, new NavigatablePsiElement[]{reference}, title, "Overridden Variable of " + (reference.getName()), renderer);
		}
	}
	);

	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Variable)) return super.getLineMarkerInfo(element);
		Variable variable = (Variable) element;
		if (isOverridden(variable)) {
			final Icon icon = AllIcons.Gutter.OverridingMethod;
			final MarkerType type = markerType;
			return new LineMarkerInfo<>(leafOf(element), element.getTextRange(), icon, type.getTooltip(), type.getNavigationHandler(), LEFT, variable::name);
		} else return super.getLineMarkerInfo(element);
	}

	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}

	private boolean isOverridden(Variable variable) {
		return getOverriddenVariable(variable) != null;
	}
}
