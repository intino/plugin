package io.intino.plugin.project;

import java.util.Collections;
import java.util.List;

public class Safe {

	public interface StringWrapper {

		String value();
	}

	public interface Wrapper<T> {

		T value();
	}

	public interface ListWrapper<T> {
		List<T> value();
	}


	public static String safe(StringWrapper wrapper) {
		return safe(wrapper, "");
	}

	public static String safe(StringWrapper wrapper, String defaultValue) {
		try {
			return wrapper.value();
		} catch (Throwable e) {
			return defaultValue;
		}
	}


	public static <T> T safe(Wrapper<T> wrapper) {
		try {
			return wrapper.value();
		} catch (Throwable e) {
			return null;
		}
	}

	public static <T> T safe(Wrapper<T> wrapper, T defaultValue) {
		try {
			return wrapper.value();
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	public static <T> List<T> safeList(ListWrapper<T> wrapper) {
		try {
			return wrapper.value();
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}
}
