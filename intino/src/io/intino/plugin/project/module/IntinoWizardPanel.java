package io.intino.plugin.project.module;

import com.intellij.util.ui.UIUtil;
import io.intino.alexandria.logger.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class IntinoWizardPanel {
	private JRadioButton business;
	private JRadioButton amidas;
	private JRadioButton sumus;
	private JRadioButton datahub;
	private JRadioButton arquetype;
	private JPanel root;
	private JTextField groupId;
	private JPanel diagramBox;
	private JPanel boxPanel;
	private JPanel modelPanel;
	private JButton conceptsButton;
	private JButton sentinelsButton;
	private JButton agendaButton;
	private JPanel aaa;


	public IntinoWizardPanel() {
	}

	private void initImagePanel() {
		final String name = UIManager.getLookAndFeel().getName();
		boolean retina = UIUtil.isRetina();
		final boolean dark = name.equalsIgnoreCase("Darcula") || name.equalsIgnoreCase("High Contrast");
		try {
			diagramBox = new JPanel();
			final JLabel label = new JLabel(new ImageIcon(this.getClass().getResourceAsStream("/module/module_structure_" + (dark ? "dark" : "light") + (retina ? "_retina" : "") + ".png").readAllBytes()));
			final Dimension dimension = new Dimension(400 * (retina ? 1 : 2), 400 * (retina ? 1 : 2));
			diagramBox.setSize(dimension);
			diagramBox.setPreferredSize(dimension);
			diagramBox.setMaximumSize(dimension);
			label.setSize(dimension);
			label.setMaximumSize(dimension);
			diagramBox.add(label);
			diagramBox.setBounds(0, 50, dimension.width, dimension.height);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public JPanel panel() {
		return root;
	}

	public IntinoModuleType.Type selected() {
		if (business.isSelected()) return IntinoModuleType.Type.Business;
		if (amidas.isSelected()) return IntinoModuleType.Type.Amidas;
		if (sumus.isSelected()) return IntinoModuleType.Type.Susmus;
		if (datahub.isSelected()) return IntinoModuleType.Type.Datahub;
		return IntinoModuleType.Type.Archetype;
	}

	public String groupId() {
		return groupId.getText();
	}

	private void createUIComponents() {
		initImagePanel();
	}
}
