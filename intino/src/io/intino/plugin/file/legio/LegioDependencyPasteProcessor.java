package io.intino.plugin.file.legio;

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
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
		if (!psiFile.getFileType().equals(LegioFileType.instance())) return text;
		if (isMavenDependency(text.trim())) return processAsMaven(text);
		if (isBowerDependency(text.trim())) return processAsBower(text);
		return text;
	}

	private String processAsMaven(String text) {
		List<String[]> parameters = extractInfoFrom(text);
		if (parameters.isEmpty()) return text;
		String result = "";
		for (String[] parameter : parameters)
			result += parameter[0] + "(\"" + parameter[1] + "\", \"" + parameter[2] + "\", \"" + parameter[3] + "\")\n";
		return result;
	}

	private String processAsBower(String text) {
		final String[] split = text.split(" ");
		String name = split[split.length - 1];
		return "WebComponent(\"" + name + "\", \"latest\")";
	}

	private boolean isMavenDependency(String text) {
		return text.startsWith("<dependency>") && text.endsWith("</dependency>") &&
				text.contains("<groupId>") && text.contains("<artifactId>");
	}

	private List<String[]> extractInfoFrom(String text) {
		List<String[]> dependencyList = new ArrayList<>();
		for (String dependency : text.split("<dependency>")) {
			if (dependency.trim().isEmpty()) continue;
			try {
				String scope = dependency.contains("<scope>test</scope>") ? "Test" : "Compile";
				String groupId = dependency.substring(dependency.indexOf(GROUP_ID) + GROUP_ID.length(), dependency.indexOf("</groupId>"));
				String artifactId = dependency.substring(dependency.indexOf(ARTIFACT_ID) + ARTIFACT_ID.length(), dependency.indexOf("</artifactId>"));
				String version = dependency.substring(dependency.indexOf(VERSION) + VERSION.length(), dependency.indexOf("</version>"));
				dependencyList.add(new String[]{scope, groupId, artifactId, version});
			} catch (IndexOutOfBoundsException e) {
				return Collections.emptyList();
			}
		}
		return dependencyList;
	}

	private boolean isBowerDependency(String text) {
		return text.startsWith("bower install ");
	}
}
