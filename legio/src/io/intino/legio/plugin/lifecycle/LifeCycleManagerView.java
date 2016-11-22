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

package io.intino.legio.plugin.lifecycle;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import io.intino.legio.plugin.LegioIcons;
import io.intino.legio.plugin.build.ArtifactManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static io.intino.legio.plugin.build.LifeCyclePhase.PREDEPLOY;
import static io.intino.legio.plugin.build.LifeCyclePhase.valueOf;

public class LifeCycleManagerView extends JPanel {
	private JPanel contentPane;
	private JPanel modulesPanel;
	private static final Map<String, Icon> actions = new LinkedHashMap<>();

	static {
		actions.put("package", LegioIcons.BLUE);
		actions.put("install", LegioIcons.GREEN);
		actions.put("distribute", LegioIcons.YELLOW);
		actions.put("predeploy", LegioIcons.ORANGE);
		actions.put("deploy", LegioIcons.RED);
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
			label.setMinimumSize(new Dimension(100, 20));
			label.setPreferredSize(new Dimension(100, 20));
			label.setMaximumSize(new Dimension(100, 20));
			label.setAlignmentX(LEFT_ALIGNMENT);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			panel.add(label);
			for (String action : actions.keySet()) {
				panel.add(Box.createRigidArea(new Dimension(PREDEPLOY.name().equals(action.toUpperCase()) ? 20 : 10, 0)));
				JButton button = new JButton(actions.get(action));
				customizeButton(button, module.getName() + "_" + action, action);
				button.addActionListener(e -> new ArtifactManager(project, Collections.singletonList(module), valueOf(action.toUpperCase())).publish());
				panel.add(button);
			}
			panel.setAlignmentX(LEFT_ALIGNMENT);
			modulesPanel.add(panel);
		}

	}

	private void customizeButton(JButton button, String name, String action) {
		button.setAlignmentX(LEFT_ALIGNMENT);
		button.setToolTipText(action);
		button.setName(name);
		button.setBorder(null);
		button.setBorderPainted(false);
		button.setBackground(null);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setOpaque(false);
	}

	Component contentPane() {
		return contentPane;
	}
}