package io.intino.plugin.project.configuration.model.retrocompatibility;

import com.intellij.openapi.application.ApplicationManager;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;

import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioBox implements Configuration.Artifact.Dsl {
	private final LegioArtifact artifact;
	private final TaraMogram mogram;
	private String version;
	private String effectiveVersion;

	public LegioBox(LegioArtifact artifact, Mogram mogram) {
		this.artifact = artifact;
		this.mogram = (TaraMogram) mogram;
	}

	@Override
	public String groupId() {
		return "io.intino.konos";
	}

	@Override
	public String artifactId() {
		return "builder";
	}

	@Override
	public String name() {
		return parameterValue(mogram, "language", 0);
	}

	@Override
	public String version() {
		return version == null ? version = parameterValue(mogram, "version", 1) : version;
	}

	@Override
	public Level level() {
		return Level.Model;
	}

	@Override
	public String generationPackage() {
		return null;
	}

	@Override
	public String effectiveVersion() {
		if (this.effectiveVersion == null) return version();
		return this.effectiveVersion;
	}

	@Override
	public void effectiveVersion(String s) {
		this.effectiveVersion = version;
	}

	public void version(String newVersion) {
		writeCommandAction(mogram.getProject(), mogram.getContainingFile()).run(() -> {
			Parameter version = mogram.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(mogram.parameters().get(1));
			if (version != null) version.substituteValues(Collections.singletonList(newVersion));
		});
		ApplicationManager.getApplication().invokeAndWait(() -> IntinoUtil.commitDocument(mogram.getContainingFile()));
	}

	@Override
	public Builder builder() {
		try {
			Version changeVersion = new Version("12.0.0");
			Version version = new Version(LegioBox.this.version());
			return new Builder() {

				@Override
				public String generationPackage() {
					String targetPackage = parameterValue(mogram, "targetPackage");
					return targetPackage == null ? "" : targetPackage;
				}

				@Override
				public List<ExcludedPhases> excludedPhases() {
					return List.of();
				}

				@Override
				public String groupId() {
					return version.compareTo(changeVersion) >= 0 ? "io.intino" : "io.intino.konos";
				}

				@Override
				public String artifactId() {
					return version.compareTo(changeVersion) >= 0 ? "konos" : "builder";
				}

				@Override
				public String version() {
					return version.toString();
				}

				@Override
				public void version(String version) {

				}

				@Override
				public Configuration root() {
					return null;
				}

				@Override
				public Configuration.ConfigurationNode owner() {
					return null;
				}
			};
		} catch (IntinoException e) {
			return null;
		}
	}

	@Override
	public Runtime runtime() {
		return null;
	}

	@Override
	public OutputDsl outputDsl() {
		return null;
	}


	@Override
	public Configuration root() {
		return artifact.root();
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return artifact;
	}
}