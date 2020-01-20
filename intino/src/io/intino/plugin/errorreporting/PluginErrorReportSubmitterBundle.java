package io.intino.plugin.errorreporting;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class PluginErrorReportSubmitterBundle {

	private static final String PATH_TO_BUNDLE = "messages.PluginErrorReportSubmitterBundle";
	private static Reference<ResourceBundle> ourBundle;

	private PluginErrorReportSubmitterBundle() {
	}

	public static String message(@PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params) {
		return CommonBundle.message(getBundle(), key, params);
	}

	private static ResourceBundle getBundle() {
		ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE);
			ourBundle = new SoftReference<>(bundle);
		}
		return bundle;
	}
}
