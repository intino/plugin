package io.intino.plugin.toolwindows.factory.components;

import java.awt.*;

public enum Element {
	Model(1780, 20), Box(3000, 20), Imports(19, 1550), Src(1240, 1550), Gen(2399, 1550), Exports(4082, 1550),
	Out(1802, 3078), Pack(1802, 4470), Dist(1802, 5850), Deploy(1802, 7238);

	private Rectangle rect;
	private static final int size = 900;

	Element(int x, int y) {
		this.rect = new Rectangle(x, y, size, size);
	}

	Rectangle rect() {
		return rect;
	}
}
