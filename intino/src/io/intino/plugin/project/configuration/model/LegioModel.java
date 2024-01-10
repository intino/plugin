package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;

public class LegioModel implements Configuration.Artifact.Model {
	private final LegioArtifact artifact;
	private final TaraMogram mogram;
	private LegioLanguage language;

	public LegioModel(LegioArtifact artifact, TaraMogram mogram) {
		this.artifact = artifact;
		this.mogram = mogram;
	}

	@Override
	@NotNull
	public LegioLanguage language() {
		return language == null ? language = new LegioLanguage(this) : language;
	}

	@Override
	public String outLanguage() {
		if (mogram == null) return null;
		String outLanguage = TaraPsiUtil.parameterValue(mogram, "outLanguage");
		return outLanguage == null ? artifact.name() : outLanguage;
	}

	@Override
	public String outLanguageVersion() {
		return artifact.version();
	}

	public String sdkVersion() {
		return TaraPsiUtil.parameterValue(mogram, "sdkVersion", 2);
	}

	public void sdkVersion(String version) {
		writeCommandAction(node().getProject(), node().getContainingFile()).run(() -> {
			if (node() == null) return;
			node().parameters().stream().filter(p -> p.name().equals("sdkVersion")).findFirst().
					ifPresent(p -> p.substituteValues(Collections.singletonList(version)));
		});
	}

	@Override
	public String sdk() {
		String sdk = TaraPsiUtil.parameterValue(mogram, "sdk", 3);
		return sdk != null ? sdk : "io.intino.magritte:builder";
	}

	@Override
	public List<ExcludedPhases> excludedPhases() {
		List<String> excludedPhases = TaraPsiUtil.parameterValues(mogram, "exclude", 4);
		return excludedPhases == null ? List.of() : excludedPhases.stream().map(ExcludedPhases::valueOf).toList();
	}

	@Override
	public Level level() {
		Attributes parameters = language().parameters();
		if (parameters == null) return null;
		String level = ensureCompatibility(parameters.getValue("level"));
		return level == null ? Level.MetaModel : Level.values()[Level.valueOf(level).ordinal() - 1];
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
