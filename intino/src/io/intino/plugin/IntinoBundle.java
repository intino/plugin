package io.intino.plugin;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class IntinoBundle {

	@NonNls
	private static final String BUNDLE = "IntinoBundle";
	private static final DynamicBundle INSTANCE = new DynamicBundle(IntinoBundle.class, BUNDLE);

	private IntinoBundle() {
	}

	public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
		return INSTANCE.getMessage(key, params);
	}
}
