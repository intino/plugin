package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import java.util.Collections;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.builder.BuildConstants.*;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.getOrDefault;

public class LegioOutputDslRuntime implements Configuration.Artifact.Dsl.Runtime {
	private final LegioOutputDsl outputDsl;
	private final TaraMogram mogram;

	public LegioOutputDslRuntime(LegioOutputDsl outputDsl, TaraMogram mogram) {
		this.outputDsl = outputDsl;
		this.mogram = mogram;
	}

	@Override
	public String groupId() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "groupId", 0), outputDsl.owner().attributes().getValue(normalizeForManifest(RUNTIME_GROUP_ID)));
	}

	@Override
	public String artifactId() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "artifactId", 1), outputDsl.owner().attributes().getValue(normalizeForManifest(RUNTIME_ARTIFACT_ID)));
	}

	@Override
	public String version() {
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "version", 2), outputDsl.owner().attributes().getValue(normalizeForManifest(RUNTIME_VERSION)));
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
	public Configuration root() {
		return outputDsl.root();
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return outputDsl;
	}
}
