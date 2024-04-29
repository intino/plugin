package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.builder.BuildConstants.*;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.getOrDefault;
import static io.intino.plugin.project.Safe.safe;

public class LegioDslBuilder implements Configuration.Artifact.Dsl.Builder {
	private final LegioDsl dsl;
	private final TaraMogram mogram;
	private final Attributes attributes;

	public LegioDslBuilder(LegioDsl dsl, TaraMogram mogram) {
		this.dsl = dsl;
		this.mogram = mogram;
		this.attributes = dsl.attributes();
	}

	@Override
	public String groupId() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "groupId", 0), safe(() -> attributes.getValue(normalizeForManifest(BUILDER_GROUP_ID))));
	}

	@Override
	public String artifactId() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "artifactId", 1), safe(() -> attributes.getValue(normalizeForManifest(BUILDER_ARTIFACT_ID))));
	}

	@Override
	public String version() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "version", 2), safe(() -> attributes.getValue(normalizeForManifest(BUILDER_VERSION))));
	}

	@Override
	public String generationPackage() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "generationPackage", 3), dsl.name());
	}


	@Override
	public List<ExcludedPhases> excludedPhases() {
		List<String> excludedPhases = TaraPsiUtil.parameterValues(mogram, "exclude", 4);
		return excludedPhases == null ? List.of() : excludedPhases.stream().map(ExcludedPhases::valueOf).toList();
	}

	@Override
	public void version(String version) {
		if (mogram == null) return;
		writeCommandAction(mogram.getProject(), mogram.getContainingFile()).run(() -> {
			mogram.parameters().stream().filter(p -> p.name().equals("version")).findFirst().
					ifPresent(p -> p.substituteValues(Collections.singletonList(version)));
		});
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return dsl;
	}

	@Override
	public Configuration root() {
		return dsl.root();
	}
}
