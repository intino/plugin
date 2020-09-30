package io.intino.plugin.lang.file;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StashFileType implements FileType {
	public static final StashFileType INSTANCE = new StashFileType();

	private StashFileType() {
	}

	public static StashFileType instance() {
		return INSTANCE;
	}

	@NotNull
	public String getName() {
		return "Stash";
	}

	@NotNull
	public String getDescription() {
		return "Stash file";
	}

	@NotNull
	public String getDefaultExtension() {
		return "stash";
	}


	@Nullable
	@Override
	public javax.swing.Icon getIcon() {
		return IntinoIcons.STASH_16;
	}

	@Override
	public boolean isBinary() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Nullable
	@Override
	public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
		return null;
	}

}