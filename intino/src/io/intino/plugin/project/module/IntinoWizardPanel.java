package io.intino.plugin.project.module;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.archetype.Formatters.firstUpperCase;

public class IntinoWizardPanel {
	private final List<JButton> componentsButtons;
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
	private JButton modelButton;
	private JButton metaModelButton;
	private JButton sentinelsButton;
	private JButton agendaButton;
	private JButton webUIButton;
	private JButton EventSourcingButton;
	private JButton APIRESTServiceButton;
	private JButton workflowButton;
	private static final JBColor modelColor = new JBColor(new Color(0, 62, 87), new Color(0, 62, 87));
	private static final JBColor boxColor = new JBColor(new Color(48, 106, 42), new Color(48, 106, 42));


	public IntinoWizardPanel() {
		componentsButtons = List.of(modelButton, metaModelButton, sentinelsButton, agendaButton, webUIButton, EventSourcingButton, APIRESTServiceButton, workflowButton);
		initDiagramPanel();

	}

	private void initDiagramPanel() {
		for (JButton button : componentsButtons) {
			button.addMouseListener(new MouseListener() {
				final Color defaultColor = button.getBackground();

				@Override
				public void mouseClicked(MouseEvent mouseEvent) {
					JButton source = (JButton) mouseEvent.getSource();
					source.setSelected(!source.isSelected());
					button.setBackground(source.isSelected() ?
							selectedBackground(source) :
							new JBColor(defaultColor, defaultColor));
				}

				@Override
				public void mousePressed(MouseEvent mouseEvent) {

				}

				@Override
				public void mouseReleased(MouseEvent mouseEvent) {
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent) {
					button.setBackground(selectedBackground((JButton) mouseEvent.getSource()));

				}

				@Override
				public void mouseExited(MouseEvent mouseEvent) {
					JButton source = (JButton) mouseEvent.getSource();
					if (!source.isSelected()) button.setBackground(new JBColor(defaultColor, defaultColor));
				}
			});
		}

	}

	private Color selectedBackground(JButton source) {
		return source.getName().contains("Concept") ? modelColor : boxColor;
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


	public List<Components> components() {
		return componentsButtons.stream()
				.filter(AbstractButton::isSelected)
				.map(b -> Components.valueOf(firstUpperCase(b.getName().replace("Button", ""))))
				.collect(Collectors.toList());
	}

	public String groupId() {
		return groupId.getText();
	}

	private void createUIComponents() {
	}

	public enum Components {
		Model, Metamodel, sentinels, agenda, webUI, EventSourcing, APIRESTService, workflow
	}
}
