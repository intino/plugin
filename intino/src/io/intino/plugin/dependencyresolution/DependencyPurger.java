package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.module.Module;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.build.maven.PomCreator;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.IOException;

public class DependencyPurger {
	private final Module module;

	public DependencyPurger(Module module) {
		this.module = module;
	}

	public void execute() {
		try {
			File file = new PomCreator(module).frameworkPom();
			MavenRunner runner = new MavenRunner(module);
			runner.invokeMaven(file, "dependency:purge-local-repository");
		} catch (IOException | MavenInvocationException ignored) {
		}
	}
}
