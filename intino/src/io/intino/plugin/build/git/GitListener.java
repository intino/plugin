package io.intino.plugin.build.git;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;
import io.intino.plugin.actions.ReloadConfigurationAction;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GitListener implements Notifications {
	private static final Logger logger = Logger.getInstance(GitListener.class.getName());

	private static final String UpdateID = "git.project.updated";
	private final Project project;

	public GitListener(Project project) {
		this.project = project;
	}

	@Override
	public void notify(@NotNull Notification notification) {
		if (!UpdateID.equals(notification.getDisplayId())) return;
		int numberOfCommits = numberOfCommits(notification.getTitle());
		if (numberOfCommits == 0) return;
		GitRepository repository = GitUtil.repositoryManager(project).getRepositories().get(0);
		withSyncVoidTask("Intino: Analyzing received commits...", () -> {
			analyzeCommits(numberOfCommits, repository);
			return true;
		});
	}

	private void analyzeCommits(int numberOfCommits, GitRepository repository) {
		changedArtifactFiles(numberOfCommits, repository).forEach(a -> invalidateCacheAndReload(configurationOf(a)));
	}

	@NotNull
	private List<File> changedArtifactFiles(int numberOfCommits, GitRepository repository) {
		GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.LOG);
		handler.addParameters("--name-only", "--pretty=format:", "-n", String.valueOf(numberOfCommits));
		GitCommandResult result = Git.getInstance().runCommand(handler);
		if (!result.success()) {
			logger.warn("Unable to read git history: " + String.join("\n", result.getErrorOutput()));
			return Collections.emptyList();
		}
		return result.getOutput().stream()
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.filter(s -> s.endsWith(LegioFileType.ARTIFACT_LEGIO))
				.map(path -> new File(repository.getRoot().getPath(), path))
				.distinct()
				.toList();
	}

	private void invalidateCacheAndReload(ArtifactLegioConfiguration conf) {
		if (conf == null) return;
		new ReloadConfigurationAction().execute(conf.module());
	}

	private ArtifactLegioConfiguration configurationOf(File artifact) {
		ModuleManager manager = ModuleManager.getInstance(project);
		return Arrays.stream(manager.getModules())
				.map(IntinoUtil::configurationOf)
				.filter(c -> c instanceof ArtifactLegioConfiguration)
				.map(c -> (ArtifactLegioConfiguration) c)
				.filter(c -> c.legiovFile().toNioPath().toFile().getAbsolutePath().equals(artifact.getAbsolutePath()))
				.findFirst().orElse(null);
	}

	private int numberOfCommits(String content) {
		String[] s = content.split(" ");
		if (s.length < 3) return 0;
		try {
			return Integer.parseInt(s[s.length - 2]);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private boolean withSyncVoidTask(String title, ThrowableComputable<Object, Exception> runnable) {
		try {
			ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, title, false, project);
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

}
