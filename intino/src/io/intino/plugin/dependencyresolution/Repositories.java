package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.module.Module;
import io.intino.Configuration.Repository;
import io.intino.plugin.settings.IntinoSettings;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;

import java.util.List;

import static io.intino.Configuration.Repository.Snapshot;
import static io.intino.plugin.dependencyresolution.ArtifactoryConnector.MAVEN_URL;
import static java.util.stream.Collectors.toList;
import static org.sonatype.aether.repository.RepositoryPolicy.UPDATE_POLICY_ALWAYS;
import static org.sonatype.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;

public class Repositories {

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
		return repository;
	}

	public RemoteRepository maven(String updatePolicy) {
		return new RemoteRepository("maven-central", "default", MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(updatePolicy));
	}

	private Authentication provideAuthentication(String mavenId) {
		return IntinoSettings.getSafeInstance(module.getProject()).artifactories().stream().
				filter(c -> c.serverId.equals(mavenId)).findFirst().
				map(c -> new Authentication(c.username, c.password)).
				orElse(null);
	}
}
