package io.intino.plugin.build.maven;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dsl;
import io.intino.Configuration.Artifact.Dsl.OutputDsl;
import io.intino.Configuration.Distribution.ArtifactoryDistribution;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.IntinoException;
import io.intino.plugin.actions.utils.FileSystemUtils;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenExecutionOptions;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.execution.RunnerBundle;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.utils.MavenUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.build.FactoryPhase.DISTRIBUTE;
import static io.intino.plugin.project.Safe.safe;
import static org.jetbrains.idea.maven.execution.MavenExecutionOptions.LoggingLevel.ERROR;
import static org.jetbrains.idea.maven.execution.MavenRunConfigurationType.createRunnerAndConfigurationSettings;
import static org.jetbrains.idea.maven.execution.MavenRunConfigurationType.generateName;

public class MavenRunner {
	private static final Logger LOG = Logger.getInstance(MavenRunner.class.getName());
	private static final Object monitor = new Object();
	private final Module module;
	private final String output = "";

	public MavenRunner(Module module) {
		this.module = module;
	}

	public void buildLanguage(Configuration conf, OutputDsl outputDsl) throws IOException, IntinoException {
		Configuration.Repository distRepository = repository(conf);
		if (distRepository == null) throw new IOException(message("none.distribution.language.repository"));
		Configuration.Artifact artifact = conf.artifact();
		final File jar = jarFile(outputDsl);
		final File pom = pomFile(outputDsl, jar, distRepository, artifact);
		final InvocationResult result = invokeMavenWithConfigurationAndOptions(pom, mavenOpts(distRepository, artifact, outputDsl, jarFile(outputDsl), pom), "deploy:deploy-file");
		if (result != null && result.getExitCode() != 0)
			throwException(result, "error.publishing.language", DISTRIBUTE);
		else if (result == null)
			throw new IOException(message("error.publishing.language", DISTRIBUTE, "Maven HOME not found"));
	}

	private @NotNull File pomFile(OutputDsl outputDsl, File jar, Configuration.Repository distRepository, Configuration.Artifact artifact) throws IOException {
		final File pom = new File(jar.getParentFile(), "pom.xml");
		Files.writeString(pom.toPath(), dslPom(distRepository, artifact, outputDsl));
		return pom;
	}

	private @NotNull File jarFile(OutputDsl outputDsl) {
		File jar = new File(dslFile(outputDsl));
		File dest = new File(jar.getParentFile(), jar.getName().toLowerCase());
		jar.renameTo(dest);
		return dest;
	}

	private String dslPom(Configuration.Repository repository, Configuration.Artifact artifact, OutputDsl outputDsl) {
		return new PomTemplate().render(new FrameBuilder("pom", "deployFile")
				.add("groupId", "tara.dsl")
				.add("artifactId", outputDsl.name().toLowerCase())
				.add("version", artifact.version())
				.add("dependency", languageDependency(outputDsl, artifact))
				.add("repository", new FrameBuilder("repository", "release").add("name", repository.identifier()).add("url", repository.url())));
	}

	private Frame languageDependency(OutputDsl outputDsl, Configuration.Artifact artifact) {
		return language(new LanguageResolver(module, artifact.root().repositories()).resolve((Dsl) outputDsl.owner()));
	}

	private Frame language(List<org.eclipse.aether.graph.Dependency> deps) {
		return deps.stream().map(org.eclipse.aether.graph.Dependency::getArtifact)
				.filter(d -> d.getArtifactId().equalsIgnoreCase("language"))
				.findFirst()
				.map(d -> new FrameBuilder("dependency")
						.add("groupId", d.getGroupId())
						.add("artifactId", d.getArtifactId())
						.add("scope", "compile")
						.add("version", d.getVersion()).toFrame())
				.orElse(null);
	}

	@NotNull
	private static String mavenOpts(Configuration.Repository repository, Configuration.Artifact artifact, OutputDsl outputDsl, File jar, File pomFile) {
		return "-Durl=" + repository.url() + " " +
			   "-DrepositoryId=" + repository.identifier() + " " +
			   "-DgroupId=tara.dsl " +
			   "-DartifactId=" + outputDsl.name().toLowerCase() + " " +
			   "-Dversion=" + artifact.version() + " " +
			   "-Dfile=" + jar.getAbsolutePath() + " " +
			   "-DpomFile=" + pomFile.getAbsolutePath();
	}

	private Configuration.Repository repository(Configuration configuration) throws IntinoException {
		ArtifactoryDistribution dist = safe(() -> configuration.artifact().distribution().onArtifactory());
		Version version = new Version(configuration.artifact().version());
		if (version.isSnapshot()) return safe(() -> dist.snapshot());
		return safe(() -> dist.release());
	}

	public void executeArtifact(FactoryPhase phase) throws IOException {
		final File pom;
		try {
			pom = new PomCreator(module).artifactPom(phase);
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

	public synchronized InvocationResult invokeMavenWithConfigurationAndOptions(File pom, String mvnOptions, String... goals) {
		MavenGeneralSettings generalSettings = new MavenGeneralSettings();
		generalSettings.setOutputLevel(ERROR);
		generalSettings.setPrintErrorStackTraces(false);
		generalSettings.setFailureBehavior(MavenExecutionOptions.FailureMode.AT_END);
		MavenRunnerParameters parameters = new MavenRunnerParameters(true, pom.getParent(), pom.getName(), List.of(goals), Collections.emptyList());
		InvocationResult result = new InvocationResult();
		synchronized (monitor) {
			ApplicationManager.getApplication().invokeLater(() -> {
				MavenRunnerSettings runnerSettings = new MavenRunnerSettings();
				runnerSettings.setVmOptions((mvnOptions + " -Djansi.passthrough=true").trim());
				RunnerAndConfigurationSettings configSettings = createRunnerAndConfigurationSettings(generalSettings, runnerSettings, parameters, module.getProject(), runnerName(pom, parameters), false);
				runConfiguration(module.getProject(), configSettings, callback(result));
			}, ModalityState.nonModal());
			try {
				monitor.wait();
			} catch (InterruptedException ignored) {
			}
		}
		return result;
	}

	private @NotNull String runnerName(File pom, MavenRunnerParameters parameters) {
		return pom.getParentFile().getName() + generateName(module.getProject(), parameters);
	}


	public static void runConfiguration(Project project, RunnerAndConfigurationSettings configSettings, ProgramRunner.Callback callback) {
		ProgramRunner<?> runner = DefaultJavaProgramRunner.getInstance();
		Executor executor = DefaultRunExecutor.getRunExecutorInstance();
		ExecutionEnvironment environment = new ExecutionEnvironment(executor, runner, configSettings, project);
		environment.putUserData(new Key<>("IS_DELEGATE_BUILD"), false);
		environment.setCallback(callback);
		ApplicationManager.getApplication().invokeAndWait(() -> {
			try {
				runner.execute(environment);
			} catch (ExecutionException e) {
				MavenUtil.showError(project, RunnerBundle.message("notification.title.failed.to.execute.maven.goal"), e);
			}
		});
	}

	private static ProgramRunner.@NotNull Callback callback(InvocationResult result) {
		ProgramRunner.Callback callback = d -> d.getProcessHandler().addProcessListener(new ProcessListener() {
			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				result.exitCode(event.getExitCode());
				synchronized (monitor) {
					monitor.notify();
				}
			}

		});
		return callback;
	}

	private InvocationResult invokeMaven(File pom, FactoryPhase lifeCyclePhase) {
		return invokeMavenWithConfiguration(pom, lifeCyclePhase.mavenActions().toArray(new String[0]));
	}

	public String output() {
		return this.output;
	}

	@NotNull
	private String dslFile(OutputDsl output) {
		try {
			String name = output.name();
			if (name == null) return "";
			final String originalFile = LanguageManager.getLanguageDirectory(name) + "/" + output.version() + "/" + name + "-" + output.version() + ".jar";
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
