package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.actions.RunContextAction;
import com.intellij.execution.lineMarker.LineMarkerActionWrapper;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Function;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.run.IntinoRunContextAction;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;
import static com.intellij.openapi.actionSystem.ActionPlaces.STATUS_BAR_PLACE;
import static com.intellij.openapi.util.text.StringUtil.join;
import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public class RunLineMarkerProvider extends LineMarkerProviderDescriptor {

	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
		if (!isRunConfiguration(element)) return null;
		final Info info = createInfo(ModuleProvider.moduleOf(element), element);
		if (info == null) return null;
		final DefaultActionGroup actionGroup = new DefaultActionGroup("run actions", true);
		actionGroup.getTemplatePresentation().setVisible(true);
		actionGroup.getTemplatePresentation().setEnabled(true);
		for (AnAction action : info.actions) actionGroup.add(new LineMarkerActionWrapper(element, action));
		Function<PsiElement, String> tooltipProvider = element1 -> {
			final String value = info.tooltipProvider.apply(element1);
			return value.length() == 0 ? null : value;
		};
		return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), info.icon, Pass.LINE_MARKERS,
				tooltipProvider, null,
				GutterIconRenderer.Alignment.CENTER) {
			@NotNull
			@Override
			public GutterIconRenderer createGutterRenderer() {
				return new LineMarkerGutterIconRenderer<PsiElement>(this) {
					@Override
					public AnAction getClickAction() {
						return null;
					}

					@Override
					public boolean isNavigateAction() {
						return false;
					}

					@NotNull
					@Override
					public ActionGroup getPopupMenuActions() {
						return actionGroup;
					}
				};
			}
		};
	}

	private Info createInfo(Module module, PsiElement runConfiguration) {
		final PsiClass runnerClass = findRunnerClass(module);
		if (runnerClass == null) return null;
		final Executor[] executors = ExecutorRegistry.getInstance().getRegisteredExecutors();

		final RunContextAction[] actions =
				new RunContextAction[]{new IntinoRunContextAction(executors[0], runnerClass, runConfiguration), new IntinoRunContextAction(executors[1], runnerClass, runConfiguration)};
		for (RunContextAction action : actions) {
			action.getTemplatePresentation().setEnabled(true);
			action.getTemplatePresentation().setVisible(true);
		}
		return new Info(Run, element -> join(mapNotNull(actions, a -> getText(a, runnerClass)), "\n"), actions);
	}

	private static String getText(@NotNull AnAction action, @NotNull PsiElement element) {
		DataContext parent = DataManager.getInstance().getDataContext();
		DataContext dataContext = SimpleDataContext.getSimpleContext(CommonDataKeys.PSI_ELEMENT.getName(), element, parent);
		AnActionEvent event = AnActionEvent.createFromAnAction(action, null, STATUS_BAR_PLACE, dataContext);
		action.update(event);
		Presentation presentation = event.getPresentation();
		return presentation.isEnabled() && presentation.isVisible() ? presentation.getText() : null;
	}

	private PsiClass findRunnerClass(Module module) {
		final LegioConfiguration configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		return JavaPsiFacade.getInstance(module.getProject()).findClass(configuration.runnerClass(), GlobalSearchScope.moduleScope(module));
	}

	public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
	}

	@NotNull
	@Override
	public String getName() {
		return "Run line marker";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return AllIcons.RunConfigurations.TestState.Run;
	}


	private boolean isRunConfiguration(PsiElement e) {
		return !(!(e instanceof TaraNode) || !e.getContainingFile().getFileType().equals(LegioFileType.instance())) &&
				((TaraNode) e).type().equals("RunConfiguration");
	}

	public static class Info {
		public final Icon icon;
		public final AnAction[] actions;

		public final java.util.function.Function<PsiElement, String> tooltipProvider;

		public Info(Icon icon, @Nullable com.intellij.util.Function<PsiElement, String> tooltipProvider, @NotNull AnAction... actions) {
			this.icon = icon;
			this.actions = actions;
			this.tooltipProvider = tooltipProvider == null ? null : tooltipProvider::fun;
		}
	}
}