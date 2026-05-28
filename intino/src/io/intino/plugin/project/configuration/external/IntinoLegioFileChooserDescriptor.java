package io.intino.plugin.project.configuration.external;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.stream.Stream;

public class IntinoLegioFileChooserDescriptor extends FileChooserDescriptor {

	public IntinoLegioFileChooserDescriptor() {
		super(false, true, false, false, false, false);
		withFileFilter(this::containsArtifactLegio);
	}

	private boolean containsArtifactLegio(VirtualFile file) {
		return file != null && file.isDirectory() && Stream.of(file.getChildren()).anyMatch(f -> f.getName().equals("artifact.legio"));
	}
}
