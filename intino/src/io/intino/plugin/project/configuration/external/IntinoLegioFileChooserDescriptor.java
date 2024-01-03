package io.intino.plugin.project.configuration.external;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class IntinoLegioFileChooserDescriptor extends FileChooserDescriptor {

	public IntinoLegioFileChooserDescriptor() {
		super(false, true, false, false, false, false);
	}

	@Override
	public boolean isFileSelectable(@Nullable VirtualFile file) {
		if (!super.isFileSelectable(file)) return false;
		return Stream.of(file.getChildren()).anyMatch(f -> f.getName().equals("artifact.legio"));
	}
}
