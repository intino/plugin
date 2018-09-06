package io.intino.plugin.toolwindows.output;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.NotNull;

public class ConsoleActionsProcessor extends ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		return super.postProcess(console, actions);
	}

	@NotNull
	@Override
	public AnAction[] postProcessPopupActions(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		return super.postProcessPopupActions(console, actions);
	}
}
