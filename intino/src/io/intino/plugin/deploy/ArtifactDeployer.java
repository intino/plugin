package io.intino.plugin.deploy;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.cesar.CesarRestAccessor;
import io.intino.cesar.schemas.Artifactory;
import io.intino.cesar.schemas.Packaging;
import io.intino.cesar.schemas.Runtime;
import io.intino.cesar.schemas.SystemSchema;
import io.intino.konos.exceptions.BadRequest;
import io.intino.konos.exceptions.Forbidden;
import io.intino.konos.exceptions.Unknown;
import io.intino.legio.graph.Argument;
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

import java.util.*;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static java.util.stream.Collectors.toList;

public class ArtifactDeployer {
	private static final Logger LOG = Logger.getInstance(ArtifactDeployer.class.getName());

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
			if (!configuration.pack().isRunnable()) throw new IntinoException("Packaging must be runnable");
			if (!correctParameters(destination.runConfiguration().argumentList()))
				throw new IntinoException("Arguments are duplicated");
			new CesarRestAccessor(urlOf(destination.server().cesar())).postDeploySystem(user, createSystem(destination));
		} catch (Unknown | Forbidden | BadRequest unknown) {
			throw new IntinoException(unknown.getMessage());
		}
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
		final String classpathPrefix = configuration.pack().asRunnable().classpathPrefix();
		return new SystemSchema().project(destination.project() != null ? destination.project() : module.getProject().getName()).
				name(id).tag(destination.core$().ownerAs(Artifact.Deployment.class).tags()).
				publicURL(destination.url()).
				artifactoryList(artifactories()).packaging(new Packaging().
				artifact(id).parameterList(extractParameters(destination.runConfiguration())).
				classpathPrefix(classpathPrefix == null || classpathPrefix.isEmpty() ? "dependency" : classpathPrefix)).
				runtime(new Runtime().serverName(destination.server().name$()));
	}

	private List<io.intino.cesar.schemas.Parameter> extractParameters(RunConfiguration configuration) {
		return configuration.argumentList().stream().map(ArtifactDeployer::parametersFromNode).collect(toList());
	}

	private boolean correctParameters(List<Argument> arguments) {
		Set<String> parameters = new HashSet<>();
		return arguments.stream().allMatch(argument -> parameters.add(argument.name()));
	}

	private static io.intino.cesar.schemas.Parameter parametersFromNode(Argument node) {
		return new io.intino.cesar.schemas.Parameter().name(node.name().replace("-", ".")).value(node.value());
	}

	private List<Artifactory> artifactories() {
		List<Artifactory> artifactories = new ArrayList<>();
		Map<String, String> repositories = collectRepositories();
		artifactories.addAll(repositories.entrySet().stream().map(entry -> addCredentials(new Artifactory().url(entry.getKey()).id(entry.getValue()))).collect(toList()));
		return artifactories;
	}

	private Map<String, String> collectRepositories() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(configuration.distributionReleaseRepository().getKey(), configuration.distributionReleaseRepository().getValue());
		map.putAll(configuration.releaseRepositories());
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
