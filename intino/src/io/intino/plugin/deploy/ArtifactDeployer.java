package io.intino.plugin.deploy;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.cesar.RestCesarAccessor;
import io.intino.cesar.schemas.Artifactory;
import io.intino.cesar.schemas.Packaging;
import io.intino.cesar.schemas.Runtime;
import io.intino.cesar.schemas.SystemSchema;
import io.intino.konos.exceptions.Unknown;
import io.intino.legio.LifeCycle;
import io.intino.legio.LifeCycle.Deploy.Destination;
import io.intino.legio.Parameter;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.LifeCyclePhase;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.settings.ArtifactoryCredential;
import io.intino.tara.plugin.settings.TaraSettings;

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
	private LifeCycle.Deploy publishing;

	public ArtifactDeployer(LifeCyclePhase phase, Module module) {
		this.phase = phase;
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		this.publishing = configuration.lifeCycle().deploy();
	}

	public void execute() throws IntinoException {
		final List<? extends Destination> destinies = phase.equals(LifeCyclePhase.PREDEPLOY) ? publishing.preList() : publishing.proList();
		for (Destination destination : destinies)
			try {
				new RestCesarAccessor(urlOf(publishing)).postDeploySystem(createSystem(destination));
			} catch (Unknown unknown) {
				throw new IntinoException(unknown.getMessage());
			}
	}

	private SystemSchema createSystem(Destination destination) {
		final String id = configuration.groupId() + ":" + configuration.artifactId() + ":" + configuration.version();
		final String classpathPrefix = configuration.lifeCycle().package$().asRunnable().classpathPrefix();
		return new SystemSchema().id(id).publicURL(destination.publicURL()).
				artifactoryList(artifactories()).packaging(new Packaging().
				artifact(id).parameterList(extractParameters(destination.configuration())).
				classpathPrefix(classpathPrefix == null || classpathPrefix.isEmpty() ? "dependency" : classpathPrefix)).runtime(new Runtime().jmxPort(destination.owner().as(LifeCycle.Deploy.class).managementPort()));
	}

	private List<io.intino.cesar.schemas.Parameter> extractParameters(Destination.Configuration configuration) {
		List<io.intino.cesar.schemas.Parameter> parameters = new ArrayList<>();
		for (Parameter p : configuration.parameterList()) parameters.add(parametersFromNode(p, configuration));
		for (Destination.Configuration.Service service : configuration.serviceList())
			parameters.addAll(service.parameterList().stream().map(p -> parametersFromNode(p, configuration)).collect(Collectors.toList()));
		if (configuration.store() != null)
			parameters.add(new io.intino.cesar.schemas.Parameter().name("graph.store").value(configuration.store().path()));
		return parameters;
	}

	private static io.intino.cesar.schemas.Parameter parametersFromNode(Parameter node, Destination.Configuration c) {
		return new io.intino.cesar.schemas.Parameter().name(c.name() + "." + node.name()).value(node.value());
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
		final TaraSettings settings = TaraSettings.getSafeInstance(module.getProject());
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(artifactory.id()))
				artifactory.user(credential.username).password(credential.password);
		return artifactory;
	}
}
