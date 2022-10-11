package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.plugin.file.LegioFileType;

public class LegioProblemFileHighlightFilter implements Condition<VirtualFile> {

	@Override
	public boolean value(VirtualFile virtualFile) {
		return virtualFile.getFileType() == LegioFileType.instance();
	}
}
