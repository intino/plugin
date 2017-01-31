package io.intino.plugin.build.maven;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import io.intino.plugin.build.LifeCyclePhase;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.actions.utils.FileSystemUtils;
import io.intino.tara.plugin.lang.LanguageManager;
import org.apache.maven.shared.invoker.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenExecutionOptions;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static io.intino.plugin.MessageProvider.message;
import static java.util.Arrays.asList;
import static org.jetbrains.idea.maven.execution.MavenExecutionOptions.LoggingLevel.ERROR;
import static org.jetbrains.idea.maven.utils.MavenUtil.resolveMavenHomeDirectory;

public class MavenRunner {

	private static final Logger LOG = Logger.getInstance(MavenRunner.class.getName());
	private Module module;
	private InvocationOutputHandler handler;
	private String output = "";

	public MavenRunner(Module module) {
		this.module = module;
		handler = defaultHandler();
	}

	public MavenRunner(Module module, InvocationOutputHandler handler) {
		this.module = module;
		this.handler = handler == null ? defaultHandler() : handler;
	}

	@NotNull
	private InvocationOutputHandler defaultHandler() {
		return s -> {
			output += (s.startsWith("[ERROR]")) ? s + "\n" : "";
			System.out.println(s);
		};
	}

	public void executeLanguage(Configuration conf) throws MavenInvocationException, IOException {
		if (conf.distributionLanguageRepository() == null) throw new IOException(message("none.distribution.language.repository"));
		InvocationRequest request = new DefaultInvocationRequest().setGoals(Collections.singletonList("deploy:deploy-file"));
		request.setMavenOpts("-Durl=" + conf.distributionLanguageRepository().getKey() + " " +
				"-DrepositoryId=" + conf.distributionLanguageRepository().getValue() + " " +
				"-DgroupId=tara.dsl " +
				"-DartifactId=" + conf.artifactId() + " " +
				"-Dversion=" + conf.version() + " " +
				"-Dfile=" + fileOfLanguage(conf));
		final Properties properties = new Properties();
		request.setProperties(properties);
		final InvocationResult result = invokeMaven(request);
		if (result != null && result.getExitCode() != 0)
			throwException(result, "error.publishing.language", LifeCyclePhase.DISTRIBUTE);
		else if (result == null)
			throw new IOException(message("error.publishing.language", LifeCyclePhase.DISTRIBUTE, "Maven HOME not found"));
	}

	public void executeFramework(LifeCyclePhase phase) throws MavenInvocationException, IOException {
		final File pom = new PomCreator(module).frameworkPom();
		final InvocationResult result = invokeMaven(pom, phase);
		if (result != null && result.getExitCode() != 0) throwException(result, "error.publishing.framework", phase);
		else {
			FileUtil.delete(pom);
			if (result == null)
				throw new IOException(message("error.publishing.framework", phase.name().toLowerCase(), "Maven HOME not found"));
		}
	}

	public void invokeMaven(String... phases) {
		final MavenProject project = MavenProjectsManager.getInstance(module.getProject()).findProject(module);
		if (project == null) return;
		MavenGeneralSettings generalSettings = new MavenGeneralSettings();
		generalSettings.setOutputLevel(ERROR);
		generalSettings.setPrintErrorStackTraces(false);
		generalSettings.setFailureBehavior(MavenExecutionOptions.FailureMode.AT_END);
		MavenRunnerSettings runnerSettings = org.jetbrains.idea.maven.execution.MavenRunner.getInstance(module.getProject()).getSettings().clone();
		runnerSettings.setSkipTests(false);
		runnerSettings.setRunMavenInBackground(true);
		MavenRunnerParameters parameters = new MavenRunnerParameters(true, new File(project.getPath()).getParent(), asList(phases), Collections.emptyList());
		MavenRunConfigurationType.runConfiguration(module.getProject(), parameters, generalSettings, runnerSettings, null);
	}

	public InvocationResult invokeMaven(File pom, String... phases) throws MavenInvocationException, IOException {
		final String ijMavenHome = MavenProjectsManager.getInstance(module.getProject()).getGeneralSettings().getMavenHome();
		InvocationRequest request = new DefaultInvocationRequest().setPomFile(pom).setGoals(Arrays.asList(phases));
		request.setJavaHome(new File(System.getProperty("java.home")));
		final File mavenHome = resolveMavenHomeDirectory(ijMavenHome);
		if (mavenHome == null) return null;
		Invoker invoker = new DefaultInvoker().setMavenHome(mavenHome);
		log(invoker);
		config(request, mavenHome);
		return invoker.execute(request);
	}

	private InvocationResult invokeMaven(File pom, LifeCyclePhase lifeCyclePhase) throws MavenInvocationException, IOException {
		return invokeMaven(pom, lifeCyclePhase.mavenActions().toArray(new String[lifeCyclePhase.mavenActions().size()]));
	}

	public String output() {
		return this.output;
	}

	private InvocationResult invokeMaven(InvocationRequest request) throws MavenInvocationException, IOException {
		final String ijMavenHome = MavenProjectsManager.getInstance(module.getProject()).getGeneralSettings().getMavenHome();
		final File mavenHome = resolveMavenHomeDirectory(ijMavenHome);
		if (mavenHome == null) return null;
		Invoker invoker = new DefaultInvoker().setMavenHome(mavenHome);
		log(invoker);
		config(request, mavenHome);
		return invoker.execute(request);
	}

	@NotNull
	private String fileOfLanguage(Configuration conf) {
		try {
			final String originalFile = LanguageManager.getLanguageDirectory(conf.outDSL()) + "/" + conf.version() + "/" + conf.artifactId() + "-" + conf.version() + ".jar";
			final Path deployLanguage = Files.createTempDirectory("deployLanguage");
			final File destination = new File(deployLanguage.toFile(), new File(originalFile).getName());
			FileSystemUtils.copyFile(originalFile, destination.getAbsolutePath());
			return destination.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private void throwException(InvocationResult result, String message, LifeCyclePhase phase) throws IOException {
		if (result.getExecutionException() != null)
			throw new IOException(message(message, phase.gerund().toLowerCase(), result.getExecutionException().getMessage()), result.getExecutionException());
		else throw new IOException(message(message, phase.gerund().toLowerCase(), output), new IOException(output));
	}

	private void log(Invoker invoker) throws IOException {
		invoker.setOutputHandler(handler);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void config(InvocationRequest request, File mavenHome) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
		if (sdk != null && sdk.getHomePath() != null) request.setJavaHome(new File(sdk.getHomePath()));
	}
}
