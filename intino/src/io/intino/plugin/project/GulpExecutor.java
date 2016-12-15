package io.intino.plugin.project;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.legio.Project;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.web.Package_jsonTemplate;
import io.intino.plugin.project.web.GulpPomTemplate;
import io.intino.plugin.project.web.GulpfileTemplate;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.siani.itrules.model.Frame;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static io.intino.plugin.MessageProvider.message;

public class GulpExecutor {
	private static final Logger LOG = Logger.getInstance(GulpExecutor.class.getName());

	private Project project;
	private final Module module;
	private final File rootDirectory;
	private final File nodeDirectory;

	public GulpExecutor(Module module, Project legioProject) {
		this.module = module;
		this.project = legioProject;
		this.rootDirectory = new File(module.getModuleFilePath()).getParentFile();
		this.nodeDirectory = new File(System.getProperty("user.home"), ".node" + File.separator + "node");
	}

	void startGulpDev() {
		final File gulp = createGulp();
		final File gulpPom = createGulpPom("dev");
		final File packageJson = createPackageFile();
		new Thread(() -> run(gulpPom)).start();
//		gulp.delete();
//		gulpPom.delete();
	}

	public void startGulpDeploy() {
		final File gulp = createGulp();
		final File packageJson = createPackageFile();
		final File gulpPom = createGulpPom("deploy");
		run(gulpPom);
//		gulp.delete();
//		gulpPom.delete();
	}


	private void run(File pom) {
		try {
			final MavenRunner mavenRunner = new MavenRunner(module);
			final InvocationResult result = mavenRunner.invokeMaven(pom, "generate-resources");
			processResult(mavenRunner, pom, result);
		} catch (MavenInvocationException | IOException e) {
			notifyError(message("error.executing.gulp", e.getMessage()));
		} catch (IntinoException e) {
			notifyError(e.getMessage());
		}
	}

	private void processResult(MavenRunner mavenRunner, File pom, InvocationResult result) throws IntinoException {
		if (result != null && result.getExecutionException() != null && result.getExitCode() != 0)
			throw new IntinoException(message("error.executing.gulp", result.getExecutionException().getMessage()));
		else {
			FileUtil.delete(pom);
			if (result != null)
				throw new IntinoException(message("error.executing.gulp", mavenRunner.output()));
		}
	}

	private void notifyError(String message) {
		final NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Tara Language");
		if (balloon != null)
			balloon.createNotification(message, MessageType.ERROR).setImportant(true).notify(null);
	}

	private File createGulp() {
		final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
		if (compilerModuleExtension == null) return null;
		final List<String> resourceDirectories = resourceDirectories();
		return write(new File(nodeDirectory, "gulpFile.js"), GulpfileTemplate.create().format(new Frame().addTypes("gulp").
				addSlot("rootDirectory", rootDirectory.getPath()).addSlot("outDirectory", outDirectory(compilerModuleExtension)).addSlot("resDirectory", resourceDirectories.get(0)).
				addSlot("artifactID", project.name()).addSlot("port", new Random().nextInt(1000))));
	}

	private File createGulpPom(String task) {
		return write(new File(rootDirectory, "pom.xml"), GulpPomTemplate.create().format(new Frame().addTypes("pom").
				addSlot("groupID", project.groupId()).addSlot("artifactID", project.name()).addSlot("version", project.version()).addSlot("task", task)));
	}

	private File createPackageFile() {
		File packageFile = new File(nodeDirectory, "package.json");
		packageFile.getParentFile().mkdirs();
		if (packageFile.exists()) return packageFile;
		write(packageFile, Package_jsonTemplate.create().format(new Frame().addTypes("package").addSlot("groupID", project.groupId()).addSlot("artifactID", project.name()).addSlot("version", project.version())));
		return packageFile;
	}

	private String outDirectory(CompilerModuleExtension compilerModuleExtension) {
		try {
			return new URL(compilerModuleExtension.getCompilerOutputUrl()).getFile();
		} catch (MalformedURLException e) {
			return "";
		}
	}

	private List<String> resourceDirectories() {
		final ModuleRootManager manager = ModuleRootManager.getInstance(this.module);
		final List<VirtualFile> sourceRoots = manager.getSourceRoots(JavaResourceRootType.RESOURCE);
		return sourceRoots.stream().map(VirtualFile::getPath).collect(Collectors.toList());
	}


	private File write(File destiny, String content) {
		try {
			return Files.write(destiny.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		return destiny;
	}
}