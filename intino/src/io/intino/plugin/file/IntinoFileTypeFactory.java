package io.intino.plugin.file;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import io.intino.plugin.file.cesar.CesarFileType;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.plugin.file.legio.LegioFileType;
import org.jetbrains.annotations.NotNull;

public class IntinoFileTypeFactory extends com.intellij.openapi.fileTypes.FileTypeFactory {
	@Override
	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(KonosFileType.instance(), KonosFileType.instance().getDefaultExtension());
		consumer.consume(LegioFileType.instance(), LegioFileType.instance().getDefaultExtension());
		consumer.consume(CesarFileType.instance(), CesarFileType.instance().getDefaultExtension());
	}
}
