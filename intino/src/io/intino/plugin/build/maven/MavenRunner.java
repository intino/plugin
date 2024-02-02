package io.intino.plugin.build.maven;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.intino.Configuration;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.IntinoException;
import io.intino.plugin.actions.utils.FileSystemUtils;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenExecutionOptions;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.build.FactoryPhase.DISTRIBUTE;
import static io.intino.plugin.project.Safe.safe;
import static org.jetbrains.idea.maven.execution.MavenExecutionOptions.LoggingLevel.ERROR;

public class MavenRunner {
	private static final Logger LOG = Logger.getInstance(MavenRunner.class.getName());
	private static final Object monitor = new Object();
	private final Module module;
	private final String output = "";

	public MavenRunner(Module module) {
		this.module = module;
	}

	public void executeLanguage(Configuration conf) throws IOException, IntinoException {
		Configuration.Repository languageRepository = repository(conf);
		if (languageRepository == null) throw new IOException(message("none.distribution.language.repository"));
		Configuration.Artifact artifact = conf.artifact();
		final File jar = new File(fileOfLanguage(artifact));
		final File pom = new File(jar.getParentFile(), "pom.xml");
		String pomContent = languagePom(languageRepository, artifact);
		Files.writeString(pom.toPath(), pomContent);
		final InvocationResult result = invokeMavenWithConfigurationAndOptions(pom, mavenOpts(languageRepository, artifact, jar), "deploy:deploy-file");
		if (result != null && result.getExitCode() != 0)
			throwException(result, "error.publishing.language", DISTRIBUTE);
		else if (result == null)
			throw new IOException(message("error.publishing.language", DISTRIBUTE, "Maven HOME not found"));
	}

	private static String languagePom(Configuration.Repository languageRepository, Configuration.Artifact artifact) {
		return new PomTemplate().render(new FrameBuilder("pom", "deployFile")
				.add("groupId", "tara.dsl")
				.add("artifactId", artifact.model().outLanguage())
				.add("version", artifact.version())
				.add("repository", new FrameBuilder("repository", "release").add("name", languageRepository.identifier()).add("url", languageRepository.url())));
	}

	@NotNull
	private static String mavenOpts(Configuration.Repository languageRepository, Configuration.Artifact artifact, File jar) {
		return "-Durl=" + languageRepository.url() + " " +
				"-DrepositoryId=" + languageRepository.identifier() + " " +
				"-DgroupId=tara.dsl " +
				"-DartifactId=" + artifact.model().outLanguage() + " " +
				"-Dversion=" + artifact.version() + " " +
				"-Dfile=" + jar;
	}

	private Configuration.Repository repository(Configuration configuration) throws IntinoException {
		Version version = new Version(configuration.artifact().version());
		if (version.isSnapshot()) return safe(() -> configuration.artifact().distribution().snapshot());
		return safe(() -> configuration.artifact().distribution().release());
	}

	public void executeArtifact(FactoryPhase phase) throws IOException {
		final File pom;
		try {
			pom = new PomCreator(module).frameworkPom(phase);
		} catch (IntinoException e) {
			LOG.error(e);
			throw new IOException(message("error.creating.pom"));
		}
		final InvocationResult result = invokeMaven(pom, phase);
		applyBuildFixes(module);
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
		Boolean safe = safe(() -> ((ArtifactLegioConfiguration) IntinoUtil.configurationOf(module)).artifact().packageConfiguration().createMavenPom());
		return safe != null ? safe : false;
	}

	private void applyBuildFixes(Module module) {
		new BuildFixer(module).apply();
	}

	public synchronized InvocationResult invokeMavenWithConfiguration(File pom, String... phases) {
		return invokeMavenWithConfigurationAndOptions(pom, "", phases);
	}

	public synchronized InvocationResult invokeMavenWithConfigurationAndOptions(File pom, String mvnOptions, String... phases) {
		MavenGeneralSettings generalSettings = new MavenGeneralSettings();
		generalSettings.setOutputLevel(ERROR);
		generalSettings.setPrintErrorStackTraces(false);
		ArrayList<String> goals = new ArrayList<>();
		Collections.addAll(goals, phases);
		generalSettings.setFailureBehavior(MavenExecutionOptions.FailureMode.AT_END);
		MavenRunnerParameters parameters = new MavenRunnerParameters(true, pom.getParent(), pom.getName(), goals, Collections.emptyList());
		InvocationResult result = new InvocationResult();
		synchronized (monitor) {
			ApplicationManager.getApplication().invokeLater(() -> {
				ProgramRunner.Callback callback = d -> d.getProcessHandler().addProcessListener(new ProcessListener() {
					@Override
					public void processTerminated(@NotNull ProcessEvent event) {
						result.exitCode(event.getExitCode());
						synchronized (monitor) {
							monitor.notify();
						}
					}

				});
				MavenRunnerSettings runnerSettings = new MavenRunnerSettings();
				runnerSettings.setVmOptions((mvnOptions + " -Djansi.passthrough=true").trim());
				MavenRunConfigurationType.runConfiguration(module.getProject(), parameters, generalSettings, runnerSettings, callback);
			}, ModalityState.nonModal());
			try {
				monitor.wait();
			} catch (InterruptedException ignored) {
			}
		}
		return result;
	}

	private InvocationResult invokeMaven(File pom, FactoryPhase lifeCyclePhase) {
		return invokeMavenWithConfiguration(pom, lifeCyclePhase.mavenActions().toArray(new String[0]));
	}

	public String output() {
		return this.output;
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


	public static final class InvocationResult {
		private CommandLineException executionException;
		private int exitCode = -2147483648;

		InvocationResult() {
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

		public InvocationResult setExecutionException(CommandLineException executionException) {
			this.executionException = executionException;
			return this;
		}
	}
}
