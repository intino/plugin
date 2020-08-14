package io.intino.plugin.file.goros;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GorosFileType extends LanguageFileType {
	public static final GorosFileType INSTANCE = new GorosFileType();

	protected GorosFileType() {
		super(JsonLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public String getName() {
		return "Goros File";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "Goros file";
	}

	@NotNull
	@Override
	public String getDefaultExtension() {
		return "goros";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return IntinoIcons.GOROS_16;
	}
}
