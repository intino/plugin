package io.intino.plugin.toolwindows.factory.components;

import java.awt.*;

import static io.intino.plugin.toolwindows.factory.components.Element.*;

public enum Operation {
	GenerateCode(Gen), ImportPackages(Imports), ExportAccessors(Exports),
	BuildArtifact(Out), PackArtifact(Pack), DistributeArtifact(Dist), DeployArtifact(Deploy);

	private Rectangle rect;
	private static final int size = 600;

	Operation(Element element) {
		this.rect = calculate(element.rect());
	}

	private static Rectangle calculate(Rectangle r) {
		return new Rectangle(r.x + (r.width - size) / 2, r.y - size / 2, size, size);
	}

	public Rectangle rect() {
		return rect;
	}
}
