package io.intino.plugin.toolwindows.cesarbot;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import io.intino.plugin.project.CesarAccessor;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CesarBot {

	static Project project;
	private final CesarAccessor cesarAccessor;
	private JPanel chat;
	private JTextField chatInput;
	private JTextPane console;
	private JPanel myToolWindowContent;

	public CesarBot(Project project) {
		CesarBot.project = project;
		setConsoleStyle();
		cesarAccessor = new CesarAccessor(project);
		chatInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					String response = sendCommand(chatInput.getText());
					insertMessage("user", chatInput.getText());
					chatInput.setText("");
					insertMessage("cesar", response);
				}
			}
		});

	}

	private void insertMessage(String user, String message) {
		StyledDocument document = console.getStyledDocument();
		try {
			document.insertString(document.getLength(), "\n", new SimpleAttributeSet());
			String header = user + ": ";
			document.insertString(document.getLength(), header, document.getStyle("Heading2"));
			document.insertString(document.getLength(), message, null);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	private void setConsoleStyle() {
		StyledDocument document = console.getStyledDocument();
		final Style heading2Style = document.addStyle("Heading2", null);
		heading2Style.addAttribute(StyleConstants.Foreground, JBColor.GREEN);
		heading2Style.addAttribute(StyleConstants.FontSize, 16);
		heading2Style.addAttribute(StyleConstants.FontFamily, "serif");
		heading2Style.addAttribute(StyleConstants.Bold, Boolean.TRUE);

	}

	private String sendCommand(String text) {
		return cesarAccessor.talk(text);

	}

	public JPanel content() {
		return myToolWindowContent;
	}
}