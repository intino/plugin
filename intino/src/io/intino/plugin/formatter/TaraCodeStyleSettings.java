package io.intino.plugin.formatter;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class TaraCodeStyleSettings extends CustomCodeStyleSettings {

	public static boolean SPACE_WITHIN_BRACES = false;
	public static boolean SPACE_BEFORE_PY_COLON = false;
	public static boolean SPACE_AFTER_PY_COLON = true;
	public static boolean SPACE_BEFORE_LBRACKET = false;
	public static boolean SPACE_AROUND_EQ_IN_NAMED_PARAMETER = true;
	public static boolean SPACE_AROUND_EQ_IN_KEYWORD_ARGUMENT = true;
	public static boolean SPACE_BEFORE_BACKSLASH = true;

	public static boolean BLANK_LINE_AT_FILE_END = true;

	public static boolean ALIGN_COLLECTIONS_AND_COMPREHENSIONS = true;
	public static boolean ALIGN_MULTILINE_IMPORTS = true;

	public static boolean NEW_LINE_AFTER_COLON = false;

	public static boolean SPACE_AFTER_NUMBER_SIGN = true;
	public static boolean SPACE_BEFORE_NUMBER_SIGN = true;

	public static int BLANK_LINES_AFTER_LOCAL_IMPORTS = 0;


	public TaraCodeStyleSettings(CodeStyleSettings container) {
		super("Tara", container);
	}
}
