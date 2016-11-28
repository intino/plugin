package io.intino.plugin;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

public class IntinoIcons {

    private static final boolean RETINA = UIUtil.isRetina();

    private static Icon scale(Icon icon) {
        return IconUtil.scale(icon, 0.5);
    }

    public static final Icon LEGIO_16 = RETINA ? scale(IconLoader.getIcon("/icons/legio/legio-16-retina.png")) : IconLoader.getIcon("/icons/legio/legio-16.png");
    public static final Icon LEGIO_13 = RETINA ? scale(IconLoader.getIcon("/icons/legio/legio-13-retina.png")) : IconLoader.getIcon("/icons/legio/legio-13.png");

    public static final Icon INTINO_16 = RETINA ? scale(IconLoader.getIcon("/icons/intino/intino-16-retina.png")) : IconLoader.getIcon("/icons/intino/intino-16.png");


    public static final Icon PANDORA_16 = RETINA ? scale(IconLoader.getIcon("/icons/pandora/pandora-16-retina.png")) : IconLoader.getIcon("/icons/pandora/pandora-16.png");

    public static final Icon BLUE = RETINA ? scale(IconLoader.getIcon("/icons/actionButtons/blue_32.png")) : IconLoader.getIcon("/icons/actionButtons/blue.png");
    public static final Icon GREEN = RETINA ? scale(IconLoader.getIcon("/icons/actionButtons/green_32.png")) : IconLoader.getIcon("/icons/actionButtons/green.png");
    public static final Icon YELLOW = RETINA ? scale(IconLoader.getIcon("/icons/actionButtons/yellow_32.png")) : IconLoader.getIcon("/icons/actionButtons/yellow.png");
    public static final Icon ORANGE = RETINA ? scale(IconLoader.getIcon("/icons/actionButtons/orange_32.png")) : IconLoader.getIcon("/icons/actionButtons/orange.png");
    public static final Icon RED = RETINA ? scale(IconLoader.getIcon("/icons/actionButtons/red_32.png")) : IconLoader.getIcon("/icons/actionButtons/red.png");


}
