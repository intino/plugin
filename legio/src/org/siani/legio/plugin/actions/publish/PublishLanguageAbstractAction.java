package org.siani.legio.plugin.actions.publish;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.siani.legio.plugin.build.LanguagePublisher;
import org.siani.legio.plugin.build.LegioMavenRunner;
import org.siani.legio.plugin.project.LegioConfiguration;
import tara.intellij.lang.LanguageManager;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.intellij.messages.MessageProvider;
import tara.intellij.project.configuration.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class PublishLanguageAbstractAction extends AnAction implements DumbAware {
	private static final Logger LOG = Logger.getInstance(PublishLanguageAbstractAction.class.getName());
	private static final String JAR_EXTENSION = ".jar";

	List<String> errorMessages = new ArrayList<>();
	List<String> successMessages = new ArrayList<>();

	boolean publish(final Module module) {
		publishLanguage(module);
		publishFramework(module);
		return true;
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

	private boolean publishLanguage(Module module) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		return ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> publishLanguage(module, configuration), MessageProvider.message("export.language", configuration.outDSL()), false, module.getProject());
	}

	private void publishLanguage(Module module, Configuration configuration) {
		final String outDSL = configuration.outDSL();
		File dslFile = dslFilePath(configuration);
		LocalFileSystem.getInstance().refreshIoFiles(Collections.singleton(dslFile), true, false, null);
		publish(module, outDSL, dslFile);
	}

	@NotNull
	private File dslFilePath(Configuration configuration) {
		final String outDSL = configuration.outDSL();
		return new File(LanguageManager.getLanguageDirectory(outDSL) + configuration.modelVersion() + File.separator + outDSL + JAR_EXTENSION);
	}

	private void publish(Module module, String dsl, File dslFile) {
		try {
			final int i = new LanguagePublisher(module, dsl, dslFile).export();
			if (i != 201) throw new IOException("Error uploading language. Code: " + i);
			successMessages.add(MessageProvider.message("saved.message", dsl));
		} catch (final IOException e) {
			LOG.info(e.getMessage(), e);
			errorMessages.add(e.getMessage() + "\n(" + FileUtil.getNameWithoutExtension(dsl) + ")");
		}
	}
}
