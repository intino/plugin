/*
 *  Copyright 2013-2016 SIANI - ULPGC
 *
 *  This File is part of JavaFMI Project
 *
 *  JavaFMI Project is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License.
 *
 *  JavaFMI Project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JavaFMI. If not, see <http://www.gnu.org/licenses/>.
 */

package io.intino.plugin.lifecycle;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.build.ArtifactManager;
import io.intino.plugin.build.LifeCyclePhase;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.psi.impl.TaraUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class LifeCycleManagerView extends JPanel {
	private JPanel contentPane;
	private JPanel modulesPanel;
	private static final Map<String, Icon> actions = new LinkedHashMap<>();

	static {
		actions.put("package", IntinoIcons.BLUE);
		actions.put("install", IntinoIcons.GREEN);
		actions.put("distribute", IntinoIcons.YELLOW);
		actions.put("predeploy", IntinoIcons.ORANGE);
		actions.put("deploy", IntinoIcons.RED);
	}

	private Project project;

	LifeCycleManagerView(Project project) {
		this.project = project;
		modulesPanel.setBorder(BorderFactory.createTitledBorder(project.getName()));
		initModuleActions(Arrays.asList(ModuleManager.getInstance(project).getModules()));
	}

	private void createUIComponents() {
		modulesPanel = new JPanel();
		modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.Y_AXIS));
	}

	private void initModuleActions(List<Module> modules) {
		for (Module module : modules) {
			JPanel panel = new JPanel();
			BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
			panel.setLayout(layout);
			JBLabel label = new JBLabel(module.getName());
			label.setMinimumSize(new Dimension(100, 30));
			label.setPreferredSize(new Dimension(100, 30));
			label.setMaximumSize(new Dimension(100, 30));
			label.setAlignmentX(LEFT_ALIGNMENT);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			panel.add(label);
			for (String action : actions.keySet()) {
				panel.add(Box.createRigidArea(new Dimension(LifeCyclePhase.PREDEPLOY.name().equals(action.toUpperCase()) ? 20 : 10, 0)));
				JButton button = new JButton(actions.get(action));
				customizeButton(button, module, module.getName() + "_" + action, action);
				panel.add(button);
				if (!isAvailable(module, action)) button.setEnabled(false);
			}
			panel.setAlignmentX(LEFT_ALIGNMENT);
			modulesPanel.add(panel);
		}

	}

	private boolean isAvailable(Module module, String action) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (!action.equalsIgnoreCase(LifeCyclePhase.PREDEPLOY.name()) && action.equalsIgnoreCase(LifeCyclePhase.DEPLOY.name())) return true;
		return configuration != null && (configuration.level() != null || configuration.level().equals(Configuration.Level.System));
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
			@Override
			public void mouseEntered(MouseEvent e) {
				button.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1)));
				button.setBorderPainted(true);
				button.setOpaque(true);
				button.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setOpaque(false);
				button.setBorder(null);
				button.setBorderPainted(false);
				button.setBackground(UIManager.getColor("control"));
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				new ArtifactManager(project, Collections.singletonList(module), LifeCyclePhase.valueOf(action.toUpperCase())).process();
			}
		});
	}

	Component contentPane() {
		return contentPane;
	}
}