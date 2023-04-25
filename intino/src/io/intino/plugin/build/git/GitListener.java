package io.intino.plugin.build.git;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import git4idea.GitCommit;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import io.intino.plugin.actions.ReloadConfigurationAction;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
		int numberOfCommits = numberOfCommits(notification.getContent());
		if (numberOfCommits == 0) return;
		GitRepository repository = GitUtil.repositoryManager(project).getRepositories().get(0);
		try {
			List<GitCommit> history = GitHistoryUtils.history(project, repository.getRoot(), "log-size " + numberOfCommits);
			for (GitCommit gitCommit : history) {
				List<File> artifacts = gitCommit.getAffectedPaths().stream().filter(fp -> fp.getName().equals("artifact.legio")).map(FilePath::getIOFile).toList();
				for (File artifact : artifacts) {
					LegioConfiguration conf = findConfigurationOf(artifact);
					if (conf != null) invalidateCacheAndReload(conf);
				}
			}
		} catch (VcsException e) {
			logger.error(e);
		}
	}

	private void invalidateCacheAndReload(LegioConfiguration conf) {
		conf.dependencyAuditor().invalidateAll();
		new ReloadConfigurationAction().execute(conf.module());
	}

	private LegioConfiguration findConfigurationOf(File artifact) {
		ModuleManager manager = ModuleManager.getInstance(project);
		return Arrays.stream(manager.getModules())
				.map(module -> (LegioConfiguration) IntinoUtil.configurationOf(module))
				.filter(Objects::nonNull)
				.filter(c -> c.legiovFile().toNioPath().toFile().getAbsolutePath().equals(artifact.getAbsolutePath()))
				.findFirst().orElse(null);
	}

	private int numberOfCommits(String content) {
		String[] s = content.split(" ");
		if (s.length < 3) return 0;
		return s.length - 2;
	}


}
