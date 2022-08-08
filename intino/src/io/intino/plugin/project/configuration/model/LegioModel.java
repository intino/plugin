package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.magritte.lang.model.Aspect;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.project.Safe.safeList;

public class LegioModel implements Configuration.Artifact.Model {
	private final LegioArtifact artifact;
	private final TaraNode node;
	private LegioLanguage language;

	public LegioModel(LegioArtifact artifact, TaraNode node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	@NotNull
	public LegioLanguage language() {
		return language == null ? language = new LegioLanguage(this) : language;
	}

	@Override
	public String outLanguage() {
		if (node == null) return null;
		String outLanguage = TaraPsiUtil.parameterValue(node, "outLanguage");
		return outLanguage == null ? artifact.name() : outLanguage;
	}

	@Override
	public String outLanguageVersion() {
		return artifact.version();
	}

	public String sdkVersion() {
		return TaraPsiUtil.parameterValue(node, "sdkVersion", 2);
	}

	@Override
	public String sdk() {
		String sdk = TaraPsiUtil.parameterValue(node, "sdk", 3);
		return sdk != null ? sdk : "io.intino.magritte:builder";
	}

	@Override
	public List<ExcludedPhases> excludedPhases() {
		List<String> excludedPhases = TaraPsiUtil.parameterValues(node, "excludedPhases");
		return excludedPhases.stream().map(ExcludedPhases::valueOf).collect(Collectors.toList());
	}

	@Override
	public Level level() {
		List<Aspect> safe = safeList(() -> artifact.node().appliedAspects());
		if (safe.isEmpty()) return null;
		return Level.valueOf(safe.get(0).type());
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

	public TaraNode node() {
		return node;
	}
}
