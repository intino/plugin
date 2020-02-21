package io.intino.plugin.build.git;

import com.intellij.openapi.module.Module;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

public class GitUtils {

	public static GitRepository repository(@NotNull Module module) {
		return GitRepositoryManager.getInstance(module.getProject()).getRepositoryForFile(module.getModuleFile());
	}
}
