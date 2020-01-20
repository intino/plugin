package io.intino.plugin.project.configuration.model;

import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;

public class LegioServer implements Configuration.Server {
	private final LegioConfiguration configuration;
	private final TaraNode node;

	public LegioServer(LegioConfiguration configuration, TaraNode node) {
		this.configuration = configuration;
		this.node = node;
	}

	@Override
	public String name() {
		return node.name();
	}

	@Override
	public Type type() {
		return Type.valueOf(TaraPsiUtil.parameterValue(node, "type", 0));
	}
}
