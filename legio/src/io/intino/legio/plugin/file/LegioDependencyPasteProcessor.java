package io.intino.legio.plugin.file;

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LegioDependencyPasteProcessor implements CopyPastePreProcessor {

	private static final String GROUP_ID = "<groupId>";
	private static final String ARTIFACT_ID = "<artifactId>";
	private static final String VERSION = "<version>";

	@Nullable
	@Override
	public String preprocessOnCopy(PsiFile psiFile, int[] ints, int[] ints1, String s) {
		return s;
	}

	@NotNull
	@Override
	public String preprocessOnPaste(Project project, PsiFile psiFile, Editor editor, String text, RawText rawText) {
		if (!psiFile.getFileType().equals(LegioFileType.instance()) || !isMavenDependency(text.trim())) return text;
		String[] parameters = extractInfoFrom(text);
		return parameters[0] + "(\"" + parameters[1] + "\", \"" + parameters[2] + "\", \"" + parameters[3] + "\")";
	}

	private boolean isMavenDependency(String text) {
		return text.startsWith("<dependency>") && text.endsWith("</dependency>") &&
				text.contains("<groupId>") && text.contains("<artifactId>");
	}

	private String[] extractInfoFrom(String text) {
		String scope = text.contains("<scope>test</scope>") ? "Test" : "Compile";
		String groupId = text.substring(text.indexOf(GROUP_ID) + GROUP_ID.length(), text.indexOf("</groupId>"));
		String artifactId = text.substring(text.indexOf(ARTIFACT_ID) + ARTIFACT_ID.length(), text.indexOf("</artifactId>"));
		String version = text.substring(text.indexOf(VERSION) + VERSION.length(), text.indexOf("</version>"));
		return new String[]{scope, groupId, artifactId, version};
	}
}
