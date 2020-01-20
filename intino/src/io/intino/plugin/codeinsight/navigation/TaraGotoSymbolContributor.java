package io.intino.plugin.codeinsight.navigation;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.lang.model.Node;
import org.intellij.plugins.relaxNG.GotoSymbolContributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaraGotoSymbolContributor extends GotoSymbolContributor {
	@NotNull
	@Override
	public String[] getNames(Project project, boolean includeNonProjectItems) {
		List<String> names = getNodes(project).stream().map(Node::qualifiedName).collect(Collectors.toList());
		return names.toArray(new String[names.size()]);
	}

	@NotNull
	@Override
	public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
		final List<Node> all = getNodes(project);
		final List<NavigationItem> items = all.stream().filter(node -> node.qualifiedName().startsWith(pattern)).map(n -> (NavigationItem) n).collect(Collectors.toList());
		return items.toArray(new NavigationItem[items.size()]);
	}

	private List<Node> getNodes(Project project) {
		List<Node> names = new ArrayList<>();
		for (Module module : ModuleManager.getInstance(project).getModules())
			TaraUtil.getTaraFilesOfModule(module).forEach(model -> names.addAll(TaraUtil.getAllNodesOfFile(model)));
		return names;
	}

}
