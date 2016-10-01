package org.siani.legio.plugin.build;

import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import org.apache.maven.shared.invoker.*;
import org.jetbrains.idea.maven.execution.*;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.jetbrains.idea.maven.execution.MavenExecutionOptions.LoggingLevel.ERROR;
import static org.jetbrains.idea.maven.utils.MavenUtil.resolveMavenHomeDirectory;

public class LegioMavenRunner {

	private static final Logger LOG = Logger.getInstance(LegioMavenRunner.class.getName());
	private final Module module;

	public LegioMavenRunner(Module module) {
		this.module = module;

	}

	public void publishNativeMaven() {
		final MavenProject project = MavenProjectsManager.getInstance(module.getProject()).findProject(module);
		if (project == null) return;
		MavenGeneralSettings generalSettings = new MavenGeneralSettings();
		generalSettings.setOutputLevel(ERROR);
		generalSettings.setPrintErrorStackTraces(false);
		generalSettings.setFailureBehavior(MavenExecutionOptions.FailureMode.AT_END);
		MavenRunnerSettings runnerSettings = MavenRunner.getInstance(module.getProject()).getSettings().clone();
		runnerSettings.setSkipTests(false);
		runnerSettings.setRunMavenInBackground(true);
		MavenRunnerParameters parameters = new MavenRunnerParameters(true, new File(project.getPath()).getParent(), Arrays.asList(ParametersList.parse("deploy")), Collections.emptyList());
		MavenRunConfigurationType.runConfiguration(module.getProject(), parameters, generalSettings, runnerSettings, null);
	}

	public void publishFramework() throws MavenInvocationException, IOException {
		final File pom = PomCreator.createPom(module);
		final InvocationResult result = invoke(pom);
		FileUtil.delete(pom);
		if (result != null && result.getExitCode() != 0) {
			if (result.getExecutionException() != null)
				throw new IOException("Failed to publish language.", result.getExecutionException());
			else throw new IOException("Failed to publish language. Exit code: " + result.getExitCode());
		} else if (result == null) throw new IOException("Failed to publish language. Maven HOME not found");
	}

	private InvocationResult invoke(File pom) throws MavenInvocationException, IOException {
		final String ijMavenHome = MavenProjectsManager.getInstance(module.getProject()).getGeneralSettings().getMavenHome();
		InvocationRequest request = new DefaultInvocationRequest().setPomFile(pom).setGoals(Arrays.asList("install", "deploy"));
		final File mavenHome = resolveMavenHomeDirectory(ijMavenHome);
		if (mavenHome == null) return null;
		LOG.info("Maven HOME: " + mavenHome.getAbsolutePath());
		Invoker invoker = new DefaultInvoker().setMavenHome(mavenHome);
		log(invoker);
		config(request, mavenHome);
		return invoker.execute(request);
	}

	private void log(Invoker invoker) throws IOException {
		invoker.setErrorHandler(LOG::error);
		invoker.setOutputHandler(System.out::println);
	}

	private void config(InvocationRequest request, File mavenHome) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
		if (sdk != null && sdk.getHomePath() != null) request.setJavaHome(new File(sdk.getHomePath()));
	}
}
