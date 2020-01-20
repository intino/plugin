package io.intino.plugin.highlighting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TaraColorSettingPage implements ColorSettingsPage {
	private static final AttributesDescriptor[] DESCRIPTORS =
			new AttributesDescriptor[]{
					new AttributesDescriptor("Meta Identifier", TaraSyntaxHighlighter.META_IDENTIFIER),
					new AttributesDescriptor("Identifier", TaraSyntaxHighlighter.IDENTIFIER),
					new AttributesDescriptor("String", TaraSyntaxHighlighter.STRING),
					new AttributesDescriptor("Documentation", TaraSyntaxHighlighter.DOCUMENTATION),
					new AttributesDescriptor("Primitive", TaraSyntaxHighlighter.PRIMITIVE),
					new AttributesDescriptor("Annotations", TaraSyntaxHighlighter.ANNOTATION),
					new AttributesDescriptor("Number", TaraSyntaxHighlighter.NUMBER),
					new AttributesDescriptor("Operator", TaraSyntaxHighlighter.OPERATOR),
					new AttributesDescriptor("Address", TaraSyntaxHighlighter.ANCHORS),
					new AttributesDescriptor("Bad Characters", TaraSyntaxHighlighter.BAD_CHARACTER)
			};

	@Nullable
	@Override
	public javax.swing.Icon getIcon() {
		return IntinoIcons.MODEL_16;
	}

	@NotNull
	@Override
	public com.intellij.openapi.fileTypes.SyntaxHighlighter getHighlighter() {
		return new TaraSyntaxHighlighter();
	}

	@NotNull
	@Override
	public String getDemoText() {
		return
				"dsl Tafat\n\n" +
						"use sample\n\n" +
						"!!	documentation of Board entity\n" +
						"Entity:{1..1} Board\n" +
						"\tvar function:Position positionOf = '$.squareList().indexOf(square);'\n" +
						"\thas Square is final\n" +
						"\n" +
						"Entity:{1..1} Dices\n" +
						"\tvar integer value1 = 0\n" +
						"\tvar integer value2 = 0\n" +
						"\tvar function:Roll roll\n" +
						"\t\t----\n" +
						"\t\t$.value1(new Random().nextInt(6) + 1);\n" +
						"\t\t$.value2(new Random().nextInt(6) + 1);\n" +
						"\t\t----\n" +
						"\tvar function:Doubles doubles = '$.value1() == $.value2();'\n" +
						"\tvar function:Value v = '$.value1() + $.value2();';\n" +
						"\n" +
						"\n" +
						"Entity Cards\n" +
						"\tvar function:Get get = '$.card(new Random().nextInt($.cardList().size()));'\n" +
						"\thas Card\n" +
						"\tsub:{1..1} LuckyCards\n" +
						"\tsub:{1..1} CommunityCards\n\n" +
						"Behavior JailScape on Player\n" +
						"\tvar word[]:{Card Money} modes = Card Money\n" +
						"\tvar string message= \"out of jail\"";
	}

	@Nullable
	@Override
	public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
		return new HashMap<>();
	}

	@NotNull
	@Override
	public AttributesDescriptor[] getAttributeDescriptors() {
		return DESCRIPTORS;
	}

	@NotNull
	@Override
	public ColorDescriptor[] getColorDescriptors() {
		return ColorDescriptor.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return "Tara";
	}
}