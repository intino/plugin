package org.siani.legio.plugin.file;

import org.siani.legio.plugin.LegioIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tara.intellij.lang.file.TaraFileType;

public class LegioFileType extends TaraFileType {
	public static LegioFileType INSTANCE;


	private LegioFileType() {
		super();
	}

	public static LegioFileType instance() {
		return INSTANCE != null ? INSTANCE : (INSTANCE = new LegioFileType());
	}

	@NotNull
	public String getName() {
		return "Legio";
	}

	@NotNull
	public String getDescription() {
		return "Legio file";
	}

	@NotNull
	public String getDefaultExtension() {
		return "legio";
	}

	@Nullable
	@Override
	public javax.swing.Icon getIcon() {
		return LegioIcons.ICON_16;
	}


}