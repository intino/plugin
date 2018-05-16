package io.intino.plugin.toolwindows.dependencytree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import io.intino.legio.graph.Artifact.Imports.Dependency;
import io.intino.plugin.dependencyresolution.DependencyLogger;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import static io.intino.plugin.project.Safe.safeList;

public class DependencyTreeView extends JPanel {
	private JPanel contentPane;
	private JTree dependencyTree;
	private Project project;
	private DefaultMutableTreeNode root;


	DependencyTreeView(Project project) {
		this.project = project;
		initProjectTree();
	}

	Component contentPane() {
		return contentPane;
	}

	private void initProjectTree() {
		dependencyTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = dependencyTree.getRowForLocation(e.getX(), e.getY());
				TreePath library = dependencyTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 2) {
						goToLibrary(library);
					}
				}
			}
		});
		dependencyTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				final DefaultMutableTreeNode component = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
				if (component.getUserObject() instanceof String) renderProject(component);
				if (component.getUserObject() instanceof ModuleNode)
					renderModule(component, moduleOf(((ModuleNode) component.getUserObject()).name));
				else if (component.getUserObject() instanceof DependencyNode)
					renderLibrary(component, ((DependencyNode) component.getUserObject()));
				else renderLibrary(component, component.getUserObject().toString());
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {

			}
		});
	}

	private void goToLibrary(TreePath library) {

	}

	private Module moduleOf(String name) {
		return Arrays.stream(ModuleManager.getInstance(project).getModules()).filter(m -> m.getName().equals(name)).findFirst().orElse(null);
	}

	private void renderProject(DefaultMutableTreeNode parent) {
		parent.removeAllChildren();
		for (Module module : ModuleManager.getInstance(project).getModules()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ModuleNode(module.getName()));
			parent.add(node);
			node.setAllowsChildren(true);
			node.add(new DefaultMutableTreeNode(module));
		}
		dependencyTree.updateUI();
	}

	private void renderModule(DefaultMutableTreeNode parent, Module module) {
		parent.removeAllChildren();
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) return;
		for (Dependency dependency : safeList(() -> ((LegioConfiguration) configuration).graph().artifact().imports().dependencyList())) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DependencyNode(dependency.identifier()));
			parent.add(node);
			node.setAllowsChildren(true);
			node.add(new DefaultMutableTreeNode(module));
		}

		dependencyTree.updateUI();
	}

	private void renderLibrary(DefaultMutableTreeNode parent, DependencyNode library) {
		parent.removeAllChildren();
		final List<String> libraries = DependencyLogger.instance().dependencyTree().get(library.library);
		addLibraries(parent, libraries);
		dependencyTree.updateUI();
	}

	private void renderLibrary(DefaultMutableTreeNode parent, String library) {
		parent.removeAllChildren();
		final List<String> libraries = DependencyLogger.instance().dependencyTree().get(library);
		addLibraries(parent, libraries);
		dependencyTree.updateUI();
	}

	private void addLibraries(DefaultMutableTreeNode parent, List<String> libraries) {
		if (libraries != null) for (String lib : libraries) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DependencyNode(lib));
			parent.add(node);
			node.setAllowsChildren(false);
		}
	}

	private void createUIComponents() {
		root = new DefaultMutableTreeNode(project.getName(), true);
		dependencyTree = new Tree(root);
		dependencyTree.setRootVisible(true);
		DefaultTreeCellRenderer renderer = new IntinoDependencyRenderer();
		renderer.setLeafIcon(AllIcons.Modules.Library);
		dependencyTree.setCellRenderer(renderer);
		dependencyTree.setVisible(true);
		renderProject(root);
		dependencyTree.expandRow(0);
	}

	private class DependencyNode {
		private String library;

		DependencyNode(String library) {
			this.library = library;
		}

		@Override
		public String toString() {
			return library;
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


	private static class IntinoDependencyRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
			if (tree.getModel().getRoot().equals(nodo)) {
				setIcon(AllIcons.Modules.ModulesNode);
			} else if (nodo.getUserObject() instanceof ModuleNode) {
				setIcon(AllIcons.Modules.ModulesNode);
			} else {
				setIcon(AllIcons.Modules.Library);
			}
			return this;
		}
	}
}