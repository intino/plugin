package io.intino.plugin.lang.file;

import com.intellij.openapi.fileTypes.LanguageFileType;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.TaraLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaraFileType extends LanguageFileType {
	public static final TaraFileType INSTANCE = new TaraFileType();

	protected TaraFileType() {
		super(TaraLanguage.INSTANCE);
	}

	public static TaraFileType instance() {
		return INSTANCE;
	}

	@NotNull
	@Override
	public String getName() {
		return "Tara";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "Tara file";
	}

	@NotNull
	@Override
	public String getDefaultExtension() {
		return "tara";
	}

	@Nullable
	@Override
	public javax.swing.Icon getIcon() {
		return IntinoIcons.MODEL_16;
	}
}