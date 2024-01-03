package io.intino.plugin.dependencyresolution.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.io.ZipUtil;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Artifact.WebArtifact;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.intino.plugin.dependencyresolution.Repositories.INTINO_RELEASES;

public class WebArtifactResolver {
	private static final Logger logger = Logger.getInstance(PackageJsonCreator.class.getName());
	private final Artifact artifact;
	private final List<Repository> repositories;
	private final File destination;
	private final Module module;
	private final MavenDependencyResolver resolver;

	public WebArtifactResolver(Module module, Artifact artifact, List<Repository> repositories, File destination) {
		this.module = module;
		this.artifact = artifact;
		this.repositories = repositories;
		this.destination = destination;
		this.resolver = new MavenDependencyResolver(repos());
	}

	public List<JsonObject> resolveArtifacts() {
		List<JsonObject> manifests = new ArrayList<>();
		for (WebArtifact artifact : artifact.webArtifacts()) {
			if (isOverriding(artifact)) continue;
			var artifacts = resolve(artifact);
			if (!artifacts.isEmpty()) {
				File packageJson = extract(artifact, artifacts.get(0).getArtifact().getFile());
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

	public void extractArtifacts() {
		for (WebArtifact artifact : artifact.webArtifacts()) {
			if (isOverriding(artifact)) continue;
			var artifacts = resolve(artifact);
			if (!artifacts.isEmpty()) {
				File packageJson = extract(artifact, artifacts.get(0).getArtifact().getFile());
				if (packageJson == null) continue;
				JsonObject jsonObject = readPackageJson(packageJson);
				if (jsonObject == null) continue;
				jsonObject.addProperty("name", artifact.artifactId());
				write(jsonObject.toString(), packageJson);
			}
		}
	}


	private boolean isOverriding(WebArtifact artifact) {
		final File file = new File(destination, artifact.artifactId() + File.separator + "package.json");
		if (!file.exists()) return false;
		try {
			JsonObject element = readJson(file);
			return artifact.version().equals(element.get("version").getAsString());
		} catch (IOException e) {
			return false;
		}
	}

	private File extract(WebArtifact artifact, File jarFile) {
		try {
			final File outputDir = new File(destination, artifact.name().toLowerCase());
			ZipUtil.extract(jarFile.toPath(), outputDir.toPath(), null);
			FileUtil.delete(new File(outputDir, "META-INF"));
			return new File(outputDir, "package.json");
		} catch (IOException e) {
			logger.error("Error extracting widgets", e);
			return null;
		}
	}

	private List<Dependency> resolve(WebArtifact web) {
		try {
			DefaultArtifact artifact = new DefaultArtifact(web.groupId().toLowerCase(), web.artifactId().toLowerCase(), "sources", "jar", web.version());
			return MavenDependencyResolver.dependenciesFrom(resolver.resolve(artifact, JavaScopes.COMPILE), false);
		} catch (DependencyResolutionException e) {
			logger.warn("Error resolving widgets", e);
			return Collections.emptyList();
		}
	}


	private JsonObject readPackageJson(File file) {
		try {
			return readJson(file);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private JsonObject readJson(File file) throws IOException {
		return new Gson().fromJson(new String(Files.readAllBytes(file.toPath())), JsonObject.class);
	}

	@NotNull
	private List<RemoteRepository> repos() {
		Repositories repositoryManager = new Repositories(module);
		List<RemoteRepository> repos = repositoryManager.map(repositories);
		repos.add(repositoryManager.maven(ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY));
		if (repos.stream().noneMatch(r -> r.getUrl().equals(INTINO_RELEASES)))
			repos.add(repositoryManager.intino(ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY));
		return repos;
	}

	private void write(String content, File destiny) {
		try {
			Files.write(destiny.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
