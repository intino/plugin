package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.build.maven.PomCreator;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.plugin.project.builders.ModelBuilderManager;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static io.intino.plugin.project.Safe.safe;

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
		} catch (MavenInvocationException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public void purgeDependency(String id) {
		File file = new File(localRepository(), id.replace(":", File.separator));
		if (!file.exists()) return;
		try {
			FileUtils.deleteDirectory(file);
		} catch (IOException e) {
			LOG.error(e);
		}
	}

	private void purgeDependencies() throws MavenInvocationException {
		if (!(TaraUtil.configurationOf(module) instanceof LegioConfiguration)) return;
		File file = new PomCreator(module).frameworkPom();
		runner.invokeMaven(file, "", "dependency:purge-local-repository");
		file.delete();
	}

	private void purgeModelBuilder() {
		new ModelBuilderManager(this.module.getProject(), safe(() -> configuration.graph().artifact().asLevel().model())).purge();
	}

	@NotNull
	private File localRepository() {
		return new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	}

	private void purgeInterfaceBuilder() {
		new InterfaceBuilderManager().purge(configuration.boxVersion());
	}
}
