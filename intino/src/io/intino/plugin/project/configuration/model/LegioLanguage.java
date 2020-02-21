package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.dsl.Meta;
import io.intino.tara.dsl.Proteo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.tara.compiler.shared.TaraBuildConstants.GENERATION_PACKAGE;

public class LegioLanguage implements Configuration.Artifact.Model.Language {
	private final LegioModel model;
	private String name;
	private String version;
	private String effectiveVersion;

	LegioLanguage(LegioModel model) {
		this.model = model;
	}

	@Override
	public String name() {
		return name == null ? name = TaraPsiUtil.parameterValue(model.node(), "language", 0) : name;
	}

	@Override
	public String version() {
		return version == null ? version = TaraPsiUtil.parameterValue(model.node(), "version", 1) : version;
	}

	@Override
	public String effectiveVersion() {
		return this.effectiveVersion == null ? version() : this.effectiveVersion;
	}

	@Override
	public void effectiveVersion(String version) {
		this.effectiveVersion = version;
	}

	@Override
	public void version(String version) {
		writeCommandAction(model.node().getProject(), model.node().getContainingFile()).run(() -> {
			if (model.node() == null) return;
			model.node().parameters().stream().filter(p -> p.name().equals("version")).findFirst().
					ifPresent(p -> p.substituteValues(Collections.singletonList(version)));
		});
	}


	@Override
	public String generationPackage() {
		Attributes attributes = parameters();
		return attributes == null ? null : attributes.getValue(GENERATION_PACKAGE.replace(".", "-"));
	}

	public Attributes parameters() {
		if (model == null) return null;
		if (isCoreLanguage()) return new Attributes();
		final File languageFile = LanguageManager.getLanguageFile(name(), effectiveVersion().isEmpty() ? version() : effectiveVersion());
		if (!languageFile.exists()) return null;
		try {
			Manifest manifest = new JarFile(languageFile).getManifest();
			return manifest == null ? null : manifest.getAttributes("tara");
		} catch (IOException e) {
			return null;
		}
	}

	private boolean isCoreLanguage() {
		return Proteo.class.getSimpleName().equalsIgnoreCase(name()) || Meta.class.getSimpleName().equalsIgnoreCase(name());
	}
}
