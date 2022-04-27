package io.intino.plugin.project.module;

import javax.swing.*;

public class IntinoWizardPanel {
	private JRadioButton business;
	private JRadioButton amidas;
	private JRadioButton sumus;
	private JRadioButton datahub;
	private JRadioButton arquetype;
	private JPanel root;
	private JTextField groupId;


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

}
