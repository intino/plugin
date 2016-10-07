package org.siani.legio.plugin.project;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.vfs.VirtualFile;
import org.siani.legio.LegioApplication;
import tara.io.Stash;
import tara.magritte.Graph;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

class GraphLoader {
	private static final Logger LOG = Logger.getInstance("GraphLoader");

	static LegioApplication loadGraph(Module module, Stash stash) {
		final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
		VirtualFile compilerOutputPath = CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
		final ClassLoader temporalLoader = createClassLoader(compilerOutputPath);
		Thread.currentThread().setContextClassLoader(temporalLoader);
		final Graph graph = Graph.from(stash).wrap(LegioApplication.class);
		Thread.currentThread().setContextClassLoader(currentLoader);
		return graph.application();
	}

	private static ClassLoader createClassLoader(VirtualFile directory) {
		return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
			try {
				return directory == null ? new URLClassLoader(new URL[0]) : new URLClassLoader(new URL[]{new File(directory.getPath()).toURI().toURL()}, GraphLoader.class.getClassLoader());
			} catch (MalformedURLException e) {
				LOG.error(e.getMessage(), e);
				return null;
			}
		});
	}
}
