package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dsl.OutputBuilder;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import static io.intino.plugin.lang.psi.impl.IntinoUtil.getOrDefault;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.componentOfType;

public class LegioOutputDsl implements Configuration.Artifact.Dsl.OutputDsl {
	private final LegioDsl dsl;
	private final TaraMogram mogram;
	private String name;

	public LegioOutputDsl(LegioDsl dsl, TaraMogram mogram) {
		this.dsl = dsl;
		this.mogram = mogram;
	}

	@Override
	public String name() {
		if (name == null)
			name = getOrDefault(TaraPsiUtil.parameterValue(mogram, "name", 0), ((Configuration.Artifact) dsl.owner()).name());
		return name;
	}

	@Override
	public String version() {
		return ((Configuration.Artifact) dsl.owner()).version();
	}

	@Override
	public OutputBuilder builder() {
		return new LegioOutputDslBuilder(this, (TaraMogram) componentOfType(mogram, "Builder"));
	}

	@Override
	public Configuration.Artifact.Dsl.Runtime runtime() {
		LegioOutputDslRuntime runtime = new LegioOutputDslRuntime(this, (TaraMogram) componentOfType(mogram, "Runtime"));
		return runtime.groupId() == null ? null : runtime;
	}

	@Override
	public Configuration root() {
		return dsl.root();
	}

	@Override
	public LegioDsl owner() {
		return dsl;
	}
}