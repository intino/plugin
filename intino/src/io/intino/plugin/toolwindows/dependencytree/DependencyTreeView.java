package io.intino.plugin.toolwindows.dependencytree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dependency;
import io.intino.plugin.actions.ReloadConfigurationAction;
import io.intino.plugin.dependencyresolution.DependencyPurger;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import static io.intino.plugin.dependencyresolution.LanguageResolver.runtimeCoors;
import static io.intino.plugin.dependencyresolution.MavenDependencyResolver.dependenciesFrom;
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
		project.getMessageBus().connect().subscribe(ModuleListener.TOPIC, new ModuleListener() {
			@Override
			public void modulesAdded(@NotNull Project project, @NotNull List<? extends Module> modules) {
				renderProject(root);
			}

			@Override
			public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
				renderProject(root);
			}
		});
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
		if (treePath == null || treePath.getParentPath() == null) return;
		Object userObject = ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
		if (!(userObject instanceof DependencyNode)) return;
		JBPopupMenu options = new JBPopupMenu("options");
		options.add(deleteAndReloadAction(treePath, (DependencyNode) userObject));
		options.show(e.getComponent(), e.getX(), e.getY());
	}

	@NotNull
	private JBMenuItem deleteAndReloadAction(TreePath treePath, DependencyNode userObject) {
		JBMenuItem item = new JBMenuItem("Delete from Local Repository, flushed cache, and Reload it");
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
		MavenDependencyResolver.resetSession();
		new DependencyPurger().purgeDependency(mavenId);
		invalidateAndReload(treeNode);
	}

	private void invalidateAndReload(DefaultMutableTreeNode treeNode) {
		DefaultMutableTreeNode target = treeNode.getAllowsChildren() ? treeNode : ((DefaultMutableTreeNode) treeNode.getParent());
		new ReloadConfigurationAction().execute(((DependencyNode) target.getUserObject()).module);
	}

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
			DefaultMutableTreeNode mogram = new DefaultMutableTreeNode(new ModuleNode(module.getName()));
			parent.add(mogram);
			mogram.setAllowsChildren(true);
			mogram.add(new DefaultMutableTreeNode(module));
		}
		tree.updateUI();
	}

	private void renderModule(DefaultMutableTreeNode parent, Module module) {
		parent.removeAllChildren();
		final Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof ArtifactLegioConfiguration)) return;
		MavenDependencyResolver resolver = new MavenDependencyResolver(Repositories.of(module));
		configuration.artifact().dsls().forEach(dsl -> renderDsl(parent, module, resolver, dsl));
		renderDataHub(parent, module, (ArtifactLegioConfiguration) configuration);
		renderArchetype(parent, module, (ArtifactLegioConfiguration) configuration);
		renderDependencies(parent, module, (ArtifactLegioConfiguration) configuration);
		tree.updateUI();
	}

	private void renderDsl(DefaultMutableTreeNode parent, Module module, MavenDependencyResolver resolver, Configuration.Artifact.Dsl dsl) {
		if (dsl.name() == null) return;
		try {
			String runtimeCoors = runtimeCoors(dsl.name(), dsl.version());
			var dependencies = resolver.resolve(new DefaultArtifact(runtimeCoors), JavaScopes.COMPILE);
			renderDependency(parent, module, runtimeCoors, dslLabel(dependencies));
		} catch (DependencyResolutionException ignored) {
		}
	}

	private void renderDependencies(DefaultMutableTreeNode parent, Module module, ArtifactLegioConfiguration configuration) {
		for (Dependency dependency : safeList(() -> configuration.artifact().dependencies()))
			renderDependency(parent, module, dependency.identifier(), labelIdentifier(dependency));
	}

	private void renderDataHub(DefaultMutableTreeNode parent, Module module, ArtifactLegioConfiguration configuration) {
		Dependency.DataHub safe = safe(() -> configuration.artifact().datahub());
		if (safe != null) renderDependency(parent, module, safe.identifier(), labelIdentifier(safe));
	}

	private void renderArchetype(DefaultMutableTreeNode parent, Module module, ArtifactLegioConfiguration configuration) {
		Dependency.Archetype safe = safe(() -> configuration.artifact().archetype());
		if (safe != null) renderDependency(parent, module, safe.identifier(), labelIdentifier(safe));
	}

	private void renderDependency(DefaultMutableTreeNode parent, Module module, String identifier, String label) {
		DefaultMutableTreeNode mogram = new DefaultMutableTreeNode(new DependencyNode(module, identifier, label));
		parent.add(mogram);
		mogram.setAllowsChildren(true);
		mogram.add(new DefaultMutableTreeNode(module));
	}

	private void renderLibrary(DefaultMutableTreeNode parent, DependencyNode rootLibrary) {
		parent.removeAllChildren();
		MavenDependencyResolver resolver = new MavenDependencyResolver(Repositories.of(moduleOf(((ModuleNode) ((DefaultMutableTreeNode) parent.getParent()).getUserObject()).name)));
		try {
			var result = dependenciesFrom(resolver.resolve(new DefaultArtifact(rootLibrary.library), rootLibrary.scope), false);
			var dependencies = result.subList(1, result.size());
			for (org.eclipse.aether.graph.Dependency dep : dependencies) {
				DefaultMutableTreeNode mogram = new DefaultMutableTreeNode(new DependencyNode(rootLibrary.module, label(dep.getArtifact()) + ":" + dep.getScope()));
				parent.add(mogram);
				mogram.setAllowsChildren(false);
			}
			tree.updateUI();
		} catch (DependencyResolutionException ignored) {
		}
	}

	@NotNull
	private String labelIdentifier(Dependency dependency) {
		return dependency.identifier() + ":" + dependency.getClass().getInterfaces()[0].getSimpleName();
	}


	@NotNull
	private String dslLabel(DependencyResult result) {
		Artifact artifact = dependenciesFrom(result, false).get(0).getArtifact();
		return label(artifact) + ":dsl";
	}

	@NotNull
	private static String label(Artifact artifact) {
		return String.join(":", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
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
		private final String library;

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

	}

	private static class ModuleNode {
		private final String name;

		ModuleNode(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}