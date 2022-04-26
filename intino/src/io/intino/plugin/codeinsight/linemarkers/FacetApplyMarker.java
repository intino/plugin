package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import io.intino.itrules.formatters.StringFormatters.PluralInflector;
import io.intino.itrules.formatters.inflectors.EnglishPluralInflector;
import io.intino.magritte.lang.model.Aspect;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.messages.MessageProvider;
import io.intino.plugin.project.module.IntinoModuleType;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static io.intino.plugin.lang.psi.resolve.ReferenceManager.resolveJavaClassReference;

public class FacetApplyMarker extends JavaLineMarkerProvider {

	private static final String FACETS_PATH = "extensions";
	private static final String DOT = ".";
	private final MarkerType markerType = new MarkerType("Unknown", element -> {
		if (!(element instanceof Node)) return null;
		Node node = (Node) element;
		List<PsiElement> references = getFacetClasses(node);
		String start = (references.size() == 1 ? "Facet" : "Facets") + " declared in ";
		@NonNls StringBuilder pattern = new StringBuilder();
		if (references.isEmpty()) return "";
		for (PsiElement reference : references)
			pattern.append(", ").append(reference.getNavigationElement().getContainingFile().getName());
		pattern = new StringBuilder(pattern.substring(2));
		return GutterTooltipHelper.getTooltipText(references, start, false, pattern.toString());
	}, new LineMarkerNavigator() {
		@Override
		public void browse(MouseEvent e, PsiElement element) {
			if (!(element instanceof Node)) return;
			Node node = (Node) element;
			if (DumbService.isDumb(element.getProject())) {
				DumbService.getInstance(element.getProject()).showDumbModeNotification("Navigation to implementation classes is not possible during index update");
				return;
			}
			List<PsiElement> facetClasses = getFacetClasses(node);
			if (facetClasses.isEmpty()) return;
			String title = MessageProvider.message("aspect.class.chooser", node.name(), facetClasses.size());
			PsiElementListNavigator.openTargets(e, facetClasses.toArray(toNavigatable(facetClasses)), title, "Facet implementations of " + (node.name()), (ListCellRenderer) (jList, o, i, b, b1) -> null);
		}
	}
	);


	private NavigatablePsiElement[] toNavigatable(List<PsiElement> facetClasses) {
		return facetClasses.stream().map(facetClass -> (NavigatablePsiElement) facetClass).toArray(NavigatablePsiElement[]::new);
	}

	private List<PsiElement> getFacetClasses(Node node) {
		List<PsiElement> references = new ArrayList<>();
		for (Aspect apply : node.appliedAspects()) {
			PsiElement reference = resolveExternal(node, apply);
			if (reference != null)
				references.add(reference);
		}
		return references;
	}

	@Override
	public LineMarkerInfo<?> getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Node)) return super.getLineMarkerInfo(element);
		Node node = (Node) element;
		if (node.appliedAspects().isEmpty()) return null;
		PsiElement reference = null;
		for (Aspect aspectApply : node.appliedAspects()) {
			reference = resolveExternal(node, aspectApply);
			if (reference != null) break;
		}
		if (reference != null) {
			final PsiElement leaf = leafOf(element);
			return new LineMarkerInfo<>(leaf, element.getTextRange(), AllIcons.Gutter.ImplementedMethod, markerType.getTooltip(),
					markerType.getNavigationHandler(), GutterIconRenderer.Alignment.LEFT, leaf::getText);
		} else return super.getLineMarkerInfo(element);
	}

	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}

	private PsiElement resolveExternal(Node node, Aspect apply) {
		return resolveJavaClassReference(((TaraNode) node).getProject(), getFacetApplyPackage(node, apply) + DOT + node.name() + apply.type());
	}

	private String getFacetApplyPackage(Node node, Aspect apply) {
		PluralInflector inflector = getInflector(apply);
		if (inflector == null) return "";
		return (getFacetPackage(node) + DOT + inflector.plural(apply.type())).toLowerCase();
	}

	private PluralInflector getInflector(Aspect apply) {
		return !IntinoModuleType.isIntino(ModuleProvider.moduleOf((PsiElement) apply)) ? null : new EnglishPluralInflector();
	}

	private String getFacetPackage(Node node) {
		return (((TaraNode) node).getProject().getName() + DOT + FACETS_PATH).toLowerCase();
	}
}
