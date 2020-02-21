package io.intino.plugin.codeinsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.util.ProcessingContext;
import io.intino.Configuration;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.ArtifactorySensor;
import io.intino.plugin.project.IntinoModuleType;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.intino.plugin.project.ArtifactorySensor.*;
import static io.intino.plugin.project.module.ModuleProvider.moduleOf;


public class LegioCompletionContributor extends CompletionContributor {

	public LegioCompletionContributor() {
		inLanguageName();
		inLanguageVersion();
		inImportVersion();
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
						if (!IntinoModuleType.isIntino(module)) return;
						final Node container = (Node) TaraPsiUtil.getContainerOf(parameters.getOriginalPosition());
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

	private void inImportVersion() {
		extend(CompletionType.BASIC, LegioFilters.inDependencyVersion, new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						resolveDependency(parameters, resultSet);
					}
				}
		);
	}

	private void resolveDependency(CompletionParameters parameters, CompletionResultSet resultSet) {
		final Module module = moduleOf(parameters.getOriginalFile());
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) return;
		final List<String> values = new ArtifactorySensor(configuration.repositories()).dependencyVersions(artifactFrom(TaraPsiUtil.getContainerNodeOf(parameters.getOriginalPosition())));
		if (values == null) return;
		for (String value : values) resultSet.addElement(LookupElementBuilder.create(value));
		JavaCompletionSorting.addJavaSorting(parameters, resultSet);
	}

	private String artifactFrom(Node node) {
		return groupId(node.parameters()) + ":" + artifactId(node.parameters());
	}

	private String artifactId(List<Parameter> parameters) {
		for (Parameter parameter : parameters)
			if (parameter.name().equalsIgnoreCase("artifactId")) return parameter.values().get(0).toString();
		return parameters.isEmpty() ? "" : parameters.get(0).values().get(0).toString();
	}

	private String groupId(List<Parameter> parameters) {
		for (Parameter parameter : parameters)
			if (parameter.name().equalsIgnoreCase("groupId")) return parameter.values().get(0).toString();
		return parameters.isEmpty() ? "" : parameters.get(0).values().get(0).toString();
	}

	private void resolve(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet resultSet, String tag) {
		final Module module = moduleOf(parameters.getOriginalFile());
		if (!IntinoModuleType.isIntino(module)) return;
		final String[] values = PropertiesComponent.getInstance().getValues(tag);
		if (values == null) return;
		for (String value : values) resultSet.addElement(LookupElementBuilder.create(value));
		JavaCompletionSorting.addJavaSorting(parameters, resultSet);
	}
}