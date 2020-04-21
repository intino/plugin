package io.intino.plugin.deploy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.Configuration;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Deployment;
import io.intino.Configuration.RunConfiguration;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Forbidden;
import io.intino.alexandria.exceptions.Unknown;
import io.intino.cesar.box.CesarRestAccessor;
import io.intino.cesar.box.schemas.ProcessDeployment;
import io.intino.cesar.box.schemas.ProcessDeployment.Artifactory;
import io.intino.cesar.box.schemas.ProcessDeployment.Packaging.Parameter;
import io.intino.plugin.IntinoException;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.settings.IntinoSettings.getSafeInstance;
import static java.util.stream.Collectors.toList;

public class ArtifactDeployer {
	private final Module module;
	private final LegioConfiguration configuration;
	private final List<Deployment> deployments;

	public ArtifactDeployer(Module module, List<Deployment> deployments) {
		this.module = module;
		this.configuration = (LegioConfiguration) IntinoUtil.configurationOf(module);
		this.deployments = deployments;
	}

	public boolean execute() throws IntinoException {
		for (Deployment destination : deployments) {
			deploy(destination);
			waitASecond();
		}
		return true;
	}

	private void deploy(Deployment destination) throws IntinoException {
		try {
			final Map.Entry<String, String> cesar = getSafeInstance(module.getProject()).cesar();
			if (destination.server() == null) throw new IntinoException("Server not found");
			final Artifact.Package aPackage = safe(() -> configuration.artifact().packageConfiguration());
			if (aPackage == null) throw new IntinoException("Package configuration not found");
			if (!aPackage.isRunnable()) throw new IntinoException("Packaging must be runnable");
			List<Configuration.Parameter> incorrectParameters = incorrectParameters(destination.runConfiguration().finalArguments());
			if (!incorrectParameters.isEmpty())
				throw new IntinoException("Parameters missed: " + incorrectParameters.stream().map(Configuration.Parameter::name).collect(Collectors.joining("; ")));
			new CesarRestAccessor(urlOf(cesar.getKey()), cesar.getValue()).postDeployProcess(createProcess(aPackage, destination));
		} catch (Unknown | Forbidden | BadRequest unknown) {
			throw new IntinoException(unknown.getMessage());
		}
	}

	private List<Configuration.Parameter> incorrectParameters(Map<String, String> arguments) {
		return configuration.artifact().parameters().stream().filter(p -> !arguments.containsKey(p.name()) || arguments.get(p.name()) == null).collect(Collectors.toList());
	}

	private ProcessDeployment createProcess(Artifact.Package packageConfiguration, Deployment destination) {
		final String classpathPrefix = packageConfiguration.classpathPrefix();
		return new ProcessDeployment().
				groupId(configuration.artifact().groupId()).artifactId(configuration.artifact().name().toLowerCase()).version(configuration.artifact().version()).
				jvmOptions(destination.runConfiguration().vmOptions()).
				artifactoryList(artifactories()).
				prerequisites(requirements(destination)).
				packaging(new ProcessDeployment.Packaging().mainClass(packageConfiguration.mainClass()).parameterList(extractParameters(destination.runConfiguration())).classpathPrefix(classpathPrefix == null || classpathPrefix.isEmpty() ? "dependency" : classpathPrefix)).
				destinationServer(destination.server().name());
	}

	@NotNull
	private ProcessDeployment.Prerequisites requirements(Deployment destination) {
		final ProcessDeployment.Prerequisites prerequisites = new ProcessDeployment.Prerequisites();
		Deployment.Requirements r = destination.requirements();
		if (r != null) {
			if (r.minMemory() != 0) prerequisites.memory(r.minMemory());
			if (r.minHdd() != 0) prerequisites.hdd(r.minHdd());
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
		List<Configuration.Repository> repositories = collectRepositories();
		return repositories.stream().map(entry -> credentials(new Artifactory().url(entry.url()).id(entry.identifier()))).collect(toList());
	}

	private List<Configuration.Repository> collectRepositories() {
		List<Configuration.Repository> repositories = new ArrayList<>(deployRepositories(configuration.repositories()));
		for (Module dependant : ModuleRootManager.getInstance(module).getDependencies()) {
			final Configuration dependantConf = IntinoUtil.configurationOf(dependant);
			if (dependantConf == null) continue;
			deployRepositories(dependantConf.repositories()).stream().filter(repository -> !contains(repositories, repository)).forEach(repositories::add);
		}
		return repositories;
	}

	private boolean contains(List<Configuration.Repository> repositories, Configuration.Repository repository) {
		return repositories.stream().anyMatch(r -> r.url().equals(repository.url()));
	}

	private List<Configuration.Repository> deployRepositories(List<Configuration.Repository> repositories) {
		return repositories.stream().filter(r -> !(r instanceof Configuration.Repository.Language) && !isDistribution(r)).collect(Collectors.toList());
	}

	private boolean isDistribution(Configuration.Repository r) {
		LegioArtifact artifact = configuration.artifact();
		if (artifact.distribution() == null) return false;
		return r instanceof Configuration.Repository.Release ? r.url().equals(safe(() -> artifact.distribution().release().url())) : r.url().equals(safe(() -> artifact.distribution().snapshot().url()));
	}

	private Artifactory credentials(Artifactory artifactory) {
		final IntinoSettings settings = getSafeInstance(module.getProject());
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(artifactory.id()))
				artifactory.user(credential.username).password(credential.password);
		return artifactory;
	}

	private void waitASecond() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignored) {
		}
	}
}
