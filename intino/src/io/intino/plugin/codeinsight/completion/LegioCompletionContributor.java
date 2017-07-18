package io.intino.plugin.codeinsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.util.ProcessingContext;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.plugin.lang.psi.impl.TaraPsiImplUtil;
import io.intino.tara.plugin.project.TaraModuleType;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.project.ArtifactorySensor.*;
import static io.intino.tara.plugin.project.module.ModuleProvider.moduleOf;


public class LegioCompletionContributor extends CompletionContributor {

	public LegioCompletionContributor() {
		inLanguageName();
		inLanguageVersion();
		inGenerationVersion();
		inBoxVersion();
		inBoxLanguage();
	}

	private void inLanguageName() {
		extend(CompletionType.BASIC, LegioFilters.inModelLanguage, new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						resolve(parameters, resultSet, LANGUAGES_TAG);
					}
				}
		);
	}

	private void inLanguageVersion() {
		extend(CompletionType.BASIC, LegioFilters.inLanguageVersion, new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						final Module module = moduleOf(parameters.getOriginalFile());
						if (!TaraModuleType.isTara(module)) return;
						final Node container = (Node) TaraPsiImplUtil.getContainerOf(parameters.getOriginalPosition());
						if (container == null) return;
						final Parameter name = container.parameters().stream().filter(p -> p.name().equals("language")).findAny().orElse(null);
						if (name == null) return;
						final String[] values = PropertiesComponent.getInstance().getValues(LANGUAGE_TAG + name.values().get(0).toString());
						if (values == null) return;
						for (String value : values) resultSet.addElement(LookupElementBuilder.create(value));
					}
				}
		);
	}

	private void inBoxVersion() {
		extend(CompletionType.BASIC, LegioFilters.inBoxVersion, new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						resolve(parameters, resultSet, BOXING_TAG);
					}
				}
		);
	}

	private void inBoxLanguage() {
		extend(CompletionType.BASIC, LegioFilters.inBoxLanguage, new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						resultSet.addElement(LookupElementBuilder.create("Konos"));
					}
				}
		);
	}

	private void inGenerationVersion() {
		extend(CompletionType.BASIC, LegioFilters.inSDKVersion, new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						resolve(parameters, resultSet, GENERATION_TAG);
					}
				}
		);
	}

	private void resolve(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet resultSet, String tag) {
		final Module module = moduleOf(parameters.getOriginalFile());
		if (!TaraModuleType.isTara(module)) return;
		final String[] values = PropertiesComponent.getInstance().getValues(tag);
		if (values == null) return;
		for (String value : values) resultSet.addElement(LookupElementBuilder.create(value));
		JavaCompletionSorting.addJavaSorting(parameters, resultSet);
	}
}