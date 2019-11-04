package io.intino.plugin.build;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.wm.WindowManager;
import io.intino.itrules.formatters.StringFormatters;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Destination;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.web.PackageJsonCreator;
import io.intino.plugin.deploy.ArtifactDeployer;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.intellij.openapi.roots.ModuleRootManager.getInstance;
import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.build.FactoryPhase.DEPLOY;
import static io.intino.plugin.build.FactoryPhase.DISTRIBUTE;
import static io.intino.plugin.dependencyresolution.WebDependencyResolver.NodeModules;
import static io.intino.plugin.project.Safe.safe;
import static java.util.Collections.emptyList;


abstract class AbstractArtifactBuilder {
	private static final String JAR_EXTENSION = ".jar";
	List<String> errorMessages = new ArrayList<>();
	List<String> successMessages = new ArrayList<>();

	void process(final Module module, FactoryPhase phase, ProgressIndicator indicator) {
		processLanguage(module, phase, indicator);
		if (!errorMessages.isEmpty()) return;
		processFramework(module, phase, indicator);
		if (!errorMessages.isEmpty()) return;
		if (deploy(module, phase, indicator)) successMessages.add("deployment Done");
	}

	private void processLanguage(Module module, FactoryPhase lifeCyclePhase, ProgressIndicator indicator) {
		if (shouldDistributeLanguage(module, lifeCyclePhase)) {
			updateProgressIndicator(indicator, message("language.action", StringFormatters.get(Locale.getDefault()).get("FirstLowerCase".toLowerCase()).format(lifeCyclePhase.gerund().toLowerCase()).toString()));
			distributeLanguage(module);
		}
	}

	private void distributeLanguage(Module module) {
		try {
			Configuration configuration = TaraUtil.configurationOf(module);
			File dslFile = dslFilePath(configuration);
			LocalFileSystem.getInstance().refreshIoFiles(Collections.singleton(dslFile), true, false, null);
			new MavenRunner(module).executeLanguage(configuration);
		} catch (Exception e) {
			errorMessages.add(e.getMessage());
		}
	}

	private void processFramework(Module module, FactoryPhase phase, ProgressIndicator indicator) {
		updateProgressIndicator(indicator, message("framework.action", StringFormatters.get(Locale.getDefault()).get("FirstUpperCase".toLowerCase()).format(phase.gerund().toLowerCase()).toString()));
		final LegioConfiguration configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		try {
			check(phase, configuration);
			buildWeb(module);
			new MavenRunner(module).executeFramework(phase);
			cleanWebOutputs(module);
			bitbucket(phase, configuration);
		} catch (MavenInvocationException | IOException | IntinoException e) {
			errorMessages.add(e.getMessage());
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
			Logger.getInstance(AbstractArtifactBuilder.class.getName()).info("removing directory -> " + file.getAbsolutePath());
			if (file.exists()) FileUtils.deleteQuietly(file);
		}
	}

	private void bitbucket(FactoryPhase phase, LegioConfiguration configuration) {
		if (phase.ordinal() >= DISTRIBUTE.ordinal() && configuration.graph().artifact().distribution() != null && configuration.graph().artifact().distribution().onBitbucket() != null)
			new BitbucketDeployer(configuration).execute();
	}

	private boolean deploy(Module module, FactoryPhase phase, ProgressIndicator indicator) {
		if (phase.equals(DEPLOY)) {
			updateProgressIndicator(indicator, message("publishing.artifact"));
			try {
				List<Destination> destinations = collectDestinations(module.getProject(), (LegioConfiguration) TaraUtil.configurationOf(module));
				return !destinations.isEmpty() && new ArtifactDeployer(module, destinations).execute();
			} catch (IntinoException e) {
				errorMessages.add(e.getMessage());
			}
		}
		return false;
	}

	private void buildWeb(Module module) {
		if (!ModuleTypeWithWebFeatures.isAvailable(module)) return;
		Artifact artifact = ((LegioConfiguration) TaraUtil.configurationOf(module)).graph().artifact();
		if (artifact == null) return;
		createPackageJson(module, artifact);
	}

	private void createPackageJson(Module module, Artifact artifact) {
		new PackageJsonCreator(artifact, ((LegioConfiguration) TaraUtil.configurationOf(module)).repositoryTypes(), new File(new File(module.getModuleFilePath()).getParentFile(), NodeModules)).createPackageFile(new File(module.getModuleFilePath()).getParentFile());
	}

	private void check(FactoryPhase phase, Configuration configuration) throws IntinoException {
		if (!(configuration instanceof LegioConfiguration))
			throw new IntinoException(message("legio.artifact.not.found"));
		if (safe(() -> ((LegioConfiguration) configuration).graph().artifact().package$()) == null)
			throw new IntinoException(message("packaging.configuration.not.found"));
		if (noDistributionRepository(phase, configuration))
			throw new IntinoException(message("distribution.repository.not.found"));
	}

	private List<Destination> collectDestinations(Project project, LegioConfiguration conf) {
		final List<Artifact.Deployment> deployments = safe(() -> conf.graph().artifact().deploymentList());
		if (deployments.size() > 1 || (!deployments.isEmpty() && deployments.get(0).destinations().size() > 1))
			return new SelectDestinationsDialog(WindowManager.getInstance().suggestParentWindow(project), deployments).showAndGet();
		return deployments.isEmpty() ? emptyList() : destinationsOf(deployments.get(0));
	}

	private List<Destination> destinationsOf(Artifact.Deployment deployment) {
		List<Destination> destinations = new ArrayList<>();
		if (deployment.pre() != null) destinations.add(deployment.pre());
		if (deployment.pro() != null) destinations.add(deployment.pro());
		return destinations;
	}

	private boolean noDistributionRepository(FactoryPhase lifeCyclePhase, Configuration configuration) {
		return configuration.distributionReleaseRepository() == null && lifeCyclePhase.mavenActions().contains("deploy");
	}

	boolean shouldDistributeLanguage(Module module, FactoryPhase lifeCyclePhase) {
		Configuration configuration = TaraUtil.configurationOf(module);
		return configuration.model().level() != null && !configuration.model().level().isSolution() && lifeCyclePhase.mavenActions().contains("deploy");
	}

	@NotNull
	private File dslFilePath(Configuration configuration) {
		final String outDSL = configuration.model().outLanguage();
		return new File(LanguageManager.getLanguageDirectory(outDSL) + File.separator +
				configuration.version() + File.separator + outDSL + "-" + configuration.version() + JAR_EXTENSION);
	}

	private void updateProgressIndicator(ProgressIndicator progressIndicator, String message) {
		if (progressIndicator != null) {
			progressIndicator.setText(message);
			progressIndicator.setIndeterminate(true);
		}
	}
}
