package io.intino.plugin.file.legio;

import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.file.TaraFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LegioFileType extends TaraFileType {
	public static LegioFileType INSTANCE;
	public static final String LEGIO_FILE = "artifact.legio";


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
		return IntinoIcons.LEGIO_16;
	}
}