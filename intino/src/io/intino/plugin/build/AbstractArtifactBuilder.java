package io.intino.plugin.build;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.vfs.LocalFileSystem;
import io.intino.itrules.formatters.StringFormatters;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.web.PackageJsonCreator;
import io.intino.plugin.deploy.ArtifactDeployer;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.compiler.shared.Configuration.Artifact;
import io.intino.tara.compiler.shared.Configuration.Deployment;
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
import static io.intino.plugin.project.Safe.safeList;


abstract class AbstractArtifactBuilder {
	private static final String JAR_EXTENSION = ".jar";
	List<String> errorMessages = new ArrayList<>();
	List<String> successMessages = new ArrayList<>();

	void process(final Module module, FactoryPhase phase, ProgressIndicator indicator) {
		processPackagePlugins(module, phase, indicator);
		if (!errorMessages.isEmpty()) return;
		processLanguage(module, phase, indicator);
		if (!errorMessages.isEmpty()) return;
		processFramework(module, phase, indicator);
		if (!errorMessages.isEmpty()) return;
		if (deploy(module, phase, indicator)) successMessages.add("deployment Done");
	}

	private void processPackagePlugins(Module module, FactoryPhase phase, ProgressIndicator indicator) {
		Configuration configuration = TaraUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) return;
		List<Artifact.Plugin> intinoPlugins = safeList(() -> ((LegioConfiguration) configuration).artifact().plugins());
		intinoPlugins.stream().filter(i -> i.phase() == Artifact.Plugin.Phase.PrePackage).forEach(plugin ->
				new PluginExecutor(module, phase, (LegioConfiguration) configuration, plugin.artifact(), plugin.pluginClass(), errorMessages, indicator).execute());
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
		LegioArtifact artifact = configuration.artifact();
		if (phase.ordinal() >= DISTRIBUTE.ordinal()) {
			artifact.distribution();
			if (artifact.distribution().onBitbucket() != null) new BitbucketDeployer(configuration).execute();
		}
	}

	private boolean deploy(Module module, FactoryPhase phase, ProgressIndicator indicator) {
		if (phase.equals(DEPLOY)) {
			updateProgressIndicator(indicator, message("publishing.artifact"));
			try {
				List<Deployment> destinations = collectDeployments((LegioConfiguration) TaraUtil.configurationOf(module));
				return !destinations.isEmpty() && new ArtifactDeployer(module, destinations).execute();
			} catch (IntinoException e) {
				errorMessages.add(e.getMessage());
			}
		}
		return false;
	}

	private void buildWeb(Module module) {
		if (!ModuleTypeWithWebFeatures.isAvailable(module)) return;
		Artifact artifact = ((LegioConfiguration) TaraUtil.configurationOf(module)).artifact();
		createPackageJson(module, artifact);
	}

	private void createPackageJson(Module module, Artifact artifact) {
		new PackageJsonCreator(artifact, TaraUtil.configurationOf(module).repositories(), new File(new File(module.getModuleFilePath()).getParentFile(), NodeModules)).createPackageFile(new File(module.getModuleFilePath()).getParentFile());
	}

	private void check(FactoryPhase phase, Configuration configuration) throws IntinoException {
		if (!(configuration instanceof LegioConfiguration))
			throw new IntinoException(message("legio.artifact.not.found"));
		if (safe(() -> ((LegioConfiguration) configuration).artifact().packageConfiguration()) == null)
			throw new IntinoException(message("packaging.configuration.not.found"));
		if (noDistributionRepository(phase, configuration))
			throw new IntinoException(message("distribution.repository.not.found"));
	}

	private List<Deployment> collectDeployments(LegioConfiguration conf) {
		return safeList(() -> conf.artifact().deployments());
	}

	private boolean noDistributionRepository(FactoryPhase lifeCyclePhase, Configuration configuration) {
		return safe(() -> configuration.artifact().distribution().release()) == null && lifeCyclePhase.mavenActions().contains("deploy");
	}

	boolean shouldDistributeLanguage(Module module, FactoryPhase lifeCyclePhase) {
		Configuration configuration = TaraUtil.configurationOf(module);
		Artifact.Model model = safe(() -> configuration.artifact().model());
		return model != null && model.level() != null && !model.level().isSolution() && lifeCyclePhase.mavenActions().contains("deploy");
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
}
