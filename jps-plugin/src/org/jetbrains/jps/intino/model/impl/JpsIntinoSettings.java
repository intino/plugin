package org.jetbrains.jps.intino.model.impl;

import com.intellij.util.xmlb.annotations.Tag;

public class JpsIntinoSettings {
	public static String FILE = "IntinoSettings.xml";
	public static String NAME = "Intino.Settings";

	@Tag("modelMemory")
	public int modelMemory = 1024;

	@Tag("boxMemory")
	public int boxMemory = 2048;


}
