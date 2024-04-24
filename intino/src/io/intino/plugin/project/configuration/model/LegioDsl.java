package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.TaraMogram;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.builder.BuildConstants.GENERATION_PACKAGE;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.componentOfType;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioDsl implements Configuration.Artifact.Dsl {
	private final LegioArtifact artifact;
	private final TaraMogram mogram;
	private String name;
	private String version;
	private String effectiveVersion;

	public LegioDsl(LegioArtifact artifact, TaraMogram mogram) {
		this.artifact = artifact;
		this.mogram = mogram;
	}

	@Override
	public String name() {
		if (name == null) {
			String value = parameterValue(mogram, "name", 0);
			return name = value != null ? value.toLowerCase() : null;
		}
		return name;
	}

	@Override
	public String version() {
		if (version == null) return version = parameterValue(mogram, "version", 1);
		return version;
	}

	@Override
	public String effectiveVersion() {
		if (this.effectiveVersion == null) return version();
		return this.effectiveVersion;
	}

	@Override
	public void effectiveVersion(String version) {
		this.effectiveVersion = version;
	}

	@Override
	public void version(String version) {
		if (mogram == null) return;
		writeCommandAction(mogram.getProject(), mogram.getContainingFile())
				.run(() -> mogram.parameters().stream().filter(p -> p.name().equals("version")).findFirst().
						ifPresent(p -> p.substituteValues(Collections.singletonList(version))));
	}

	@Override
	public String generationPackage() {
		Attributes attributes = attributes();
		return attributes == null ? null : attributes.getValue(GENERATION_PACKAGE.replace(".", "-"));
	}

	@Override
	public Runtime runtime() {
		return new LegioDslRuntime(this, attributes());
	}

	@Override
	public Builder builder() {
		return new LegioDslBuilder(this, (TaraMogram) componentOfType(mogram, "Builder"));
	}

	@Override
	public OutputDsl outputDsl() {
		TaraMogram outputDsl = (TaraMogram) componentOfType(mogram, "OutputDsl");
		return level().equals(Level.Model) ? null : new LegioOutputDsl(this, outputDsl);
	}

	@Override
	public Level level() {
		Attributes attributes = attributes();
		if (attributes == null) return Level.MetaModel;
		String level = ensureCompatibility(attributes.getValue("level"));
		return level == null ? Level.MetaModel : Level.values()[Level.valueOf(level).ordinal() - 1];
	}

	private String ensureCompatibility(String level) {
		if ("Product".equalsIgnoreCase(level)) return Level.MetaModel.name();
		if ("Platform".equalsIgnoreCase(level)) return Level.MetaMetaModel.name();
		if ("Solution".equalsIgnoreCase(level)) return Level.Model.name();
		return level;
	}

	public Attributes attributes() {
		String effectiveVersion = effectiveVersion();
		String name = name();
		if (name == null) return new Attributes();
		final File languageFile = LanguageManager.getLanguageFile(name, effectiveVersion == null || effectiveVersion.isEmpty() ? version() : effectiveVersion());
		if (!languageFile.exists()) return null;
		try (JarFile jarFile = new JarFile(languageFile)) {
			Manifest manifest = jarFile.getManifest();
			return manifest == null ? null : manifest.getAttributes("tara");
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	@NotNull
	public Configuration root() {
		return artifact.root();
	}

	@Override
	@NotNull
	public Configuration.ConfigurationNode owner() {
		return artifact;
	}

	public TaraMogram mogram() {
		return mogram;
	}
}