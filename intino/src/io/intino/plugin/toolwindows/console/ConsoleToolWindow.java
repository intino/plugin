package io.intino.plugin.toolwindows.console;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.Gray;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ConsoleToolWindow {

	private JPanel myToolWindowContent;
	private JTextArea console;
	private JButton clean;
	private JScrollPane scrollPane;
	private Project project;


	public ConsoleToolWindow(Project project) {
		this.project = project;
		clean.addActionListener(e -> console.setText(""));
	}

	public JPanel content() {
		return myToolWindowContent;
	}

	private void createUIComponents() {
		clean = new JButton(AllIcons.Actions.GC);
		clean.setBorderPainted(false);
		clean.setBorder(null);
		clean.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				clean.setBackground(Gray._70);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				clean.setBackground(myToolWindowContent.getBackground());
			}
		});
	}

	void addText(String text) {
		console.append(text);
		scrollPane.getHorizontalScrollBar().setValue(0);
	}
}
