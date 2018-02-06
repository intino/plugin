package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.NodeRoot;
import io.intino.tara.plugin.codeinsight.JavaHelper;
import io.intino.tara.plugin.lang.psi.TaraModel;
import io.intino.tara.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Stack;

import static com.intellij.icons.AllIcons.Gutter.ImplementedMethod;
import static io.intino.tara.plugin.lang.psi.impl.TaraUtil.configurationOf;

public class InterfaceToJavaImplementation extends RelatedItemLineMarkerProvider {

	@Override
	protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
		if (!(element instanceof Node) || (element instanceof TaraModel) || !isInterfaceFile(element)) return;
		Node node = (Node) element;
		PsiElement destiny = resolveToJavaImplementation(node);
		if (destiny != null) addResult(element, result, destiny);
	}

	private void addResult(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result, PsiElement destiny) {
		NavigationGutterIconBuilder<PsiElement> builder =
				NavigationGutterIconBuilder.create(ImplementedMethod).setTarget(destiny).setTooltipText("Navigate to the native code");
		result.add(builder.createLineMarkerInfo(element));
	}


	private static PsiElement resolveToJavaImplementation(Node node) {
		Module module = ModuleProvider.moduleOf((PsiElement) node);
		if (module == null) return null;
		String workingPackage = boxPackage(module);
		String qn = qn(node);
		return qn.isEmpty() ? null : JavaHelper.getJavaHelper(((PsiElement) node).getProject()).findClass(workingPackage.toLowerCase() + ".displays" + qn);
	}

	private static String qn(Node node) {
		Stack<Node> chain = new Stack<>();
		collectAncestors(node, chain);
		if (chain.isEmpty()) return "";
		return buildQn(chain);
	}

	private static String buildQn(Stack<Node> chain) {
		StringBuilder builder = new StringBuilder();
		while (!chain.isEmpty()) builder.append(".").append(firstUpperCase(chain.pop().name()));
		return builder.toString();
	}

	private static void collectAncestors(Node node, Stack<Node> stack) {
		if (node.isAnonymous()) {
			stack.clear();
			return;
		}
		stack.add(node);
		if (node.container() != null && !(node.container() instanceof NodeRoot))
			collectAncestors(node.container(), stack);
	}

	private static String boxPackage(Module module) {
		final Configuration conf = configurationOf(module);
		if (conf == null) return "box";
		return conf.workingPackage() + (conf.boxPackage().isEmpty() ? "" : "." + conf.boxPackage());
	}

	private boolean isInterfaceFile(PsiElement e) {
		return e.getContainingFile().getFileType().equals(KonosFileType.instance());
	}

	private static String firstUpperCase(String value) {
		return value.substring(0, 1).toUpperCase() + value.substring(1);
	}
}