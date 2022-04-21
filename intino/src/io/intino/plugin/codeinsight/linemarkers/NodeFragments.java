package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.NodeContainer;
import io.intino.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NodeFragments extends JavaLineMarkerProvider {

	private final MarkerType markerType = new MarkerType("", element -> {
		if (!(element instanceof Node)) return null;
		List<NavigatablePsiElement> references = getFragmentNodes((Node) element);
		@NonNls String pattern;
		if (references.isEmpty()) return null;
		pattern = references.get(0).getNavigationElement().getContainingFile().getName();
		return GutterTooltipHelper.getTooltipText(references, "fragment ", false, pattern);
	}, new LineMarkerNavigator() {
		@Override
		public void browse(MouseEvent e, PsiElement element) {
			if (!(element instanceof Node)) return;
			if (DumbService.isDumb(element.getProject())) {
				DumbService.getInstance(element.getProject()).showDumbModeNotification("Navigation to elements is not possible during index update");
				return;
			}
			List<NavigatablePsiElement> references = getFragmentNodes((Node) element);
			references.remove(element);
			if (references.isEmpty()) return;
			DefaultPsiElementListCellRenderer renderer = new DefaultPsiElementListCellRenderer();
			PsiElementListNavigator.openTargets(e, references.toArray(new NavigatablePsiElement[0]), "Fragment of " + (((Node) element).name()), "Fragment of " + (((Node) element).name()), renderer);
		}
	});

	@Override
	public LineMarkerInfo<?> getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Node)) return super.getLineMarkerInfo(element);
		final List<NavigatablePsiElement> fragmentNodes = getFragmentNodes((Node) element);
		if (fragmentNodes.size() > 1) {
			final MarkerType type = markerType;
			return new LineMarkerInfo<>(element, element.getTextRange(), AllIcons.Gutter.Unique, type.getTooltip(),
					type.getNavigationHandler(), GutterIconRenderer.Alignment.LEFT, element::getText);
		} else return super.getLineMarkerInfo(element);
	}

	private List<NavigatablePsiElement> getFragmentNodes(Node node) {
		if (node.isAnonymous()) return Collections.emptyList();
		NodeContainer container = node.container();
		if (container == null) return Collections.emptyList();
		return componentsWithSameSignature(container, node).stream().map(c -> (NavigatablePsiElement) c).collect(Collectors.toList());
	}

	private List<Node> componentsWithSameSignature(NodeContainer container, Node node) {
		String name = name(node);
		return container.components().stream().filter(c -> !c.isReference()).filter(component -> name.equals(name(component))).collect(Collectors.toList());
	}

	private String name(Node node) {
		return node.name() + (node.isAspect() ? "Aspect" : "");
	}

	private static class DefaultPsiElementListCellRenderer extends PsiElementListCellRenderer {
		@Override
		public String getElementText(final PsiElement element) {
			if (element instanceof PsiNamedElement) {
				String name = ((PsiNamedElement) element).getName();
				if (name != null) {
					return name;
				}
			}
			return ((TaraModel) element.getContainingFile()).getPresentableName();
		}

		@Override
		protected String getContainerText(final PsiElement element, final String name) {
			if (element instanceof NavigationItem) {
				final ItemPresentation presentation = ((NavigationItem) element).getPresentation();
				return presentation != null ? presentation.getLocationString() : null;
			}
			return null;
		}

		@Override
		protected int getIconFlags() {
			return 0;
		}
	}
}