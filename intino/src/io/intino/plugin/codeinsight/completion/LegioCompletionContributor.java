package io.intino.plugin.codeinsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.util.ProcessingContext;
import io.intino.Configuration;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.module.IntinoModuleType;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.intino.plugin.project.ArtifactorySensor.LanguageLibrary;
import static io.intino.plugin.project.ArtifactorySensor.Languages;
import static io.intino.plugin.project.module.ModuleProvider.moduleOf;


public class LegioCompletionContributor extends CompletionContributor {

	public LegioCompletionContributor() {
		inDslName();
		inDslVersion();
		inImportVersion();
	}

	private void inDslName() {
		extend(CompletionType.BASIC, LegioFilters.inDslName, new CompletionProvider<>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   @NotNull ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						resolve(parameters, resultSet, Languages);
					}
				}
		);
	}

	private void inDslVersion() {
		extend(CompletionType.BASIC, LegioFilters.inDslVersion, new CompletionProvider<>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   @NotNull ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						final Module module = moduleOf(parameters.getOriginalFile());
						if (!IntinoModuleType.isIntino(module)) return;
						final Mogram container = (Mogram) TaraPsiUtil.getContainerOf(parameters.getOriginalPosition());
						if (container == null) return;
						final Parameter name = container.parameters().stream().filter(p -> p.name().equals("name")).findAny().orElse(null);
						if (name == null) return;
						String languageName = name.values().get(0).toString();
						PropertiesComponent component = PropertiesComponent.getInstance();
						List<String> list = component.getList(LanguageLibrary + languageName);
						final @Nullable Set<String> values = new HashSet<>(list == null ? List.of() : list);
						List<String> lower = component.getList(LanguageLibrary + languageName.toLowerCase());
						if (lower != null) values.addAll(lower);
						for (String value : values) resultSet.addElement(LookupElementBuilder.create(value));
					}
				}
		);
	}

	private void inImportVersion() {
		extend(CompletionType.BASIC, LegioFilters.inDependencyVersion, new CompletionProvider<>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
											   @NotNull ProcessingContext context,
											   @NotNull CompletionResultSet resultSet) {
						resolveDependency(parameters, resultSet);
					}
				}
		);
	}

	private void resolveDependency(CompletionParameters parameters, CompletionResultSet resultSet) {
		final Module module = moduleOf(parameters.getOriginalFile());
		final Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof ArtifactLegioConfiguration)) return;
		final List<String> values = new ArtifactoryConnector(module.getProject(), configuration.repositories())
				.versions(artifactFrom(TaraPsiUtil.getContainerNodeOf(parameters.getOriginalPosition())));
		if (values == null) return;
		values.stream().map(LookupElementBuilder::create).forEach(resultSet::addElement);
		JavaCompletionSorting.addJavaSorting(parameters, resultSet);
	}

	private String artifactFrom(Mogram node) {
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
		final @Nullable List<String> values = PropertiesComponent.getInstance().getList(tag);
		if (values == null) return;
		for (String value : values) resultSet.addElement(LookupElementBuilder.create(value));
		JavaCompletionSorting.addJavaSorting(parameters, resultSet);
	}
}