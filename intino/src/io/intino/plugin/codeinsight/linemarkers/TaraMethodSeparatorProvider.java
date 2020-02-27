package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.LineMarkersPass;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class TaraMethodSeparatorProvider implements LineMarkerProvider {


	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
		if (!(element instanceof TaraNode) || !isRoot((Node) element)) return null;
		return LineMarkersPass.createMethodSeparatorLineMarker(leafOf(element), EditorColorsManager.getInstance());
	}

	private boolean isRoot(Node element) {
		return element.container() instanceof TaraModel;
	}

	@Override
	public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

	}

	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}
}
