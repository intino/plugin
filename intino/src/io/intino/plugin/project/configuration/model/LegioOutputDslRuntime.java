package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import java.util.Collections;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.builder.BuildConstants.*;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.getOrDefault;
import static io.intino.plugin.project.Safe.safe;

public class LegioOutputDslRuntime implements Configuration.Artifact.Dsl.Runtime {
	public static final String EMPTY = "empty";
	private final LegioOutputDsl outputDsl;
	private final TaraMogram mogram;

	public LegioOutputDslRuntime(LegioOutputDsl outputDsl, TaraMogram mogram) {
		this.outputDsl = outputDsl;
		this.mogram = mogram;
	}

	@Override
	public String groupId() {
		String groupId = getOrDefault(TaraPsiUtil.parameterValue(mogram, "groupId", 0), safe(() -> outputDsl.owner().attributes().getValue(normalizeForManifest(RUNTIME_GROUP_ID))));
		return groupId.equals(EMPTY) ? null : groupId;
	}

	@Override
	public String artifactId() {
		String artifactId = getOrDefault(TaraPsiUtil.parameterValue(mogram, "artifactId", 1), safe(() -> outputDsl.owner().attributes().getValue(normalizeForManifest(RUNTIME_ARTIFACT_ID))));
		return artifactId.equals(EMPTY) ? null : artifactId;
	}

	@Override
	public String version() {
		boolean versionFollower = mogram != null && mogram.appliedFacets().stream().anyMatch(a -> a.type().equals("ArtifactVersionFollower"));
		if (versionFollower) return root().artifact().version();
		return getOrDefault(TaraPsiUtil.parameterValue(mogram, "version", 2), safe(() -> outputDsl.owner().attributes().getValue(normalizeForManifest(RUNTIME_VERSION))));
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
