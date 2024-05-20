package io.intino.plugin.project.configuration.model.retrocompatibility;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;

public class LegioModelBuilder implements Configuration.Artifact.Dsl.Builder {
	private final LegioModel model;
	private final TaraMogram mogram;
	private final String sdk;

	public LegioModelBuilder(LegioModel model, TaraMogram mogram) {
		this.model = model;
		this.mogram = mogram;
		String sdk = TaraPsiUtil.parameterValue(mogram, "sdk", 3);
		this.sdk = sdk != null ? sdk : "io.intino.magritte:builder";
	}

	@Override
	public String groupId() {
		return sdk.split(":")[0];
	}

	@Override
	public String artifactId() {
		return sdk.split(":")[1];
	}

	@Override
	public String version() {
		return TaraPsiUtil.parameterValue(mogram, "sdkVersion", 2);
	}

	@Override
	public void version(String version) {
		writeCommandAction(mogram.getProject(), mogram.getContainingFile()).run(() -> mogram.parameters().stream().filter(p -> p.name().equals("sdkVersion")).findFirst().
				ifPresent(p -> p.substituteValues(Collections.singletonList(version))));
	}

	@Override
	public String generationPackage() {
		return "model";
	}

	@Override
	public List<ExcludedPhases> excludedPhases() {
		List<String> excludedPhases = TaraPsiUtil.parameterValues(mogram, "exclude", 4);
		if (excludedPhases == null) return List.of();
		return excludedPhases.stream().map(LegioModelBuilder::code).collect(Collectors.toList());
	}

	@NotNull
	private static ExcludedPhases code(String e) {
		return e.contains("Language") ?
				ExcludedPhases.ExcludeLanguageGeneration :
				ExcludedPhases.ExcludeCodeBaseGeneration;
	}

	@Override
	public Configuration root() {
		return model.root();
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return model;
	}
}