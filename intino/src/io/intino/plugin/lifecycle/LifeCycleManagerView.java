package io.intino.plugin.lifecycle;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.Function;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.build.ArtifactBuilder;
import io.intino.plugin.build.LifeCyclePhase;
import io.intino.plugin.console.IntinoTopics;
import io.intino.plugin.deploy.ArtifactManager;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.project.configuration.ConfigurationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class LifeCycleManagerView extends JPanel {
	private JPanel contentPane;
	private JPanel modulesPanel;
	private static final Map<LifeCyclePhase, Icon> actions = new LinkedHashMap<>();

	static {
		actions.put(LifeCyclePhase.PACKAGE, IntinoIcons.BLUE);
		actions.put(LifeCyclePhase.INSTALL, IntinoIcons.GREEN);
		actions.put(LifeCyclePhase.DISTRIBUTE, IntinoIcons.YELLOW);
		actions.put(LifeCyclePhase.PREDEPLOY, IntinoIcons.ORANGE);
		actions.put(LifeCyclePhase.DEPLOY, IntinoIcons.RED);
		actions.put(LifeCyclePhase.MANAGE, IntinoIcons.MANAGE);
	}

	private Project project;

	LifeCycleManagerView(Project project) {
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
	}

	private void createUIComponents() {
		modulesPanel = new JPanel();
		modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.Y_AXIS));
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
			panel.add(Box.createRigidArea(new Dimension(LifeCyclePhase.PREDEPLOY.equals(action) ? 20 : 10, 0)));
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
		if (!LifeCyclePhase.PREDEPLOY.equals(action) && LifeCyclePhase.DEPLOY.equals(action)) return true;
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

	Component contentPane() {
		return contentPane;
	}
}