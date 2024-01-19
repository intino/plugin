package io.intino.plugin.build.plugins;

import com.intellij.execution.process.ProcessIOExecutorService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.BaseInputStreamReader;
import com.intellij.util.io.BaseOutputReader;
import io.intino.Configuration;
import io.intino.konos.compiler.shared.KonosBuildConstants;
import io.intino.plugin.OutputItem;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.build.postcompileactions.PostCompileActionFactory;
import io.intino.tara.builder.shared.TaraBuildConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import static com.intellij.openapi.diagnostic.Logger.getInstance;
import static com.intellij.util.io.BaseOutputReader.Options.NON_BLOCKING;
import static io.intino.plugin.BuildConstants.*;
import static io.intino.plugin.CompilerMessage.WARNING;
import static java.nio.charset.Charset.defaultCharset;

public class PluginOSProcessHandler {
	private static final Logger LOG = getInstance(PluginOSProcessHandler.class);
	private final Process process;
	private final Module module;
	private final Configuration.Artifact.Plugin plugin;
	private final ProgressIndicator indicator;
	private final StringBuilder outputBuffer = new StringBuilder();
	private final List<OutputItem> compiledItems = new ArrayList<>();
	private final List<CompilerMessage> compilerMessages = new ArrayList<>();
	private final List<PostCompileAction> postCompileActions = new ArrayList<>();

	PluginOSProcessHandler(Process process, Module module, Configuration.Artifact.Plugin plugin, ProgressIndicator indicator) {
		this.process = process;
		this.module = module;
		this.plugin = plugin;
		this.indicator = indicator;
	}

	public void listen() {
		new SimpleOutputReader(new BaseInputStreamReader(process.getInputStream(), defaultCharset()), NON_BLOCKING, "Stream of PluginRunner");
		new SimpleOutputReader(new BaseInputStreamReader(process.getErrorStream(), defaultCharset()), NON_BLOCKING, "Error Stream of PluginRunner");
	}

	public void waitFor() throws InterruptedException {
		process.waitFor();
	}

	public StringBuilder outputBuffer() {
		return outputBuffer;
	}

	public List<OutputItem> compiledItems() {
		return compiledItems;
	}

	public List<CompilerMessage> compilerMessages() {
		return compilerMessages;
	}

	public List<PostCompileAction> postCompileActions() {
		return postCompileActions;
	}

	private void processOutput(String text) {
		final String trimmed = text.trim();
		if (trimmed.startsWith(PRESENTABLE_MESSAGE)) {
			indicator.setText(trimmed.substring(PRESENTABLE_MESSAGE.length()));
			return;
		}
		if (TaraBuildConstants.CLEAR_PRESENTABLE.equals(trimmed)) {
			indicator.setText(null);
			return;
		}
		if (StringUtil.isNotEmpty(text)) {
			outputBuffer.append(trimmed);
			if (trimmed.startsWith(COMPILED_START)) indicator.setText("Finishing...");
			else if (trimmed.startsWith(MESSAGES_START)) processMessage();
			if (trimmed.endsWith(COMPILED_END)) processCompiledItems();
			if (trimmed.startsWith(BUILD_END)) {
				if (!postCompileActions.isEmpty()) indicator.setText("Executing post compile actions...");
				collectPostCompileActionMessages();
			}
		}
	}

	private void collectPostCompileActionMessages() {
		String substring = outputBuffer.substring(outputBuffer.indexOf(START_ACTIONS_MESSAGE), outputBuffer.indexOf(END_ACTIONS_MESSAGE));
		postCompileActions.addAll(Arrays.stream(substring.replace(START_ACTIONS_MESSAGE, "").split(MESSAGE_ACTION_END)).map(this::createCompileAction).toList());
	}

	private void processCompiledItems() {
		if (outputBuffer.indexOf(COMPILED_END) == -1) return;
		final String compiled = handleOutputBuffer(COMPILED_START, COMPILED_END);
		final List<String> list = splitAndTrim(compiled);
		String outputFile = list.get(0);
		String sourceFile = list.get(1);

		OutputItem item = new OutputItem(outputFile, sourceFile);
		LOG.info("Output: " + item);
		compiledItems.add(item);
	}

	private void processMessage() {
		if (outputBuffer.indexOf(MESSAGES_END) == -1) return;
		String text = handleOutputBuffer(MESSAGES_START, MESSAGES_END);
		List<String> tokens = splitAndTrim(text);
		LOG.assertTrue(tokens.size() > 4, "Wrong number of output params");
		String category = tokens.get(0);
		String message = tokens.get(1);
		String url = tokens.get(2);
		String lineNum = tokens.get(3);
		String columnNum = tokens.get(4);
		int lineInt, columnInt;
		try {
			lineInt = Integer.parseInt(lineNum);
			columnInt = Integer.parseInt(columnNum);
		} catch (NumberFormatException e) {
			lineInt = 0;
			columnInt = 0;
		}
		BuildMessage.Kind kind = category.equals(io.intino.plugin.CompilerMessage.ERROR)
				? BuildMessage.Kind.ERROR
				: category.equals(WARNING)
				? BuildMessage.Kind.WARNING
				: BuildMessage.Kind.INFO;
		CompilerMessage compilerMessage = new CompilerMessage(plugin.artifactId(), kind, message, url, -1, -1, -1, lineInt, columnInt);
		if (LOG.isDebugEnabled()) LOG.debug("Message: " + compilerMessage);
		compilerMessages.add(compilerMessage);
	}

	@Nullable
	private PostCompileAction createCompileAction(String m) {
		m = m.replace(KonosBuildConstants.MESSAGE_ACTION_END, "");
		List<String> split = List.of(m.split(SEPARATOR));
		return PostCompileActionFactory.get(module, split.get(1), split.subList(2, split.size()));
	}


	private String handleOutputBuffer(String startMarker, String endMarker) {
		final int start = outputBuffer.indexOf(startMarker);
		final int end = outputBuffer.indexOf(endMarker);
		if (start > end)
			throw new AssertionError("Malformed Tarac output: " + outputBuffer);
		String text = outputBuffer.substring(start + startMarker.length(), end);
		outputBuffer.delete(start, end + endMarker.length());
		return text.trim();
	}

	private List<String> splitAndTrim(String compiled) {
		return ContainerUtil.map(StringUtil.split(compiled, SEPARATOR), String::trim);
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
			try {
				processOutput(text);
			} catch (Throwable e) {
				LOG.error(e);
			}
		}

	}
}
