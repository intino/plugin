package org.jetbrains.jps.intino.model.impl;

import com.intellij.util.xmlb.annotations.Tag;

public class JpsTaraSettings {
	public static String FILE = "taraSettings.xml";
	public static String NAME = "Tara.Settings";

	@Tag("destinyLanguage")
	public String destinyLanguage = "Java";

	@Tag("trackerProjectId")
	public String trackerProjectId = "";

	@Tag("trackerApiToken")
	public String trackerApiToken = "";


}
