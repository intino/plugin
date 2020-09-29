package io.intino.plugin.toolwindows.factory.components;

import javax.swing.*;
import java.awt.event.MouseListener;

public abstract class ImageButton extends JComponent implements MouseListener {
	protected Operation operation;


	public Operation operation() {
		return operation;
	}

}
