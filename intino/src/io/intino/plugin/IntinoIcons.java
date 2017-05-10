package io.intino.plugin;

import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

import static com.intellij.openapi.util.IconLoader.getIcon;

public class IntinoIcons {

	private static final boolean RETINA = UIUtil.isRetina();

	private static Icon scale(Icon icon) {
		return IconUtil.scale(icon, 0.5);
	}

	public static final Icon INTINO_16 = RETINA ? scale(getIcon("/icons/intino/intino-32.png")) : getIcon("/icons/intino/intino-16.png");

	public static final Icon INTINO_13 = RETINA ? scale(getIcon("/icons/intino/intino-26.png")) : getIcon("/icons/intino/intino-13.png");
	public static final Icon INTINO_80 = RETINA ? scale(getIcon("/icons/intino/intino-80.png")) : getIcon("/icons/intino/intino-80.png");
	public static final Icon LOGO_16 = RETINA ? scale(getIcon("/icons/intino/logo-32.png")) : getIcon("/icons/intino/logo-16.png");

	public static final Icon LEGIO_16 = RETINA ? scale(getIcon("/icons/files/artifact-32.png")) : getIcon("/icons/files/artifact-16.png");
	public static final Icon KONOS_16 = RETINA ? scale(getIcon("/icons/files/case-32.png")) : getIcon("/icons/files/case-16.png");


	public static class Operations {
		public static class Dark {

			public static final Icon BUILD = RETINA ? scale(getIcon("/icons/toolwindow/dark/build-64.png")) : getIcon("/icons/toolwindow/dark/build-32.png");
			public static final Icon DEPLOY = RETINA ? scale(getIcon("/icons/toolwindow/dark/deploy-64.png")) : getIcon("/icons/toolwindow/dark/deploy-32.png");
			public static final Icon DISTRIBUTE = RETINA ? scale(getIcon("/icons/toolwindow/dark/distribute-64.png")) : getIcon("/icons/toolwindow/dark/distribute-32.png");
			public static final Icon EXPORT = RETINA ? scale(getIcon("/icons/toolwindow/dark/export-64.png")) : getIcon("/icons/toolwindow/dark/export-32.png");
			public static final Icon GENERATE = RETINA ? scale(getIcon("/icons/toolwindow/dark/generate-64.png")) : getIcon("/icons/toolwindow/dark/generate-32.png");
			public static final Icon IMPORT = RETINA ? scale(getIcon("/icons/toolwindow/dark/import-64.png")) : getIcon("/icons/toolwindow/dark/import-32.png");
			public static final Icon PACK = RETINA ? scale(getIcon("/icons/toolwindow/dark/pack-64.png")) : getIcon("/icons/toolwindow/dark/pack-32.png");
		}

		public static class Light {
			public static final Icon BUILD = RETINA ? scale(getIcon("/icons/toolwindow/light/build-64.png")) : getIcon("/icons/toolwindow/light/build-32.png");
			public static final Icon DEPLOY = RETINA ? scale(getIcon("/icons/toolwindow/light/deploy-64.png")) : getIcon("/icons/toolwindow/light/deploy-32.png");
			public static final Icon DISTRIBUTE = RETINA ? scale(getIcon("/icons/toolwindow/light/distribute-64.png")) : getIcon("/icons/toolwindow/light/distribute-32.png");
			public static final Icon EXPORT = RETINA ? scale(getIcon("/icons/toolwindow/light/export-64.png")) : getIcon("/icons/toolwindow/light/export-32.png");
			public static final Icon GENERATE = RETINA ? scale(getIcon("/icons/toolwindow/light/generate-64.png")) : getIcon("/icons/toolwindow/light/generate-32.png");
			public static final Icon IMPORT = RETINA ? scale(getIcon("/icons/toolwindow/light/import-64.png")) : getIcon("/icons/toolwindow/light/import-32.png");
			public static final Icon PACK = RETINA ? scale(getIcon("/icons/toolwindow/light/pack-64.png")) : getIcon("/icons/toolwindow/light/pack-32.png");
		}

	}
}
