package io.intino.plugin.console;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.Gray;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ConsoleWindowFactory implements ToolWindowFactory {

	private JPanel myToolWindowContent;
	private JTextArea console;
	private JButton clean;
	private JScrollPane scrollPane;
	private ToolWindow myToolWindow;


	public ConsoleWindowFactory() {
		clean.addActionListener(e -> console.setText(""));
	}

	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		project.getMessageBus().connect().subscribe(IntinoTopics.MAVEN, line -> ApplicationManager.getApplication().invokeLater(() -> {
			if (!myToolWindow.isVisible()) myToolWindow.show(null);
			console.setText(console.getText() + "\n" + line);
			scrollPane.getHorizontalScrollBar().setValue(0);
		}));
		myToolWindow = toolWindow;
		myToolWindow.setAutoHide(true);
		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		Content content = contentFactory.createContent(myToolWindowContent, "Console", false);
		toolWindow.getContentManager().addContent(content);
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
}
