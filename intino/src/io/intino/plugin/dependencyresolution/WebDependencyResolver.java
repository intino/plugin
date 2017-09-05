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
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Artifact.WebImports.Resolution;
import io.intino.legio.graph.Artifact.WebImports.WebArtifact;
import io.intino.legio.graph.Artifact.WebImports.WebComponent;
import io.intino.legio.graph.Repository;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.web.BowerTemplate;
import io.intino.plugin.dependencyresolution.web.Package_jsonTemplate;
import io.intino.plugin.dependencyresolution.web.PomTemplate;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.siani.itrules.model.Frame;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.legio.graph.Artifact.WebImports;
import static io.intino.plugin.MessageProvider.message;

public class WebDependencyResolver {

	private static final Logger LOG = Logger.getInstance(WebDependencyResolver.class.getName());
	private static final String LIB_DIRECTORY = "lib";
	private static final File nodeDirectory = new File(System.getProperty("user.home"), ".node" + File.separator + "node");

	private final Module module;
	private final Artifact artifact;
	private final List<Repository.Type> repositories;
	private final List<WebComponent> webComponents;
	private final List<WebArtifact> webArtifacts;
	private final List<Resolution> resolutions;
	private final File rootDirectory;
	private final File libComponentsDirectory;

	public WebDependencyResolver(Module module, Artifact artifact, List<Repository.Type> repositories, WebImports dependencies) {
		this.module = module;
		this.artifact = artifact;
		this.repositories = repositories;
		this.webComponents = dependencies.webComponentList();
		this.webArtifacts = dependencies.webArtifactList();
		this.resolutions = dependencies.resolutionList();
		this.rootDirectory = new File(module.getModuleFilePath()).getParentFile();
		this.libComponentsDirectory = new File(rootDirectory, LIB_DIRECTORY);
	}

	public void resolve() {
		File bower = createBowerFile();
		File bowerrc = createBowerrcFile();
		File pom = createPomFile();
		File packageJson = createPackageFile();
		resolveArtifacts();
		run(pom);
		VfsUtil.findFileByIoFile(rootDirectory, true);
		bower.delete();
		bowerrc.delete();
		packageJson.delete();
	}

	private void resolveArtifacts() {
		Aether aether = new Aether(collectRemotes(), new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
		for (WebArtifact artifact : webArtifacts) {
			if (isOverriding(artifact)) continue;
			final List<org.sonatype.aether.artifact.Artifact> artifacts = resolve(aether, artifact);
			if (!artifacts.isEmpty()) extractInLibDirectory(artifact, artifacts.get(0).getFile());
		}
	}

	private boolean isOverriding(WebArtifact artifact) {
		final File file = new File(libComponentsDirectory, artifact.artifactId() + File.separator + "bower.json");
		if (!file.exists()) return false;
		try {
			JsonObject element = new JsonParser().parse(new String(Files.readAllBytes(file.toPath()))).getAsJsonObject();
			return artifact.version().equals(element.get("version").getAsString());
		} catch (IOException e) {
			return false;
		}
	}

	private void extractInLibDirectory(WebArtifact artifact, File jarFile) {
		try {
			final File outputDir = new File(libComponentsDirectory, artifact.name$().toLowerCase());
			ZipUtil.extract(jarFile, outputDir, null);
			FileUtil.delete(new File(outputDir, "META-INF"));
			writeManifest(artifact, outputDir);
		} catch (IOException e) {
			LOG.error("Error extracting widgets", e);
		}
	}

	private void writeManifest(WebArtifact artifact, File outputDir) {
		final File file = new File(outputDir, "bower.json");
		final JsonObject jsonObject = new JsonObject();
		jsonObject.add("name", new JsonPrimitive(artifact.groupId() + "." + artifact.artifactId()));
		jsonObject.add("version", new JsonPrimitive(artifact.version()));
		write(new Gson().toJson(jsonObject), file);
	}

	private List<org.sonatype.aether.artifact.Artifact> resolve(Aether aether, WebArtifact artifact) {
		try {
			return aether.resolve(new DefaultArtifact(artifact.groupId().toLowerCase(), artifact.artifactId().toLowerCase(), "sources", "jar", artifact.version()), JavaScopes.COMPILE);
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
		StringBuilder result = new StringBuilder();
		for (String line : lines) if (line.contains("bower")) result.append(line).append("\n");
		return result.toString();
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
			final Frame dependency = new Frame().addSlot("name", webComponent.name$()).addSlot("version", webComponent.version());
			if (webComponent.url() != null && !webComponent.url().isEmpty())
				dependency.addSlot("url", webComponent.url());
			frame.addSlot("dependency", dependency);
		}
		for (Resolution resolution : resolutions)
			frame.addSlot("resolution", new Frame().addSlot("name", resolution.name()).addSlot("version", resolution.version()));
		return write(BowerTemplate.create().format(frame), new File(nodeDirectory, "bower.json"));
	}

	private File createBowerrcFile() {
		return write("{\"directory\": \"" + libComponentsDirectory.getAbsolutePath().replace("\\", "/") + "\"}", new File(nodeDirectory, ".bowerrc"));
	}

	private Frame fill(Frame frame) {
		return frame.addSlot("groupId", artifact.groupId()).addSlot("artifactId", artifact.name$()).addSlot("version", artifact.version());
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
		remotes.addAll(repositories.stream().map(r -> new RemoteRepository(r.name$(), "default", r.url())).collect(Collectors.toList()));
		return remotes;
	}

}
