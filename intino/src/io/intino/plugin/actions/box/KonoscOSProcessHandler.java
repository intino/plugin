package io.intino.plugin.actions.box;

import com.intellij.execution.process.ProcessIOExecutorService;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.util.Key;
import com.intellij.util.Consumer;
import com.intellij.util.io.BaseInputStreamReader;
import com.intellij.util.io.BaseOutputReader;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.concurrent.Future;

public class KonoscOSProcessHandler {
	private final Process process;
	private final Consumer<String> statusUpdater;

	KonoscOSProcessHandler(Process process, Consumer<String> statusUpdater) {
		this.process = process;
		this.statusUpdater = statusUpdater;
	}


	public void startNotify() {
		InputStream inputStream = process.getInputStream();
		InputStream errStream = process.getErrorStream();
		new SimpleOutputReader(new BaseInputStreamReader(inputStream, Charset.defaultCharset()), ProcessOutputTypes.STDOUT, BaseOutputReader.Options.NON_BLOCKING, "Stream of KonosRunner");
		new SimpleOutputReader(new BaseInputStreamReader(errStream, Charset.defaultCharset()), ProcessOutputTypes.STDERR, BaseOutputReader.Options.NON_BLOCKING, "Stream of KonosRunner");
	}

	public void waitFor() throws InterruptedException {
		process.waitFor();
	}

	protected class SimpleOutputReader extends BaseOutputReader {
		private final Key myProcessOutputType;

		public SimpleOutputReader(Reader reader, Key outputType, BaseOutputReader.Options options, @NotNull String presentableName) {
			super(reader, options);
			myProcessOutputType = outputType;
			start(presentableName);
		}

		@NotNull
		@Override
		protected Future<?> executeOnPooledThread(@NotNull Runnable runnable) {
			return ProcessIOExecutorService.INSTANCE.submit(runnable);
		}

		@Override
		protected void onTextAvailable(@NotNull String text) {
			statusUpdater.consume(text);
		}

	}
}
