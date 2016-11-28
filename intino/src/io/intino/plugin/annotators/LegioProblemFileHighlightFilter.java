package io.intino.plugin.annotators;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.plugin.file.legio.LegioFileType;

public class LegioProblemFileHighlightFilter implements Condition<VirtualFile> {

	@Override
	public boolean value(VirtualFile virtualFile) {
		final FileType fileType = virtualFile.getFileType();
		return fileType == LegioFileType.instance();
	}
}
