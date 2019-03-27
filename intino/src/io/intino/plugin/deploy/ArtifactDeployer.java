package io.intino.plugin.deploy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Forbidden;
import io.intino.alexandria.exceptions.Unknown;
import io.intino.cesar.box.CesarRestAccessor;
import io.intino.cesar.box.schemas.ProcessDeployment;
import io.intino.cesar.box.schemas.ProcessDeployment.Artifactory;
import io.intino.cesar.box.schemas.ProcessDeployment.Packaging.Parameter;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.settings.IntinoSettings.getSafeInstance;
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
		for (Destination destination : destinations) {
			deploy(destination);
			waitASecond();
		}
		return true;
	}

	private void deploy(Destination destination) throws IntinoException {
		try {
			final Map.Entry<String, String> cesar = getSafeInstance(module.getProject()).cesar();
			if (destination.server() == null) throw new IntinoException("Server not found");
			final Artifact.Package aPackage = safe(() -> configuration.graph().artifact().package$());
			if (aPackage == null) throw new IntinoException("Package configuration not found");
			if (!aPackage.isRunnable()) throw new IntinoException("Packaging must be runnable");
			if (!correctParameters(destination.runConfiguration().finalArguments()))
				throw new IntinoException("Arguments are duplicated");
			new CesarRestAccessor(urlOf(cesar.getKey()), cesar.getValue()).postDeployProcess(createProcess(destination));
		} catch (Unknown | Forbidden | BadRequest unknown) {
			throw new IntinoException(unknown.getMessage());
		}
	}

	private boolean correctParameters(Map<String, String> arguments) {
		return configuration.graph().artifact().parameterList().stream().allMatch(p -> arguments.containsKey(p.name()) && arguments.get(p.name()) != null);
	}

	private ProcessDeployment createProcess(Destination destination) {
		final String classpathPrefix = configuration.graph().artifact().package$().asRunnable().classpathPrefix();
		return new ProcessDeployment().project(destination.project() != null ? destination.project() : module.getProject().getName()).
				groupId(configuration.groupId()).artifactId(configuration.artifactId().toLowerCase()).version(configuration.version()).
				jvmOptions(destination.runConfiguration().vmOptions()).
				datalake(destination.core$().ownerAs(Artifact.Deployment.class).tags().contains("Datalake")).
				artifactoryList(artifactories()).
				prerequisites(requirements(destination)).
				packaging(new ProcessDeployment.Packaging().parameterList(extractParameters(destination.runConfiguration())).classpathPrefix(classpathPrefix == null || classpathPrefix.isEmpty() ? "dependency" : classpathPrefix)).
				destinationServer(destination.server().name$());
	}

	@NotNull
	private ProcessDeployment.Prerequisites requirements(Destination destination) {
		final ProcessDeployment.Prerequisites prerequisites = new ProcessDeployment.Prerequisites();
		Destination.Requirements r = destination.requirements();
		if (r != null) {
			if (r.memory() != null) prerequisites.memory(r.memory().min());
			if (r.hDD() != null) prerequisites.hdd(r.hDD().min());
		}
		return prerequisites;
	}

	private List<Parameter> extractParameters(RunConfiguration configuration) {
		return configuration.finalArguments().entrySet().stream().map(this::parametersFromNode).collect(toList());
	}

	private Parameter parametersFromNode(Map.Entry<String, String> node) {
		return new Parameter().name(node.getKey()).value(node.getValue());
	}

	private List<Artifactory> artifactories() {
		Map<String, String> repositories = collectRepositories();
		return repositories.entrySet().stream().map(entry -> addCredentials(new Artifactory().url(entry.getKey()).id(entry.getValue()))).collect(toList());
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
		final IntinoSettings settings = getSafeInstance(module.getProject());
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(artifactory.id()))
				artifactory.user(credential.username).password(credential.password);
		return artifactory;
	}

	private void waitASecond() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}
}
