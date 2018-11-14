package io.intino.plugin.toolwindows.project.components;

import static io.intino.plugin.toolwindows.project.components.Element.Box;
import static io.intino.plugin.toolwindows.project.components.Element.Model;

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
