package io.intino.plugin;

import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

import static com.intellij.openapi.util.IconLoader.getIcon;

public class IntinoIcons {

	private static final boolean RETINA = UIUtil.isRetina();

	private static Icon scale(Icon icon) {
		return IconUtil.scale(icon, null, 0.5f);
	}


	public static final Icon BOX_PROCESS = RETINA ? scale(getIcon("/icons/box_process-32.png")) : getIcon("/icons/box_process-16.png");

	public static final Icon INTINO_16 = RETINA ? scale(getIcon("/icons/intino/intino-32.png")) : getIcon("/icons/intino/intino-16.png");


	public static final Icon INTINO_13 = RETINA ? scale(getIcon("/icons/intino/intino-26.png")) : getIcon("/icons/intino/intino-13.png");
	public static final Icon INTINO_80 = RETINA ? scale(getIcon("/icons/intino/intino-80.png")) : getIcon("/icons/intino/intino-80.png");
	public static final Icon LOGO_16 = RETINA ? scale(getIcon("/icons/intino/logo-32.png")) : getIcon("/icons/intino/logo-16.png");

	public static final Icon LEGIO_16 = RETINA ? scale(getIcon("/icons/files/artifact-32.png")) : getIcon("/icons/files/artifact-16.png");
	public static final Icon KONOS_16 = RETINA ? scale(getIcon("/icons/files/box-32.png")) : getIcon("/icons/files/box-16.png");
	public static final Icon GENARATION_16 = RETINA ? scale(getIcon("/icons/generate-32.png")) : getIcon("/icons/generate-16.png");

	public static final Icon GOROS_13 = RETINA ? scale(getIcon("/icons/goros/goros-26.png")) : getIcon("/icons/goros/goros-13.png");
	public static final Icon GOROS_16 = RETINA ? scale(getIcon("/icons/goros/goros-32.png")) : getIcon("/icons/goros/goros-16.png");

	public static final Icon MODEL_16 = RETINA ? scale(getIcon("/icons/files/model-32.png")) : getIcon("/icons/files/model-16.png");
	public static final Icon NODE = RETINA ? scale(getIcon("/icons/files/model-32.png")) : getIcon("/icons/files/model-16.png");
	public static final Icon STASH_16 = RETINA ? scale(getIcon("/icons/files/stash-32.png")) : getIcon("/icons/files/stash-16.png");

	public static Icon ICON_13 = RETINA ? scale(getIcon("/icons/icon-26.png")) : getIcon("/icons/icon-13.png");
	public static Icon ICON_16 = RETINA ? scale(getIcon("/icons/icon-32.png")) : getIcon("/icons/icon-16.png");


	public static class Operations {
		public static class Dark {
			public static final Icon BUILD = RETINA ? scale(getIcon("/toolwindow/dark/build-64.png")) : getIcon("/toolwindow/dark/build-32.png");
			public static final Icon DEPLOY = RETINA ? scale(getIcon("/toolwindow/dark/deploy-64.png")) : getIcon("/toolwindow/dark/deploy-32.png");
			public static final Icon DISTRIBUTE = RETINA ? scale(getIcon("/toolwindow/dark/distribute-64.png")) : getIcon("/toolwindow/dark/distribute-32.png");
			public static final Icon EXPORT = RETINA ? scale(getIcon("/toolwindow/dark/export-64.png")) : getIcon("/toolwindow/dark/export-32.png");
			public static final Icon GENERATE = RETINA ? scale(getIcon("/toolwindow/dark/generate-64.png")) : getIcon("/toolwindow/dark/generate-32.png");
			public static final Icon IMPORT = RETINA ? scale(getIcon("/toolwindow/dark/import-64.png")) : getIcon("/toolwindow/dark/import-32.png");
			public static final Icon PACK = RETINA ? scale(getIcon("/toolwindow/dark/pack-64.png")) : getIcon("/toolwindow/dark/pack-32.png");
		}

		public static class Light {
			public static final Icon BUILD = RETINA ? scale(getIcon("/toolwindow/light/build-64.png")) : getIcon("/toolwindow/light/build-32.png");
			public static final Icon DEPLOY = RETINA ? scale(getIcon("/toolwindow/light/deploy-64.png")) : getIcon("/toolwindow/light/deploy-32.png");
			public static final Icon DISTRIBUTE = RETINA ? scale(getIcon("/toolwindow/light/distribute-64.png")) : getIcon("/toolwindow/light/distribute-32.png");
			public static final Icon EXPORT = RETINA ? scale(getIcon("/toolwindow/light/export-64.png")) : getIcon("/toolwindow/light/export-32.png");
			public static final Icon GENERATE = RETINA ? scale(getIcon("/toolwindow/light/generate-64.png")) : getIcon("/toolwindow/light/generate-32.png");
			public static final Icon IMPORT = RETINA ? scale(getIcon("/toolwindow/light/import-64.png")) : getIcon("/toolwindow/light/import-32.png");
			public static final Icon PACK = RETINA ? scale(getIcon("/toolwindow/light/pack-64.png")) : getIcon("/toolwindow/light/pack-32.png");
		}

	}
}
