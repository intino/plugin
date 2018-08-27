package io.intino.plugin.toolwindows.project.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

class CompactImageButton extends ImageButton {
	private final Color foregroundColor;
	private ActionListener listener;
	private boolean pressed = false;
	private boolean hover = false;

	public CompactImageButton(Operation operation, Color foregroundColor) {
		this.operation = operation;
		this.foregroundColor = foregroundColor;
		this.addMouseListener(this);
	}

	@Override
	public void paint(Graphics g) {
		if (!hover) return;
		int size = pressed ? 15 : 10;

		int width = (this.getWidth() * size) / 10;
		int height = (this.getHeight() * size) / 10;

		int x = (this.getWidth() - width) / 2;
		int y = (this.getHeight() - height) / 2;
		g.setColor(foregroundColor);
		g.fillOval(x, y, width, height);
		g.setColor(foregroundColor);
	}


	public void addActionListener(ActionListener listener) {
		this.listener = listener;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (listener == null) return;
		listener.actionPerformed(new ActionEvent(operation, 0, "Clicked", e.getModifiers()));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		pressed = true;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressed = false;
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		hover = true;
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		hover = false;
		repaint();
	}
}
