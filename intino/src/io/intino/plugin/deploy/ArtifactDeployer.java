package io.intino.plugin.deploy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.cesar.CesarRestAccessor;
import io.intino.cesar.schemas.SystemSchema;
import io.intino.cesar.schemas.SystemSchema.Artifactory;
import io.intino.cesar.schemas.SystemSchema.Packaging;
import io.intino.konos.alexandria.exceptions.BadRequest;
import io.intino.konos.alexandria.exceptions.Forbidden;
import io.intino.konos.alexandria.exceptions.Unknown;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Destination;
import io.intino.legio.graph.RunConfiguration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static io.intino.plugin.project.Safe.safe;
import static java.util.stream.Collectors.toList;

public class ArtifactDeployer {
	private final Module module;
	private final LegioConfiguration configuration;
	private List<Destination> destinations;

	public ArtifactDeployer(Module module, List<Destination> destinations) {
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		this.destinations = destinations;
	}

	public boolean execute() throws IntinoException {
		for (Destination destination : destinations) deploy(destination);
		return true;
	}

	private void deploy(Destination destination) throws IntinoException {
		try {
			final String user = user();
			if (destination.server() == null) throw new IntinoException("Server not found");
			final Artifact.Package aPackage = safe(() -> configuration.graph().artifact().package$());
			if (aPackage == null) throw new IntinoException("Package configuration not found");
			if (!aPackage.isRunnable()) throw new IntinoException("Packaging must be runnable");
			if (!correctParameters(destination.runConfiguration().finalArguments()))
				throw new IntinoException("Arguments are duplicated");
			new CesarRestAccessor(urlOf(destination.server().cesar())).postDeployProcess(user, createSystem(destination));
		} catch (Unknown | Forbidden | BadRequest unknown) {
			throw new IntinoException(unknown.getMessage());
		}
	}

	private boolean correctParameters(Map<String, String> arguments) {
		return configuration.graph().artifact().parameterList().stream().allMatch(p -> arguments.containsKey(p.name()) && arguments.get(p.name()) != null);
	}

	@NotNull
	private String user() throws IntinoException {
		final String user = cesarUser();
		if (user.isEmpty()) throw new IntinoException("Cesar user not found, please specify it in Intino settings");
		return user;
	}

	private String cesarUser() {
		return IntinoSettings.getSafeInstance(module.getProject()).cesarUser();
	}

	private SystemSchema createSystem(Destination destination) {
		final String id = (configuration.groupId() + ":" + configuration.artifactId() + ":" + configuration.version()).toLowerCase();
		final String classpathPrefix = configuration.graph().artifact().package$().asRunnable().classpathPrefix();
		return new SystemSchema().project(destination.project() != null ? destination.project() : module.getProject().getName()).
				name(id).tag(destination.core$().ownerAs(Artifact.Deployment.class).tags()).
				publicURL(destination.url()).
				artifactoryList(artifactories()).packaging(new Packaging().
				artifact(id).parameterList(extractParameters(destination.runConfiguration())).
				classpathPrefix(classpathPrefix == null || classpathPrefix.isEmpty() ? "dependency" : classpathPrefix)).
				runtime(new SystemSchema.Runtime().serverName(destination.server().name$()));
	}

	private List<Packaging.Parameter> extractParameters(RunConfiguration configuration) {
		return configuration.finalArguments().entrySet().stream().map(ArtifactDeployer::parametersFromNode).collect(toList());
	}

	private static Packaging.Parameter parametersFromNode(Map.Entry<String, String> node) {
		return new Packaging.Parameter().name(node.getKey()).value(node.getValue());
	}

	private List<Artifactory> artifactories() {
		Map<String, String> repositories = collectRepositories();
		return new ArrayList<>(repositories.entrySet().stream().map(entry -> addCredentials(new Artifactory().url(entry.getKey()).id(entry.getValue()))).collect(toList()));
	}

	private Map<String, String> collectRepositories() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(configuration.distributionReleaseRepository().getKey(), configuration.distributionReleaseRepository().getValue());
		map.putAll(configuration.releaseRepositories());
		map.putAll(configuration.snapshotRepositories());
		for (Module dependant : ModuleRootManager.getInstance(module).getDependencies()) {
			final Configuration dependantConf = TaraUtil.configurationOf(dependant);
			if (dependantConf != null) map.putAll(dependantConf.releaseRepositories());
		}
		return map;
	}

	private Artifactory addCredentials(Artifactory artifactory) {
		final IntinoSettings settings = IntinoSettings.getSafeInstance(module.getProject());
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(artifactory.id()))
				artifactory.user(credential.username).password(credential.password);
		return artifactory;
	}
}
