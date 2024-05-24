package io.intino.plugin.actions.itrules;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.intellij.notification.NotificationType.ERROR;
import static java.util.regex.Pattern.DOTALL;

public class UpgradeTemplateAction extends GenerationAction {
	public static final Logger LOG = Logger.getInstance("ItRules: Upgrade");

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getData(PlatformDataKeys.PROJECT);
		if (projectExists(e, project)) return;
		List<VirtualFile> rulesFiles = getVirtualFile(e);
		rulesFiles.forEach(r -> {
			var generator = upgradeTemplate(project, r);
			if (generator != null) notify(project, r);
		});
	}

	private Upgrade upgradeTemplate(Project project, VirtualFile rulesFile) {
		if (checkDocument(project, rulesFile)) return null;
		@NotNull Upgrade upgrade;
		Module module = getModuleOf(project, rulesFile);
		try {
			upgrade = createTask(module, rulesFile);
		} catch (Exception e1) {
			error(project, e1.getMessage());
			return null;
		}
		ProgressManager.getInstance().run(upgrade);
		refreshFiles(rulesFile.toNioPath().toFile());
		return upgrade;
	}

	@NotNull
	private Upgrade createTask(Module module, VirtualFile rulesFile) {
		return new Upgrade(module.getProject(), "Upgrade Template", false, rulesFile.toNioPath().toFile());
	}

	private void error(Project project, String message) {
		Notifications.Bus.notify(new Notification("Intino", "Error reading template", message, ERROR), project);
	}

	private void notify(Project project, VirtualFile rulesFile) {
		Notifications.Bus.notify(
				new Notification("Intino", "ItRules", rulesFile.getName() + " upgraded", NotificationType.INFORMATION), project);
	}

	private static class Upgrade extends Task.Modal {
		private final File file;

		public Upgrade(@Nullable Project project, @NlsContexts.DialogTitle @NotNull String title, boolean canBeCancelled, File file) {
			super(project, title, canBeCancelled);
			this.file = file;
		}

		@Override
		public void run(@NotNull ProgressIndicator indicator) {
			try {
				String content = mapHeaders(file);
				content = mapExpressions(content);
				content = removeEscapeCharacter(content);
				Files.writeString(file.toPath(), content);
			} catch (IOException e) {
				LOG.error(e);
			}
		}

		private static String removeEscapeCharacter(String content) {
			if (content.contains("$[") || content.contains("$]"))
				return content.replace("$[", "[").replace("$]", "]");
			return content;
		}

		private static String mapExpressions(String content) {
			String result = separeTokens(content);
			Pattern pattern = Pattern.compile("(?<!\\.{3}|\\$)(?=\\[).*?]", DOTALL);
			List<MatchResult> results = pattern.matcher(result).results().toList();
			for (int i = 0; i < results.size(); i++) {
				MatchResult r = results.get(i);
				int start = r.start() + i * 2;
				int end = r.end() + i * 2;
				if (containsList(result, start, end)) end = result.indexOf("]", end) + 1;
				result = replace(result, start, "<<");
				result = replace(result, end, ">>");
			}
			return result;
		}

		private static String separeTokens(String content) {
			return content
					.replace("[<", "[~<")
					.replace("<[", "<~[")
					.replace("]>", "]~>")
					.replace(">]", ">~]");
		}

		private static boolean containsList(String result, int start, int end) {
			return result.substring(start, end).contains("...[");
		}

		private static String replace(String content, int index, String replacement) {
			if (index >= 0 && index < content.length())
				return content.substring(0, index) + replacement + content.substring(index + 1);
			else {
				System.err.println("out of range");
				return content;
			}
		}

		private static String mapHeaders(File file) {
			try (var lines = Files.lines(file.toPath())) {
				return lines.map(l -> (isHeader(l)) ? mapHeader(l) : l).collect(Collectors.joining("\n"));
			} catch (IOException ignored) {
				return "";
			}
		}

		private static boolean isHeader(String l) {
			return l.startsWith("def");
		}

		private static String mapHeader(String l) {
			if (l.contains(" and ") || l.contains(" not") || l.contains(" or(") || l.contains(" or ")) return l;
			String result = l.replace(") ", ") and ")
					.replace(" &", ",")
					.replace("&:", ",");
			if (result.contains("|")) {
				Pattern pattern = Pattern.compile("type\\([a-zA-Z]+\\s*\\|\\s*[a-zA-Z]+\\)");
				List<MatchResult> matches = pattern.matcher(result).results().toList();
				for (MatchResult match : matches) {
					result = result.replace(match.group(), "(" + match.group()
							.replace(" | ", ") or type(")
							.replace("|", ") or type(") + ")");
				}
			}
			if (result.contains("!")) result = result.replace("!", "not ");
			return result;
		}
	}
}