package io.intino.plugin.build.git;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitBranch;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.GitVcs;
import git4idea.actions.GitRepositoryAction;
import git4idea.branch.GitBranchesCollection;
import git4idea.commands.*;
import git4idea.push.GitPushParamsImpl;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static git4idea.commands.GitImpl.REBASE_CONFIG_PARAMS;
import static git4idea.history.GitHistoryUtils.getCurrentRevision;

public class GitUtil {
	private static final Logger logger = Logger.getInstance(GitUtil.class.getName());

	public static GitRepository repository(@NotNull Module module) {
		Application application = ApplicationManager.getApplication();
		if (application.isDispatchThread())
			return (GitRepository) withSyncTask(module.getProject(), "Refreshing vcs", () -> getRepositoryForFile(module));
		return getRepositoryForFile(module);
	}

	private static GitRepository getRepositoryForFile(@NotNull Module module) {
		return repositoryManager(module.getProject()).getRepositoryForFile(ProjectUtil.guessModuleDir(module));
	}

	public static boolean isModified(Module module, VirtualFile file) {
		VirtualFile vcsRoot = GitRepositoryAction.getGitRoots(module.getProject(), GitVcs.getInstance(module.getProject())).get(0);
		String relativeFilePath = file.getPath().replace(vcsRoot.getPath(), "");
		if (relativeFilePath.startsWith("/")) relativeFilePath = relativeFilePath.substring(1);
		GitLineHandler handler = new GitLineHandler(module.getProject(), vcsRoot, GitCommand.STATUS);
		GitCommandResult result = (GitCommandResult) withSyncTask(module.getProject(), "Checking vcs", () -> Git.getInstance().runCommand(handler));
		if (result != null && result.success()) {
			String finalRelativeFilePath = relativeFilePath;
			return (!upToDate(result.getOutput())) && (!finalRelativeFilePath.isEmpty() && result.getOutput().stream().anyMatch(l -> l.contains(finalRelativeFilePath)));
		}
		return false;
	}

	private static boolean upToDate(@NotNull List<String> output) {
		return output.stream().anyMatch(l -> l.contains("nothing to commit"));
	}

	public static String currentBranch(Module module) {
		GitRepository repository = repository(module);
		if (repository == null) return null;
		Application application = ApplicationManager.getApplication();
		if (application.isDispatchThread()) {
			withSyncVoidTask(module.getProject(), "Refreshing vcs", () -> {
				git4idea.GitUtil.updateRepositories(List.of(repository));
				return true;
			});
		} else git4idea.GitUtil.updateRepositories(List.of(repository));
		return repository.getCurrentBranch().getName();
	}

	public static @Nullable GitBranch mainBranch(@NotNull Module module) {
		GitRepository repository = repository(module);
		if (repository == null) return null;
		Application application = ApplicationManager.getApplication();
		if (application.isDispatchThread()) {
			withSyncVoidTask(module.getProject(), "Refreshing vcs", () -> {
				git4idea.GitUtil.updateRepositories(List.of(repository));
				return true;
			});
		} else git4idea.GitUtil.updateRepositories(List.of(repository));
		GitBranchesCollection branches = repository.getBranches();
		if (branches.findBranchByName("main") != null) return branches.findBranchByName("main");
		return branches.findBranchByName("master");
	}


	public static GitCommandResult tagCurrentAndPush(@NotNull Module module, String tag) {
		GitCommandResult result = Git.getInstance().createNewTag(repository(module), tag, soutListener("tagCurrentAndPush" + " " + tag), "HEAD");
		return result.success() ? pushMaster(module, tag) : result;
	}

	@NotNull
	public static GitCommandResult pushMaster(@NotNull Module module, String tagMode) {
		GitLocalBranch localMaster = findMasterLocalBranch(module);
		GitRemoteBranch remoteMaster = localMaster != null ? localMaster.findTrackedBranch(repository(module)) : null;
		if (remoteMaster == null)
			return new GitCommandResult(true, 0, Collections.singletonList("Master branch does not exist"), Collections.emptyList());
		String spec = localMaster.getFullName() + ":" + remoteMaster.getNameForRemoteOperations();
		return Git.getInstance().push(repository(module), new GitPushParamsImpl(remoteMaster.getRemote(), spec, true, false, true, tagMode, Collections.emptyList()));
	}

	@Nullable
	private static GitLocalBranch findMasterLocalBranch(@NotNull Module module) {
		GitLocalBranch master = repository(module).getBranches().findLocalBranch("master");
		return master != null ? master : repository(module).getBranches().findLocalBranch("main");
	}

	public static GitCommandResult checkoutTo(@NotNull Module module, String branch) {
		return Git.getInstance().checkout(repository(module), branch, null, true, false, soutListener("checkout to " + branch));
	}

	public static GitCommandResult stashChanges(@NotNull Module module, String message) {
		return Git.getInstance().stashSave(repository(module), message);
	}

	public static void popStash(@NotNull Module module, String stash) {
		if (stash == null) return;
		String result = stashList(module);
		if (result == null) return;
		String index = findStash(result, stash);
		if (index == null) return;
		popStashByIndex(module, index);

	}

	private static String stashList(@NotNull Module module) {
		final GitLineHandler handler = new GitLineHandler(module.getProject(), repository(module).getRepositoryFiles().getRootDir(), GitCommand.STASH);
		handler.addParameters("list");
		StringBuilder builder = new StringBuilder();
		handler.addLineListener(stringBuilderListener(builder));
		GitCommandResult gitCommandResult = Git.getInstance().runCommand(handler);
		if (!gitCommandResult.success()) return null;
		return builder.toString();
	}

	private static void popStashByIndex(@NotNull Module module, String stashIndex) {
		final GitLineHandler handler = new GitLineHandler(module.getProject(), repository(module).getRoot(), GitCommand.STASH);
		handler.addParameters("pop");
		handler.addParameters(stashIndex);
		StringBuilder builder = new StringBuilder();
		handler.addLineListener(stringBuilderListener(builder));
		Git.getInstance().runCommand(handler);
	}


	private static String findStash(String out, String stash) {
		String[] lines = out.split("\n");
		for (String line : lines)
			if (line.contains(stash) && line.contains(":")) {
				logger.info("Stash found:" + line);
				return line.substring(0, line.indexOf(":"));
			}
		return null;
	}

	public static GitCommandResult mergeBranchIntoCurrent(Module module, String branch) {
		GitRepository repository = repository(module);
		String beforeRevision = currentRevision(module);
		Git git = Git.getInstance();
		GitCommandResult result = git.merge(repository, branch, Collections.emptyList(), soutListener("merge branch " + branch));
		if (!result.success()) git.resetMerge(repository, beforeRevision);
		return result;
	}


	public static @Nullable String currentRevision(Module module) {
		try {
			return getCurrentRevision(module.getProject(), new LocalFilePath(GitUtil.repository(module).getRepositoryFiles().getRootDir().getPath(), true), null).asString();
		} catch (VcsException e) {
			return null;
		}
	}


	public static GitCommandResult pull(@NotNull Module module, String branch) {
		List<VirtualFile> gitRoots = GitRepositoryAction.getGitRoots(module.getProject(), GitVcs.getInstance(module.getProject()));
		if (gitRoots == null || gitRoots.isEmpty())
			return new GitCommandResult(true, -1, Collections.singletonList("Git root not found"), Collections.emptyList());
		GitLineHandler handler = new GitLineHandler(module.getProject(), gitRoots.get(0), GitCommand.PULL, REBASE_CONFIG_PARAMS);
		return Git.getInstance().runCommand(handler);
	}

	@NotNull
	public static GitRepositoryManager repositoryManager(Project project) {
		return GitRepositoryManager.getInstance(project);
	}

	@NotNull
	private static GitLineHandlerListener soutListener(String prefix) {
		return (line, outputType) -> logger.info(prefix + "> " + outputType.toString() + ": " + line);
	}

	@NotNull
	private static GitLineHandlerListener stringBuilderListener(StringBuilder builder) {
		return (line, outputType) -> builder.append(line);
	}

	private static Object withSyncTask(Project project, String title, ThrowableComputable<Object, Exception> runnable) {
		try {
			return ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, title, false, project);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	private static boolean withSyncVoidTask(Project project, String title, ThrowableComputable<Object, Exception> runnable) {
		try {
			ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, title, false, project);
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
}
