package io.intino.plugin.highlighting;

import com.intellij.lexer.FlexAdapter;
import com.intellij.openapi.project.Project;

public class TaraHighlighterLexAdapter extends FlexAdapter {

	public TaraHighlighterLexAdapter(Project project) {
		super(new TaraHighlighterLex(null, project));
	}
}
