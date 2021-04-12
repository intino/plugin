package io.intino.plugin.dependencyresolution.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.io.ZipUtil;
import com.jcabi.aether.Aether;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Artifact.WebArtifact;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.sonatype.aether.repository.RepositoryPolicy.UPDATE_POLICY_ALWAYS;
import static org.sonatype.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;

public class WebArtifactResolver {
	private static final Logger logger = Logger.getInstance(PackageJsonCreator.class.getName());
	private final Project project;
	private final Artifact artifact;
	private final List<Repository> repositories;
	private final File destination;

	public WebArtifactResolver(Project project, Artifact artifact, List<Repository> repositories, File destination) {
		this.project = project;
		this.artifact = artifact;
		this.repositories = repositories;
		this.destination = destination;
	}

	public List<JsonObject> resolveArtifacts() {
		List<JsonObject> manifests = new ArrayList<>();
		Aether aether = new Aether(collectRemotes(), new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
		for (WebArtifact artifact : artifact.webArtifacts()) {
			if (isOverriding(artifact)) continue;
			final List<org.sonatype.aether.artifact.Artifact> artifacts = resolve(aether, artifact);
			if (!artifacts.isEmpty()) {
				File packageJson = extract(artifact, artifacts.get(0).getFile());
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
		Aether aether = new Aether(collectRemotes(), new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
		for (WebArtifact artifact : artifact.webArtifacts()) {
			if (isOverriding(artifact)) continue;
			final List<org.sonatype.aether.artifact.Artifact> artifacts = resolve(aether, artifact);
			if (!artifacts.isEmpty()) {
				File packageJson = extract(artifact, artifacts.get(0).getFile());
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
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		remotes.add(new RemoteRepository("maven-central", "default", ArtifactoryConnector.MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_DAILY)));
		remotes.addAll(repositories.stream().map(this::repository).collect(Collectors.toList()));
		return remotes;
	}

	private RemoteRepository repository(Repository r) {
		final RemoteRepository repository = new RemoteRepository(r.identifier(), "default", r.url()).setAuthentication(provideAuthentication(r.identifier()));
		if (r instanceof Repository.Snapshot) {
			repository.setPolicy(true, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_ALWAYS));
			repository.setPolicy(false, new RepositoryPolicy().setEnabled(false));
		} else {
			repository.setPolicy(true, new RepositoryPolicy().setEnabled(false).setUpdatePolicy(UPDATE_POLICY_ALWAYS));
			repository.setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY));
		}
		return repository;
	}


	private Authentication provideAuthentication(String mavenId) {
		final IntinoSettings settings = IntinoSettings.getSafeInstance(project);
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(mavenId))
				return new Authentication(credential.username, credential.password);
		return null;
	}

	private void write(String content, File destiny) {
		try {
			Files.write(destiny.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
