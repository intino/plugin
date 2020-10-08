package io.intino.plugin.file;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ArchetypeFileType extends LanguageFileType {
	public static final ArchetypeFileType INSTANCE = new ArchetypeFileType();

	private ArchetypeFileType() {
		super(PlainTextLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public String getName() {
		return "Archetype";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "Archetype file";
	}

	@NotNull
	@Override
	public String getDefaultExtension() {
		return "archetype";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return IntinoIcons.ARCHETYPE_16;
	}

	public static ArchetypeFileType instance() {
		return INSTANCE;
	}
}
