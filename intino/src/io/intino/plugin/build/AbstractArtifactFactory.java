package io.intino.plugin.build;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.ui.ConfirmationDialog;
import git4idea.commands.GitCommandResult;
import io.intino.Configuration;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Deployment;
import io.intino.plugin.IntinoException;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.build.git.GitUtil;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.deploy.ArtifactDeployer;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.intellij.openapi.roots.ModuleRootManager.getInstance;
import static com.intellij.openapi.vcs.VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION;
import static io.intino.Configuration.Server.Type.Dev;
import static io.intino.Configuration.Server.Type.Pre;
import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.build.FactoryPhase.*;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static java.lang.String.join;

public abstract class AbstractArtifactFactory {
	private static final String JAR_EXTENSION = ".jar";
	final Module module;
	final FactoryPhase phase;
	final Project project;
	final Configuration configuration;
	final String startingBranch;
	List<String> errorMessages = new ArrayList<>();
	List<String> successMessages = new ArrayList<>();
	FactoryPhaseChecker checker = new FactoryPhaseChecker();

	public AbstractArtifactFactory(Module module, FactoryPhase phase) {
		this.module = module;
		this.configuration = IntinoUtil.configurationOf(module);
		this.project = module.getProject();
		this.phase = phase;
		this.startingBranch = GitUtil.currentBranch(module);
	}

	ProcessResult process(ProgressIndicator indicator) {
		processPackagePlugins(indicator);
		if (!errorMessages.isEmpty()) return ProcessResult.NothingDone;
		return processArtifact(indicator);
	}

	private void processPackagePlugins(ProgressIndicator indicator) {
		Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) return;
		List<Artifact.Plugin> intinoPlugins = safeList(() -> ((LegioConfiguration) configuration).artifact().plugins());
		intinoPlugins.stream().filter(i -> i.phase() == Artifact.Plugin.Phase.PrePackage).forEach(plugin ->
				new PluginExecutor(module, phase, (LegioConfiguration) configuration, plugin.artifact(), plugin.pluginClass(), errorMessages, indicator).execute());
	}

	private ProcessResult processArtifact(ProgressIndicator indicator) {
		final LegioConfiguration configuration = (LegioConfiguration) IntinoUtil.configurationOf(module);
		try {
			checker.check(phase, configuration);
			if (mavenNeeded(phase, configuration)) {
				ProcessResult result = runMavenPhases(indicator);
				if (!result.equals(ProcessResult.Done)) return result;
				bitbucket(phase, configuration);
			}
			if (phase.equals(DEPLOY)) return deploy(indicator);
		} catch (MavenInvocationException | IOException | IntinoException e) {
			errorMessages.add(e.getMessage());
			return ProcessResult.Done;
		}
		return ProcessResult.Done;

	}

	private boolean mavenNeeded(FactoryPhase phase, LegioConfiguration configuration) {
		return phase != DEPLOY || !isDistributed(configuration.artifact());
	}

	private ProcessResult runMavenPhases(ProgressIndicator indicator) throws MavenInvocationException, IOException, IntinoException {
		if (!errorMessages.isEmpty()) return ProcessResult.NothingDone;
		LegioConfiguration configuration = (LegioConfiguration) IntinoUtil.configurationOf(module);
		Version version = new Version(configuration.artifact().version());
		if (version.isSnapshot()) buildModule(module, configuration, phase, indicator);
		else {
			if (!includeDistribution(phase) || !isDistributed(configuration.artifact()))
				buildModule(module, configuration, phase, indicator);
			else if (askForSnapshotBuild()) {
				configuration.artifact().version(version.nextSnapshot().get());
				configuration.save();
				return ProcessResult.Retry;
			} else return ProcessResult.NothingDone;
		}
		cleanWebOutputs(module);
		return ProcessResult.Done;
	}

	protected boolean includeDistribution(FactoryPhase phase) {
		return phase.ordinal() >= DISTRIBUTE.ordinal();
	}

	private void buildModule(Module module, LegioConfiguration configuration, FactoryPhase phase, ProgressIndicator indicator) throws MavenInvocationException, IOException {
		buildLanguage(module, phase, indicator);
		buildArtifact(module, phase, indicator);
		if (phase.ordinal() > INSTALL.ordinal() && !isSnapshot()) {
			String tag = configuration.artifact().name().toLowerCase() + "/" + configuration.artifact().version();
			GitCommandResult gitCommandResult = GitUtil.tagCurrentAndPush(module, tag);
			if (gitCommandResult.success()) successMessages.add("Release tagged with tag '" + tag + "'");
			else
				errorMessages.add("Error tagging release:\n" + join("\n", gitCommandResult.getErrorOutput()));
		}
	}

	private void buildArtifact(Module module, FactoryPhase phase, ProgressIndicator indicator) throws MavenInvocationException, IOException {
		updateProgressIndicator(indicator, message("artifact.action", firstUpperCase().format(phase.gerund().toLowerCase()).toString()));
		new MavenRunner(module).executeArtifact(phase);
	}

	private void buildLanguage(Module module, FactoryPhase lifeCyclePhase, ProgressIndicator indicator) {
		if (checker.shouldDistributeLanguage(module, lifeCyclePhase)) {
			updateProgressIndicator(indicator, message("language.action", firstUpperCase().format(lifeCyclePhase.gerund().toLowerCase()).toString()));
			buildLanguage(module);
		}
	}

	private void buildLanguage(Module module) {
		try {
			Configuration configuration = IntinoUtil.configurationOf(module);
			File dslFile = dslFilePath(configuration);
			LocalFileSystem.getInstance().refreshIoFiles(Collections.singleton(dslFile), true, false, null);
			new MavenRunner(module).executeLanguage(configuration);
		} catch (Exception e) {
			errorMessages.add(e.getMessage());
		}
	}


	protected boolean askForReleaseDistribute() {
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> {
			response.set(new ConfirmationDialog(module.getProject(),
					"If you are in develop branch, ensure you have all changes committed. Changes will be merged into master and pushed.",
					"Release distribution. Are you sure to distribute a Release version?", IntinoIcons.INTINO_80, STATIC_SHOW_CONFIRMATION).showAndGet());
		});
		return response.get();
	}

	private boolean askForSnapshotBuild() {
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> {
			response.set(new ConfirmationDialog(module.getProject(),
					"Do you want to upgrade artifact to a new SNAPSHOT version and retry?",
					"This version already exists", IntinoIcons.INTINO_80, STATIC_SHOW_CONFIRMATION).showAndGet());
		});
		return response.get();
	}

	protected boolean isDistributed(Artifact artifact) {
		String identifier = artifact.groupId() + ":" + artifact.name().toLowerCase();
		List<String> versions = new ArtifactoryConnector(artifact.root().repositories().stream().
				filter(r -> r instanceof Configuration.Repository.Release).collect(Collectors.toList()))
				.versions(identifier);
		return versions.contains(artifact.version());
	}

	protected boolean isSnapshot() {
		try {
			return new Version(configuration.artifact().version()).isSnapshot();
		} catch (IntinoException e) {
			return true;
		}
	}


	protected boolean isInMasterBranch() {
		return "master".equalsIgnoreCase(startingBranch);
	}

	private void cleanWebOutputs(Module module) {
		final CompilerModuleExtension moduleExtension = CompilerModuleExtension.getInstance(module);
		if (moduleExtension == null || moduleExtension.getCompilerOutputUrl() == null) return;
		File outDirectory = new File(moduleExtension.getCompilerOutputUrl().replaceFirst("file:", ""));
		for (Module dependant : getInstance(module).getModuleDependencies())
			if (ModuleTypeWithWebFeatures.isAvailable(dependant)) cleanWebResources(outDirectory, dependant);
	}

	private void cleanWebResources(File outDirectory, Module dependant) {
		final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(dependant);
		if (extension == null || extension.getCompilerOutputUrl() == null) return;
		final String[] list = new File(extension.getCompilerOutputUrl().replaceFirst("file:", "")).list();
		if (list == null) return;
		for (String name : list) {
			final File file = new File(outDirectory, name);
			Logger.getInstance(AbstractArtifactFactory.class.getName()).info("removing directory -> " + file.getAbsolutePath());
			if (file.exists()) FileUtils.deleteQuietly(file);
		}
	}

	private void bitbucket(FactoryPhase phase, LegioConfiguration configuration) {
		Artifact artifact = configuration.artifact();
		if (includeDistribution(phase)) {
			Configuration.Distribution distribution = artifact.distribution();
			if (distribution != null && artifact.distribution().onBitbucket() != null)
				new BitbucketDeployer(configuration).execute();
		}
	}

	private ProcessResult deploy(ProgressIndicator indicator) throws IntinoException {
		updateProgressIndicator(indicator, message("publishing.artifact"));
		LegioConfiguration conf = (LegioConfiguration) IntinoUtil.configurationOf(module);
		Version version = new Version(conf.artifact().version());
		List<Deployment> deployments = collectDeployments(module.getProject(), conf, version.isSnapshot());
		if (deployments.isEmpty()) return ProcessResult.NothingDone;
		new ArtifactDeployer(module, deployments).execute();
		successMessages.add("Deployment Done");
		return ProcessResult.Done;
	}

	private boolean askForDeploy(Module module, LegioConfiguration conf) {
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> response.set(new ConfirmationDialog(module.getProject(),
				"Are you sure?", "You are going to deploy " + conf.artifact().name(), IntinoIcons.INTINO_16, STATIC_SHOW_CONFIRMATION).showAndGet()));
		return response.get();
	}

	private List<Deployment> collectDeployments(Project project, LegioConfiguration conf, boolean snapshot) {
		List<Deployment> deployments = safeList(() -> conf.artifact().deployments()).stream().filter(deployment -> {
			if (!snapshot) return true;
			return deployment.server().type().equals(Dev) || deployment.server().type().equals(Pre);
		}).collect(Collectors.toList());
		if (deployments.isEmpty()) {
			errorMessages.add("Not Suitable Destinations have been found");
			return Collections.emptyList();
		} else if (deployments.size() > 1)
			return new SelectDestinationsDialog(WindowManager.getInstance().suggestParentWindow(project), deployments).showAndGet();
		if (askForDeploy(module, conf)) {
			return deployments;
		}
		return Collections.emptyList();
	}


	@NotNull
	private File dslFilePath(Configuration configuration) {
		final String outDSL = safe(() -> configuration.artifact().model().outLanguage());
		return new File(LanguageManager.getLanguageDirectory(outDSL) + File.separator +
				configuration.artifact().version() + File.separator + outDSL + "-" + configuration.artifact().version() + JAR_EXTENSION);
	}

	private void updateProgressIndicator(ProgressIndicator progressIndicator, String message) {
		if (progressIndicator != null) {
			progressIndicator.setText(message);
			progressIndicator.setIndeterminate(true);
		}
	}


	public enum ProcessResult {
		Done, Retry, NothingDone
	}
}
