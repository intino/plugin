package io.intino.plugin.toolwindows.factory.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

class CompactImageButton extends ImageButton {
	private final Color foregroundColor;
	private ActionListener listener;
	private boolean hover = false;
	private static Map<Operation, Integer> operationYPositions = new HashMap<>();

	static {
		operationYPositions.put(Operation.ImportPackages, 31);
		operationYPositions.put(Operation.GenerateCode, 115);
		operationYPositions.put(Operation.ExportAccessors, 199);
		operationYPositions.put(Operation.PackArtifact, 284);
		operationYPositions.put(Operation.DistributeArtifact, 368);
		operationYPositions.put(Operation.DeployArtifact, 453);
	}

	CompactImageButton(Operation operation, Color foregroundColor) {
		this.operation = operation;
		this.foregroundColor = foregroundColor;
		this.addMouseListener(this);
	}

	Point getDefaultLocation() {
		return new Point(48, operationYPositions.get(this.operation));
	}

	public void paint(Graphics g) {
		if (!hover) return;
		g.setColor(foregroundColor);
		g.fillOval(0, 0, this.getWidth(), this.getWidth());
	}

	void addActionListener(ActionListener listener) {
		this.listener = listener;
	}

	public void mouseClicked(MouseEvent e) {
		if (listener == null) return;
		listener.actionPerformed(new ActionEvent(operation, 0, "Clicked", e.getModifiersEx()));
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
		hover = true;
		repaint();
	}

	public void mouseExited(MouseEvent e) {
		hover = false;
		repaint();
	}
}
