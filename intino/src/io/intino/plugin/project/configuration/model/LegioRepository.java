package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.project.LegioConfiguration;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public abstract class LegioRepository implements Configuration.Repository {
	private final LegioConfiguration configuration;
	private final Node node;

	public LegioRepository(LegioConfiguration configuration, Node node) {
		this.configuration = configuration;
		this.node = node;
	}

	@Override
	public String identifier() {
		return parameterValue(node.container(), "identifier", 0);
	}

	@Override
	public String url() {
		return parameterValue(node, "url", 1);
	}

	@Override
	public Configuration root() {
		return configuration;
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return null;
	}

	public Node getNode() {
		return node;
	}


	public static class LegioReleaseRepository extends LegioRepository implements Configuration.Repository.Release {
		public LegioReleaseRepository(LegioConfiguration configuration, TaraNode node) {
			super(configuration, node);
		}
	}

	public static class LegioSnapshotRepository extends LegioRepository implements Configuration.Repository.Snapshot {
		public LegioSnapshotRepository(LegioConfiguration configuration, TaraNode node) {
			super(configuration, node);
		}
	}

}
