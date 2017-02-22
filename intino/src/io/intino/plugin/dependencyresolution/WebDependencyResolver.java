package io.intino.plugin.dependencyresolution;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.util.io.ZipUtil;
import com.jcabi.aether.Aether;
import io.intino.legio.Project;
import io.intino.legio.Project.WebDependencies.Resolution;
import io.intino.legio.Project.WebDependencies.WebActivity;
import io.intino.legio.Project.WebDependencies.WebComponent;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.web.BowerTemplate;
import io.intino.plugin.dependencyresolution.web.Package_jsonTemplate;
import io.intino.plugin.dependencyresolution.web.PomTemplate;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.siani.itrules.model.Frame;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.plugin.MessageProvider.message;

public class WebDependencyResolver {

	private static final Logger LOG = Logger.getInstance(WebDependencyResolver.class.getName());
	private static final String LIB_DIRECTORY = "lib";
	private static final File nodeDirectory = new File(System.getProperty("user.home"), ".node" + File.separator + "node");

	private final Module module;
	private final Project project;
	private final List<WebComponent> webComponents;
	private final List<WebActivity> webActivities;
	private final List<Resolution> resolutions;
	private final File rootDirectory;
	private final File libComponentsDirectory;

	public WebDependencyResolver(Module module, Project project, Project.WebDependencies dependencies) {
		this.module = module;
		this.project = project;
		this.webComponents = dependencies.webComponentList();
		this.webActivities = dependencies.webActivityList();
		this.resolutions = dependencies.resolutionList();
		this.rootDirectory = new File(module.getModuleFilePath()).getParentFile();
		this.libComponentsDirectory = new File(rootDirectory, LIB_DIRECTORY);
	}

	public void resolve() {
		File bower = createBowerFile();
		File bowerrc = createBowerrcFile();
		File pom = createPomFile();
		File packageJson = createPackageFile();
		resolveActivities();
		run(pom);
		VfsUtil.findFileByIoFile(rootDirectory, true);
		bower.delete();
		bowerrc.delete();
		packageJson.delete();
	}

	private void resolveActivities() {
		Aether aether = new Aether(collectRemotes(), new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
		for (WebActivity webActivity : webActivities) {
			if (isOverriding(webActivity)) continue;
			final List<Artifact> artifacts = resolve(aether, webActivity);
			if (!artifacts.isEmpty()) extractInLibDirectory(webActivity, artifacts.get(0).getFile());
		}
	}

	private boolean isOverriding(WebActivity activity) {
		final File file = new File(libComponentsDirectory, activity.artifactId() + File.separator + "bower.json");
		if (!file.exists()) return false;
		try {
			JsonObject element = new JsonParser().parse(new String(Files.readAllBytes(file.toPath()))).getAsJsonObject();
			return activity.version().equals(element.get("version").getAsString());
		} catch (IOException e) {
			return false;
		}
	}

	private void extractInLibDirectory(WebActivity activity, File jarFile) {
		try {
			final File outputDir = new File(libComponentsDirectory, activity.name().toLowerCase());
			ZipUtil.extract(jarFile, outputDir, null);
			FileUtil.delete(new File(outputDir, "META-INF"));
			writeManifest(activity, outputDir);
		} catch (IOException e) {
			LOG.error("Error extracting widgets", e);
		}
	}

	private void writeManifest(WebActivity activity, File outputDir) {
		final File file = new File(outputDir, "bower.json");
		final JsonObject jsonObject = new JsonObject();
		jsonObject.add("name", new JsonPrimitive(activity.groupId() + "." + activity.artifactId()));
		jsonObject.add("version", new JsonPrimitive(activity.version()));
		write(new Gson().toJson(jsonObject), file);
	}

	private List<Artifact> resolve(Aether aether, WebActivity webActivity) {
		try {
			return aether.resolve(new DefaultArtifact(webActivity.groupId().toLowerCase(), webActivity.artifactId().toLowerCase(), "sources", "jar", webActivity.version()), JavaScopes.COMPILE);
		} catch (DependencyResolutionException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
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
		NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Tara Language");
		if (balloon == null) balloon = NotificationGroup.balloonGroup("Tara Language");
		List<String> lines = Arrays.asList(message.split("\n"));
		for (int i = 0; i <= lines.size() / 5; i++) {
			String choppedMessage = "";
			for (int j = 0; j < 5; j++)
				if (lines.size() > j + 5 * i) choppedMessage += lines.get(j + 5 * i) + "\n";
			if (choppedMessage.trim().isEmpty()) choppedMessage = "No content";
			balloon.createNotification(choppedMessage, MessageType.ERROR).setImportant(true).notify(this.module.getProject());
		}
	}

	private void processResult(MavenRunner mavenRunner, File pom, InvocationResult result) throws IntinoException {
		if (result != null && result.getExitCode() != 0 && result.getExecutionException() != null)
			throw new IntinoException(message("error.resolving.web.dependencies", result.getExecutionException().getMessage()));
		else {
			FileUtil.delete(pom);
			if (result != null && result.getExitCode() != 0)
				throw new IntinoException(message("error.resolving.web.dependencies", filterBower(mavenRunner.output())));
		}
	}

	private String filterBower(String output) {
		String[] lines = output.split("\n");
		String result = "";
		for (String line : lines) if (line.contains("bower")) result += line + "\n";
		return result;
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
			if (webComponent.url() != null && !webComponent.url().isEmpty())
				dependency.addSlot("url", webComponent.url());
			frame.addSlot("dependency", dependency);
		}
		for (Resolution resolution : resolutions)
			frame.addSlot("resolution", new Frame().addSlot("name", resolution.name()).addSlot("version", resolution.version()));
		return write(BowerTemplate.create().format(frame), new File(nodeDirectory, "bower.json"));
	}

	private File createBowerrcFile() {
		return write("{\"directory\": \"" + libComponentsDirectory.getAbsolutePath() + "\"}", new File(nodeDirectory, ".bowerrc"));
	}

	private Frame fill(Frame frame) {
		return frame.addSlot("groupId", project.groupId()).addSlot("artifactId", project.name()).addSlot("version", project.version());
	}

	private File write(String content, File destiny) {
		try {
			return Files.write(destiny.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return destiny;
	}

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
		remotes.addAll(project.repositories().repositoryList().stream().map(remote -> new RemoteRepository(remote.name(), "default", remote.url())).collect(Collectors.toList()));
		return remotes;
	}

}
