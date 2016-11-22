package io.intino.legio.plugin.build;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vfs.LocalFileSystem;
import io.intino.legio.plugin.LegioException;
import io.intino.legio.plugin.project.LegioConfiguration;
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

import static io.intino.legio.plugin.MessageProvider.message;
import static tara.intellij.codeinsight.languageinjection.helpers.QualifiedNameFormatter.firstUpperCase;


abstract class AbstractArtifactManager {
	private static final String JAR_EXTENSION = ".jar";

	List<String> errorMessages = new ArrayList<>();
	List<String> successMessages = new ArrayList<>();

	protected boolean publish(final Module module, LifeCyclePhase lifeCyclePhase, ProgressIndicator indicator) {
		if (!Configuration.Level.System.equals(TaraUtil.configurationOf(module).level()) && lifeCyclePhase.mavenActions().contains("deploy")) {
			updateProgressIndicator(indicator, message("language.action", firstUpperCase(lifeCyclePhase.gerund().toLowerCase())));
			publishLanguage(module);
		}
		updateProgressIndicator(indicator, message("framework.action", firstUpperCase(lifeCyclePhase.gerund().toLowerCase())));
		publishFramework(module, lifeCyclePhase);
		return true;
	}

	private void publishLanguage(Module module) {
		Configuration configuration = TaraUtil.configurationOf(module);
		File dslFile = dslFilePath(configuration);
		LocalFileSystem.getInstance().refreshIoFiles(Collections.singleton(dslFile), true, false, null);
		publishLanguage(module, configuration);
	}

	private void publishLanguage(Module module, Configuration configuration) {
		try {
			LegioMavenRunner runner = new LegioMavenRunner(module);
			runner.publishLanguage(configuration);
		} catch (MavenInvocationException | IOException e) {
			errorMessages.add(e.getMessage());
		}
	}

	private void publishFramework(Module module, LifeCyclePhase lifeCyclePhase) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration instanceof LegioConfiguration) {
			try {
				if (configuration.distributionRepository().isEmpty() && lifeCyclePhase.mavenActions().contains("deploy"))
					throw new LegioException(message("distribution.repository.not.found"));
				new LegioMavenRunner(module).publishFramework(lifeCyclePhase);
			} catch (MavenInvocationException | IOException | LegioException e) {
				errorMessages.add(e.getMessage());
			}
		} else new LegioMavenRunner(module).publishNativeMaven();
	}

	@NotNull
	private File dslFilePath(Configuration configuration) {
		final String outDSL = configuration.outDSL();
		return new File(LanguageManager.getLanguageDirectory(outDSL) + File.separator + configuration.modelVersion() + File.separator + outDSL + "-" + configuration.modelVersion() + JAR_EXTENSION);
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
