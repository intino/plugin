package io.intino.plugin.project;

import com.intellij.openapi.diagnostic.Logger;
import io.intino.legio.Legio;
import io.intino.tara.io.Stash;
import io.intino.tara.io.StashSerializer;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.Store;
import io.intino.tara.magritte.stores.FileSystemStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class GraphLoader {
	private static final Logger LOG = Logger.getInstance("GraphLoader");

	static Legio loadGraph(Stash stash, File stashDestiny) {
		final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(GraphLoader.class.getClassLoader());
		try {
			final Graph graph = Graph.use(store(stash, stashDestiny), Legio.class).load();
			Thread.currentThread().setContextClassLoader(currentLoader);
			return graph.wrapper(Legio.class);
		} catch (Throwable e) {
			LOG.debug(e.getMessage());
			return null;
		}
	}

	private static Store store(Stash stash, File stashDestiny) {
		return new FileSystemStore(stashDestiny.getParentFile()) {
			@Override
			public Stash stashFrom(String path) {
				Stash result = super.stashFrom(path);
				return result != null ? result : path.equalsIgnoreCase("model.stash") ? stash : null;
			}

			@Override
			public void writeStash(Stash stash, String path) {
				try {
					Files.write(stashDestiny.toPath(), StashSerializer.serialize(stash));
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		};
	}
}
