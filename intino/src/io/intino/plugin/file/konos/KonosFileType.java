package io.intino.plugin.file.konos;

import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.intino.tara.plugin.lang.file.TaraFileType;

public class KonosFileType extends TaraFileType {
	private static TaraFileType INSTANCE;


	private KonosFileType() {
		super();
	}

	public static KonosFileType instance() {
		return INSTANCE != null ? (KonosFileType) INSTANCE : (KonosFileType) (INSTANCE = new KonosFileType());
	}

	@NotNull
	public String getName() {
		return "Konos";
	}

	@NotNull
	public String getDescription() {
		return "Box file";
	}

	@NotNull
	public String getDefaultExtension() {
		return "konos";
	}

	@Nullable
	@Override
	public javax.swing.Icon getIcon() {
		return IntinoIcons.KONOS_16;
	}


}