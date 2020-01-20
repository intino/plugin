package io.intino.plugin.formatter;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.DisplayPriority;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.messages.MessageProvider;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.SPACES_AROUND_OPERATORS;
import static com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.SPACES_WITHIN;


public class TaraLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
	@NotNull
	@Override
	public Language getLanguage() {
		return TaraLanguage.INSTANCE;
	}

	@Override
	public String getCodeSample(@NotNull SettingsType settingsType) {
		if (settingsType == SettingsType.SPACING_SETTINGS) return SPACING_SETTINGS_PREVIEW;
		if (settingsType == SettingsType.BLANK_LINES_SETTINGS) return BLANK_LINES_SETTINGS_PREVIEW;
		if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) return WRAP_SETTINGS_PREVIEW;
		if (settingsType == SettingsType.INDENT_SETTINGS) return INDENT_SETTINGS_PREVIEW;
		return "";
	}

	@Override
	public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
		if (settingsType == SettingsType.SPACING_SETTINGS) {
			consumer.showStandardOptions(
					"SPACE_AROUND_ASSIGNMENT_OPERATORS",
					"SPACE_AROUND_EQUALITY_OPERATORS",
					"SPACE_AFTER_COMMA",
					"SPACE_BEFORE_COMMA",
					"SPACE_BEFORE_SEMICOLON");
			consumer.showCustomOption(TaraCodeStyleSettings.class, "SPACE_AROUND_EQ_IN_NAMED_PARAMETER",
					MessageProvider.message("formatter.around.eq.in.named.parameter"), SPACES_AROUND_OPERATORS);
			consumer.showCustomOption(TaraCodeStyleSettings.class, "SPACE_AROUND_EQ_IN_KEYWORD_ARGUMENT",
					MessageProvider.message("formatter.around.eq.in.keyword.argument"), SPACES_AROUND_OPERATORS);
			consumer.showCustomOption(TaraCodeStyleSettings.class, "SPACE_WITHIN_BRACES", MessageProvider.message("formatter.braces"), SPACES_WITHIN);
		} else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
			consumer.showStandardOptions("BLANK_LINES_AFTER_IMPORTS");
			consumer.renameStandardOption("BLANK_LINES_AFTER_IMPORTS", MessageProvider.message("formatter.around.top.level.imports"));

		} else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
			consumer.showStandardOptions("RIGHT_MARGIN",
					"WRAP_ON_TYPING",
					"KEEP_LINE_BREAKS",
					"WRAP_LONG_LINES",
					"ALIGN_MULTILINE_PARAMETERS",
					"ALIGN_MULTILINE_PARAMETERS_IN_CALLS");
			consumer.showCustomOption(TaraCodeStyleSettings.class, "ALIGN_MULTILINE_IMPORTS",
					MessageProvider.message("formatter.align.when.multiline"),
					MessageProvider.message("formatter.import.statements"));
		}
	}

	@Override
	public IndentOptionsEditor getIndentOptionsEditor() {
		return new SmartIndentOptionsEditor();
	}

	@Override
	protected void customizeDefaults(@NotNull CommonCodeStyleSettings defaultSettings, @NotNull CommonCodeStyleSettings.IndentOptions indentOptions) {
		indentOptions.INDENT_SIZE = 4;
		indentOptions.USE_TAB_CHARACTER = true;
		defaultSettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS = false;
		defaultSettings.KEEP_BLANK_LINES_IN_DECLARATIONS = 0;
		defaultSettings.KEEP_BLANK_LINES_IN_CODE = 0;
	}

	@Override
	public DisplayPriority getDisplayPriority() {
		return DisplayPriority.LANGUAGE_SETTINGS;
	}

	@SuppressWarnings("FieldCanBeLocal")
	private static String SPACING_SETTINGS_PREVIEW = "Timeline(from = \"01/01/2011 00:00\", to = \"31/01/2011 00:00\", Minute)\n\n" +
			"Indicator(Weather, \"Temperature\") temperatureIndicator\n" +
			"\tAggregate('item.as(Weather.class).temperature()', Max Min Average)";

	@SuppressWarnings("FieldCanBeLocal")
	private static String BLANK_LINES_SETTINGS_PREVIEW = "dsl Sumus\n" +
			"\n" +
			"use Store\n" +
			"\n" +
			"Timeline(from = \"01/01/2011 00:00\", to = \"31/01/2011 00:00\", Minute)\n" +
			"\n" +
			"Classifier by\n" +
			"\tClassification(Node, label = \"Room side\") roomSide\n" +
			"\t\tOrdinal('item.as(Node.class) != null ? item.as(Node.class).side() : null')\n" +
			"\tClassification(Event, \"Month of year\") monthOfYear";
	@SuppressWarnings("FieldCanBeLocal")
	private static String WRAP_SETTINGS_PREVIEW = "dsl Sumus\n" +
			"\" +\n" +
			"\t\t\"\\n\" +\n" +
			"\t\t\"use Store\\n\" +\n" +
			"\t\t\"\\n\" +\n" +
			"\t\t\"Timeline(from = \\\"01/01/2011 00:00\\\", to = \\\"31/01/2011 00:00\\\", Minute)\\n\" +\n" +
			"\t\t\"\\n\" +\n" +
			"\t\t\"Classifier by\\n\" +\n" +
			"\t\t\"\\tClassification(Node, label = \\\"Room side\\\") roomSide\\n\" +\n" +
			"\t\t\"\\t\\tOrdinal('item.as(Node.class) != null ? item.as(Node.class).side() : null')\\n\" +\n" +
			"\t\t\"\tClassification(Event, \"Month of year\") monthOfYear";
	@SuppressWarnings("FieldCanBeLocal")
	private static String INDENT_SETTINGS_PREVIEW = "dsl Sumus\n" +
			"\n" +
			"Member Building\n" +
			"\n" +
			"Member Node\n" +
			"\tvar Building building\n" +
			"\tvar string floor\n" +
			"\tvar word:[Left Right] side\n" +
			"\tvar word:[HZG KUE BAD SLZ KIZ WNZ FWE BHK HKB] sensor\n" +
			"Fact Event\n" +
			"\tsub HeatingSystem\n" +
			"\t\tvar Node node\n" +
			"\t\tvar double:Wh energy\n" +
			"\t\tvar double:L volume\n" +
			"\t\tvar double:°C inputTemperature\n" +
			"\t\tvar double:°C outputTemperature\n";
}
