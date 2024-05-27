package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class ActionUtils {

	private ActionUtils() {
	}

	public static void selectedFilesAre(AnActionEvent e, String extension) {
		List<VirtualFile> vfile = getFilesFromEvent(e, extension);
		if (vfile.isEmpty()) disable(e);
		else enable(e);
	}

	@NotNull
	public static List<VirtualFile> getFilesFromEvent(AnActionEvent e, String extension) {
		VirtualFile[] files = LangDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
		if ((files == null) || (files.length == 0)) return Collections.emptyList();
		final List<VirtualFile> virtualFiles = Arrays.asList(files);
		return virtualFiles.stream().flatMap(v -> v.isDirectory() ? collectAll(v, extension) : fileOf(extension, v)).toList();
	}

	private static @NotNull Stream<VirtualFile> fileOf(String extension, VirtualFile v) {
		return v.getName().endsWith(extension) ? Stream.of(v) : Stream.of();
	}

	private static Stream<VirtualFile> collectAll(VirtualFile v, String extension) {
		return VfsUtil.collectChildrenRecursively(v).stream().filter(f -> f.getName().endsWith("." + extension));
	}

	private static void enable(AnActionEvent e) {
		e.getPresentation().setEnabled(true);
		e.getPresentation().setVisible(true);
	}

	private static void disable(AnActionEvent e) {
		e.getPresentation().setVisible(false);
		e.getPresentation().setEnabled(false);
	}

}
