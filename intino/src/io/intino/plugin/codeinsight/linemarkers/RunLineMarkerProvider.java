package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.execution.Executor;
import com.intellij.execution.actions.RunContextAction;
import com.intellij.execution.lineMarker.LineMarkerActionWrapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Function;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.plugin.project.run.IntinoRunContextAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;
import static com.intellij.openapi.actionSystem.ActionPlaces.STATUS_BAR_PLACE;
import static com.intellij.openapi.util.text.StringUtil.join;
import static com.intellij.util.containers.ContainerUtil.mapNotNull;
import static io.intino.plugin.DataContext.getContext;

public class RunLineMarkerProvider extends LineMarkerProviderDescriptor {

	@Nullable
	@Override
	public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
		if (!isRunConfiguration(element)) return null;
		final Info info = createInfo(ModuleProvider.moduleOf(element), element);
		if (info == null) return null;
		final DefaultActionGroup actionGroup = new DefaultActionGroup("Run Actions", true);
//		actionGroup.getTemplatePresentation().setVisible(true);
//		actionGroup.getTemplatePresentation().setEnabled(true);
		for (AnAction action : info.actions) actionGroup.add(new LineMarkerActionWrapper(element, action));
		Function<PsiElement, String> tooltipProvider = e -> {
			final String value = info.tooltipProvider.apply(e);
			return value.length() == 0 ? null : value;
		};
		return createLineMarkerInfo(element, info, actionGroup, tooltipProvider);
	}

	@NotNull
	private LineMarkerInfo<PsiElement> createLineMarkerInfo(@NotNull PsiElement element, Info info, DefaultActionGroup actionGroup, Function<PsiElement, String> tooltipProvider) {
		final PsiElement psiElement = leafOf(element);
		return new LineMarkerInfo<>(psiElement, element.getTextRange(), info.icon, tooltipProvider, null, GutterIconRenderer.Alignment.CENTER, psiElement::getText) {
			@NotNull
			@Override
			public GutterIconRenderer createGutterRenderer() {
				return new LineMarkerGutterIconRenderer<>(this) {
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

	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}

	private Info createInfo(Module module, PsiElement runConfiguration) {
		final PsiClass runnerClass = findRunnerClass(module);
		if (runnerClass == null) return null;
		final List<Executor> executors = Executor.EXECUTOR_EXTENSION_NAME.getExtensionList();
		final RunContextAction[] actions = new RunContextAction[]{new IntinoRunContextAction(executors.get(0), runConfiguration), new IntinoRunContextAction(executors.get(1), runConfiguration)};
		return new Info(Run, element -> join(mapNotNull(actions, a -> getText(a, runnerClass)), "\n"), actions);
	}

	private String getText(@NotNull AnAction action, @NotNull PsiElement element) {
		DataContext parent = getContext();
		DataContext dataContext = SimpleDataContext.getSimpleContext(CommonDataKeys.PSI_ELEMENT, element, parent);
		AnActionEvent event = AnActionEvent.createFromAnAction(action, null, STATUS_BAR_PLACE, dataContext);
		ApplicationManager.getApplication().invokeLater(() -> action.update(event));
		Presentation presentation = event.getPresentation();
		return presentation.isEnabled() && presentation.isVisible() ? presentation.getText() : null;
	}

	private PsiClass findRunnerClass(Module module) {
		final LegioConfiguration configuration = (LegioConfiguration) IntinoUtil.configurationOf(module);
		if (configuration == null) return null;
		String qualifiedName = configuration.artifact().packageConfiguration().mainClass();
		if (qualifiedName == null) return null;
		return JavaPsiFacade.getInstance(module.getProject()).findClass(qualifiedName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
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
		return e.getContainingFile().getFileType().equals(LegioFileType.instance()) && e instanceof TaraNode &&
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