package io.intino.plugin.project;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import io.intino.legio.Project;
import io.intino.legio.Project.WebDependencies.Resolution;
import io.intino.legio.Project.WebDependencies.WebComponent;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.webComponents.BowerTemplate;
import io.intino.plugin.dependencyresolution.webComponents.Package_jsonTemplate;
import io.intino.plugin.dependencyresolution.webComponents.PomTemplate;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.siani.itrules.model.Frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static io.intino.plugin.MessageProvider.message;

class WebDependencyResolver {

	private static final Logger LOG = Logger.getInstance(MavenRunner.class.getName());

	private final Module module;
	private final Project project;
	private final List<WebComponent> webComponents;
	private final List<Resolution> resolutions;
	private final File rootDirectory;
	private final File bowerComponentsDirectory;
	private File nodeDirectory;

	WebDependencyResolver(Module module, Project project, Project.WebDependencies dependencies) {
		this.module = module;
		this.project = project;
		this.webComponents = dependencies.webComponentList();
		this.resolutions = dependencies.resolutionList();
		this.rootDirectory = new File(module.getModuleFilePath()).getParentFile();
		this.nodeDirectory = new File(System.getProperty("user.home"), ".node" + File.separator + "node");
		this.bowerComponentsDirectory = new File(rootDirectory, "lib");
	}

	void resolve() {
		File bower = createBowerFile();
		File bowerrc = createBowerrcFile();
		File pom = createPomFile();
		File packageJson = createPackageFile();
		run(pom);
		VfsUtil.findFileByIoFile(rootDirectory, true);
		bower.delete();
		bowerrc.delete();
		packageJson.delete();
	}

	private void run(File pom) {
		try {
			final MavenRunner mavenRunner = new MavenRunner(module);
			final InvocationResult result = mavenRunner.invokeMaven(pom, "generate-resources");
			processResult(mavenRunner, pom, result);
		} catch (MavenInvocationException | IOException e) {
			notifyError(message("error.resolving.web.dependencies", e.getMessage()));
		} catch (IntinoException e) {
			notifyError(e.getMessage());
		}
	}

	private void notifyError(String message) {
		final NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Tara Language");
		if (balloon != null)
			balloon.createNotification(message, MessageType.ERROR).setImportant(true).notify(null);
	}

	private void processResult(MavenRunner mavenRunner, File pom, InvocationResult result) throws IntinoException {
		if (result != null && result.getExecutionException() != null && result.getExitCode() != 0)
			throw new IntinoException(message("error.resolving.web.dependencies", result.getExecutionException().getMessage()));
		else {
			FileUtil.delete(pom);
			if (result != null)
				throw new IntinoException(message("error.resolving.web.dependencies", mavenRunner.output()));
		}
	}

	private File createPackageFile() {
		File packageFile = new File(nodeDirectory, "package.json");
		packageFile.getParentFile().mkdirs();
		if (packageFile.exists()) return packageFile;
		write(Package_jsonTemplate.create().format(fill(new Frame().addTypes("package"))), packageFile);
		return packageFile;
	}

	private File createPomFile() {
		return write(PomTemplate.create().format(fill(new Frame().addTypes("pom"))), new File(rootDirectory, "pom.xml"));
	}

	private File createBowerFile() {
		final Frame frame = fill(new Frame().addTypes("bower"));
		for (WebComponent webComponent : webComponents) {
			final Frame dependency = new Frame().addSlot("name", webComponent.name()).addSlot("version", webComponent.version());
			if (webComponent.url() != null && !webComponent.url().isEmpty()) dependency.addSlot("url", webComponent.url());
			frame.addSlot("dependency", dependency);
		}
		for (Resolution resolution : resolutions)
			frame.addSlot("resolution", new Frame().addSlot("name", resolution.name()).addSlot("version", resolution.version()));
		return write(BowerTemplate.create().format(frame), new File(nodeDirectory, "bower.json"));
	}

	private File createBowerrcFile() {
		return write("{\"directory\": \"" + bowerComponentsDirectory.getAbsolutePath() + "\"}", new File(nodeDirectory, ".bowerrc"));

	}

	private Frame fill(Frame frame) {
		return frame.addSlot("groupId", project.groupId()).addSlot("artifactId", project.name()).addSlot("version", project.version());
	}

	private File write(String content, File destiny) {
		try {
			return Files.write(destiny.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		return destiny;
	}

}
