package io.intino.plugin.build.git;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import git4idea.GitCommit;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import io.intino.plugin.actions.ReloadConfigurationAction;
import io.intino.plugin.dependencyresolution.ResolutionCache;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
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
		try {
			List<GitCommit> history = GitHistoryUtils.history(project, repository.getRoot()).subList(0, numberOfCommits);
			history.stream()
					.flatMap(c -> c.getAffectedPaths().stream().filter(fp -> fp.getName().equals(LegioFileType.LEGIO_FILE)).map(FilePath::getIOFile).distinct())
					.distinct()
					.forEach(a -> invalidateCacheAndReload(configurationOf(a)));
		} catch (VcsException e) {
			logger.error(e);
		}
	}

	private void invalidateCacheAndReload(LegioConfiguration conf) {
		if (conf == null) return;
		conf.dependencyAuditor().invalidateAll();
		ResolutionCache.instance(project).invalidate();
		new ReloadConfigurationAction().execute(conf.module());
	}

	private LegioConfiguration configurationOf(File artifact) {
		ModuleManager manager = ModuleManager.getInstance(project);
		return Arrays.stream(manager.getModules())
				.map(IntinoUtil::configurationOf)
				.filter(c -> c instanceof LegioConfiguration)
				.map(c -> (LegioConfiguration) c)
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
