package io.intino.plugin.build.maven;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.util.io.FileUtil;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.macos.artifact.MacOSPackage;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.io.FileUtil.copyDir;

public class BuildFixer {
	private static final Logger LOG = Logger.getInstance(BuildFixer.class);

	private final File buildDirectory;
	private final LegioConfiguration configuration;
	private final Module module;
	private final Artifact.Package build;

	BuildFixer(Module module) {
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		this.buildDirectory = new File(buildDirectory(), "build");
		this.build = configuration.artifact().package$();
	}

	void apply() {
		if (build.isMacOS()) {
			File appFile = appFile();
			if (appFile != null) {
				final MacOSPackage macos = build.asMacOS();
				if (macos.resourceDirectory() != null) copyResources(macos.resourceDirectory(), appFile);
			}
		}
	}

	private void copyResources(String resourceDirectory, File appDirectory) {
		final File resources = new File(moduleDirectory(), resourceDirectory);
		try {
			copyDir(resources, new File(appDirectory, resources.getName()));
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private File appFile() {
		final List<File> files = FileUtil.findFilesOrDirsByMask(Pattern.compile("[^\\.]*" + configuration.artifactId().toLowerCase() + "\\.app"), new File(buildDirectory, configuration.artifactId().toLowerCase()));
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