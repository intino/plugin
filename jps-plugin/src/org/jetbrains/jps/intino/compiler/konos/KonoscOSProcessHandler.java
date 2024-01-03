package org.jetbrains.jps.intino.compiler.konos;

import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.LogLevel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import io.intino.konos.compiler.shared.KonosBuildConstants;
import io.intino.tara.builder.shared.TaraBuildConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.BuildMessage.Kind;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.CustomBuilderMessage;
import org.jetbrains.jps.intino.compiler.OutputItem;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.intino.konos.compiler.shared.KonosBuildConstants.*;
import static io.intino.tara.builder.shared.TaraCompilerMessageCategories.ERROR;
import static io.intino.tara.builder.shared.TaraCompilerMessageCategories.WARNING;

class KonoscOSProcessHandler extends BaseOSProcessHandler {
	private static final String KONOS_COMPILER_IN_OPERATION = "Konos compiler in operation...";
	private static final Logger LOG = Logger.getInstance(KonoscOSProcessHandler.class);

	private final List<OutputItem> compiledItems = new ArrayList<>();
	private final List<BuildMessage> compilerMessages = new ArrayList<>();
	private final List<String> postCompileActionMessages = new ArrayList<>();
	private final StringBuilder stdErr = new StringBuilder();
	private final Consumer<String> statusUpdater;
	private final StringBuilder outputBuffer = new StringBuilder();

	KonoscOSProcessHandler(Process process, String commandLine, Consumer<String> statusUpdater) {
		super(process, commandLine, StandardCharsets.UTF_8);
		LOG.setLevel(LogLevel.ALL);
		this.statusUpdater = statusUpdater;
	}

	private List<String> splitAndTrim(String compiled) {
		return ContainerUtil.map(StringUtil.split(compiled, TaraBuildConstants.SEPARATOR), String::trim);
	}

	public void notifyTextAvailable(final String text, final Key outputType) {
		super.notifyTextAvailable(text, outputType);
		System.out.println("konosc: " + text);
		if (outputType == ProcessOutputTypes.SYSTEM) return;
		if (outputType == ProcessOutputTypes.STDERR) {
			stdErr.append(StringUtil.convertLineSeparators(text));
			return;
		}
		parseOutput(text);
	}

	private void updateStatus(@Nullable String status) {
		statusUpdater.consume(status == null ? KONOS_COMPILER_IN_OPERATION : status);
	}

	private void parseOutput(String text) {
		final String trimmed = text.trim();
		if (trimmed.startsWith(PRESENTABLE_MESSAGE)) {
			updateStatus(trimmed.substring(PRESENTABLE_MESSAGE.length()));
			return;
		}
		if (TaraBuildConstants.CLEAR_PRESENTABLE.equals(trimmed)) {
			updateStatus(null);
			return;
		}
		if (StringUtil.isNotEmpty(text)) {
			outputBuffer.append(trimmed);
			if (trimmed.startsWith(COMPILED_START)) updateStatus("Finishing...");
			else if (trimmed.startsWith(MESSAGES_START)) processMessage();
			if (trimmed.endsWith(COMPILED_END)) processCompiledItems();
			if (trimmed.startsWith(BUILD_END)) {
				if (!postCompileActionMessages.isEmpty()) updateStatus("Updating source classes...");
				collectPostCompileActionMessages();
				compilerMessages.add(new CustomBuilderMessage(KONOSC, ACTION_MESSAGE, String.join(MESSAGE_ACTION_SEPARATOR, postCompileActionMessages)));
			}
		}
	}

	private void collectPostCompileActionMessages() {
		String substring = outputBuffer.substring(outputBuffer.indexOf(START_ACTIONS_MESSAGE), outputBuffer.indexOf(END_ACTIONS_MESSAGE));
		String[] messages = substring.replace(START_ACTIONS_MESSAGE, "").split(END_ACTIONS_MESSAGE);
		Collections.addAll(postCompileActionMessages, messages);
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
		CompilerMessage compilerMessage = new CompilerMessage(KONOSC, kindFrom(category), message, url, -1, -1, -1, lineInt, columnInt);
		if (LOG.isDebugEnabled()) LOG.debug("Message: " + compilerMessage);
		compilerMessages.add(compilerMessage);
	}

	@NotNull
	private Kind kindFrom(String category) {
		return category.equals(ERROR) || category.equals("rebuild_needed")
				? Kind.ERROR
				: category.equals(WARNING)
				? Kind.WARNING
				: Kind.INFO;
	}

	private void processCompiledItems() {
		if (outputBuffer.indexOf(COMPILED_END) == -1) return;
		final String compiled = handleOutputBuffer(COMPILED_START, COMPILED_END);
		final List<String> list = splitAndTrim(compiled);
		String outputFile = list.get(0);
		String sourceFile = list.get(1);
		OutputItem item = new OutputItem(outputFile, sourceFile);
		compiledItems.add(item);
	}

	private String handleOutputBuffer(String startMarker, String endMarker) {
		final int start = outputBuffer.indexOf(startMarker);
		final int end = outputBuffer.indexOf(endMarker);
		if (start > end) throw new AssertionError("Malformed Konosc output: " + outputBuffer.toString());
		String text = outputBuffer.substring(start + startMarker.length(), end);
		outputBuffer.delete(start, end + endMarker.length());
		return text.trim();
	}

	List<OutputItem> getSuccessfullyCompiled() {
		return compiledItems;
	}

	List<BuildMessage> getCompilerMessages(String moduleName) {
		List<BuildMessage> messages = new ArrayList<>(compilerMessages);
		final StringBuilder unParsedBuffer = getStdErr();
		if (unParsedBuffer.length() != 0) {
			String msg = unParsedBuffer.toString();
			if (msg.contains(KonosBuildConstants.NO_KONOS)) {
				msg = "Cannot compile Konos files: no Konos library is defined for module '" + moduleName + "'";
				messages.add(new CompilerMessage(KONOSC, Kind.INFO, msg));
			}
		}
		final int exitValue = getProcess().exitValue();
		if (exitValue != 0) {
			for (BuildMessage message : messages)
				if (message.getKind() == Kind.ERROR)
					return messages;
			messages.add(new CompilerMessage(KONOSC, Kind.ERROR, "Internal Konosc error:" + exitValue));
		}
		return messages;
	}

	boolean shouldRetry() {
		for (BuildMessage message : compilerMessages) {
			if (message.getKind() == Kind.ERROR) {
//				LOG.debug("Error message: " + message);
				return true;
			}
			if (message.getMessageText().contains(KonosBuildConstants.KONOSC_STUB_GENERATION_FAILED)) {
				LOG.debug("Stub failed message: " + message);
				return true;
			}
		}
		if (getStdErr().length() > 0) {
			if (Arrays.stream(getStdErr().toString().split("\n")).allMatch(l -> l.startsWith("WARNING:"))) return false;
			LOG.debug("Non-empty stderr: '" + getStdErr() + "'");
			LOG.error(getStdErr().toString());
			return true;
		}
		return false;
	}

	private StringBuilder getStdErr() {
		return stdErr;
	}

}