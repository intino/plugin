package io.intino.plugin.build.cesar;

import com.intellij.openapi.module.Module;
import io.intino.cesar.RestCesarAccessor;
import io.intino.cesar.schemas.Artifactory;
import io.intino.cesar.schemas.SystemSchema;
import io.intino.legio.LifeCycle;
import io.intino.legio.LifeCycle.Publishing.Destination;
import io.intino.legio.Parameter;
import io.intino.pandora.exceptions.Unknown;
import io.intino.plugin.build.LifeCyclePhase;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PublishManager {
	private final LifeCyclePhase phase;
	private final Module module;
	private final LegioConfiguration configuration;

	public PublishManager(LifeCyclePhase phase, Module module) {
		this.phase = phase;
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
	}

	public void execute() {
		final LifeCycle.Publishing publishing = configuration.lifeCycle().publishing();
		final List<? extends Destination> destinies = phase.equals(LifeCyclePhase.PREDEPLOY) ? publishing.preDeployList() : publishing.deployList();
		for (Destination destination : destinies)
			try {
				new RestCesarAccessor(urlOf(publishing)).postDeploySystem(createSystem(destination));
			} catch (Unknown unknown) {
				unknown.printStackTrace();
			}
	}

	private SystemSchema createSystem(Destination destination) {
		return new SystemSchema().artifact(configuration.artifactId()).
				artifactoryList(artifactories()).
				parameterList(extractParameters(destination.parameterList()));
	}

	private List<io.intino.cesar.schemas.Parameter> extractParameters(List<Parameter> parameters) {
		return parameters.stream().map(p -> parametersFromNode(p.node())).collect(Collectors.toList());
	}

	private static io.intino.cesar.schemas.Parameter parametersFromNode(tara.magritte.Node node) {
		io.intino.cesar.schemas.Parameter schema = new io.intino.cesar.schemas.Parameter();
		final java.util.Map<String, java.util.List<?>> variables = node.variables();
		variables.put("name", java.util.Collections.singletonList(node.name()));
		for (String variable : variables.keySet())
			if (variable.equals("type")) schema.type(variables.get(variable).get(0).toString());
			else if (variable.equals("value")) schema.value(variables.get(variable).get(0).toString());
		return schema;
	}

	private List<Artifactory> artifactories() {
		List<Artifactory> artifactories = new ArrayList<>();
		AbstractMap.SimpleEntry<String, String> distribution = configuration.distributionReleaseRepository();
		artifactories.add(new Artifactory().url(distribution.getKey()).id(distribution.getValue()));
		artifactories.addAll(configuration.releaseRepositories().entrySet().stream().map(entry -> new Artifactory().url(entry.getKey()).id(entry.getValue())).collect(Collectors.toList()));
		return artifactories;
	}

	private URL urlOf(LifeCycle.Publishing publishing) {
		try {
			return new URL(publishing.cesarURL());
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
