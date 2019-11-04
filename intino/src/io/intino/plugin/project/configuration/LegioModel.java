package io.intino.plugin.project.configuration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.legio.graph.Artifact;
import io.intino.tara.compiler.shared.Configuration;

public class LegioModel implements Configuration.Model {

	private final Module module;
	private final VirtualFile legioFile;
	private final Artifact.Level.Model model;

	public LegioModel(Module module, VirtualFile legioFile, Artifact.Level.Model model) {
		this.module = module;
		this.legioFile = legioFile;
		this.model = model;
	}

	@Override
	public LegioLanguage language() {
		return model != null ? new LegioLanguage(module, legioFile, model) : null;
	}

	@Override
	public String outLanguage() {
		return model != null ? model.outLanguage() : null;
	}

	@Override
	public String outLanguageVersion() {
		return model != null ? model.core$().ownerAs(Artifact.class).version() : null;
	}

	@Override
	public Level level() {
		if (model != null) {
			Artifact artifact = model.core$().ownerAs(Artifact.class);
			final String level = artifact.core$().conceptList().stream().filter(c -> c.id().contains("$")).map(c -> c.id().split("\\$")[1]).findFirst().orElse(null);
			return level == null ? null : Level.valueOf(level);
		} else return null;
	}

}
