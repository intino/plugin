package org.jetbrains.jps.intino.compiler.tara;

import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.intino.compiler.OutputItem;

import java.util.List;

public record CompilationResult(List<BuildMessage> compilerMessages, boolean shouldRetry,
								List<OutputItem> successfullyCompiled) {
}
