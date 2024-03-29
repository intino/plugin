package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.plugin.codeinsight.JavaHelper;
import io.intino.plugin.file.KonosFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.icons.AllIcons.Gutter.ImplementedMethod;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.configurationOf;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.simpleType;
import static io.intino.plugin.project.Safe.safe;

public class InterfaceToJavaImplementation extends RelatedItemLineMarkerProvider {

	public static Map<String, String> nodeMap = new HashMap<>();

	@Override
	protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
		if (!(element instanceof Mogram) || (element instanceof TaraModel) || !isInterfaceFile(element) || nodeMap.isEmpty())
			return;
		Mogram mogram = (Mogram) element;
		PsiElement destiny = resolveToJavaImplementation(mogram);
		if (destiny != null) addResult(element, result, destiny);
	}

	private PsiElement resolveToJavaImplementation(Mogram node) {
		final String type = simpleType(node);
		String key = type + "#" + node.name();
		if (node.name().isEmpty()) return null;
		else if (!nodeMap.containsKey(key)) {
			key = type + "#" + node.container().name();
			if (!nodeMap.containsKey(key)) return null;
		}
		Module module = ModuleProvider.moduleOf((PsiElement) node);
		if (module == null) return null;
		return JavaHelper.getJavaHelper(((PsiElement) node).getProject()).findClass(boxPackage(module).toLowerCase() + "." + nodeMap.get(key));
	}

	private void addResult(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result, PsiElement destiny) {
		NavigationGutterIconBuilder<PsiElement> builder =
				NavigationGutterIconBuilder.create(ImplementedMethod).setTarget(destiny).setTooltipText("Navigate to the native code");
		result.add(builder.createLineMarkerInfo(leafOf(element)));
	}



	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}

	private String boxPackage(Module module) {
		final Configuration conf = configurationOf(module);
		if (conf == null) return "box";
		Configuration.Artifact.Box box = safe(() -> conf.artifact().box());
		if (box == null || box.targetPackage() == null) return "box";
		return conf.artifact().code().generationPackage() + (box.targetPackage().isEmpty() ? "" : "." + box.targetPackage());
	}

	private boolean isInterfaceFile(PsiElement e) {
		return e.getContainingFile().getFileType().equals(KonosFileType.instance());
	}
}