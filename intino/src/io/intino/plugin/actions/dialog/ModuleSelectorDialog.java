package io.intino.plugin.actions.dialog;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ScrollPaneFactory;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class ModuleSelectorDialog extends DialogWrapper {
	private final JScrollPane mainPanel;
	private final List<Module> modules;
	private Module selected;

	public ModuleSelectorDialog(Project project) {
		super(project, false);
		super.setTitle("Select Module");
		this.modules = Arrays.stream(ModuleManager.getInstance(project).getModules()).filter(m -> IntinoUtil.configurationOf(m) != null).sorted(Comparator.comparing(Module::getName)).toList();
		this.centerRelativeToParent();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dimension = new Dimension((int) (screenSize.getWidth() / 6), (int) (screenSize.getHeight() / 6));
		mainPanel = createButtons();
		mainPanel.setPreferredSize(dimension);
		mainPanel.setMinimumSize(dimension);
		mainPanel.setMaximumSize(dimension);
		mainPanel.setSize(dimension);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				selected = null;
			}
		});
		mainPanel.registerKeyboardAction(e -> selected = null, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		init();
	}

	private JScrollPane createButtons() {
		JPanel contentPanel = new JPanel(new GridLayout(0, 1));
		ButtonGroup group = new ButtonGroup();
		for (Module module : this.modules) {
			JRadioButton b = new JRadioButton(module.getName());
			b.setSelected(false);
			contentPanel.add(b);
			group.add(b);
			b.addActionListener(l -> selected = findModule(l.getActionCommand()));
		}
		JScrollPane tablePanel = ScrollPaneFactory.createScrollPane(contentPanel);
		tablePanel.setPreferredSize(new Dimension(tablePanel.getWidth(), 200));
		return tablePanel;
	}

	private Module findModule(String name) {
		return modules.stream().filter(m -> m.getName().equals(name)).findFirst().orElse(null);
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return mainPanel;
	}

	public Module selected() {
		return selected;
	}
}
