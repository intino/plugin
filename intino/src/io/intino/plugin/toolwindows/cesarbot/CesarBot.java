package io.intino.plugin.toolwindows.cesarbot;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import io.intino.plugin.project.CesarAccessor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static javax.swing.text.StyleConstants.LineSpacing;

public class CesarBot {
	private static final com.intellij.openapi.diagnostic.Logger logger = Logger.getInstance(CesarBot.class);
	static Project project;
	private final List<String> icons = List.of("disconnected", "harddisk", "memory", "network", "threads", "work", "small_red_triangle", "small_red_triangle_down", "clock10", "hammer_and_pick");
	private final CesarAccessor cesarAccessor;
	private JTextField chatInput;
	private JTextPane console;
	private JPanel myToolWindowContent;

	public CesarBot(Project project) {
		CesarBot.project = project;
		setConsoleStyle();
		cesarAccessor = new CesarAccessor(project);
		cesarAccessor.subscribeToNotifications(message -> insertMessage("cesar", message.trim()));
		chatInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					insertMessage("you", chatInput.getText());
					String response = sendCommand(chatInput.getText());
					chatInput.setText("");
					if (response == null)
						insertMessage("cesar", "Bot not available. Ensure your credential are correct and have connectivity.");
					else insertMessage("cesar", response.trim());
				}
			}
		});

	}

	private void insertMessage(String user, String message) {
		StyledDocument document = console.getStyledDocument();
		try {
			document.insertString(document.getLength(), "\n", document.getStyle("normal"));
			String header = user + ": ";
			document.insertString(document.getLength(), header, document.getStyle(user + "Style"));
			for (String icon : icons) message = message.replace(":" + icon + ":", "#" + icon + "#");
			String state = "";
			StringBuilder buffer = new StringBuilder();
			message = message.replace("*", "`");
			for (char aChar : message.toCharArray()) {
				if (aChar == '#')
					if (state.isEmpty()) {
						state = "icon";
						continue;
					} else {
						printIcon(document, buffer.toString());
						buffer = new StringBuilder();
						state = "";
						continue;
					}
				if (aChar == '`')
					if (state.isEmpty()) {
						state = "box";
						continue;
					} else {
						document.insertString(document.getLength(), buffer.toString(), document.getStyle("bold"));
						buffer = new StringBuilder();
						state = "";
						continue;
					}
				if (state.equals("icon") || state.equals("box")) buffer.append(aChar);
				else document.insertString(document.getLength(), aChar + "", document.getStyle("normal"));
			}
			console.scrollRectToVisible(new Rectangle(0, console.getBounds(null).height, 1, 1));
		} catch (BadLocationException e1) {
			logger.error(e1);
		}
	}

	private void printIcon(StyledDocument document, String words) throws BadLocationException {
		SimpleAttributeSet attrs = new SimpleAttributeSet();
		StyleConstants.setIcon(attrs, createImage(words));
		document.insertString(document.getLength(), words, attrs);
	}

	private void setConsoleStyle() {
		StyledDocument document = console.getStyledDocument();
		AttributeSet paragraphAttributes = console.getParagraphAttributes();
		final Style paragraph = document.addStyle("paragraph", null);
		paragraphAttributes.getAttributeNames().asIterator().forEachRemaining(a -> {
			if (!a.equals(LineSpacing)) paragraph.addAttribute(a, paragraphAttributes.getAttribute(a));
		});
		StyleConstants.setLineSpacing(paragraph, 0.3f);
		console.setParagraphAttributes(paragraph, true);
		final Style userStype = document.addStyle("youStyle", null);
		userStype.addAttribute(StyleConstants.Foreground, JBColor.GREEN);
		userStype.addAttribute(StyleConstants.FontSize, 14);
		userStype.addAttribute(StyleConstants.FontFamily, "Jetbrains Mono");
		userStype.addAttribute(StyleConstants.Bold, Boolean.TRUE);

		final Style cesarStyle = document.addStyle("cesarStyle", null);
		cesarStyle.addAttribute(StyleConstants.Foreground, JBColor.RED);
		cesarStyle.addAttribute(StyleConstants.FontSize, 14);
		cesarStyle.addAttribute(StyleConstants.FontFamily, "Jetbrains Mono");

		cesarStyle.addAttribute(StyleConstants.Bold, Boolean.TRUE);

		final Style boldStyle = document.addStyle("bold", null);
		boldStyle.addAttribute(StyleConstants.FontSize, 14);
		boldStyle.addAttribute(StyleConstants.FontFamily, "Jetbrains Mono");
		boldStyle.addAttribute(StyleConstants.Bold, Boolean.TRUE);

		final Style normalStyle = document.addStyle("normal", null);
		normalStyle.addAttribute(StyleConstants.FontSize, 14);
		normalStyle.addAttribute(StyleConstants.FontFamily, "Jetbrains Mono");
		normalStyle.addAttribute(StyleConstants.Bold, Boolean.FALSE);

	}

	private String sendCommand(String text) {
		return cesarAccessor.talk(text);
	}

	ImageIcon createImage(String code) {
		InputStream resourceAsStream = this.getClass().getResourceAsStream("/icons/cesar/" + code + ".png");
		try {
			if (resourceAsStream == null) throw new IOException(code + " not found");
			return new ImageIcon(ImageIO.read(resourceAsStream));
		} catch (IOException e) {
			logger.error(e);
			return null;
		}

	}

	public JPanel content() {
		return myToolWindowContent;
	}

	public void disconnect() {
		cesarAccessor.disconnect();
	}
}