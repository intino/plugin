package io.intino.legio.plugin.file;

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
		List<String[]> parameters = extractInfoFrom(text);
		String result = "";
		for (String[] parameter : parameters)
			result += parameter[0] + "(\"" + parameter[1] + "\", \"" + parameter[2] + "\", \"" + parameter[3] + "\")\n";
		return result;
	}

	private boolean isMavenDependency(String text) {
		return text.startsWith("<dependency>") && text.endsWith("</dependency>") &&
				text.contains("<groupId>") && text.contains("<artifactId>");
	}

	private List<String[]> extractInfoFrom(String text) {
		List<String[]> dependencyList = new ArrayList<>();
		for (String dependency : text.split("<dependency>")) {
			if (dependency.isEmpty()) continue;
			String scope = dependency.contains("<scope>test</scope>") ? "Test" : "Compile";
			String groupId = dependency.substring(dependency.indexOf(GROUP_ID) + GROUP_ID.length(), dependency.indexOf("</groupId>"));
			String artifactId = dependency.substring(dependency.indexOf(ARTIFACT_ID) + ARTIFACT_ID.length(), dependency.indexOf("</artifactId>"));
			String version = dependency.substring(dependency.indexOf(VERSION) + VERSION.length(), dependency.indexOf("</version>"));
			dependencyList.add(new String[]{scope, groupId, artifactId, version});
		}
		return dependencyList;
	}
}
