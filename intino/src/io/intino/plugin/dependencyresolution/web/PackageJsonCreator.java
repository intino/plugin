package io.intino.plugin.dependencyresolution.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.io.ZipUtil;
import com.jcabi.aether.Aether;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Artifact.WebImports.WebArtifact;
import io.intino.legio.graph.Repository;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class PackageJsonCreator {
	private static final Logger logger = Logger.getInstance(PackageJsonCreator.class.getName());
	private final Artifact artifact;
	private final List<Repository.Type> repositories;
	private final List<Artifact.WebImports.WebComponent> webComponents;
	private final List<Artifact.WebImports.Resolution> resolutions;
	private final File nodeModulesDirectory;

	public PackageJsonCreator(Artifact artifact, List<Repository.Type> repositories, File nodeModulesDirectory) {
		this.artifact = artifact;
		this.repositories = repositories;
		this.webComponents = artifact.webImports() == null ? emptyList() : artifact.webImports().webComponentList();
		this.resolutions = artifact.webImports() == null ? emptyList() : artifact.webImports().resolutionList();
		this.nodeModulesDirectory = nodeModulesDirectory;
	}

	public void createPackageFile(File rootDirectory) {
		write(new Package_jsonTemplate().render(packageFrame().toFrame()), new File(rootDirectory, "package.json"));
	}

	@NotNull
	private FrameBuilder packageFrame() {
		List<JsonObject> packages = resolveArtifacts();
		FrameBuilder builder = baseFrame().add("package");
		Map<String, String> dependencies = collectDependencies(packages);
		dependencies.forEach((key, value) -> builder.add("dependency", new FrameBuilder().add("name", key).add("version", value)));
		resolutions.forEach(resolution -> builder.add("resolution", resolutionFrameFrom(resolution)));
		packages.stream().map(this::resolutionFrameFrom).filter(Objects::nonNull).forEach(frames -> builder.add("resolution", frames));
		return builder;
	}

	@NotNull
	private Map<String, String> collectDependencies(List<JsonObject> packages) {
		Map<String, String> dependencies = new LinkedHashMap<>();
		webComponents.forEach(c -> dependencies.putIfAbsent(c.url(), c.version()));
		packages.forEach(p -> dependenciesFrom(p).forEach(dependencies::putIfAbsent));
		return dependencies;
	}


	private List<JsonObject> resolveArtifacts() {
		List<JsonObject> manifests = new ArrayList<>();
		Aether aether = new Aether(collectRemotes(), new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
		for (WebArtifact artifact : artifact.webImports().webArtifactList()) {
			if (isOverriding(artifact)) continue;
			final List<org.sonatype.aether.artifact.Artifact> artifacts = resolve(aether, artifact);
			if (!artifacts.isEmpty()) {
				File packageJson = extractInNodeModulesDirectory(artifact, artifacts.get(0).getFile());
				if (packageJson == null) continue;
				JsonObject jsonObject = readPackageJson(packageJson);
				if (jsonObject == null) continue;
				jsonObject.addProperty("name", artifact.name$());
				manifests.add(jsonObject);
				write(jsonObject.toString(), packageJson);
			}
		}
		return manifests;
	}

	private boolean isOverriding(WebArtifact artifact) {
		final File file = new File(nodeModulesDirectory, artifact.name$() + File.separator + "package.json");
		if (!file.exists()) return false;
		try {
			JsonObject element = new JsonParser().parse(new String(Files.readAllBytes(file.toPath()))).getAsJsonObject();
			return artifact.version().equals(element.get("version").getAsString());
		} catch (IOException e) {
			return false;
		}
	}

	private File extractInNodeModulesDirectory(WebArtifact artifact, File jarFile) {
		try {
			final File outputDir = new File(nodeModulesDirectory, artifact.name$().toLowerCase());
			ZipUtil.extract(jarFile, outputDir, null);
			FileUtil.delete(new File(outputDir, "META-INF"));
			return new File(outputDir, "package.json");
		} catch (IOException e) {
			logger.error("Error extracting widgets", e);
			return null;
		}
	}

	private List<org.sonatype.aether.artifact.Artifact> resolve(Aether aether, WebArtifact artifact) {
		try {
			return aether.resolve(new DefaultArtifact(artifact.groupId().toLowerCase(), artifact.artifactId().toLowerCase(), "sources", "jar", artifact.version()), JavaScopes.COMPILE);
		} catch (DependencyResolutionException e) {
			logger.warn("Error resolving widgets", e);
		}
		return Collections.emptyList();
	}


	private JsonObject readPackageJson(File file) {
		try {
			return new JsonParser().parse(new String(Files.readAllBytes(file.toPath()))).getAsJsonObject();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private FrameBuilder baseFrame() {
		return new FrameBuilder().add("groupId", artifact.groupId()).add("artifactId", artifact.name$()).add("version", artifact.version());
	}

	private Frame[] resolutionFrameFrom(JsonObject object) {
		List<Frame> frames = new ArrayList<>();
		if (object.get("resolutions") == null) return null;
		final JsonObject dependencies = object.get("resolutions").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : dependencies.entrySet())
			frames.add(new FrameBuilder().add("name", entry.getKey()).add("version", entry.getValue().toString().replaceAll("\"", "")).toFrame());
		return frames.toArray(new Frame[0]);
	}

	private Frame resolutionFrameFrom(Artifact.WebImports.Resolution resolution) {
		return new FrameBuilder().add("name", resolution.name()).add("version", resolution.version()).toFrame();
	}


	private Map<String, String> dependenciesFrom(JsonObject object) {
		Map<String, String> map = new HashMap<>();
		if (object.get("dependencies") == null) return map;
		final JsonObject dependencies = object.get("dependencies").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : dependencies.entrySet())
			map.put(entry.getKey(), entry.getValue().getAsString());
		return map;
	}

	private void write(String content, File destiny) {
		try {
			Files.write(destiny.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/").setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_DAILY)));
		remotes.addAll(repositories.stream().map(r -> new RemoteRepository(r.name$(), "default", r.url()).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_DAILY))).collect(Collectors.toList()));
		return remotes;
	}

}