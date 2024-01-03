package io.intino.plugin.project.module;

import com.intellij.ui.JBColor;
import io.intino.plugin.project.configuration.ModuleTemplateDeployer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

import static io.intino.plugin.archetype.Formatters.firstUpperCase;
import static io.intino.plugin.project.module.IntinoWizardPanel.Components.Model;

public class IntinoWizardPanel {
	private final List<JButton> componentsButtons;
	private final List<JRadioButton> infrastructureButtons;
	private final boolean templatesAvailable;
	private JRadioButton business;
	private JRadioButton amidas;
	private JRadioButton sumus;
	private JRadioButton datahub;
	private JRadioButton archetype;
	private JPanel root;
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
	private JLabel errorLabel;
	private static final JBColor modelColor = new JBColor(new Color(0, 62, 87), new Color(0, 62, 87));
	private static final JBColor boxColor = new JBColor(new Color(48, 106, 42), new Color(48, 106, 42));


	public IntinoWizardPanel() {
		errorLabel.setVisible(false);
		templatesAvailable = areTemplatesAvailable();
		componentsButtons = List.of(modelButton, metaModelButton, sentinelsButton, agendaButton, webUIButton, EventSourcingButton, APIRESTServiceButton, workflowButton);
		infrastructureButtons = List.of(amidas, sumus, datahub, archetype);
		componentsButtons.forEach(b -> b.setFocusable(false));
		if (templatesAvailable) initDiagramPanel();
		initRadioButtons();
	}

	private void initRadioButtons() {
		if (templatesAvailable)
			business.addChangeListener(changeEvent -> componentsButtons.forEach(b -> b.setEnabled(business.isSelected())));
		else {
			componentsButtons.forEach(b -> b.setEnabled(false));
			infrastructureButtons.forEach(i -> i.setEnabled(false));
			errorLabel.setVisible(true);
		}
	}

	private boolean areTemplatesAvailable() {
		try {
			URLConnection connection = new URL(ModuleTemplateDeployer.Artifactory).openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			connection.connect();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private void initDiagramPanel() {
		for (JButton button : componentsButtons) {
			button.addMouseListener(new MouseListener() {
				final Color defaultColor = button.getBackground();

				@Override
				public void mouseClicked(MouseEvent mouseEvent) {
					if (!business.isSelected()) return;
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
					if (!business.isSelected()) return;
					button.setBackground(selectedBackground((JButton) mouseEvent.getSource()));

				}

				@Override
				public void mouseExited(MouseEvent mouseEvent) {
					if (!business.isSelected()) return;
					JButton source = (JButton) mouseEvent.getSource();
					if (!source.isSelected()) button.setBackground(new JBColor(defaultColor, defaultColor));
				}
			});
		}

	}

	private Color selectedBackground(JButton source) {
		return source.getText().contains("Concept") ? modelColor : boxColor;
	}

	public JPanel panel() {
		return root;
	}

	public IntinoModuleType.Type selected() {
		if (business.isSelected()) return IntinoModuleType.Type.Business;
		if (amidas.isSelected()) return IntinoModuleType.Type.Federation;
		if (sumus.isSelected()) return IntinoModuleType.Type.DataAnalytic;
		if (datahub.isSelected()) return IntinoModuleType.Type.Datahub;
		return IntinoModuleType.Type.Archetype;
	}


	public List<Components> components() {
		if (!business.isSelected()) return Collections.emptyList();
		List<Components> components = componentsButtons.stream()
				.filter(AbstractButton::isSelected)
				.map(b -> Components.valueOf(makeUp(b)))
				.toList();
		if (components.contains(Model) && components.contains(Components.MetaModel)) components.remove(Model);
		return components;
	}

	@NotNull
	private String makeUp(JButton b) {
		return firstUpperCase(b.getText().replace("Concepts", "Model").replace(" ", ""));
	}

	private void createUIComponents() {
	}

	public enum Components {
		Model, MetaModel, Sentinels, Agenda, WebUI, EventSourcing, APIRESTService, Workflow
	}
}
