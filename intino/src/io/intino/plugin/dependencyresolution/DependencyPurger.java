package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.build.maven.PomCreator;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.plugin.project.builders.ModelBuilderManager;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.IOException;

public class DependencyPurger {
	private static final Logger LOG = Logger.getInstance(DependencyPurger.class.getName());

	private final Module module;
	private final MavenRunner runner;
	private LegioConfiguration configuration;

	public DependencyPurger(Module module, LegioConfiguration configuration) {
		this.module = module;
		this.runner = new MavenRunner(module);
		this.configuration = configuration;
	}

	public void execute() {
		try {
			purgeDependencies();
			purgeModelBuilder();
			purgeInterfaceBuilder();
		} catch (IOException | MavenInvocationException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void purgeDependencies() throws IOException, MavenInvocationException {
		File file = new PomCreator(module).frameworkPom();
		runner.invokeMaven(file, "dependency:purge-local-repository");
		file.delete();
	}

	private void purgeModelBuilder() {
		new ModelBuilderManager(this.module.getProject(), configuration.model()).purge();
	}

	private void purgeInterfaceBuilder() {
		new InterfaceBuilderManager().purge(configuration.boxVersion());
	}
}
