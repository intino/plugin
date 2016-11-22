package io.intino.legio.plugin.actions.publish;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import io.intino.legio.plugin.LegioException;
import io.intino.legio.plugin.actions.publish.ArtifactPublisher.Actions;
import io.intino.legio.plugin.build.LegioMavenRunner;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.intino.legio.plugin.MessageProvider.message;


abstract class AbstractArtifactPublisher {
	private static final String JAR_EXTENSION = ".jar";

	List<String> errorMessages = new ArrayList<>();
	List<String> successMessages = new ArrayList<>();

	protected boolean publish(final Module module, Actions actions) {
		return ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
			if (!Configuration.Level.System.equals(TaraUtil.configurationOf(module).level()) && Arrays.asList(actions.actions()).contains("deploy")) {
				updateProgressIndicator(message("publishing.language"));
				publishLanguage(module);
			}
			updateProgressIndicator(message("publishing.framework"));
			publishFramework(module, actions);
		}, "Publishing " + TaraUtil.configurationOf(module).artifactId(), false, module.getProject());
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

	private void publishFramework(Module module, Actions actions) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration instanceof LegioConfiguration) {
			try {
				if (configuration.distributionRepository().isEmpty() && Arrays.asList(actions.actions()).contains("deploy"))
					throw new LegioException(message("distribution.repository.not.found"));
				new LegioMavenRunner(module).publishFramework(actions);
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
	private ProgressIndicator updateProgressIndicator(String message) {
		final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
		if (progressIndicator != null) {
			progressIndicator.setText2(message);
			progressIndicator.setIndeterminate(true);
		}
		return progressIndicator;
	}
}
