package io.intino.plugin.dependencyresolution;

import io.intino.Configuration.Artifact;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.metadata.DefaultMetadata;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.classpath.ClasspathTransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.eclipse.aether.util.repository.SimpleResolutionErrorPolicy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MavenDependencyResolver {
	private static RepositorySystem system;
	private static final String DEFAULT_REPO_LOCAL = String.format("%s/.m2/repository", System.getProperty("user.home"));
	private static final RemoteRepository DEFAULT_REPO_REMOTE = new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
	private static RepositorySystemSession session;
	private final List<RemoteRepository> remoteRepos;

	static {
		loadService();
	}

	private static void loadService() {
		var locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
		locator.addService(TransporterFactory.class, ClasspathTransporterFactory.class);
		system = locator.getService(RepositorySystem.class);
	}

	private static void init() {
		init(DEFAULT_REPO_LOCAL);
	}

	private static void init(String localRepo) {
		if (system == null) loadService();
		if (session == null) session = buildSession(localRepo);
	}

	static void resetSession() {
		File basedir = session.getLocalRepository().getBasedir();
		session = null;
		init(basedir.getAbsolutePath());
	}

	static void removeResolutionFromSession(String[] coors) {
		session.getCache().put(session, coors, null);
	}

	public static List<Dependency> dependenciesFrom(DependencyResult result, boolean includeUnresolved) {
		var nodeListGenerator = new PreorderNodeListGenerator();
		result.getRoot().accept(nodeListGenerator);
		return nodeListGenerator.getDependencies(includeUnresolved);
	}

	public MavenDependencyResolver(List<RemoteRepository> remoteRepos) {
		init();
		this.remoteRepos = remoteRepos.isEmpty() ? List.of(DEFAULT_REPO_REMOTE) : remoteRepos;
	}

	public MavenDependencyResolver(String localRepository, List<RemoteRepository> remoteRepos) {
		init(localRepository);
		this.remoteRepos = remoteRepos.isEmpty() ? List.of(DEFAULT_REPO_REMOTE) : remoteRepos;
	}

	public static MetadataResult metadata(DefaultArtifact artifact) {
		init();
		return system.resolveMetadata(session, Collections.singleton(new MetadataRequest(new DefaultMetadata(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getClassifier(), Metadata.Nature.RELEASE_OR_SNAPSHOT)))).get(0);
	}

	public ArtifactResult resolveSources(org.eclipse.aether.artifact.Artifact dependency) {
		try {
			return system.resolveArtifact(session, new ArtifactRequest(dependency, remoteRepos, null));
		} catch (ArtifactResolutionException e) {
			return e.getResult();
		}
	}

	public DependencyResult resolve(DefaultArtifact artifact, String scope) throws DependencyResolutionException {
		return system.resolveDependencies(session, request(artifact, scope));
	}

	public DependencyResult resolve(List<Artifact.Dependency> dependencies) throws DependencyResolutionException {
		return system.resolveDependencies(session, request(dependencies));
	}

	private DependencyRequest request(DefaultArtifact artifact, String scope) {
		return new DependencyRequest(new CollectRequest(List.of(new Dependency(artifact, scope.toLowerCase())), null, remoteRepos), null);
	}

	@NotNull
	private DependencyRequest request(List<Artifact.Dependency> deps) {
		return new DependencyRequest(new CollectRequest(deps.stream()
				.map(d -> new Dependency(artifactOf(d), d.scope().toLowerCase()).setExclusions(exclusionsOf(d))).toList(), null, remoteRepos), null);
	}

	@NotNull
	private Collection<Exclusion> exclusionsOf(Artifact.Dependency dependency) {
		return dependency.excludes().stream().map(e -> new Exclusion(e.groupId(), e.artifactId(), "", "jar")).collect(toList());
	}

	public static DefaultArtifact artifactOf(Artifact.Dependency d) {
		return new DefaultArtifact(d.identifier());
	}

	private static RepositorySystemSession buildSession(String localRepo) {
		var session = MavenRepositorySystemUtils.newSession();
		return session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, new LocalRepository(localRepo)))
				.setResolutionErrorPolicy(new SimpleResolutionErrorPolicy(ResolutionErrorPolicy.CACHE_DISABLED))
				.setCache(new DefaultRepositoryCache());
	}
}
