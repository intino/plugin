package io.intino.plugin.publishing;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.cesar.RestCesarAccessor;
import io.intino.cesar.schemas.Artifactory;
import io.intino.cesar.schemas.Packaging;
import io.intino.cesar.schemas.Runtime;
import io.intino.cesar.schemas.SystemSchema;
import io.intino.legio.LifeCycle;
import io.intino.legio.LifeCycle.Publishing.Destination;
import io.intino.legio.Parameter;
import io.intino.konos.exceptions.Unknown;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.LifeCyclePhase;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.magritte.Node;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.plugin.publishing.ArtifactManager.urlOf;
import static java.util.Collections.singletonList;

public class ArtifactPublisher {
	private static final Logger LOG = Logger.getInstance(ArtifactPublisher.class.getName());

	private final LifeCyclePhase phase;
	private final Module module;
	private final LegioConfiguration configuration;
	private LifeCycle.Publishing publishing;

	public ArtifactPublisher(LifeCyclePhase phase, Module module) {
		this.phase = phase;
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		this.publishing = configuration.lifeCycle().publishing();
	}

	public void execute() throws IntinoException {
		final List<? extends Destination> destinies = phase.equals(LifeCyclePhase.PREDEPLOY) ? publishing.preDeployList() : publishing.deployList();
		for (Destination destination : destinies)
			try {
				new RestCesarAccessor(urlOf(publishing)).postDeploySystem(createSystem(destination));
			} catch (Unknown unknown) {
				throw new IntinoException(unknown.getMessage());
			}
	}

	private SystemSchema createSystem(Destination destination) {
		final String id = configuration.groupId() + ":" + configuration.artifactId() + ":" + configuration.modelVersion();
		return new SystemSchema().id(id).publicURL(destination.publicURL()).
				artifactoryList(artifactories()).packaging(new Packaging().
				artifact(id).parameterList(extractParameters(destination.parameterList())).
				classpathPrefix(configuration.lifeCycle().package$().asRunnable().classpathPrefix())).runtime(new Runtime().jmxPort(destination.owner().as(LifeCycle.Publishing.class).managementPort()));
	}

	private List<io.intino.cesar.schemas.Parameter> extractParameters(List<Parameter> parameters) {
		return parameters.stream().map(p -> parametersFromNode(p.node())).collect(Collectors.toList());
	}

	private static io.intino.cesar.schemas.Parameter parametersFromNode(Node node) {
		io.intino.cesar.schemas.Parameter schema = new io.intino.cesar.schemas.Parameter();
		final java.util.Map<String, java.util.List<?>> variables = node.variables();
		variables.put("name", singletonList(node.name()));
		for (String variable : variables.keySet())
			if (variable.equals("type")) schema.type(variables.get(variable).get(0).toString());
			else if (variable.equals("value")) schema.value(variables.get(variable).get(0).toString());
		return schema;
	}

	private List<Artifactory> artifactories() {
		List<Artifactory> artifactories = new ArrayList<>();
		Map<String, String> repositories = collectRepositories();
		artifactories.addAll(repositories.entrySet().stream().map(entry -> new Artifactory().url(entry.getKey()).id(entry.getValue())).collect(Collectors.toList()));
		return artifactories;
	}

	private Map<String, String> collectRepositories() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(configuration.distributionReleaseRepository().getKey(), configuration.distributionReleaseRepository().getValue());
		map.putAll(configuration.releaseRepositories());
		for (Module dependant : ModuleRootManager.getInstance(module).getDependencies()) {
			final Configuration dependantConf = TaraUtil.configurationOf(dependant);
			if (dependantConf == null) continue;
			map.putAll(dependantConf.releaseRepositories());
		}
		return map;
	}
}
