package io.intino.plugin.itrules.lang.file;

import com.intellij.openapi.fileTypes.FileTypeConsumer;

public class ItrulesFileTypeFactory extends com.intellij.openapi.fileTypes.FileTypeFactory {
    public ItrulesFileTypeFactory() {
    }

    public void createFileTypes(@org.jetbrains.annotations.NotNull FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(ItrulesFileType.INSTANCE, ItrulesFileType.INSTANCE.getDefaultExtension());
    }
}
