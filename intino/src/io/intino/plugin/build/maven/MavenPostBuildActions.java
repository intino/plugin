package io.intino.plugin.build.maven;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerProjectExtension;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static io.intino.legio.graph.Artifact.Package.Mode;
import static io.intino.plugin.project.Safe.safe;
import static java.io.File.separator;

public class MavenPostBuildActions {

	private final Module module;
	private final LegioConfiguration configuration;
	private final Mode packageType;
	private final String compilerOutputUrl;
	private final String buildDirectory;

	public MavenPostBuildActions(Module module) {
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		this.packageType = safe(() -> configuration.graph().artifact().package$()) == null || configuration.graph().artifact() == null ? null : configuration.graph().artifact().package$().mode();
		this.compilerOutputUrl = pathOf(CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl());
		this.buildDirectory = this.buildDirectory();
	}


	public void execute() {
		try {
			File origin = new File(this.buildDirectory, "original-" + configuration.artifactId() + "-" + configuration.version() + ".jar");
			if (origin.exists()) FileUtils.forceDelete(origin);
			File sources = new File(this.buildDirectory, "generated-sources");
			if (sources.exists()) FileUtils.deleteDirectory(sources);
			sources = new File(this.buildDirectory, "maven-archiver");
			if (sources.exists()) FileUtils.deleteDirectory(sources);
			sources = new File(this.buildDirectory, "generated-test-sources");
			if (sources.exists()) FileUtils.deleteDirectory(sources);
			sources = new File(this.buildDirectory, "maven-status");
			if (sources.exists()) FileUtils.deleteDirectory(sources);
			sources = new File(this.buildDirectory, "surefire-reports");
			if (sources.exists()) FileUtils.deleteDirectory(sources);
		} catch (IOException e) {
			Logger.getInstance(MavenPostBuildActions.class.getName()).error(e.getMessage(), e);
		}
	}

	@NotNull
	private String buildDirectory() {
		return compilerOutputUrl + separator + "build" + separator + configuration.artifactId();
	}

	private String pathOf(String path) {
		try {
			return new URL(path).getFile();
		} catch (MalformedURLException e) {
			return path;
		}
	}
}
