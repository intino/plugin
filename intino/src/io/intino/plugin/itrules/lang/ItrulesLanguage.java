package io.intino.plugin.itrules.lang;

import com.intellij.lang.Language;

public class ItrulesLanguage extends Language {

    public static final ItrulesLanguage INSTANCE = new ItrulesLanguage();

    private ItrulesLanguage() {
        super("Itrules");
    }
}
