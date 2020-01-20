package io.intino.plugin.formatter;

import com.intellij.formatting.FormattingMode;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import io.intino.plugin.lang.TaraLanguage;

class TaraBlockContext {

	private final CommonCodeStyleSettings mySettings;
	private final TaraCodeStyleSettings myTaraSettings;
	private final SpacingBuilder mySpacingBuilder;
	private final FormattingMode myMode;

	TaraBlockContext(CodeStyleSettings settings, SpacingBuilder builder, FormattingMode mode) {
		mySettings = settings.getCommonSettings(TaraLanguage.INSTANCE);
		myTaraSettings = settings.getCustomSettings(TaraCodeStyleSettings.class);
		mySpacingBuilder = builder;
		myMode = mode;
	}

	public CommonCodeStyleSettings getSettings() {
		return mySettings;
	}

	public TaraCodeStyleSettings getPySettings() {
		return myTaraSettings;
	}

	public SpacingBuilder getSpacingBuilder() {
		return mySpacingBuilder;
	}

	public FormattingMode getMode() {
		return myMode;
	}
}
