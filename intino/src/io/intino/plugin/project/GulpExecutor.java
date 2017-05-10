package io.intino.plugin.project;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.legio.Artifact;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.web.Package_jsonTemplate;
import io.intino.plugin.project.web.GulpPomTemplate;
import io.intino.plugin.project.web.GulpfileTemplate;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.siani.itrules.Formatter;
import org.siani.itrules.model.Frame;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static io.intino.plugin.MessageProvider.message;

public class GulpExecutor {
	private static final Logger LOG = Logger.getInstance(GulpExecutor.class.getName());
	private static Boolean lock = false;

	private Artifact artifact;
	private final Module module;
	private final File rootDirectory;
	private final File nodeDirectory;

	public GulpExecutor(Module module, Artifact artifact) {
		this.module = module;
		this.artifact = artifact;
		this.rootDirectory = new File(module.getModuleFilePath()).getParentFile();
		this.nodeDirectory = new File(System.getProperty("user.home"), ".node" + File.separator + "node");
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
			Files.copy(gulp.toPath(), new File(rootDirectory, gulp.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(packageJson.toPath(), new File(rootDirectory, packageJson.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
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
			final InvocationResult result = mavenRunner.invokeMaven(pom, "generate-resources");
			processResult(pom, result);
		} catch (MavenInvocationException | IOException | IntinoException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void processResult(File pom, InvocationResult result) throws IntinoException {
		if (result != null && result.getExecutionException() != null && result.getExitCode() != 0)
			throw new IntinoException(message("error.executing.gulp", result.getExecutionException().getMessage()));
		else FileUtil.delete(pom);
	}


	private File createGulp() throws IOException {
		final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
		if (compilerModuleExtension == null || artifact == null) return null;
		final List<String> resourceDirectories = resourceDirectories();
		final Frame frame = new Frame().addTypes("gulp").
				addSlot("rootDirectory", rootDirectory.getCanonicalPath()).addSlot("outDirectory", outDirectory(compilerModuleExtension)).
				addSlot("artifactID", artifact.name()).addSlot("port", new Random().nextInt(1000));
		if (!resourceDirectories.isEmpty()) frame.addSlot("resDirectory", resourceDirectories.get(0));
		else frame.addSlot("resDirectory", new File(rootDirectory, "res").getCanonicalPath());
		return write(new File(nodeDirectory, "gulpFile.js"), GulpfileTemplate.create().add("path", pathFormatter()).format(frame));
	}

	private Formatter pathFormatter() {
		return value -> value.toString().replace("\\", "/");
	}

	private File createGulpPom(String task) {
		if (artifact == null) return null;
		return write(new File(nodeDirectory, "pom.xml"), GulpPomTemplate.create().format(new Frame().addTypes("pom").
				addSlot("groupID", artifact.groupId()).addSlot("artifactID", artifact.name()).
				addSlot("version", artifact.version()).addSlot("module", module.getName()).addSlot("task", task)));
	}

	private File createPackageFile() {
		File packageFile = new File(nodeDirectory, "package.json");
		packageFile.getParentFile().mkdirs();
		if (packageFile.exists()) return packageFile;
		write(packageFile, Package_jsonTemplate.create().format(new Frame().addTypes("package").addSlot("groupID", artifact.groupId()).addSlot("artifactID", artifact.name()).addSlot("version", artifact.version())));
		return packageFile;
	}

	private String outDirectory(CompilerModuleExtension compilerModuleExtension) {
		try {
			return new File(new URL(compilerModuleExtension.getCompilerOutputUrl()).getFile()).getCanonicalPath();
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