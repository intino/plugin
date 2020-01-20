package io.intino.plugin.codeinsight.livetemplates;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import org.jetbrains.annotations.Nullable;

public class TaraTemplateProvider implements DefaultLiveTemplatesProvider {

	@Override
	public String[] getDefaultLiveTemplateFiles() {
		return new String[]{"/livetemplates/var"};
	}

	@Nullable
	@Override
	public String[] getHiddenLiveTemplateFiles() {
		return new String[0];
	}
}
