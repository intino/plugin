package io.intino.plugin.file;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import io.intino.plugin.file.goros.GorosFileType;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.itrules.lang.file.ItrulesFileType;
import io.intino.plugin.lang.file.StashFileType;
import io.intino.plugin.lang.file.TaraFileType;
import org.jetbrains.annotations.NotNull;

public class IntinoFileTypeFactory extends com.intellij.openapi.fileTypes.FileTypeFactory {
	@Override
	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(TaraFileType.instance(), TaraFileType.instance().getDefaultExtension());
		consumer.consume(StashFileType.instance(), StashFileType.instance().getDefaultExtension());
		consumer.consume(KonosFileType.instance(), KonosFileType.instance().getDefaultExtension());
		consumer.consume(LegioFileType.instance(), LegioFileType.instance().getDefaultExtension());
		consumer.consume(ItrulesFileType.INSTANCE, ItrulesFileType.INSTANCE.getDefaultExtension());
		consumer.consume(GorosFileType.INSTANCE, GorosFileType.INSTANCE.getDefaultExtension());
	}
}
