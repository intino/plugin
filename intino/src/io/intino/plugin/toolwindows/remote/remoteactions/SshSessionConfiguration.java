package io.intino.plugin.toolwindows.remote.remoteactions;

import io.intino.plugin.TerminalWindow;
import io.intino.plugin.cesar.CesarInfo;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

public class SshSessionConfiguration extends JDialog {
	private final CesarInfo.ServerInfo server;
	private final Map<String, List<TerminalWindow.Tunnel>> savedTunnels;
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField computerName;
	private JTable tunnels;
	private JTextField textField2;
	private JTextField textField3;
	private JButton addButton;
	private JButton removeButton;
	private JLabel serverName;
	private JTextField port;

	public SshSessionConfiguration(CesarInfo.ServerInfo server, Map<String, List<TerminalWindow.Tunnel>> savedTunnels) {
		this.server = server;
		this.savedTunnels = savedTunnels;
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);
		buttonOK.addActionListener(e -> onOK());
		buttonCancel.addActionListener(e -> onCancel());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		serverName.setText(server.name());
	}

	private void onOK() {
		dispose();
	}

	private void onCancel() {
		dispose();
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
	}

	public List<TerminalWindow.Tunnel> tunnels() {
		return null;
	}
}
