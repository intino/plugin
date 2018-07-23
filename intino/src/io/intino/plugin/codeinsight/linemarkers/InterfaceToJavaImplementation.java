package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.codeinsight.JavaHelper;
import io.intino.tara.plugin.lang.psi.TaraModel;
import io.intino.tara.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.icons.AllIcons.Gutter.ImplementedMethod;
import static io.intino.tara.plugin.lang.psi.impl.TaraUtil.configurationOf;

public class InterfaceToJavaImplementation extends RelatedItemLineMarkerProvider {

	public static Map<String, String> nodeMap = new HashMap<>();

	@Override
	protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
		if (!(element instanceof Node) || (element instanceof TaraModel) || !isInterfaceFile(element) || nodeMap.isEmpty())
			return;
		Node node = (Node) element;
		PsiElement destiny = resolveToJavaImplementation(node);
		if (destiny != null) addResult(element, result, destiny);
	}

	private void addResult(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result, PsiElement destiny) {
		NavigationGutterIconBuilder<PsiElement> builder =
				NavigationGutterIconBuilder.create(ImplementedMethod).setTarget(destiny).setTooltipText("Navigate to the native code");
		result.add(builder.createLineMarkerInfo(leafOf(element)));
	}

	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}

	private static PsiElement resolveToJavaImplementation(Node node) {
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

	private static String simpleType(Node node) {
		String type = node.type();
		if (type.contains(":")) type = type.substring(node.type().indexOf(":") + 1);
		if (type.contains(".")) {
			if (node.type().endsWith(".")) type = type.substring(0, type.length() - 1);
			else type = node.type().indexOf(".") == type.length() ? type : type.substring(node.type().indexOf(".") + 1);
		}
		return type;
	}

	private static String boxPackage(Module module) {
		final Configuration conf = configurationOf(module);
		if (conf == null) return "box";
		return conf.workingPackage() + (conf.boxPackage().isEmpty() ? "" : "." + conf.boxPackage());
	}

	private boolean isInterfaceFile(PsiElement e) {
		return e.getContainingFile().getFileType().equals(KonosFileType.instance());
	}
}