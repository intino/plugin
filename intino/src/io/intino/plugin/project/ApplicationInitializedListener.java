package io.intino.plugin.project;

import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.configuration.ConfigurationManager;
import io.intino.plugin.project.configuration.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

public class ApplicationInitializedListener extends PreloadingActivity {

	@Override
	public void preload(@NotNull ProgressIndicator indicator) {
		LanguageManager.register(new tara.dsl.Legio());
		ConfigurationManager.registerProvider(LegioConfiguration.class);
	}
}
