package io.intino.plugin.formatter;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.psi.codeStyle.*;
import org.jetbrains.annotations.NotNull;

public class TaraCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
	@Override
	public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
		return new TaraCodeStyleSettings(settings);
	}

	@NotNull
	@Override
	public CodeStyleConfigurable createConfigurable(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
		return new CodeStyleAbstractConfigurable(settings, originalSettings, "Tara") {
			protected CodeStyleAbstractPanel createPanel(final CodeStyleSettings settings) {
				return new TaraCodeStyleMainPanel(getCurrentSettings(), settings);
			}

			public String getHelpTopic() {
				return "reference.settingsdialog.codestyle.tara";
			}
		};
	}

	@Override
	public String getConfigurableDisplayName() {
		return "Tara";
	}

	@Override
	public DisplayPriority getPriority() {
		return DisplayPriority.LANGUAGE_SETTINGS;
	}
}
