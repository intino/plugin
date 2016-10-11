package org.siani.legio.plugin.project;

import com.intellij.openapi.diagnostic.Logger;
import org.siani.legio.LegioApplication;
import tara.io.Stash;
import tara.magritte.Graph;

class GraphLoader {
	private static final Logger LOG = Logger.getInstance("GraphLoader");

	static LegioApplication loadGraph(Stash stash) {
		final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(GraphLoader.class.getClassLoader());
		final Graph graph = Graph.from(stash).wrap(LegioApplication.class);
		Thread.currentThread().setContextClassLoader(currentLoader);
		return graph.application();
	}
}
