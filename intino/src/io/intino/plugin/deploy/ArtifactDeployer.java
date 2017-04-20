package io.intino.plugin.deploy;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.cesar.RestCesarAccessor;
import io.intino.cesar.schemas.Artifactory;
import io.intino.cesar.schemas.Packaging;
import io.intino.cesar.schemas.Runtime;
import io.intino.cesar.schemas.SystemSchema;
import io.intino.konos.exceptions.BadRequest;
import io.intino.konos.exceptions.Forbidden;
import io.intino.konos.exceptions.Unknown;
import io.intino.legio.LifeCycle;
import io.intino.legio.LifeCycle.Deploy.Destination;
import io.intino.legio.Parameter;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.LifeCyclePhase;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;

public class ArtifactDeployer {
	private static final Logger LOG = Logger.getInstance(ArtifactDeployer.class.getName());

	private final LifeCyclePhase phase;
	private final Module module;
	private final LegioConfiguration configuration;
	private LifeCycle.Deploy deploy;

	public ArtifactDeployer(LifeCyclePhase phase, Module module) {
		this.phase = phase;
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		this.deploy = configuration.lifeCycle().deploy();
	}

	public void execute() throws IntinoException {
		final List<? extends Destination> destinies = phase.equals(LifeCyclePhase.PREDEPLOY) ? deploy.preList() : deploy.proList();
		final String user = cesarUser();
		if (user.isEmpty()) throw new IntinoException("Cesar user not found, please specify it in Intino settings");
		for (Destination destination : destinies)
			try {
				new RestCesarAccessor(urlOf(deploy)).postDeploySystem(user, createSystem(destination));
			} catch (Unknown | Forbidden | BadRequest unknown) {
				throw new IntinoException(unknown.getMessage());
			}
	}

	private String cesarUser() {
		return IntinoSettings.getSafeInstance(module.getProject()).cesarUser();
	}

	private SystemSchema createSystem(Destination destination) {
		final String id = (configuration.groupId() + ":" + configuration.artifactId() + ":" + configuration.version()).toLowerCase();
		final String classpathPrefix = configuration.lifeCycle().package$().asRunnable().classpathPrefix();
		return new SystemSchema().id(id).publicURL(destination.publicURL()).
				artifactoryList(artifactories()).packaging(new Packaging().
				artifact(id).parameterList(extractParameters(destination.configuration())).
				classpathPrefix(classpathPrefix == null || classpathPrefix.isEmpty() ? "dependency" : classpathPrefix)).
				runtime(new Runtime().serverName(destination.specificServer()));
	}

	private List<io.intino.cesar.schemas.Parameter> extractParameters(Destination.Configuration configuration) {
		List<io.intino.cesar.schemas.Parameter> parameters = new ArrayList<>();
		for (Parameter p : configuration.parameterList()) parameters.add(parametersFromNode(p));
		for (Destination.Configuration.Service service : configuration.serviceList())
			parameters.addAll(service.parameterList().stream().map(p -> parametersFromNode(p, service.name())).collect(Collectors.toList()));
		if (configuration.store() != null)
			parameters.add(new io.intino.cesar.schemas.Parameter().name("graph.store").value(configuration.store().path()));
		return parameters;
	}

	private static io.intino.cesar.schemas.Parameter parametersFromNode(Parameter node, String prefix) {
		return new io.intino.cesar.schemas.Parameter().name(prefix + "." + node.name().replace("-", ".")).value(node.value());
	}

	private static io.intino.cesar.schemas.Parameter parametersFromNode(Parameter node) {
		return new io.intino.cesar.schemas.Parameter().name(node.name().replace("-", ".")).value(node.value());
	}

	private List<Artifactory> artifactories() {
		List<Artifactory> artifactories = new ArrayList<>();
		Map<String, String> repositories = collectRepositories();
		artifactories.addAll(repositories.entrySet().stream().map(entry -> addCredentials(new Artifactory().url(entry.getKey()).id(entry.getValue()))).collect(Collectors.toList()));
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
