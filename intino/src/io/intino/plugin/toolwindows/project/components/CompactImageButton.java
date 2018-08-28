package io.intino.plugin.toolwindows.project.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

class CompactImageButton extends ImageButton {
	private final Color foregroundColor;
	private ActionListener listener;
	private boolean pressed = false;
	private boolean hover = false;
	private static Map<Operation, Integer> operationPositions = new HashMap<>();
	private static int scale;

	static {
		operationPositions.put(Operation.ImportPackages, 126);
		operationPositions.put(Operation.GenerateCode, 211);
		operationPositions.put(Operation.ExportAccessors, 295);
		operationPositions.put(Operation.PackArtifact, 379);
		operationPositions.put(Operation.DistributeArtifact, 464);
		operationPositions.put(Operation.DeployArtifact, 549);
	}

	public CompactImageButton(Operation operation, Color foregroundColor) {
		this.operation = operation;
		this.foregroundColor = foregroundColor;
		this.addMouseListener(this);
	}

	public Point getDefaultLocation() {
		return new Point(52, operationPositions.get(this.operation));
	}

	@Override
	public void paint(Graphics g) {
		if (!hover) return;
		g.setColor(foregroundColor);
		g.fillOval(0, 0, this.getWidth(), this.getWidth());
	}

	void addActionListener(ActionListener listener) {
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
