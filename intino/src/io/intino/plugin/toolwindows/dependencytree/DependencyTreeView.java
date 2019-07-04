package io.intino.plugin.toolwindows.dependencytree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.treeStructure.Tree;
import io.intino.legio.graph.Artifact.Imports.Dependency;
import io.intino.plugin.actions.ReloadConfigurationAction;
import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.dependencyresolution.DependencyCatalog;
import io.intino.plugin.dependencyresolution.DependencyPurger;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.plugin.project.Safe.safeList;

public class DependencyTreeView extends JPanel {
	private JPanel contentPane;
	private JTree tree;
	private Project project;
	private DefaultMutableTreeNode root;
	private Map<Module, DependencyAuditor> dependencyAuditors;


	DependencyTreeView(Project project) {
		this.project = project;
		this.dependencyAuditors = new HashMap<>();
		initProjectTree();
	}

	Component contentPane() {
		return contentPane;
	}

	private void initProjectTree() {
		tree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath library = tree.getPathForLocation(e.getX(), e.getY());
				if (e.getButton() != 1) {
					openContextualMenu(e, library);
				}
				if (selRow != -1) {
					if (e.getClickCount() == 2) goToLibrary(library);
				}
			}
		});
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				final DefaultMutableTreeNode component = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
				if (component.getUserObject() instanceof String) renderProject(component);
				if (component.getUserObject() instanceof ModuleNode)
					renderModule(component, moduleOf(((ModuleNode) component.getUserObject()).name));
				else if (component.getUserObject() instanceof DependencyNode)
					renderLibrary(component, ((DependencyNode) component.getUserObject()));
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {

			}
		});
	}

	private void openContextualMenu(MouseEvent e, TreePath treePath) {
		Object userObject = ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
		if (!(userObject instanceof DependencyNode)) return;
		JBPopupMenu options = new JBPopupMenu("options");
		JBMenuItem jbMenuItem = new JBMenuItem("Delete from Local Repository and Reload it");
		jbMenuItem.setAction(new AbstractAction("Delete from Local Repository and Reload it") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String library = ((DependencyNode) userObject).label;
				new DependencyPurger().purgeDependency(library.substring(0, library.lastIndexOf(" ")));
				ApplicationManager.getApplication().invokeLater(() -> new ReloadConfigurationAction().execute((((DependencyNode) userObject).module)));
			}
		});
		options.add(jbMenuItem);
		options.show(e.getComponent(), e.getX(), e.getY());
	}

	private void goToLibrary(TreePath library) {
		System.out.println(library.toString());
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
		tree.updateUI();
	}

	private void renderModule(DefaultMutableTreeNode parent, Module module) {
		parent.removeAllChildren();
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) return;
		DependencyAuditor auditor = new DependencyAuditor(module);
		dependencyAuditors.put(module, auditor);
		for (Dependency dependency : safeList(() -> ((LegioConfiguration) configuration).graph().artifact().imports().dependencyList())) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DependencyNode(module, dependency.identifier() + ":" + dependency.getClass().getSimpleName(), labelIdentifier(auditor, dependency)));
			parent.add(node);
			node.setAllowsChildren(true);
			node.add(new DefaultMutableTreeNode(module));
		}
		tree.updateUI();
	}

	@NotNull
	private String labelIdentifier(DependencyAuditor auditor, Dependency dependency) {
		return auditor.get(dependency.identifier() + ":" + dependency.getClass().getSimpleName()).get(0).mavenId() + ":" + dependency.getClass().getSimpleName();
	}

	private void renderLibrary(DefaultMutableTreeNode parent, DependencyNode rootLibrary) {
		parent.removeAllChildren();
		List<DependencyCatalog.Dependency> dependencies = dependencyAuditors.get(rootLibrary.module).get(rootLibrary.library);
		if (dependencies != null) {
			dependencies = dependencies.subList(1, dependencies.size());
			for (DependencyCatalog.Dependency dep : dependencies) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DependencyNode(rootLibrary.module, dep.identifier()));
				parent.add(node);
				node.setAllowsChildren(false);
			}
		}
		tree.updateUI();
	}

	private void createUIComponents() {
		root = new DefaultMutableTreeNode(project.getName(), true);
		tree = new Tree(root);
		DefaultTreeCellRenderer renderer = new IntinoDependencyRenderer();
		renderer.setLeafIcon(AllIcons.Modules.Library);
		tree.setCellRenderer(renderer);
		renderProject(root);
		tree.setVisible(true);
		tree.setRootVisible(true);
		tree.expandRow(0);
		tree.setRootVisible(false);
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

	private static class DependencyNode {
		private final Module module;
		private final String label;
		private String library;

		DependencyNode(Module module, String library) {
			this(module, library, label(library));
		}

		DependencyNode(Module module, String library, String label) {
			this.module = module;
			this.library = library;
			this.label = label(label);
		}

		@NotNull
		private static String label(String library) {
			return library.substring(0, library.lastIndexOf(":")) + " (" + library.substring(library.lastIndexOf(":") + 1).toLowerCase() + ")";
		}

		@Override
		public String toString() {
			return label;
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