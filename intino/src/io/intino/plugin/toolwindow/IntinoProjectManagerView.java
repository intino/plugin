package io.intino.plugin.toolwindow;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Function;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.build.ArtifactBuilder;
import io.intino.plugin.build.LifeCyclePhase;
import io.intino.plugin.console.IntinoTopics;
import io.intino.plugin.deploy.ArtifactManager;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.TaraIcons;
import io.intino.tara.plugin.lang.file.StashFileType;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.project.configuration.ConfigurationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.build.LifeCyclePhase.DEPLOY;
import static io.intino.plugin.build.LifeCyclePhase.PREDEPLOY;

public class IntinoProjectManagerView extends JPanel {
	private JPanel contentPane;
	private JPanel modulesPanel;
	private JTabbedPane tabbedPane;
	private JTree storeTree;
	private JComboBox stores;
	private DefaultMutableTreeNode root;

	private static final Map<LifeCyclePhase, Icon> actions = new LinkedHashMap<>();

	static {
		actions.put(LifeCyclePhase.PACKAGE, IntinoIcons.BLUE);
		actions.put(LifeCyclePhase.INSTALL, IntinoIcons.GREEN);
		actions.put(LifeCyclePhase.DISTRIBUTE, IntinoIcons.YELLOW);
		actions.put(PREDEPLOY, IntinoIcons.ORANGE);
		actions.put(DEPLOY, IntinoIcons.RED);
		actions.put(LifeCyclePhase.MANAGE, IntinoIcons.MANAGE);
	}

	private Project project;

	IntinoProjectManagerView(Project project) {
		this.project = project;
		final MessageBusConnection connect = project.getMessageBus().connect();
		connect.subscribe(ProjectTopics.MODULES, new ModuleListener() {
			@Override
			public void moduleAdded(@NotNull Project project, @NotNull Module module) {
				initModuleActions(module);
			}

			@Override
			public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {

			}

			@Override
			public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
				removeModuleActions(module);
			}

			@Override
			public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
				for (Module module : modules)
					if (!module.getName().equals(oldNameProvider.fun(module)))
						renameModuleActions(oldNameProvider.fun(module), module.getName());
			}
		});

		connect.subscribe(IntinoTopics.LEGIO, moduleName -> {
			final List<Module> collect = Arrays.stream(ModuleManager.getInstance(project).getModules()).filter(m -> m.getName().equals(moduleName)).collect(Collectors.toList());
			if (collect.isEmpty()) return;
			initModuleActions(collect.get(0));
		});
		modulesPanel.setBorder(BorderFactory.createTitledBorder(project.getName()));
		for (Module module : ModuleManager.getInstance(project).getModules()) initModuleActions(module);
		addListeners();
	}

	private void createUIComponents() {
		modulesPanel = new JPanel();
		modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.Y_AXIS));
		root = new DefaultMutableTreeNode("Store", true);
		storeTree = new Tree(root);
		storeTree.setRootVisible(false);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(TaraIcons.STASH_16);
		storeTree.setCellRenderer(renderer);
	}

	private void initModuleActions(Module module) {
		Configuration configuration = loadConfiguration(module);
		if (configuration == null) return;
		JPanel panel = new JPanel();
		panel.setName(module.getName());
		BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
		panel.setLayout(layout);
		JBLabel label = new JBLabel(module.getName());
		label.setMinimumSize(new Dimension(100, 30));
		label.setPreferredSize(new Dimension(100, 30));
		label.setMaximumSize(new Dimension(100, 30));
		label.setAlignmentX(LEFT_ALIGNMENT);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(label);
		for (LifeCyclePhase action : actions.keySet()) {
			JButton button = new JButton(actions.get(action));
			customizeButton(button, module, module.getName() + "_" + action.name().toLowerCase(), action.name().toLowerCase());
			if (!suitable(action, configuration)) continue;
			panel.add(Box.createRigidArea(new Dimension(PREDEPLOY.equals(action) ? 20 : 10, 0)));
			panel.add(button);
			if (!isAvailable(configuration, action)) button.setEnabled(false);
		}
		panel.setAlignmentX(LEFT_ALIGNMENT);
		modulesPanel.add(panel);
	}

	private Configuration loadConfiguration(Module module) {
		Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration == null) {
			configuration = ConfigurationManager.newSuitableProvider(module);
			if (configuration != null) ConfigurationManager.register(module, configuration);
		}
		return configuration;
	}

	private boolean suitable(LifeCyclePhase action, Configuration configuration) {
		if (!(configuration instanceof LegioConfiguration)) return false;
		if (((LegioConfiguration) configuration).factory() != null)
			if (action.equals(LifeCyclePhase.MANAGE) && !((LegioConfiguration) configuration).factory().isSystem())
				return false;
		return true;
	}

	private void removeModuleActions(Module module) {
		Component toRemove = null;
		for (Component component : modulesPanel.getComponents())
			if (component instanceof JPanel && component.getName().equals(module.getName())) toRemove = component;
		if (toRemove != null) modulesPanel.remove(toRemove);
	}

	private void renameModuleActions(String oldName, String newName) {
		for (Component component : modulesPanel.getComponents())
			if (component instanceof JPanel && component.getName().equals(oldName)) {
				component.setName(newName);
				((JBLabel) ((JPanel) component).getComponent(0)).setText(newName);
			}
	}

	private boolean isAvailable(Configuration configuration, LifeCyclePhase action) {
		if (!PREDEPLOY.equals(action) && DEPLOY.equals(action)) return true;
		return configuration != null && (configuration.level() != null || Configuration.Level.System.equals(configuration.level()));
	}

	private void customizeButton(JButton button, Module module, String name, String action) {
		button.setAlignmentX(LEFT_ALIGNMENT);
		button.setToolTipText(action);
		button.setName(name);
		button.setBorder(null);
		button.setMinimumSize(new Dimension(20, 20));
		button.setPreferredSize(new Dimension(20, 20));
		button.setMaximumSize(new Dimension(20, 20));
		button.setBorderPainted(false);
		button.setBackground(null);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setOpaque(false);
		button.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				button.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1)));
				button.setBorderPainted(true);
				button.setOpaque(true);
				button.repaint();
			}

			public void mouseExited(MouseEvent e) {
				button.setOpaque(false);
				button.setBorder(null);
				button.setBorderPainted(false);
				button.setBackground(UIManager.getColor("control"));
			}

			public void mouseClicked(MouseEvent e) {
				final LifeCyclePhase phase = LifeCyclePhase.valueOf(action.toUpperCase());
				if (phase.equals(LifeCyclePhase.MANAGE)) new ArtifactManager(module).start();
				else new ArtifactBuilder(project, Collections.singletonList(module), phase).build();
			}
		});
	}

	private void addListeners() {
		stores.addItemListener(e -> reloadTree());
		stores.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent event) {
				if (event.getKeyChar() == KeyEvent.VK_ENTER) {
					if (!((JTextComponent) ((JComboBox) ((Component) event
							.getSource()).getParent()).getEditor()
							.getEditorComponent()).getText().isEmpty()) return;
//						addStore();
				}
			}
		});
		storeTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = storeTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = storeTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 2) {
						openFile(selPath);
					}
				}
			}
		});
		storeTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				final DefaultMutableTreeNode component = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
				renderDirectory(component, component.getUserObject() instanceof String ?
						getSelectedStore() :
						((FileNode) component.getUserObject()).file);
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {

			}
		});
	}

	private void openFile(TreePath path) {
		final FileNode nodeFile = (FileNode) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		final File file = nodeFile.file;
		final VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, true);
		if (virtualFile == null) return;
		final PsiFile psiFile = PsiManager.getInstance(this.project).findFile(virtualFile);
		if (psiFile != null) psiFile.navigate(true);
	}

	private void reloadTree() {
		final File store = getSelectedStore();
		root.removeAllChildren();
		if (store == null) {
			storeTree.setRootVisible(false);
			storeTree.setVisible(false);
		} else {
			renderDirectory(root, store);
			storeTree.setRootVisible(true);
			storeTree.setVisible(true);
			storeTree.expandRow(0);
		}
	}

	private File getSelectedStore() {
		return this.stores.getSelectedItem() != null ? new File(this.stores.getSelectedItem().toString()) : null;
	}

	private void renderDirectory(DefaultMutableTreeNode parent, File directory) {
		parent.removeAllChildren();
		for (File file : directory.listFiles(candidate -> candidate.isDirectory() || candidate.getName().endsWith("." + StashFileType.INSTANCE.getDefaultExtension()))) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FileNode(file));
			parent.add(node);
			if (file.isDirectory()) node.add(new DefaultMutableTreeNode(""));
		}
		storeTree.updateUI();
	}

	private class FileNode {
		private File file;

		FileNode(File file) {
			this.file = file;
		}

		@Override
		public String toString() {
			return file.getName().replace(".stash", "");
		}
	}

	Component contentPane() {
		return contentPane;
	}
}