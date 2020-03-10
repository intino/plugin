package io.intino.plugin.dependencyresolution.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.io.ZipUtil;
import com.jcabi.aether.Aether;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Artifact.WebArtifact;
import io.intino.Configuration.Repository;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import org.apache.commons.lang.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.repository.Authentication;
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

public class PackageJsonCreator {
	private static final Logger logger = Logger.getInstance(PackageJsonCreator.class.getName());
	private final Module module;
	private final Artifact artifact;
	private final List<Repository> repositories;
	private final List<Artifact.WebComponent> webComponents;
	private final List<Artifact.WebResolution> resolutions;
	private final File nodeModulesDirectory;

	public PackageJsonCreator(Module module, Artifact artifact, List<Repository> repositories, File nodeModulesDirectory) {
		this.module = module;
		this.artifact = artifact;
		this.repositories = repositories.stream().filter(r -> !(r instanceof Repository.Language) && !isDistribution(r)).collect(Collectors.toList());
		this.webComponents = artifact.webComponents();
		this.resolutions = artifact.webResolutions();
		this.nodeModulesDirectory = nodeModulesDirectory;
	}

	private boolean isDistribution(Repository r) {
		return r instanceof Repository.Release ? artifact.distribution().release().url().equals(r.url()) : artifact.distribution().snapshot().url().equals(r.url());
	}

	public void createPackageFile(File rootDirectory) {
		write(new Package_jsonTemplate().render(packageFrame().toFrame()), new File(rootDirectory, "package.json"));
	}

	@NotNull
	private FrameBuilder packageFrame() {
		List<JsonObject> packages = resolveArtifacts();
		FrameBuilder builder = baseFrame().add("package");
		if (SystemUtils.IS_OS_MAC_OSX) builder.add("fsevents", "");
		Map<String, String> dependencies = collectDependencies(packages);
		dependencies.forEach((key, value) -> builder.add("dependency", new FrameBuilder().add("name", key).add("version", value)));
		resolutions.forEach(resolution -> builder.add("resolution", resolutionFrameFrom(resolution)));
		packages.stream().map(this::resolutionFrameFrom).filter(Objects::nonNull).forEach(frames -> builder.add("resolution", frames));
		return builder;
	}

	@NotNull
	private Map<String, String> collectDependencies(List<JsonObject> packages) {
		Map<String, String> dependencies = new LinkedHashMap<>();
		webComponents.forEach(c -> dependencies.putIfAbsent(c.name(), c.version()));
		packages.forEach(p -> dependenciesFrom(p).forEach(dependencies::putIfAbsent));
		return dependencies;
	}


	private List<JsonObject> resolveArtifacts() {
		List<JsonObject> manifests = new ArrayList<>();
		Aether aether = new Aether(collectRemotes(), new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
		for (WebArtifact artifact : artifact.webArtifacts()) {
			if (isOverriding(artifact)) continue;
			final List<org.sonatype.aether.artifact.Artifact> artifacts = resolve(aether, artifact);
			if (!artifacts.isEmpty()) {
				File packageJson = extractInNodeModulesDirectory(artifact, artifacts.get(0).getFile());
				if (packageJson == null) continue;
				JsonObject jsonObject = readPackageJson(packageJson);
				if (jsonObject == null) continue;
				jsonObject.addProperty("name", artifact.artifactId());
				manifests.add(jsonObject);
				write(jsonObject.toString(), packageJson);
			}
		}
		return manifests;
	}

	private boolean isOverriding(WebArtifact artifact) {
		final File file = new File(nodeModulesDirectory, artifact.artifactId() + File.separator + "package.json");
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
			final File outputDir = new File(nodeModulesDirectory, artifact.name().toLowerCase());
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
		return new FrameBuilder().add("groupId", artifact.groupId()).add("artifactId", artifact.name()).add("version", artifact.version());
	}

	private Frame[] resolutionFrameFrom(JsonObject object) {
		List<Frame> frames = new ArrayList<>();
		if (object.get("resolutions") == null) return null;
		final JsonObject dependencies = object.get("resolutions").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : dependencies.entrySet())
			frames.add(new FrameBuilder().add("name", entry.getKey()).add("version", entry.getValue().toString().replaceAll("\"", "")).toFrame());
		return frames.toArray(new Frame[0]);
	}

	private Frame resolutionFrameFrom(Artifact.WebResolution resolution) {
		return new FrameBuilder().add("name", resolution.url()).add("version", resolution.version()).toFrame();
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
		remotes.add(new RemoteRepository("maven-central", "default", ArtifactoryConnector.MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_DAILY)));
		remotes.addAll(repositories.stream().
				map(r -> new RemoteRepository(r.identifier(), "default", r.url()).
						setPolicy(r instanceof Repository.Snapshot, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(r instanceof Repository.Snapshot ? RepositoryPolicy.UPDATE_POLICY_ALWAYS : RepositoryPolicy.UPDATE_POLICY_DAILY)).
						setAuthentication(provideAuthentication(r.identifier()))).collect(Collectors.toList()));
		return remotes;
	}

	private Authentication provideAuthentication(String mavenId) {
		final IntinoSettings settings = IntinoSettings.getSafeInstance(module.getProject());
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(mavenId))
				return new Authentication(credential.username, credential.password);
		return null;
	}
}
