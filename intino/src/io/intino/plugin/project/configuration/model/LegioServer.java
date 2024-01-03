package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

public class LegioServer implements Configuration.Server {
	private final TaraMogram node;

	public LegioServer(TaraMogram node) {
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
