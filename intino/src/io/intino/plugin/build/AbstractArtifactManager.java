package io.intino.plugin.build;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import io.intino.plugin.IntinoException;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.build.cesar.PublishManager;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.project.GulpExecutor;
import io.intino.plugin.project.LegioConfiguration;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.LanguageManager;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static tara.intellij.codeinsight.languageinjection.helpers.QualifiedNameFormatter.firstUpperCase;


abstract class AbstractArtifactManager {
	private static final String JAR_EXTENSION = ".jar";

	List<String> errorMessages = new ArrayList<>();
	List<String> successMessages = new ArrayList<>();

	boolean process(final Module module, LifeCyclePhase phase, ProgressIndicator indicator) {
		processLanguage(module, phase, indicator);
		processFramework(module, phase, indicator);
		publish(module, phase, indicator);
		return true;
	}

	private void publish(Module module, LifeCyclePhase phase, ProgressIndicator indicator) {
		if (phase.equals(LifeCyclePhase.PREDEPLOY) || phase.equals(LifeCyclePhase.DEPLOY)) {
			updateProgressIndicator(indicator, MessageProvider.message("publishing.artifact"));
			new PublishManager(phase, module).execute();
		}
	}

	private void processLanguage(Module module, LifeCyclePhase lifeCyclePhase, ProgressIndicator indicator) {
		if (shouldDeployLanguage(module, lifeCyclePhase)) {
			updateProgressIndicator(indicator, MessageProvider.message("language.action", firstUpperCase(lifeCyclePhase.gerund().toLowerCase())));
			distributeLanguage(module);
		}
	}

	private void distributeLanguage(Module module) {
		Configuration configuration = TaraUtil.configurationOf(module);
		File dslFile = dslFilePath(configuration);
		LocalFileSystem.getInstance().refreshIoFiles(Collections.singleton(dslFile), true, false, null);
		distributeLanguage(module, configuration);
	}

	private void distributeLanguage(Module module, Configuration configuration) {
		try {
			MavenRunner runner = new MavenRunner(module);
			runner.executeLanguage(configuration);
		} catch (Exception e) {
			errorMessages.add(e.getMessage());
		}
	}

	private void processFramework(Module module, LifeCyclePhase phase, ProgressIndicator indicator) {
		updateProgressIndicator(indicator, MessageProvider.message("framework.action", firstUpperCase(phase.gerund().toLowerCase())));
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration instanceof LegioConfiguration) {
			try {
				if (noDistributionRepository(phase, configuration))
					throw new IntinoException(MessageProvider.message("distribution.repository.not.found"));
				executeGulpDependencies(module);
				new MavenRunner(module).executeFramework(phase);
			} catch (MavenInvocationException | IOException | IntinoException e) {
				errorMessages.add(e.getMessage());
			}
		} else new MavenRunner(module).invokeMaven("install", "deploy");
	}

	private void executeGulpDependencies(Module module) {
		if (WebModuleType.isWebModule(module))
			new GulpExecutor(module, ((LegioConfiguration) TaraUtil.configurationOf(module)).project()).startGulpDeploy();
		for (Module dependency : ModuleRootManager.getInstance(module).getDependencies())
			if (WebModuleType.isWebModule(dependency))
				new GulpExecutor(dependency, ((LegioConfiguration) TaraUtil.configurationOf(dependency)).project()).startGulpDeploy();
	}

	private boolean noDistributionRepository(LifeCyclePhase lifeCyclePhase, Configuration configuration) {
		return configuration.distributionReleaseRepository() == null && lifeCyclePhase.mavenActions().contains("deploy");
	}

	protected boolean shouldDeployLanguage(Module module, LifeCyclePhase lifeCyclePhase) {
		return TaraUtil.configurationOf(module).level() != null && !Configuration.Level.System.equals(TaraUtil.configurationOf(module).level()) && lifeCyclePhase.mavenActions().contains("deploy");
	}

	@NotNull
	private File dslFilePath(Configuration configuration) {
		final String outDSL = configuration.outDSL();
		return new File(LanguageManager.getLanguageDirectory(outDSL) + File.separator +
				configuration.modelVersion() + File.separator + outDSL + "-" + configuration.modelVersion() + JAR_EXTENSION);
	}

	@Nullable
	private ProgressIndicator updateProgressIndicator(ProgressIndicator progressIndicator, String message) {
		if (progressIndicator != null) {
			progressIndicator.setText(message);
			progressIndicator.setIndeterminate(true);
		}
		return progressIndicator;
	}
}
