package io.intino.plugin.file.konos;

import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.file.TaraFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KonosFileType extends TaraFileType {
	public static final KonosFileType INSTANCE = new KonosFileType();


	private KonosFileType() {
		super();
	}

	public static KonosFileType instance() {
		return INSTANCE;
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