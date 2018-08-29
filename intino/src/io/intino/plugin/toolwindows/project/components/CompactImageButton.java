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
	private boolean hover = false;
	private static Map<Operation, Integer> operationPositions = new HashMap<>();

	static {
		operationPositions.put(Operation.ImportPackages, 121);
		operationPositions.put(Operation.GenerateCode, 205);
		operationPositions.put(Operation.ExportAccessors, 289);
		operationPositions.put(Operation.PackArtifact, 374);
		operationPositions.put(Operation.DistributeArtifact, 458);
		operationPositions.put(Operation.DeployArtifact, 543);
	}

	public CompactImageButton(Operation operation, Color foregroundColor) {
		this.operation = operation;
		this.foregroundColor = foregroundColor;
		this.addMouseListener(this);
	}

	public Point getDefaultLocation() {
		return new Point(48, operationPositions.get(this.operation));
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
	}

	@Override
	public void mouseReleased(MouseEvent e) {
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
