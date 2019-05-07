package io.intino.plugin.project;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.itrules.Formatter;
import io.intino.itrules.FrameBuilder;
import io.intino.legio.graph.Artifact;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.web.Package_jsonTemplate;
import io.intino.plugin.project.web.GulpPomTemplate;
import io.intino.plugin.project.web.GulpfileTemplate;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.jps.model.java.JavaResourceRootType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static io.intino.plugin.MessageProvider.message;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class GulpExecutor {
	private static final Logger LOG = Logger.getInstance(GulpExecutor.class.getName());
	private static Boolean lock = false;
	private final Module module;
	private final File rootDirectory;
	private final File nodeDirectory;
	private Artifact artifact;

	public GulpExecutor(Module module, Artifact artifact) {
		this.module = module;
		this.artifact = artifact;
		this.rootDirectory = new File(module.getModuleFilePath()).getParentFile();
		this.nodeDirectory = new File(System.getProperty("user.home"), "node");
	}

	public static void removeDeployBower(Module module) {
		final File destination = new File(new File(module.getModuleFilePath()).getParentFile(), "src" + File.separator + "widgets");
		new File(destination, "bower.json").delete();
	}

	synchronized void startGulpDev() {
//		while (lock) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				LOG.error(e.getMessage());
//			}
//		}
//		lock = true;

//		new Thread(() -> run(gulpPom, line -> {
//			LOG.info(line);
//			if (line.contains("[INFO]") && (line.contains("Finished 'dev'") || line.contains("BUILD SUCCESS"))) {
//				lock = false;
//				gulp.delete();
//				packageJson.delete();
//				gulpPom.delete();
//			}
//		}), "GULP").start();
		try {
			final File gulp = createGulp();
			final File gulpPom = createGulpPom("dev");
			if (gulpPom == null || gulp == null) return;
			final File packageJson = createPackageFile();
			Files.copy(gulp.toPath(), new File(rootDirectory, gulp.getName()).toPath(), REPLACE_EXISTING);
			Files.copy(packageJson.toPath(), new File(rootDirectory, packageJson.getName()).toPath(), REPLACE_EXISTING);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public void startGulpDeploy() {
		try {
			final File gulp = createGulp();
			final File packageJson = createPackageFile();
			final File gulpPom = createGulpPom("deploy");
			run(gulpPom, null);
			gulp.delete();
			packageJson.delete();
			gulpPom.delete();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private synchronized void run(File pom, InvocationOutputHandler handler) {
		try {
			final MavenRunner mavenRunner = new MavenRunner(module, handler);
			final InvocationResult result = mavenRunner.invokeMaven(pom, skipOptions(), "generate-resources");
			processResult(pom, result);
		} catch (MavenInvocationException | IntinoException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void processResult(File pom, InvocationResult result) throws IntinoException {
		if (result != null && result.getExecutionException() != null && result.getExitCode() != 0)
			throw new IntinoException(message("error.executing.gulp", result.getExecutionException().getMessage()));
		else FileUtil.delete(pom);
	}

	private File createGulp() throws IOException {
		final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
		if (extension == null || artifact == null) return null;
		final List<String> resourceDirectories = resourceDirectories();
		final FrameBuilder builder = new FrameBuilder("gulp").
				add("rootDirectory", rootDirectory.getCanonicalPath()).add("outDirectory", outDirectory(extension)).
				add("artifactID", artifact.name$()).add("port", new Random().nextInt(1000)).
				add("activity", module.getName().replace("-activity", ""));
		if (!resourceDirectories.isEmpty()) builder.add("resDirectory", resourceDirectories.get(0));
		else builder.add("resDirectory", new File(rootDirectory, "res").getCanonicalPath());
		return write(new File(nodeDirectory.getParent(), "gulpFile.js"), new GulpfileTemplate().add("path", pathFormatter()).render(builder.toFrame()));
	}

	private Formatter pathFormatter() {
		return value -> value.toString().replace("\\", "/");
	}

	private File createGulpPom(String task) {
		if (artifact == null) return null;
		return write(new File(nodeDirectory.getParent(), "pom.xml"), new GulpPomTemplate().add("path", pathFormatter()).render(new FrameBuilder("pom").
				add("groupID", artifact.groupId()).add("artifactID", artifact.name$()).
				add("version", artifact.version()).add("module", module.getName()).add("task", task).
				add("workingDirectory", rootDirectory.getPath()).toFrame()));
	}

	private File createPackageFile() {
		File packageFile = new File(nodeDirectory.getParent(), "package.json");
		packageFile.getParentFile().mkdirs();
		write(packageFile, new Package_jsonTemplate().render(new FrameBuilder("package").add("groupID", artifact.groupId()).add("artifactID", artifact.name$()).add("version", artifact.version()).toFrame()));
		return packageFile;
	}

	private String skipOptions() {
		return nodeInstalled() ? "-Dskip.npm" : "";
	}

	private boolean nodeInstalled() {
		return new File(System.getProperty("user.home"), ".node/node/node").exists();
	}

	private String outDirectory(CompilerModuleExtension extension) {
		try {
			String url = extension.getCompilerOutputUrl().replace("file://", "").replace("file:", "");
			return new File(url).getCanonicalPath();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return "";
		}
	}

	private List<String> resourceDirectories() {
		final ModuleRootManager manager = ModuleRootManager.getInstance(this.module);
		final List<VirtualFile> sourceRoots = manager.getSourceRoots(JavaResourceRootType.RESOURCE);
		return sourceRoots.stream().map(VirtualFile::getPath).collect(Collectors.toList());
	}

	private File write(File destination, String content) {
		try {
			destination.getParentFile().mkdirs();
			return Files.write(destination.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		return destination;
	}

}