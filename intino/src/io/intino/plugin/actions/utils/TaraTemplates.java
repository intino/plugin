package io.intino.plugin.actions.utils;

import io.intino.plugin.lang.file.TaraFileType;
import org.jetbrains.annotations.NonNls;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TaraTemplates {

	@NonNls
	public static final Map<String, String> templates = new HashMap<>();

	static {
		templates.put("FILE", "TaraFile." + TaraFileType.instance().getDefaultExtension());
	}

	private TaraTemplates() {
	}

	public static String getTemplate(String template) {
		return templates.get(template);
	}

	public static Set<Map.Entry<String, String>> getTemplates() {
		return templates.entrySet();
	}

	public static Collection<String> getTemplateValues() {
		return templates.values();
	}


}
