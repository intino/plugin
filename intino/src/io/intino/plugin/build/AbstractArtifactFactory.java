package io.intino.plugin.build;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.commands.GitCommandResult;
import io.intino.Configuration;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Artifact.Package.LinuxService;
import io.intino.Configuration.Deployment;
import io.intino.plugin.IntinoException;
import io.intino.plugin.actions.IntinoConfirmationDialog;
import io.intino.plugin.build.git.GitUtil;
import io.intino.plugin.build.linuxservice.LinuxServiceGenerator;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.build.plugins.PluginExecutor;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.deploy.ArtifactDeployer;
import io.intino.plugin.deploy.ArtifactDeployer.DeployResult;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.plugin.toolwindows.IntinoTopics;
import io.intino.plugin.toolwindows.remote.IntinoRemoteConsoleListener;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.intellij.openapi.roots.ModuleRootManager.getInstance;
import static io.intino.Configuration.Server.Type.Dev;
import static io.intino.Configuration.Server.Type.Pre;
import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.build.FactoryPhase.*;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static java.lang.String.join;

public abstract class AbstractArtifactFactory {
	private static final Logger LOG = Logger.getInstance(AbstractArtifactFactory.class.getName());
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
		if (!(configuration instanceof ArtifactLegioConfiguration)) return;
		List<Artifact.Plugin> intinoPlugins = safeList(() -> ((ArtifactLegioConfiguration) configuration).artifact().plugins());
		intinoPlugins.stream().filter(i -> i.phase() == Artifact.Plugin.Phase.PrePackage).forEach(plugin ->
				new PluginExecutor(module, phase, (ArtifactLegioConfiguration) configuration, plugin, indicator).execute());
	}

	private ProcessResult processArtifact(ProgressIndicator indicator) {
		final ArtifactLegioConfiguration configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf(module);
		try {
			checker.check(phase, configuration);
			if (mavenNeeded(phase, configuration)) {
				cleanBuildDirectory();
				ProcessResult result = runMavenPhases(indicator);
				if (!result.equals(ProcessResult.Done)) return result;
				bitbucket(phase, configuration);
			}
			if (phase.equals(DEPLOY)) return deploy(indicator);
		} catch (IOException | IntinoException e) {
			errorMessages.add(e.getMessage());
			return ProcessResult.Done;
		}
		return ProcessResult.Done;

	}

	private boolean mavenNeeded(FactoryPhase phase, ArtifactLegioConfiguration configuration) {
		return phase != DEPLOY || !isDistributed(configuration.artifact());
	}

	private ProcessResult runMavenPhases(ProgressIndicator indicator) throws IOException, IntinoException {
		if (!errorMessages.isEmpty()) return ProcessResult.NothingDone;
		ArtifactLegioConfiguration configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf(module);
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

	private void buildModule(Module module, ArtifactLegioConfiguration configuration, FactoryPhase phase, ProgressIndicator indicator) throws IOException {
		buildLanguage(phase, indicator);
		buildArtifact(phase, indicator);
		indicator.setText("Tagging version...");
		if (phase.ordinal() > INSTALL.ordinal() && !isSnapshot()) {
			String tag = configuration.artifact().name().toLowerCase() + "/" + configuration.artifact().version();
			GitCommandResult gitCommandResult = GitUtil.tagCurrentAndPush(module, tag);
			if (gitCommandResult.success()) successMessages.add("Release tagged with tag '" + tag + "'");
			else
				errorMessages.add("Error tagging release:\n" + join("\n", gitCommandResult.getErrorOutput()));
		}
	}

	private void buildArtifact(FactoryPhase phase, ProgressIndicator indicator) throws IOException {
		updateProgressIndicator(indicator, message("artifact.action", firstUpperCase().format(phase.gerund().toLowerCase()).toString()));
		new MavenRunner(module).executeArtifact(phase);
		LinuxService linuxService = configuration.artifact().packageConfiguration().linuxService();
		if (linuxService != null && configuration.artifact().packageConfiguration().isRunnable())
			new LinuxServiceGenerator((ArtifactLegioConfiguration) configuration, linuxService, successMessages).generate();
	}

	private void buildLanguage(FactoryPhase lifeCyclePhase, ProgressIndicator indicator) {
		if (checker.shouldDistributeLanguage(lifeCyclePhase, module) && hasModelFiles(module)) {
			updateProgressIndicator(indicator, message("language.action", firstUpperCase().format(lifeCyclePhase.gerund().toLowerCase()).toString()));
			buildLanguage(module);
		}
	}

	private boolean hasModelFiles(Module module) {
		VirtualFile srcRoot = IntinoUtil.getSrcRoot(module);
		if (!srcRoot.exists()) return false;
		return !FileUtils.listFiles(srcRoot.toNioPath().toFile(), new String[]{TaraFileType.INSTANCE.getDefaultExtension()}, true).isEmpty();
	}

	private void buildLanguage(Module module) {
		try {
			Configuration configuration = IntinoUtil.configurationOf(module);
			File dslFile = dslFilePath(configuration);
			if (!dslFile.exists()) {
				errorMessages.add("Language not found");
				return;
			}
			LocalFileSystem.getInstance().refreshIoFiles(Collections.singleton(dslFile), true, false, null);
			new MavenRunner(module).executeLanguage(configuration);
		} catch (Exception e) {
			errorMessages.add(e.getMessage());
		}
	}

	protected boolean askForReleaseDistribute() {
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> response.set(new IntinoConfirmationDialog(project,
				"You aren't in master branch. Changes will be merged into master and pushed. Do you want to continue?",
				"Release Distribution. Are You Sure to Distribute a Release Version?").showAndGet()));
		return response.get();
	}

	private boolean askForSnapshotBuild() {
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> response.set(new IntinoConfirmationDialog(project, "Do you want to upgrade artifact to a new SNAPSHOT version and retry?",
				"This Version Already Exists").showAndGet()));
		return response.get();
	}

	protected boolean isDistributed(Artifact artifact) {
		String identifier = artifact.groupId() + ":" + artifact.name().toLowerCase();
		if (artifact.distribution() == null) return false;
		List<String> versions = new ArtifactoryConnector(project, Collections.singletonList(artifact.distribution().release()))
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

	protected boolean isMasterBranch() {
		return "master".equalsIgnoreCase(startingBranch);
	}

	protected boolean isHotFixBranch() {
		return startingBranch != null && startingBranch.toLowerCase().startsWith("hotfix");
	}

	protected boolean isSupportBranch() {
		return startingBranch.toLowerCase().startsWith("support");
	}

	private void cleanBuildDirectory() {
		final CompilerModuleExtension moduleExtension = CompilerModuleExtension.getInstance(module);
		if (moduleExtension == null || moduleExtension.getCompilerOutputUrl() == null) return;
		File compilerOutputPath = new File(pathOf(moduleExtension.getCompilerOutputUrl()));
		File build = new File(compilerOutputPath.getParentFile().getParentFile(), "build" + File.separator + compilerOutputPath.getName());
		try {
			if (build.exists()) FileUtils.cleanDirectory(build);
		} catch (IOException e) {
			LOG.error(e);
		}
	}

	private String pathOf(String path) {
		if (path.startsWith("file://")) return path.substring("file://".length());
		try {
			return new URL(path).getFile();
		} catch (MalformedURLException e) {
			return path;
		}
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

	private void bitbucket(FactoryPhase phase, ArtifactLegioConfiguration configuration) throws IntinoException {
		Artifact artifact = configuration.artifact();
		if (includeDistribution(phase)) {
			if (safe(() -> artifact.distribution().onBitbucket()) != null) {
				String bitbucketToken = IntinoSettings.getInstance(module.getProject()).bitbucketToken();
				if (bitbucketToken == null || bitbucketToken.isEmpty())
					throw new IntinoException("Bitbucket token not found. Please, set it on Intino Settings");
				new BitbucketDeployer(configuration, bitbucketToken).execute();
			}
		}
	}

	private ProcessResult deploy(ProgressIndicator indicator) throws IntinoException {
		updateProgressIndicator(indicator, message("publishing.artifact"));
		ArtifactLegioConfiguration conf = (ArtifactLegioConfiguration) IntinoUtil.configurationOf(module);
		Version version = new Version(conf.artifact().version());
		List<Deployment> deployments = collectDeployments(module.getProject(), conf, version.isSnapshot());
		if (deployments.isEmpty()) return ProcessResult.NothingDone;
		DeployResult result = new ArtifactDeployer(module, deployments).execute();
		if (result instanceof DeployResult.Done) {
			successMessages.addAll(((DeployResult.Done) result).successMessages());
			publishInBus(conf);
			return ProcessResult.Done;
		} else {
			String errors = ((DeployResult.Fail) result).errors().stream().map(Throwable::getMessage).collect(Collectors.joining("\n"));
			if (result instanceof DeployResult.Fail) throw new IntinoException(errors);
			else
				throw new IntinoException(String.join("\n", ((DeployResult.DoneWithErrors) result).success()) + "\n" + errors);
		}
	}

	private void publishInBus(ArtifactLegioConfiguration conf) {
		final MessageBus messageBus = module.getProject().getMessageBus();
		conf.loadRemoteProcessesInfo();
		final IntinoRemoteConsoleListener mavenListener = messageBus.syncPublisher(IntinoTopics.REMOTE_CONSOLE);
		mavenListener.refresh();
		final MessageBusConnection connect = messageBus.connect();
		connect.deliverImmediately();
		connect.disconnect();

	}

	private boolean askForDeploy(Module module, ArtifactLegioConfiguration conf) {
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> response.set(new IntinoConfirmationDialog(module.getProject(),
				"You are going to deploy " + conf.artifact().name(), "Are you sure?").showAndGet()));
		return response.get();
	}

	private List<Deployment> collectDeployments(Project project, ArtifactLegioConfiguration conf, boolean snapshot) {
		List<Deployment> deployments = safeList(() -> conf.artifact().deployments()).stream().filter(deployment -> {
			if (!snapshot) return true;
			return deployment.server().type().equals(Dev) || deployment.server().type().equals(Pre);
		}).toList();
		if (deployments.isEmpty()) {
			errorMessages.add("Not Suitable Destinations have been found");
			return Collections.emptyList();
		} else if (deployments.size() > 1) {
			final Window[] parent = new Window[1];
			ApplicationManager.getApplication().invokeAndWait(() -> parent[0] = WindowManager.getInstance().suggestParentWindow(project));
			return new SelectDestinationsDialog(conf.artifact().name(), parent[0], deployments).showAndGet();
		}
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
