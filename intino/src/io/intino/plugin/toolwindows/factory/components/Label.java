package io.intino.plugin.toolwindows.factory.components;

import static io.intino.plugin.toolwindows.factory.components.Element.Box;
import static io.intino.plugin.toolwindows.factory.components.Element.Model;

public enum Label {
	ModelLanguage(Model), BoxLanguage(Box);

	private Element element;

	public Element element() {
		return element;
	}

	Label(Element element) {
		this.element = element;
	}
}
