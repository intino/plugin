package io.intino.plugin.toolwindows.factory.components;

import java.awt.*;

public enum Mode {
	Light("#ECECEC"), Darcula("#404243");

	private final Color color;

	public Color color() {
		return color;
	}

	Mode(String color) {
		this.color = Color.decode(color);
	}
}
