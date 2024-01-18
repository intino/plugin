package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.util.net.HttpConfigurable;
import io.intino.Configuration;
import io.intino.Configuration.Repository;
import io.intino.alexandria.logger.Logger;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.settings.IntinoSettings;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static io.intino.Configuration.Repository.Snapshot;
import static io.intino.plugin.dependencyresolution.ArtifactoryConnector.MAVEN_URL;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;
import static org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_WARN;

public class Repositories {
	public static final File LOCAL = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	public static final String INTINO_RELEASES = "https://artifactory.intino.io/artifactory/releases";
	private final Module module;

	public Repositories(Module module) {
		this.module = module;
	}

	public static List<RemoteRepository> of(Module module) {
		Configuration configuration = IntinoUtil.configurationOf(module);
		return new Repositories(module).read(configuration.repositories());
	}

	public List<RemoteRepository> map(List<Repository> repositories) {
		Application app = ApplicationManager.getApplication();
		return new ArrayList<>(app.isReadAccessAllowed() ? read(repositories) : app.<List<RemoteRepository>>runReadAction(() -> read(repositories)));
	}

	@NotNull
	private List<RemoteRepository> read(List<Repository> repositories) {
		return repositories.stream().map(this::repository).toList();
	}

	RemoteRepository repository(Repository r) {
		RemoteRepository.Builder builder = new RemoteRepository.Builder(r.identifier(), "default", r.url())
				.setAuthentication(provideAuthentication(r.identifier()));
		String updatePolicy = r.updatePolicy().name().toLowerCase();
		if (r instanceof Snapshot)
			builder.setSnapshotPolicy(new RepositoryPolicy(true, updatePolicy, CHECKSUM_POLICY_WARN))
					.setReleasePolicy(new RepositoryPolicy(false, updatePolicy, CHECKSUM_POLICY_WARN));
		else builder.setReleasePolicy(new RepositoryPolicy(true, updatePolicy, CHECKSUM_POLICY_WARN))
				.setSnapshotPolicy(new RepositoryPolicy(false, updatePolicy, CHECKSUM_POLICY_WARN));
		addProxies(builder, r.url());
		return builder.build();
	}

	public RemoteRepository maven(String updatePolicy) {
		return new RemoteRepository.Builder("maven-central", "default", MAVEN_URL)
				.setPolicy(new RepositoryPolicy(true, updatePolicy, CHECKSUM_POLICY_WARN))
				.build();
	}

	public RemoteRepository intino(String updatePolicy) {
		RemoteRepository.Builder builder = new RemoteRepository.Builder("intino-maven", "default", INTINO_RELEASES)
				.setPolicy(new RepositoryPolicy(true, updatePolicy, CHECKSUM_POLICY_WARN));
		addProxies(builder, INTINO_RELEASES);
		return builder.build();
	}

	public RemoteRepository local() {
		try {
			return new RemoteRepository.Builder("local", "default", LOCAL.toURI().toURL().toString())
					.setPolicy(new RepositoryPolicy(true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN))
					.build();
		} catch (MalformedURLException e) {
			Logger.error(e);
			return null;
		}
	}

	private Authentication provideAuthentication(String mavenId) {
		return IntinoSettings.getInstance(module.getProject()).artifactories().stream()
				.filter(c -> c.serverId.equals(mavenId)).findFirst()
				.map(c -> new AuthenticationBuilder().addUsername(c.username).addPassword(c.password).build())
				.orElse(null);
	}

	private static void addProxies(RemoteRepository.Builder builder, String url) {
		final HttpConfigurable proxyConf = HttpConfigurable.getInstance();
		if (proxyConf.isHttpProxyEnabledForUrl(url))
			builder.setProxy(new Proxy("http", proxyConf.PROXY_HOST, proxyConf.PROXY_PORT, auth(proxyConf)));
	}


	@Nullable
	private static Authentication auth(HttpConfigurable proxyConf) {
		return proxyConf.getProxyLogin() != null && !proxyConf.getProxyLogin().isEmpty() ?
				new AuthenticationBuilder().addUsername(proxyConf.getProxyLogin()).addPassword(proxyConf.getPlainProxyPassword()).build() : null;
	}
}
