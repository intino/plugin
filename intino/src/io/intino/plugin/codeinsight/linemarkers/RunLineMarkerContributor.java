package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.tara.plugin.lang.psi.TaraNode;
import org.jetbrains.annotations.Nullable;

import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;

public class RunLineMarkerContributor extends com.intellij.execution.lineMarker.RunLineMarkerContributor {

	@Nullable
	@Override
	public Info getInfo(PsiElement e) {
		if (isIdentifier(e)) {
			final AnAction[] actions = ExecutorAction.getActions(0);
			return new Info(Run, element1 -> StringUtil.join(ContainerUtil.mapNotNull(actions, action -> getText(action, element1)), "\n"), actions);
		}
		return null;
	}


	private boolean isIdentifier(PsiElement e) {
		if (!(e instanceof TaraNode) || !e.getContainingFile().getFileType().equals(LegioFileType.instance()))
			return false;
		return ((TaraNode) e).type().equals("RunConfiguration");

	}
}
