package io.intino.plugin.build.maven;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import io.intino.plugin.build.LifeCyclePhase;
import org.apache.maven.shared.invoker.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.*;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import tara.compiler.core.errorcollection.TaraException;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.LanguageManager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import static io.intino.plugin.MessageProvider.message;
import static java.util.Arrays.asList;
import static org.jetbrains.idea.maven.execution.MavenExecutionOptions.LoggingLevel.ERROR;
import static org.jetbrains.idea.maven.utils.MavenUtil.resolveMavenHomeDirectory;

public class LegioMavenRunner {

	private static final Logger LOG = Logger.getInstance(LegioMavenRunner.class.getName());
	private final Module module;
	private String output = "";

	public LegioMavenRunner(Module module) {
		this.module = module;
	}

	public void executeLanguage(Configuration conf) throws MavenInvocationException, IOException {
		InvocationRequest request = new DefaultInvocationRequest().setGoals(Collections.singletonList("deploy:deploy-file"));
		request.setMavenOpts("-Durl=" + conf.languageRepository() + " " +
				"-DrepositoryId=" + conf.languageRepositoryId() + " " +
				"-DgroupId=tara.dsl " +
				"-DartifactId=" + conf.artifactId() + " " +
				"-Dversion=" + conf.modelVersion() + " " +
				"-Dfile=" + fileOfLanguage(conf));
		final Properties properties = new Properties();
		request.setProperties(properties);
		final InvocationResult result = invoke(request);
		if (result != null && result.getExitCode() != 0)
			throwException(result, "error.publishing.language", LifeCyclePhase.DISTRIBUTE);
		else if (result == null)
			throw new IOException(message("error.publishing.language", LifeCyclePhase.DISTRIBUTE, "Maven HOME not found"));
	}

	public void executeFramework(LifeCyclePhase phase) throws MavenInvocationException, IOException {
		final File pom = PomCreator.createFrameworkPom(module);
		final InvocationResult result = invoke(pom, phase);
		if (result != null && result.getExitCode() != 0) throwException(result, "error.publishing.framework", phase);
		else {
			FileUtil.delete(pom);
			if (result == null)
				throw new IOException(message("error.publishing.framework", phase.name().toLowerCase(), "Maven HOME not found"));
		}
	}

	public void executeNativeMaven() {
		final MavenProject project = MavenProjectsManager.getInstance(module.getProject()).findProject(module);
		if (project == null) return;
		MavenGeneralSettings generalSettings = new MavenGeneralSettings();
		generalSettings.setOutputLevel(ERROR);
		generalSettings.setPrintErrorStackTraces(false);
		generalSettings.setFailureBehavior(MavenExecutionOptions.FailureMode.AT_END);
		MavenRunnerSettings runnerSettings = MavenRunner.getInstance(module.getProject()).getSettings().clone();
		runnerSettings.setSkipTests(false);
		runnerSettings.setRunMavenInBackground(true);
		MavenRunnerParameters parameters = new MavenRunnerParameters(true, new File(project.getPath()).getParent(), asList("install", "deploy"), Collections.emptyList());
		MavenRunConfigurationType.runConfiguration(module.getProject(), parameters, generalSettings, runnerSettings, null);
	}

	@NotNull
	private String fileOfLanguage(Configuration conf) {
		return LanguageManager.getLanguageDirectory(conf.outDSL()) + "/" + conf.modelVersion() + "/" + conf.artifactId() + "-" + conf.modelVersion() + ".jar ";
	}

	private InvocationResult invoke(File pom, LifeCyclePhase lifeCyclePhase) throws MavenInvocationException, IOException {
		final String ijMavenHome = MavenProjectsManager.getInstance(module.getProject()).getGeneralSettings().getMavenHome();
		InvocationRequest request = new DefaultInvocationRequest().setPomFile(pom).setGoals(lifeCyclePhase.mavenActions());
		request.setJavaHome(new File(System.getProperty("java.home")));
		final File mavenHome = resolveMavenHomeDirectory(ijMavenHome);
		if (mavenHome == null) return null;
		Invoker invoker = new DefaultInvoker().setMavenHome(mavenHome);
		log(invoker);
		config(request, mavenHome);
		return invoker.execute(request);
	}

	private InvocationResult invoke(InvocationRequest request) throws MavenInvocationException, IOException {
		final String ijMavenHome = MavenProjectsManager.getInstance(module.getProject()).getGeneralSettings().getMavenHome();
		final File mavenHome = resolveMavenHomeDirectory(ijMavenHome);
		if (mavenHome == null) return null;
		Invoker invoker = new DefaultInvoker().setMavenHome(mavenHome);
		log(invoker);
		config(request, mavenHome);
		return invoker.execute(request);
	}

	private void throwException(InvocationResult result, String message, LifeCyclePhase phase) throws IOException {
		if (result.getExecutionException() != null)
			throw new IOException(message(message, phase.gerund().toLowerCase(), result.getExecutionException().getMessage()), result.getExecutionException());
		else throw new IOException(message(message, phase.gerund().toLowerCase(), output), new TaraException(output));
	}

	private void log(Invoker invoker) throws IOException {
		invoker.setOutputHandler(s -> {
			output += (s.startsWith("[ERROR]")) ? s + "\n" : "";
			System.out.println(s);
		});
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void config(InvocationRequest request, File mavenHome) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
		if (sdk != null && sdk.getHomePath() != null) request.setJavaHome(new File(sdk.getHomePath()));
	}
}
