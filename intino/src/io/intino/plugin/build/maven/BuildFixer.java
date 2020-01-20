package io.intino.plugin.build.maven;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.util.io.FileUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration.Artifact.Package.MacOs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.io.FileUtil.copyDir;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.tara.compiler.shared.Configuration.Artifact;

public class BuildFixer {
	private static final Logger LOG = Logger.getInstance(BuildFixer.class);

	private final File buildDirectory;
	private final LegioConfiguration configuration;
	private final Module module;
	private Artifact.Package build;

	BuildFixer(Module module) {
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		this.buildDirectory = new File(buildDirectory(), "build");
		if (configuration != null)
			this.build = safe(() -> configuration.artifact().packageConfiguration());
	}

	void apply() {
		if (build != null && build.macOsConfiguration() != null) {
			File appFile = appFile();
			if (appFile != null) {
				MacOs macos = build.macOsConfiguration();
				if (macos.resourceDirectory() != null && !macos.resourceDirectory().isEmpty())
					copyResources(macos.resourceDirectory(), appFile);
			}
		}
	}

	private void copyResources(String resourceDirectory, File appDirectory) {
		final File directory = new File(moduleDirectory(), resourceDirectory);
		if (!directory.exists()) return;
		try {
			copyDir(directory, new File(appDirectory, directory.getName()));
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private File appFile() {
		String artifactName = configuration.artifact().name();
		final List<File> files = FileUtil.findFilesOrDirsByMask(Pattern.compile("[^.]*" + artifactName.toLowerCase() + "\\.app"), new File(buildDirectory, artifactName.toLowerCase()));
		return files.isEmpty() ? null : files.get(0);
	}

	private String moduleDirectory() {
		return new File(module.getModuleFilePath()).getParent();
	}

	private String buildDirectory() {
		return pathOf(CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl());
	}

	private String pathOf(String path) {
		try {
			return new URL(path).getFile();
		} catch (MalformedURLException e) {
			return path;
		}
	}
}
