package io.intino.plugin.file.konos;

import io.intino.plugin.IntinoIcons;
import io.intino.tara.plugin.lang.file.TaraFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EtherFileType extends TaraFileType {
	private static TaraFileType INSTANCE;


	private EtherFileType() {
		super();
	}

	public static EtherFileType instance() {
		return INSTANCE != null ? (EtherFileType) INSTANCE : (EtherFileType) (INSTANCE = new EtherFileType());
	}

	@NotNull
	public String getName() {
		return "Ether";
	}

	@NotNull
	public String getDescription() {
		return "Ether box file";
	}

	@NotNull
	public String getDefaultExtension() {
		return "Ether";
	}

	@Nullable
	@Override
	public javax.swing.Icon getIcon() {
		return IntinoIcons.KONOS_16;
	}


}