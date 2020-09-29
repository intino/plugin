package io.intino.plugin.toolwindows.dependencytree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.containers.Convertor;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dependency;
import io.intino.plugin.actions.ReloadConfigurationAction;
import io.intino.plugin.dependencyresolution.DependencyCatalog;
import io.intino.plugin.dependencyresolution.DependencyPurger;
import io.intino.plugin.dependencyresolution.ResolutionCache;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.intino.plugin.dependencyresolution.LanguageResolver.languageId;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static java.util.Comparator.comparing;

public class DependencyTreeView extends SimpleToolWindowPanel {
	private static final Logger logger = Logger.getInstance(DependencyTreeView.class.getName());

	private JPanel contentPane;
	private SimpleTree tree;
	private JScrollPane scrollPane;
	private final Project project;
	private DefaultMutableTreeNode root;

	DependencyTreeView(Project project) {
		super(true);
		this.project = project;
		initProjectTree();
	}

	Component contentPane() {
		return contentPane;
	}

	private void initProjectTree() {
		tree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (e.getButton() != 1) openContextualMenu(e, path);
				else if (selRow != -1 && e.getClickCount() == 2) goToLibrary(path);
			}
		});
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				ResolutionCache cache = ResolutionCache.instance(project);
				final DefaultMutableTreeNode component = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
				if (component.getUserObject() instanceof String) renderProject(component);
				if (component.getUserObject() instanceof ModuleNode)
					renderModule(component, moduleOf(((ModuleNode) component.getUserObject()).name), cache);
				else if (component.getUserObject() instanceof DependencyNode)
					renderLibrary(component, ((DependencyNode) component.getUserObject()), cache);
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {

			}
		});
	}

	private void openContextualMenu(MouseEvent e, TreePath treePath) {
		if (treePath == null || treePath.getParentPath() == null) return;
		Object userObject = ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
		if (!(userObject instanceof DependencyNode)) return;
		JBPopupMenu options = new JBPopupMenu("options");
		JBMenuItem item1 = reloadAction(treePath, (DependencyNode) userObject);
		JBMenuItem item2 = deleteAndReloadAction(treePath, (DependencyNode) userObject);
		options.add(item1);
		options.add(item2);
		options.show(e.getComponent(), e.getX(), e.getY());
	}

	private JBMenuItem reloadAction(TreePath treePath, DependencyNode userObject) {
		JBMenuItem item = new JBMenuItem("Reload cache of this dependency");
		item.setAction(new AbstractAction("Reload cache of this dependency") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mavenId = mavenId(userObject);
				ApplicationManager.getApplication().invokeLater(() -> invalidateAndReload(mavenId, ((DefaultMutableTreeNode) treePath.getLastPathComponent())));
			}
		});
		return item;
	}

	@NotNull
	private JBMenuItem deleteAndReloadAction(TreePath treePath, DependencyNode userObject) {
		JBMenuItem item = new JBMenuItem("Delete from Local Repository and Reload it");
		item.setAction(new AbstractAction("Delete from Local Repository and Reload it") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mavenId = mavenId(userObject);
				ApplicationManager.getApplication().invokeLater(() -> purgeAndReload(mavenId, ((DefaultMutableTreeNode) treePath.getLastPathComponent())));
			}
		});
		return item;
	}

	private void purgeAndReload(String mavenId, DefaultMutableTreeNode treeNode) {
		new DependencyPurger().purgeDependency(mavenId);
		invalidateAndReload(mavenId, treeNode);
	}

	private void invalidateAndReload(String mavenId, DefaultMutableTreeNode treeNode) {
		List<String> deps = firstLevelDependenciesWith(mavenId);
		ResolutionCache.invalidate(mavenId);
		deps.forEach(ResolutionCache::invalidate);
		DefaultMutableTreeNode target = treeNode.getAllowsChildren() ? treeNode : ((DefaultMutableTreeNode) treeNode.getParent());
		DependencyNode dependencyNode = (DependencyNode) target.getUserObject();
		Module module = dependencyNode.module;
		((LegioConfiguration) IntinoUtil.configurationOf(module)).dependencyAuditor().invalidate(dependencyNode.identifier());
		new ReloadConfigurationAction().execute(module);
	}

	private List<String> firstLevelDependenciesWith(String mavenId) {
		List<String> selected = new ArrayList<>();
		for (Object module : childrenOf(root)) {
			for (Object lib : childrenOf((DefaultMutableTreeNode) module))
				if (containsDependency((DefaultMutableTreeNode) lib, mavenId))
					selected.add(((DependencyNode) ((DefaultMutableTreeNode) lib).getUserObject()).library);
		}
		return selected;
	}

	@NotNull
	private ArrayList childrenOf(DefaultMutableTreeNode module) {
		return Collections.list(module.children());
	}

	private boolean containsDependency(DefaultMutableTreeNode element, String mavenId) {
		for (Object o : Collections.list(element.children())) {
			if (!(((DefaultMutableTreeNode) o).getUserObject() instanceof DependencyNode)) continue;
			if (mavenIdOf((DefaultMutableTreeNode) o).equals(mavenId)) return true;
		}
		return false;
	}

	private String mavenIdOf(DefaultMutableTreeNode o) {
		String library = ((DependencyNode) o.getUserObject()).library;
		return library.substring(0, library.lastIndexOf(":"));
	}

	@NotNull
	private String mavenId(DependencyNode userObject) {
		if (!userObject.label.contains(" ")) return userObject.label;
		return userObject.label.substring(0, userObject.label.lastIndexOf(" "));
	}

	private void goToLibrary(TreePath library) {
		logger.info(library.toString());
	}

	private Module moduleOf(String name) {
		return Arrays.stream(ModuleManager.getInstance(project).getModules()).filter(m -> m.getName().equals(name)).findFirst().orElse(null);
	}

	private void renderProject(DefaultMutableTreeNode parent) {
		parent.removeAllChildren();
		for (Module module : Arrays.stream(ModuleManager.getInstance(project).getModules()).sorted(comparing(Module::getName)).toArray(Module[]::new)) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ModuleNode(module.getName()));
			parent.add(node);
			node.setAllowsChildren(true);
			node.add(new DefaultMutableTreeNode(module));
		}
		tree.updateUI();
	}

	private void renderModule(DefaultMutableTreeNode parent, Module module, ResolutionCache cache) {
		parent.removeAllChildren();
		final Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) return;
		renderModel(parent, module, cache, (LegioConfiguration) configuration);
		renderDependencies(parent, module, cache, (LegioConfiguration) configuration);
		renderDataHub(parent, module, cache, (LegioConfiguration) configuration);
		tree.updateUI();
	}

	private void renderModel(DefaultMutableTreeNode parent, Module module, ResolutionCache cache, LegioConfiguration configuration) {
		Configuration.Artifact.Model model = safe(() -> configuration.artifact().model());
		if (model == null) return;
		Configuration.Artifact.Model.Language language = model.language();
		if (language.name() == null) return;
		String languageId = languageId(language.name(), language.version());
		List<DependencyCatalog.Dependency> dependencies = cache.get(languageId);
		if (dependencies != null && !dependencies.isEmpty()) {
			renderDependency(parent, module, languageId, labelIdentifier(cache, languageId));
		}
	}

	private void renderDependencies(DefaultMutableTreeNode parent, Module module, ResolutionCache cache, LegioConfiguration configuration) {
		for (Dependency dependency : safeList(() -> configuration.artifact().dependencies())) {
			renderDependency(parent, module, dependency.identifier(), labelIdentifier(cache, dependency));
		}
	}

	private void renderDataHub(DefaultMutableTreeNode parent, Module module, ResolutionCache cache, LegioConfiguration configuration) {
		Dependency.DataHub safe = safe(() -> configuration.artifact().datahub());
		if (safe != null) renderDependency(parent, module, safe.identifier(), labelIdentifier(cache, safe));
	}

	private void renderDependency(DefaultMutableTreeNode parent, Module module, String identifier, String label) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DependencyNode(module, identifier, label));
		parent.add(node);
		node.setAllowsChildren(true);
		node.add(new DefaultMutableTreeNode(module));
	}

	private void renderLibrary(DefaultMutableTreeNode parent, DependencyNode rootLibrary, ResolutionCache cache) {
		parent.removeAllChildren();
		List<DependencyCatalog.Dependency> dependencies = cache.get(rootLibrary.library);
		if (dependencies != null && !dependencies.isEmpty()) {
			dependencies = dependencies.subList(1, dependencies.size());
			for (DependencyCatalog.Dependency dep : dependencies) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DependencyNode(rootLibrary.module, dep.identifier()));
				parent.add(node);
				node.setAllowsChildren(false);
			}
		}
		tree.updateUI();
	}

	@NotNull
	private String labelIdentifier(ResolutionCache cache, Dependency dependency) {
		List<DependencyCatalog.Dependency> dependencies = cache.get(dependency.identifier());
		if (dependencies == null || dependencies.isEmpty()) return "";
		return dependencies.get(0).mavenId() + ":" + dependency.getClass().getInterfaces()[0].getSimpleName();
	}


	@NotNull
	private String labelIdentifier(ResolutionCache cache, String languageId) {
		List<DependencyCatalog.Dependency> dependencies = cache.get(languageId);
		if (dependencies == null || dependencies.isEmpty()) return "";
		return dependencies.get(0).mavenId() + ":model";
	}

	private void createUIComponents() {
		root = new DefaultMutableTreeNode(project.getName(), true);
		tree = new SimpleTree(new DefaultTreeModel(root, true));
		tree.getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);
		DefaultTreeCellRenderer renderer = new IntinoDependencyRenderer();
		renderer.setLeafIcon(AllIcons.Nodes.PpLib);
		tree.setCellRenderer(renderer);
		renderProject(root);
		tree.setVisible(true);
		tree.setRootVisible(true);
		tree.expandRow(0);
		tree.setRootVisible(false);
		scrollPane = ScrollPaneFactory.createScrollPane(tree);
	}

	private static class IntinoDependencyRenderer extends DefaultTreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
			if (tree.getModel().getRoot().equals(nodo)) setIcon(AllIcons.Nodes.ModuleGroup);
			else if (nodo.getUserObject() instanceof ModuleNode) setIcon(AllIcons.Nodes.ModuleGroup);
			else setIcon(AllIcons.Nodes.PpLib);
			return this;
		}
	}

	private static class DependencyNode {
		private final Module module;
		private final String label;
		private final String scope;
		private String library;

		DependencyNode(Module module, String library) {
			this(module, library, library);
		}

		DependencyNode(Module module, String library, String label) {
			this.module = module;
			this.library = library;
			this.label = label.isEmpty() ? library : customize(label);
			this.scope = scope(this.label);
		}

		@NotNull
		private static String customize(String library) {
			return library.substring(0, library.lastIndexOf(":")) + " (" + library.substring(library.lastIndexOf(":") + 1).toLowerCase() + ")";
		}

		@NotNull
		private static String scope(String library) {
			String l = library.contains("(") ? library.substring(library.lastIndexOf("(") + 1, library.contains(")") ? library.lastIndexOf(")") : library.length()) : library;
			return l.replace("MODEL", "COMPILE").toUpperCase();
		}

		@Override
		public String toString() {
			return label;
		}

		String identifier() {
			return library + ":" + scope;
		}
	}

	private static class TreePathStringConvertor implements Convertor<TreePath, String> {
		@Override
		public String convert(TreePath o) {
			Object node = o.getLastPathComponent();
			if (node instanceof DefaultMutableTreeNode) {
				Object object = ((DefaultMutableTreeNode) node).getUserObject();
				if (object instanceof String) return (String) object;
				else if (object instanceof DependencyNode) return ((DependencyNode) object).label;
				else if (object instanceof ModuleNode) return ((ModuleNode) object).name;
				return "";
			}
			return "";
		}
	}

	private class ModuleNode {
		private String name;

		ModuleNode(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}