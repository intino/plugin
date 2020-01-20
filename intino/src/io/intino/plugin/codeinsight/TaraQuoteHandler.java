package io.intino.plugin.codeinsight;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import io.intino.plugin.lang.psi.TaraTypes;

public class TaraQuoteHandler extends SimpleTokenSetQuoteHandler {

	public TaraQuoteHandler() {
		super(TaraTypes.QUOTE_BEGIN, TaraTypes.QUOTE_END, TaraTypes.EXPRESSION_BEGIN, TaraTypes.EXPRESSION_END);
	}
}