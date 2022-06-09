package io.intino.plugin.build.linuxservice;

import com.intellij.openapi.roots.CompilerProjectExtension;
import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.intino.Configuration.Artifact.Package.Mode.ModulesAndLibrariesExtracted;
import static java.io.File.separator;

public class LinuxServiceGenerator {
	private final LegioConfiguration configuration;
	private final Configuration.Artifact.Package.LinuxService linuxService;
	private String artifactName;

	public LinuxServiceGenerator(LegioConfiguration configuration, Configuration.Artifact.Package.LinuxService linuxService) {
		this.configuration = configuration;
		this.linuxService = linuxService;
		this.artifactName = configuration.artifact().name();

	}

	public void generate() {
		final String compilerOutputUrl = CompilerProjectExtension.getInstance(configuration.module().getProject()).getCompilerOutputUrl();
		File dir = new File(String.join(separator, pathOf(compilerOutputUrl), "build", configuration.module().getName(), "linux-service"));
		dir.mkdirs();
		try {
			FrameBuilder builder = frame();
			Files.writeString(serviceFile(dir), new ServiceTemplate().render(builder.add("service").toFrame()));
			Files.writeString(sysconfigFile(dir), new ServiceTemplate().render(builder.add("sysconfig").toFrame()));
			Files.writeString(new File(dir, "README.txt").toPath(), readMe());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@NotNull
	private Path sysconfigFile(File dir) {
		return new File(dir, artifactName.toLowerCase()).toPath();
	}

	@NotNull
	private Path serviceFile(File dir) {
		return new File(dir, artifactName.toLowerCase() + ".service").toPath();
	}

	private String readMe() {
		String name = artifactName.toLowerCase();
		return "Always Check the generated files\n\n" +
				"Put " + name + ".service in '/etc/systemd/system/'. You need root access\n" +
				"Put " + name + " in '/etc/sysconfig/'. You need " + linuxService.user() + " access\n" +
				"Execute the command 'systemctl daemon-reload'. You need root access\n" +
				"Now you can start service with command 'service " + name + " start";

	}

	private FrameBuilder frame() {
		LegioArtifact artifact = configuration.artifact();
		FrameBuilder builder = new FrameBuilder()
				.add("artifact", artifact.name())
				.add("user", linuxService.user())
				.add("managementPort", linuxService.managementPort())
				.add("parameter", parameters())
				.add("mainClass", artifact.packageConfiguration().mainClass());
		if (linuxService.restartOnFailure()) builder.add("restart", "Restart");
		if (artifact.packageConfiguration().mode() != ModulesAndLibrariesExtracted)
			builder.add("directory", directoryFrame(artifact));
		return builder;
	}

	@NotNull
	private Frame directoryFrame(LegioArtifact artifact) {
		return new FrameBuilder("directory")
				.add("artifact", artifact.name())
				.add("directory", classPathPrefix(artifact))
				.toFrame();
	}

	private String classPathPrefix(LegioArtifact artifact) {
		return artifact.packageConfiguration().classpathPrefix() != null ? artifact.packageConfiguration().classpathPrefix() : "dependency";
	}

	private Frame[] parameters() {
		return linuxService.runConfiguration().finalArguments().entrySet().stream()
				.map(e -> new FrameBuilder("parameter").add("name", e.getKey()).add("value", e.getValue()).toFrame())
				.toArray(Frame[]::new);
	}

	private String pathOf(String path) {
		if (path.startsWith("file://")) return path.substring("file://".length());
		try {
			return new URL(path).getFile();
		} catch (MalformedURLException e) {
			return path;
		}
	}
}
