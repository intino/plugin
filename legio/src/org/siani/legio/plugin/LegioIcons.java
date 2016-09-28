package org.siani.legio.plugin;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

public class LegioIcons {

	private static final boolean RETINA = UIUtil.isRetina();

	private static Icon scale(Icon icon) {
		return IconUtil.scale(icon, 0.5);
	}

	public static final Icon ICON_16 = RETINA ? scale(IconLoader.getIcon("/icons/icon-retina.png")) : IconLoader.getIcon("/icons/icon_16.png");
}
