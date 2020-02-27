package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Primitive;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.intellij.icons.AllIcons.Gutter.ImplementedMethod;

public class TaraNativeImplementationToJava extends RelatedItemLineMarkerProvider {

	@Override
	protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
		if (!(element instanceof Valued)) return;
		Valued valued = (Valued) element;
		if (!isAvailable(valued)) return;
		PsiElement destiny = ReferenceManager.resolveTaraNativeImplementationToJava(valued);
		if (destiny != null) addResult(leafOf(element), result, destiny);
	}

	private void addResult(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result, PsiElement destiny) {
		NavigationGutterIconBuilder<PsiElement> builder =
				NavigationGutterIconBuilder.create(ImplementedMethod).setTarget(destiny).setTooltipText("Navigate to the native code");
		result.add(builder.createLineMarkerInfo(element));
	}

	private boolean isAvailable(Valued valued) {
		return Primitive.FUNCTION.equals(valued.getInferredType());
	}

	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}
}