package io.intino.plugin.build.git;

import com.intellij.openapi.module.Module;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.commands.Git;
import git4idea.commands.GitCommandResult;
import git4idea.push.GitPushParamsImpl;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class GitUtils {

	public static GitRepository repository(@NotNull Module module) {
		return GitRepositoryManager.getInstance(module.getProject()).getRepositoryForFile(module.getModuleFile());
	}

	public static GitCommandResult tagCurrent(@NotNull Module module, String tag) {
		GitRepository repository = repository(module);
		Git git = Git.getInstance();
		GitCommandResult result = git.createNewTag(repository, tag, (line, outputType) -> System.out.println(outputType.toString() + ": " + line), "HEAD");
		if (result.success()) {
			GitLocalBranch localMaster = repository.getBranches().findLocalBranch("master");
			GitRemoteBranch remoteMaster = repository.getBranches().getRemoteBranches().stream().filter(r -> r.getNameForRemoteOperations().equals("master")).findFirst().orElse(null);
			if (remoteMaster == null || localMaster == null) return result;
			String spec = localMaster.getFullName() + ":" + remoteMaster.getNameForRemoteOperations();
			return git.push(repository, new GitPushParamsImpl(remoteMaster.getRemote(), spec, true, false, true, tag, Collections.emptyList()));
		}
		return result;
	}
}
