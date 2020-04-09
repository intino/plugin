package io.intino.plugin.build.git;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.GitVcs;
import git4idea.actions.GitRepositoryAction;
import git4idea.commands.*;
import git4idea.push.GitPushParamsImpl;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static git4idea.commands.GitImpl.REBASE_CONFIG_PARAMS;

public class GitUtil {
	private static final Logger logger = Logger.getInstance(GitUtil.class.getName());

	public static GitRepository repository(@NotNull Module module) {
		return repositoryManager(module).getRepositoryForFile(module.getModuleFile());
	}

	public static boolean isModified(Module module, PsiFile file) {
		VirtualFile vcsRoot = GitRepositoryAction.getGitRoots(module.getProject(), GitVcs.getInstance(module.getProject())).get(0);
		String relativeFilePath = file.getVirtualFile().getPath().replace(vcsRoot.getPath(), "");
		if (relativeFilePath.startsWith("/")) relativeFilePath = relativeFilePath.substring(1);
		GitLineHandler handler = new GitLineHandler(module.getProject(), vcsRoot, GitCommand.STATUS);
		GitCommandResult result = Git.getInstance().runCommand(handler);
		if (result.success()) {
			String finalRelativeFilePath = relativeFilePath;
			return result.getOutput().stream().anyMatch(l -> l.contains(finalRelativeFilePath));
		}
		return false;
	}

	public static String currentBranch(Module module) {
		GitRepository repository = repository(module);
		return repository == null ? null : repository.getCurrentBranchName();
	}

	public static GitCommandResult tagCurrentAndPush(@NotNull Module module, String tag) {
		GitCommandResult result = Git.getInstance().createNewTag(repository(module), tag, soutListener(), "HEAD");
		return result.success() ? pushMaster(module, tag) : result;
	}

	@NotNull
	public static GitCommandResult pushMaster(@NotNull Module module, String tagMode) {
		GitLocalBranch localMaster = repository(module).getBranches().findLocalBranch("master");
		GitRemoteBranch remoteMaster = repository(module).getBranches().getRemoteBranches().stream().filter(r -> r.getNameForRemoteOperations().equals("master")).findFirst().orElse(null);
		if (remoteMaster == null || localMaster == null)
			return new GitCommandResult(true, 0, Collections.singletonList("Master branch does not exist"), Collections.emptyList());
		String spec = localMaster.getFullName() + ":" + remoteMaster.getNameForRemoteOperations();
		return Git.getInstance().push(repository(module), new GitPushParamsImpl(remoteMaster.getRemote(), spec, true, false, true, tagMode, Collections.emptyList()));
	}

	public static GitCommandResult checkoutTo(@NotNull Module module, String branch) {
		return Git.getInstance().checkout(repository(module), branch, null, true, false, soutListener());
	}

	public static GitCommandResult mergeDevelopIntoMaster(Module module) {
		GitRepository repository = repository(module);
		String beforeRevision = repository.getCurrentRevision();
		Git git = Git.getInstance();
		GitCommandResult result = git.merge(repository, "develop", Collections.emptyList(), soutListener());
		if (!result.success()) git.resetMerge(repository, beforeRevision);
		return result;
	}

	public static GitCommandResult pull(@NotNull Module module, String branch) {
		List<VirtualFile> gitRoots = GitRepositoryAction.getGitRoots(module.getProject(), GitVcs.getInstance(module.getProject()));
		if (gitRoots == null || gitRoots.isEmpty())
			return new GitCommandResult(true, -1, Collections.singletonList("Git root not found"), Collections.emptyList());
		GitLineHandler handler = new GitLineHandler(module.getProject(), gitRoots.get(0), GitCommand.PULL, REBASE_CONFIG_PARAMS);
		return Git.getInstance().runCommand(handler);
	}

	@NotNull
	private static GitRepositoryManager repositoryManager(@NotNull Module module) {
		return GitRepositoryManager.getInstance(module.getProject());
	}

	@NotNull
	private static GitLineHandlerListener soutListener() {
		return (line, outputType) -> logger.info(outputType.toString() + ": " + line);
	}

}
