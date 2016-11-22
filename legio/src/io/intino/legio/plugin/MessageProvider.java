package io.intino.legio.plugin;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import tara.intellij.messages.UTF8Control;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class MessageProvider {

	@NonNls
	private static final String PATH_TO_BUNDLE = "LegioBundle";
	private static Reference<ResourceBundle> ourBundle;

	private MessageProvider() {
	}

	public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params) {
		return CommonBundle.message(getBundle(), key, params);
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
