package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dsl.OutputBuilder;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import java.util.Collections;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.builder.BuildConstants.*;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.getOrDefault;
import static io.intino.plugin.project.Safe.safe;

public class LegioOutputDslBuilder implements OutputBuilder {
	private final LegioOutputDsl outputDsl;
	private final TaraMogram mogram;

	public LegioOutputDslBuilder(LegioOutputDsl outputDsl, TaraMogram mogram) {
		this.outputDsl = outputDsl;
		this.mogram = mogram;
	}

	@Override
	public String groupId() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "groupId", 0), safe(() -> outputDsl.owner().attributes().getValue(normalizeForManifest(BUILDER_GROUP_ID))));
	}

	@Override
	public String artifactId() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "artifactId", 1), safe(() -> outputDsl.owner().attributes().getValue(normalizeForManifest(BUILDER_ARTIFACT_ID))));
	}

	@Override
	public String version() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "version", 2), safe(() -> outputDsl.owner().attributes().getValue(normalizeForManifest(BUILDER_VERSION))));
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
		return outputDsl.owner();
	}

	@Override
	public Configuration root() {
		return outputDsl.root();
	}
}
