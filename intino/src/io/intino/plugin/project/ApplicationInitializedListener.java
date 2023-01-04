package io.intino.plugin.project;

import com.intellij.openapi.application.PreloadingActivity;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.configuration.ConfigurationManager;
import io.intino.plugin.project.configuration.LegioConfiguration;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ApplicationInitializedListener extends PreloadingActivity {

	@Nullable
	@Override
	public Object execute(@NotNull Continuation<? super Unit> $completion) {
		LanguageManager.register(new tara.dsl.Legio());
		ConfigurationManager.registerProvider(LegioConfiguration.class);
		return null;
	}

}
