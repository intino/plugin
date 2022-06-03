package io.intino.plugin.build.maven;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.actions.utils.FileSystemUtils;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.toolwindows.output.IntinoTopics;
import io.intino.plugin.toolwindows.output.MavenListener;
import org.apache.maven.shared.invoker.*;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenExecutionOptions;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;
import static org.jetbrains.idea.maven.execution.MavenExecutionOptions.LoggingLevel.ERROR;
import static org.jetbrains.idea.maven.utils.MavenUtil.resolveMavenHomeDirectory;

public class MavenRunner {
	private final Module module;
	private final InvocationOutputHandler handler;
	private final String output = "";

	private static final Object monitor = new Object();

	public MavenRunner(Module module) {
		this.module = module;
		handler = defaultHandler();
	}

	@NotNull
	private InvocationOutputHandler defaultHandler() {
		return this::publish;
	}

	private void publish(String line) {
		if (module.getProject().isDisposed()) return;
		final MessageBus messageBus = module.getProject().getMessageBus();
		final MavenListener mavenListener = messageBus.syncPublisher(IntinoTopics.BUILD_CONSOLE);
		mavenListener.println(line);
		final MessageBusConnection connect = messageBus.connect();
		connect.deliverImmediately();
		connect.disconnect();
	}

	public void executeLanguage(Configuration conf) throws MavenInvocationException, IOException, IntinoException {
		Configuration.Repository languageRepository = repository(conf);
		if (languageRepository == null) throw new IOException(message("none.distribution.language.repository"));
		InvocationRequest request = new DefaultInvocationRequest().setGoals(Collections.singletonList("deploy:deploy-file"));
		Configuration.Artifact artifact = conf.artifact();
		request.setMavenOpts("-Durl=" + languageRepository.url() + " " +
				"-DrepositoryId=" + languageRepository.identifier() + " " +
				"-DgroupId=tara.dsl " +
				"-DartifactId=" + artifact.model().outLanguage() + " " +
				"-Dversion=" + artifact.version() + " " +
				"-Dfile=" + fileOfLanguage(artifact));
		final Properties properties = new Properties();
		request.setProperties(properties);
		final InvocationResult result = invokeMaven(request);
		if (result != null && result.getExitCode() != 0)
			throwException(result, "error.publishing.language", FactoryPhase.DISTRIBUTE);
		else if (result == null)
			throw new IOException(message("error.publishing.language", FactoryPhase.DISTRIBUTE, "Maven HOME not found"));
	}

	private Configuration.Repository repository(Configuration configuration) throws IntinoException {
		Version version = new Version(configuration.artifact().version());
		if (version.isSnapshot()) return safe(() -> configuration.artifact().distribution().snapshot());
		return safe(() -> configuration.artifact().distribution().release());
	}

	public void executeArtifact(FactoryPhase phase) throws IOException {
		final File pom = new PomCreator(module).frameworkPom(phase);
		final InvocationResult result = invokeMaven(pom, phase);
		applyBuildFixes(module, phase);
		if (result != null && result.getExitCode() != 0) throwException(result, "error.publishing.artifact", phase);
		else {
			if (!preservePOM()) pom.delete();
			File reducedPom = new File(pom.getParentFile(), "dependency-reduced-pom.xml");
			if (reducedPom.exists()) reducedPom.delete();
			new MavenPostBuildActions(module).execute();
			if (result == null)
				throw new IOException(message("error.publishing.artifact", phase.name().toLowerCase(), "Maven HOME not found"));
		}
	}

	private boolean preservePOM() {
		Boolean safe = safe(() -> ((LegioConfiguration) IntinoUtil.configurationOf(module)).artifact().packageConfiguration().createMavenPom());
		return safe != null ? safe : false;
	}

	private void applyBuildFixes(Module module, FactoryPhase phase) {
		new BuildFixer(module).apply();
	}

	public synchronized InvocationResult invokeMavenWithConfiguration(File pom, String... phases) {
		DefaultInvocationResult result = new DefaultInvocationResult();
		MavenGeneralSettings generalSettings = new MavenGeneralSettings();
		generalSettings.setOutputLevel(ERROR);
		generalSettings.setPrintErrorStackTraces(false);
		ArrayList<String> goals = new ArrayList<>();
		Collections.addAll(goals, phases);
		goals.add(1, "verify");
		generalSettings.setFailureBehavior(MavenExecutionOptions.FailureMode.AT_END);
		MavenRunnerParameters parameters = new MavenRunnerParameters(true, pom.getParent(), pom.getName(), goals, Collections.emptyList());
		synchronized (monitor) {
			ApplicationManager.getApplication().invokeLater(() -> {
				ProgramRunner.Callback callback = d -> d.getProcessHandler().addProcessListener(new ProcessListener() {
					@Override
					public void startNotified(@NotNull ProcessEvent event) {
					}

					@Override
					public void processTerminated(@NotNull ProcessEvent event) {
						result.exitCode(event.getExitCode());
						synchronized (monitor) {
							monitor.notify();
						}
					}

					@Override
					public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
					}
				});
				MavenRunConfigurationType.runConfiguration(module.getProject(), parameters, generalSettings, null, callback);
			}, ModalityState.NON_MODAL);
			try {
				monitor.wait();
			} catch (InterruptedException e) {
			}
		}
		return result;
	}

	public InvocationResult invokeMaven(File pom, String mavenOpts, String... phases) throws MavenInvocationException {
		final String ijMavenHome = MavenProjectsManager.getInstance(module.getProject()).getGeneralSettings().getMavenHome();
		InvocationRequest request = new DefaultInvocationRequest().setPomFile(pom).setGoals(Collections.singletonList(phases[phases.length - 1]));
		final File mavenHome = resolveMavenHomeDirectory(ijMavenHome);
		if (mavenHome == null) return null;
		configure(request, mavenHome, mavenOpts);
		Invoker invoker = new DefaultInvoker().setMavenHome(mavenHome);
		addLogger(invoker);
		return invoker.execute(request);
	}

	private InvocationResult invokeMaven(File pom, FactoryPhase lifeCyclePhase) {
		return invokeMavenWithConfiguration(pom, lifeCyclePhase.mavenActions().toArray(new String[0]));
	}

	public String output() {
		return this.output;
	}

	private InvocationResult invokeMaven(InvocationRequest request) throws MavenInvocationException {
		final String ijMavenHome = MavenProjectsManager.getInstance(module.getProject()).getGeneralSettings().getMavenHome();
		final File mavenHome = resolveMavenHomeDirectory(ijMavenHome);
		if (mavenHome == null) return null;
		Invoker invoker = new DefaultInvoker().setMavenHome(mavenHome);
		addLogger(invoker);
		configure(request, mavenHome, "");
		return invoker.execute(request);
	}

	@NotNull
	private String fileOfLanguage(Configuration.Artifact artifact) {
		try {
			Configuration.Artifact.Model model = artifact.model();
			final String originalFile = LanguageManager.getLanguageDirectory(model.outLanguage()) + "/" + artifact.version() + "/" + model.outLanguage() + "-" + artifact.version() + ".jar";
			final Path deployLanguage = Files.createTempDirectory("deployLanguage");
			final File destination = new File(deployLanguage.toFile(), new File(originalFile).getName());
			FileSystemUtils.copyFile(originalFile, destination.getAbsolutePath());
			return destination.getAbsolutePath();
		} catch (IOException e) {
			Logger.getInstance(this.getClass()).warn(e);
			return "";
		}
	}

	private void throwException(InvocationResult result, String message, FactoryPhase phase) throws IOException {
		if (result.getExecutionException() != null)
			throw new IOException(message(message, phase.gerund().toLowerCase(), result.getExecutionException().getMessage()), result.getExecutionException());
		else throw new IOException(message(message, phase.gerund().toLowerCase(), output), new IOException(output));
	}

	private void addLogger(Invoker invoker) {
		invoker.setOutputHandler(handler);
		invoker.setErrorHandler(handler);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void configure(InvocationRequest request, File mavenHome, String mavenOpts) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		if (!mavenOpts.isEmpty()) request.setMavenOpts(mavenOpts);
		request.setShowErrors(true);
		final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
		if (sdk != null && sdk.getHomePath() != null) request.setJavaHome(new File(sdk.getHomePath()));
	}


	public static final class DefaultInvocationResult implements InvocationResult {
		private CommandLineException executionException;
		private int exitCode = -2147483648;

		DefaultInvocationResult() {
		}

		public int getExitCode() {
			return this.exitCode;
		}

		public CommandLineException getExecutionException() {
			return this.executionException;
		}

		void exitCode(int exitCode) {
			this.exitCode = exitCode;
		}

		void executionException(CommandLineException executionException) {
			this.executionException = executionException;
		}
	}
}
