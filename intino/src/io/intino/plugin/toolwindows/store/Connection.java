package io.intino.plugin.toolwindows.store;

import java.io.InputStream;
import java.util.List;

public interface Connection {
	File root();

	interface File {
		String name();

		String absolutePath();

		boolean isDirectory();

		List<File> children();

		InputStream content();
	}
}