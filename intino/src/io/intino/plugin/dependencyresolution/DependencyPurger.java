package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.build.maven.PomCreator;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.IOException;

public class DependencyPurger {
	private static final Logger LOG = Logger.getInstance(DependencyPurger.class.getName());

	private final Module module;
	private final MavenRunner runner;

	public DependencyPurger(Module module) {
		this.module = module;
		this.runner = new MavenRunner(module);
	}

	public void execute() {
		try {
			File file = new PomCreator(module).frameworkPom();
			runner.invokeMaven(file, "dependency:purge-local-repository");
			file.delete();
		} catch (IOException | MavenInvocationException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
