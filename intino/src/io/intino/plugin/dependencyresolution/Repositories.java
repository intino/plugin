package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.module.Module;
import com.intellij.util.net.HttpConfigurable;
import io.intino.Configuration.Repository;
import io.intino.alexandria.logger.Logger;
import io.intino.plugin.settings.IntinoSettings;
import org.jetbrains.annotations.Nullable;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import static io.intino.Configuration.Repository.Snapshot;
import static io.intino.plugin.dependencyresolution.ArtifactoryConnector.MAVEN_URL;
import static java.util.stream.Collectors.toList;
import static org.sonatype.aether.repository.RepositoryPolicy.UPDATE_POLICY_ALWAYS;
import static org.sonatype.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;

public class Repositories {
	public static final File LOCAL = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	public static final String INTINO_RELEASES = "https://artifactory.intino.io/artifactory/releases";
	private final Module module;

	public Repositories(Module module) {
		this.module = module;
	}

	public List<RemoteRepository> map(List<Repository> repositories) {
		return repositories.stream().map(this::repository).collect(toList());
	}

	RemoteRepository repository(Repository r) {
		final RemoteRepository repository = new RemoteRepository(r.identifier(), "default", r.url()).setAuthentication(provideAuthentication(r.identifier()));
		if (r instanceof Snapshot) {
			repository.setPolicy(true, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_ALWAYS));
			repository.setPolicy(false, new RepositoryPolicy().setEnabled(false));
		} else {
			repository.setPolicy(true, new RepositoryPolicy().setEnabled(false).setUpdatePolicy(UPDATE_POLICY_ALWAYS));
			repository.setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY));
		}
		addProxies(repository);
		return repository;
	}

	public RemoteRepository maven(String updatePolicy) {
		return new RemoteRepository("maven-central", "default", MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(updatePolicy));
	}

	public RemoteRepository intino(String updatePolicy) {
		RemoteRepository repo = new RemoteRepository("intino-maven", "default", INTINO_RELEASES).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(updatePolicy));
		addProxies(repo);
		return repo;
	}

	public RemoteRepository local() {
		try {
			return new RemoteRepository("local", "default", LOCAL.toURI().toURL().toString()).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY));
		} catch (MalformedURLException e) {
			Logger.error(e);
			return null;
		}
	}

	private Authentication provideAuthentication(String mavenId) {
		return IntinoSettings.getInstance(module.getProject()).artifactories().stream().
				filter(c -> c.serverId.equals(mavenId)).findFirst().
				map(c -> new Authentication(c.username, c.password)).
				orElse(null);
	}

	private static void addProxies(RemoteRepository r) {
		final HttpConfigurable proxyConf = HttpConfigurable.getInstance();
		if (proxyConf.isHttpProxyEnabledForUrl(r.getUrl()))
			r.setProxy(new Proxy("http", proxyConf.PROXY_HOST, proxyConf.PROXY_PORT, auth(proxyConf)));
	}


	@Nullable
	private static Authentication auth(HttpConfigurable proxyConf) {
		return proxyConf.getProxyLogin() != null && !proxyConf.getProxyLogin().isEmpty() ? new Authentication(proxyConf.getProxyLogin(), proxyConf.getPlainProxyPassword()) : null;
	}
}
