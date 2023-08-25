package io.intino.plugin.project;

import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.configuration.ConfigurationManager;
import io.intino.plugin.project.configuration.LegioConfiguration;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;

public class ApplicationInitializedListener implements com.intellij.ide.ApplicationInitializedListener {

	@Override
	public Object execute(@NotNull CoroutineScope asyncScope, @NotNull Continuation<? super Unit> $completion) {
		LanguageManager.register(new tara.dsl.Legio());
		ConfigurationManager.registerProvider(LegioConfiguration.class);
		return com.intellij.ide.ApplicationInitializedListener.super.execute(asyncScope, $completion);
	}
}
