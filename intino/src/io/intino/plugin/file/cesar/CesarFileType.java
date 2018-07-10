package io.intino.plugin.file.cesar;

import io.intino.plugin.IntinoIcons;
import io.intino.tara.plugin.lang.file.TaraFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CesarFileType extends TaraFileType {
	public static CesarFileType INSTANCE;
	public static final String CESAR_FILE = "project.cesar";


	private CesarFileType() {
		super();
	}

	public static CesarFileType instance() {
		return INSTANCE != null ? INSTANCE : (INSTANCE = new CesarFileType());
	}

	@NotNull
	public String getName() {
		return "Cesar";
	}

	@NotNull
	public String getDescription() {
		return "Cesar file";
	}

	@NotNull
	public String getDefaultExtension() {
		return "cesar";
	}

	@Nullable
	@Override
	public javax.swing.Icon getIcon() {
		return IntinoIcons.LOGO_16;
	}

}