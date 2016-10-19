package io.intino.legio.plugin.actions.publish;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.intino.legio.plugin.build.LegioMavenRunner;
import io.intino.legio.plugin.project.LegioConfiguration;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.LanguageManager;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static tara.intellij.messages.MessageProvider.message;

abstract class PublishLanguageAbstractAction extends AnAction implements DumbAware {
	private static final String JAR_EXTENSION = ".jar";

	List<String> errorMessages = new ArrayList<>();
	List<String> successMessages = new ArrayList<>();

	boolean publish(final Module module) {
		return ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
			updateProgressIndicator(message("publishing.language"));
			publishLanguage(module);
			updateProgressIndicator("Publishing Framework");
			publishFramework(module);
		}, "Publishing " + TaraUtil.configurationOf(module).artifactId(), false, module.getProject());
	}

	private void publishLanguage(Module module) {
		Configuration configuration = TaraUtil.configurationOf(module);
		File dslFile = dslFilePath(configuration);
		LocalFileSystem.getInstance().refreshIoFiles(Collections.singleton(dslFile), true, false, null);
		publishLanguage(module, configuration);
	}

	private void publishLanguage(Module module, Configuration configuration) {
		LegioMavenRunner runner = new LegioMavenRunner(module);
		try {
			runner.publishLanguage(configuration);
		} catch (MavenInvocationException | IOException e) {
			errorMessages.add("Error publishing language. " + e.getMessage());
		}
	}

	private void publishFramework(Module module) {
		Configuration configuration = TaraUtil.configurationOf(module);
		LegioMavenRunner runner = new LegioMavenRunner(module);
		if (configuration instanceof LegioConfiguration) {
			try {
				runner.publishFramework();
			} catch (MavenInvocationException | IOException e) {
				errorMessages.add("Error publishing framework. " + e.getMessage());
			}
		} else runner.publishNativeMaven();
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
