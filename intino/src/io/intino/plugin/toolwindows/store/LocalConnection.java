package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.SystemIndependent;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LocalConnection implements Connection {
	private final java.io.File root;

	LocalConnection(@SystemIndependent String basePath, String path) {
		this.root = new java.io.File(path).isAbsolute() ? new java.io.File(path) : new java.io.File(basePath, path);
	}

	@Override
	public File root() {
		return !root.exists() ? null : new LocalFile(root);
	}

	public static class LocalFile implements File {

		private final java.io.File file;

		private LocalFile(java.io.File file) {
			this.file = file;
		}

		@Override
		public String name() {
			return file.getName();
		}

		@Override
		public String absolutePath() {
			return file.getAbsolutePath();
		}

		@Override
		public boolean isDirectory() {
			return file.isDirectory();
		}

		@Override
		public List<File> children() {
			return Arrays.stream(Objects.requireNonNull(file.listFiles(f -> !f.getName().equals(".DS_Store")))).map(LocalFile::new).collect(Collectors.toList());
		}

		@Override
		public InputStream content() {
			try {
				return new BufferedInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				Logger.getInstance(this.getClass()).error(e);
				return new ByteArrayInputStream(new byte[0]);
			}
		}

		@Override
		public String toString() {
			return name();
		}
	}
}