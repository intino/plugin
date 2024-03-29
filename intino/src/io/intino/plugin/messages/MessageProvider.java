package io.intino.plugin.messages;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class MessageProvider {

	@NonNls
	protected static final String PATH_TO_BUNDLE = "messages.TaraBundle";
	private static Reference<ResourceBundle> ourBundle;

	private MessageProvider() {
	}

	public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params) {
		return AbstractBundle.message(getBundle(), key, params);
	}

	private static ResourceBundle getBundle() {
		ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE, new UTF8Control());
			ourBundle = new SoftReference<>(bundle);
		}
		return bundle;
	}
}
