package io.intino.plugin.actions.export;

import com.intellij.execution.process.ProcessIOExecutorService;
import com.intellij.util.io.BaseInputStreamReader;
import com.intellij.util.io.BaseOutputReader;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ExportOSProcessHandler {
	private final Process process;
	private final Consumer<String> statusUpdater;

	ExportOSProcessHandler(Process process, Consumer<String> statusUpdater) {
		this.process = process;
		this.statusUpdater = statusUpdater;
	}


	public void startNotify() {
		InputStream inputStream = process.getInputStream();
		InputStream errStream = process.getErrorStream();
		new SimpleOutputReader(new BaseInputStreamReader(inputStream, Charset.defaultCharset()), BaseOutputReader.Options.NON_BLOCKING, "Stream of KonosRunner");
		new SimpleOutputReader(new BaseInputStreamReader(errStream, Charset.defaultCharset()), BaseOutputReader.Options.NON_BLOCKING, "Stream of KonosRunner");
	}

	public void waitFor() throws InterruptedException {
		process.waitFor();
	}

	protected class SimpleOutputReader extends BaseOutputReader {

		public SimpleOutputReader(Reader reader, BaseOutputReader.Options options, @NotNull String presentableName) {
			super(reader, options);
			start(presentableName);
		}

		@NotNull
		@Override
		protected Future<?> executeOnPooledThread(@NotNull Runnable runnable) {
			return ProcessIOExecutorService.INSTANCE.submit(runnable);
		}

		@Override
		protected void onTextAvailable(@NotNull String text) {
			statusUpdater.accept(text);
		}

	}
}
