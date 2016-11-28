package io.intino.plugin.file;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.file.pandora.PandoraFileType;
import org.jetbrains.annotations.NotNull;

public class IntinoFileTypeFactory extends com.intellij.openapi.fileTypes.FileTypeFactory {
	@Override
	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(PandoraFileType.instance(), PandoraFileType.instance().getDefaultExtension());
		consumer.consume(LegioFileType.instance(), LegioFileType.instance().getDefaultExtension());

	}
}
