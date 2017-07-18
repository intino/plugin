package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.actions.RunContextAction;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.tara.plugin.lang.psi.TaraNode;
import org.jetbrains.annotations.Nullable;

import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;
import static com.intellij.openapi.util.text.StringUtil.join;
import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public class RunLineMarkerContributor extends com.intellij.execution.lineMarker.RunLineMarkerContributor {

	@Nullable
	@Override
	public Info getInfo(PsiElement e) {
		if (isRunConfiguration(e)) {
			final Executor[] executors = ExecutorRegistry.getInstance().getRegisteredExecutors();
			final AnAction[] actions = new AnAction[]{new RunContextAction(executors[0]), new RunContextAction(executors[1])};
			final Function<PsiElement, String> tooltipProvider = element -> join(mapNotNull(actions, action -> {
				DataContext parent = DataManager.getInstance().getDataContext();
				DataContext dataContext = SimpleDataContext.getSimpleContext(CommonDataKeys.PSI_ELEMENT.getName(), element, parent);
				AnActionEvent event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.STATUS_BAR_PLACE, dataContext);
				action.update(event);
				Presentation presentation = event.getPresentation();
				return presentation.isEnabled() && presentation.isVisible() ? presentation.getText() : null;
			}), "\n");
			return new Info(Run, tooltipProvider, actions);
		}
		return null;
	}

	private boolean isRunConfiguration(PsiElement e) {
		return !(!(e instanceof TaraNode) || !e.getContainingFile().getFileType().equals(LegioFileType.instance())) &&
				((TaraNode) e).type().equals("RunConfiguration");

	}
}
