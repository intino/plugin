package io.intino.plugin.dependencyresolution;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Repository;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.web.PackageJsonCreator;
import io.intino.plugin.dependencyresolution.web.PomTemplate;
import org.apache.maven.shared.invoker.InvocationResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static io.intino.plugin.MessageProvider.message;
import static java.util.Objects.requireNonNull;

public class WebDependencyResolver {
	public static final String NodeModules = "node_modules";
	private static final Logger logger = Logger.getInstance(WebDependencyResolver.class.getName());
	private final Module module;
	private final Artifact artifact;

	private final List<Repository> repositories;
	private final File rootDirectory;
	private final File nodeModulesDirectory;

	public WebDependencyResolver(Module module, Artifact artifact, List<Repository> repositories) {
		this.module = module;
		this.artifact = artifact;
		this.repositories = repositories;
		this.rootDirectory = VfsUtil.virtualToIoFile(ProjectUtil.guessModuleDir(module));
		this.nodeModulesDirectory = new File(rootDirectory, NodeModules);
	}

	public void resolve() {
		File pom = createPomFile();
		File temp = createTempDirectory();
		Logger.getInstance(this.getClass()).info("Temporal web resolution directory: " + temp.getAbsolutePath());
		new PackageJsonCreator(module, artifact, repositories, temp).createPackageFile(rootDirectory);
		run(pom);
		Arrays.stream(requireNonNull(temp.listFiles(File::isDirectory))).forEach(fromDir -> {
			File toDir = new File(nodeModulesDirectory, fromDir.getName());
			toDir.mkdirs();
			FileUtil.delete(toDir);
			FileUtil.moveDirWithContent(fromDir, toDir);
		});
		new PackageJsonCreator(module, artifact, repositories, nodeModulesDirectory).extractArtifacts();
		VfsUtil.findFileByIoFile(rootDirectory, true);
	}

	private File createTempDirectory() {
		try {
			return Files.createTempDirectory(artifact.name()).toFile();
		} catch (IOException e) {
			return null;
		}
	}

	private void run(File pom) {
		try {
			final MavenRunner mavenRunner = new MavenRunner(module);
			final InvocationResult result = mavenRunner.invokeMavenWithConfigurationAndOptions(pom, nodeInstalled() ? "-Dskip.npm" : "", "generate-resources");
			processResult(mavenRunner, pom, result);
		} catch (IntinoException e) {
			notifyError(e.getMessage());
		}
	}

	private boolean nodeInstalled() {
		return new File(System.getProperty("user.home"), "/node/node").exists() || new File(System.getProperty("user.home"), "/node/node.exe").exists();
	}

	private void notifyError(String message) {
		NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Intino");
		if (balloon == null) balloon = NotificationGroupManager.getInstance().getNotificationGroup("Intino");
		List<String> lines = Arrays.asList(message.split("\n"));
		for (int i = 0; i <= lines.size() / 5; i++) {
			StringBuilder choppedMessage = new StringBuilder();
			for (int j = 0; j < 5; j++)
				if (lines.size() > j + 5 * i) choppedMessage.append(lines.get(j + 5 * i)).append("\n");
			if (choppedMessage.toString().trim().isEmpty()) choppedMessage = new StringBuilder("No content");
			balloon.createNotification(choppedMessage.toString(), MessageType.ERROR).setImportant(true).notify(this.module.getProject());
		}
	}

	private void processResult(MavenRunner mavenRunner, File pom, InvocationResult result) throws IntinoException {
		if (result != null && result.getExitCode() != 0 && result.getExecutionException() != null)
			throw new IntinoException(message("error.resolving.web.dependencies", result.getExecutionException().getMessage()));
		else if (result != null && result.getExitCode() != 0)
			throw new IntinoException(message("error.resolving.web.dependencies", mavenRunner.output()));
		else FileUtil.delete(pom);
	}

	private File createPomFile() {
		FrameBuilder builder = baseFrame().add("pom");
		if (!nodeInstalled()) builder.add("node", "node");
		return write(new PomTemplate().render(builder.toFrame()), new File(rootDirectory, "pom.xml"));
	}

	private FrameBuilder baseFrame() {
		return new FrameBuilder().add("groupId", artifact.groupId()).add("artifactId", artifact.name()).add("version", artifact.version());
	}

	private File write(String content, File destiny) {
		try {
			return Files.write(destiny.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return destiny;
	}
}
