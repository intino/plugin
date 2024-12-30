package io.intino.plugin.project;

import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.ConfigurationManager;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ApplicationInitializedListener implements com.intellij.ide.ApplicationInitializedListener {

	@Nullable
	@Override
	public Object execute(@NotNull Continuation<? super Unit> $completion) {
		LanguageManager.register(new tara.dsl.Legio());
		ConfigurationManager.registerProvider(ArtifactLegioConfiguration.class);
		return null;
	}
}
