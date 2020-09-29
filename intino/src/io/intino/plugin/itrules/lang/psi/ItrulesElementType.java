package io.intino.plugin.itrules.lang.psi;

import com.intellij.psi.tree.IElementType;
import io.intino.plugin.itrules.lang.ItrulesLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ItrulesElementType extends IElementType {

    public ItrulesElementType(@NotNull @NonNls String name) {
        super(name, ItrulesLanguage.INSTANCE);
    }
}
