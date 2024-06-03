package io.intino.plugin.project.configuration.model.retrocompatibility;

import com.intellij.openapi.application.ApplicationManager;
import io.intino.Configuration;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import io.intino.tara.language.model.Parameter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.builder.BuildConstants.GENERATION_PACKAGE;

public class LegioModel implements Configuration.Artifact.Dsl {
	private final LegioArtifact artifact;
	private final TaraMogram mogram;
	private String name;
	private String version;
	private String effectiveVersion;

	public LegioModel(LegioArtifact artifact, TaraMogram mogram) {
		this.artifact = artifact;
		this.mogram = mogram;
	}

	@Override
	public String groupId() {
		return LanguageManager.DSL_GROUP_ID;
	}

	@Override
	public String name() {
		return name == null ? name = TaraPsiUtil.parameterValue(mogram, "language", 0) : name;
	}

	@Override
	public String version() {
		if ("proteo".equalsIgnoreCase(name())) return version = "1.0.2";
		return version = realVersion();
	}

	String realVersion() {
		return TaraPsiUtil.parameterValue(mogram, "version", 1);
	}

	@Override
	public Level level() {
		Attributes parameters = parameters();
		if (parameters == null) return Level.MetaModel;
		String level = ensureCompatibility(parameters.getValue("level"));
		return level == null ? Level.MetaModel : Level.values()[Level.valueOf(level).ordinal() - 1];
	}

	@Override
	public String generationPackage() {
		Attributes attributes = parameters();
		return attributes == null ? null : attributes.getValue(GENERATION_PACKAGE.replace(".", "-"));
	}

	@Override
	public String effectiveVersion() {
		return this.effectiveVersion == null ? version() : this.effectiveVersion;
	}

	@Override
	public void effectiveVersion(String s) {
		this.effectiveVersion = version;
	}

	@Override
	public void version(String newVersion) {
		writeCommandAction(mogram.getProject(), mogram.getContainingFile()).run(() -> {
			Parameter version = mogram.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(mogram.parameters().get(1));
			if (version != null) version.substituteValues(Collections.singletonList(newVersion));
		});
		ApplicationManager.getApplication().invokeAndWait(() -> IntinoUtil.commitDocument(mogram.getContainingFile()));
	}

	@Override
	public Builder builder() {
		return new LegioModelBuilder(this, mogram);
	}

	@Override
	public Runtime runtime() {
		return new LegioModelRuntime(this, parameters());
	}

	@Override
	public OutputDsl outputDsl() {
		return level().equals(Level.Model) ? null :
				new ModelOutputDsl(artifact, mogram, parameters());
	}

	public Attributes parameters() {
		if (this.mogram == null) return null;
		if (isCoreLanguage()) return new Attributes();
		String effectiveVersion = effectiveVersion();
		final File languageFile = LanguageManager.getLanguageFile(name(), effectiveVersion == null || effectiveVersion.isEmpty() ? realVersion() : effectiveVersion());
		if (!languageFile.exists()) return null;
		try {
			Manifest manifest = new JarFile(languageFile).getManifest();
			return manifest == null ? null : manifest.getAttributes("tara");
		} catch (IOException e) {
			return null;
		}
	}

	private boolean isCoreLanguage() {
		return "Proteo".equalsIgnoreCase(name()) || "Meta".equalsIgnoreCase(name());
	}


	private String ensureCompatibility(String level) {
		if ("Product".equalsIgnoreCase(level)) return Level.MetaModel.name();
		if ("Platform".equalsIgnoreCase(level)) return Level.MetaMetaModel.name();
		if ("Solution".equalsIgnoreCase(level)) return Level.Model.name();
		return level;
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

	public TaraMogram node() {
		return mogram;
	}
}
