package io.intino.plugin.build.git;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
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

public class GitUtils {

	public static GitRepository repository(@NotNull Module module) {
		return repositoryManager(module).getRepositoryForFile(module.getModuleFile());
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

	public static GitCommandResult checkoutToMaster(@NotNull Module module) {
		return Git.getInstance().checkout(repository(module), "master", null, true, false, soutListener());
	}

	public static GitCommandResult checkoutToDevelop(@NotNull Module module) {
		return Git.getInstance().checkout(repository(module), "develop", null, true, false, soutListener());
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
		return (line, outputType) -> System.out.println(outputType.toString() + ": " + line);
	}

}
