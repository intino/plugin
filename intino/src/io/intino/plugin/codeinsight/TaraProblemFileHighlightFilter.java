package io.intino.plugin.codeinsight;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.plugin.lang.file.TaraFileType;

public class TaraProblemFileHighlightFilter implements Condition<VirtualFile> {

	@Override
	public boolean value(VirtualFile virtualFile) {
		final FileType fileType = virtualFile.getFileType();
		return fileType == TaraFileType.instance();
	}
}
