package io.intino.plugin.refactoring;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.refactoring.rename.NameSuggestionProvider;
import io.intino.magritte.lang.model.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class TaraNameSuggestionProvider implements NameSuggestionProvider {
	@Nullable
	@Override
	public SuggestedNameInfo getSuggestedNames(PsiElement element, @Nullable PsiElement nameSuggestionContext, Set<String> result) {
		if (!(element instanceof Node)) return null;
		final String name = ((Node) element).name();
		if (name == null) return null;
		result.add(toCamelCase(name, true));
		return SuggestedNameInfo.NULL_INFO;
	}

	@NotNull
	protected String toCamelCase(@NotNull final String name, boolean uppercaseFirstLetter) {
		final List<String> strings = StringUtil.split(name, "_");
		if (!strings.isEmpty()) {
			final StringBuilder buf = new StringBuilder();
			String str = strings.get(0).toLowerCase();
			if (uppercaseFirstLetter) str = StringUtil.capitalize(str);
			buf.append(str);
			for (int i = 1; i < strings.size(); i++) {
				buf.append(StringUtil.capitalize(strings.get(i).toLowerCase()));
			}
			return buf.toString();
		}
		return name;
	}
}
