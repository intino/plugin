package io.intino.plugin.deploy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.Configuration;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Deployment;
import io.intino.Configuration.RunConfiguration;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Forbidden;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.Unauthorized;
import io.intino.cesar.box.ApiAccessor;
import io.intino.cesar.box.schemas.ProcessDeployment;
import io.intino.cesar.box.schemas.ProcessDeployment.Artifactory;
import io.intino.cesar.box.schemas.ProcessDeployment.Packaging.Parameter;
import io.intino.plugin.FatalIntinoException;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.git.GitUtil;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
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

	public DeployResult execute() {
		List<IntinoException> errors = new ArrayList<>();
		List<String> success = new ArrayList<>();
		for (Deployment destination : deployments)
			try {
				deploy(destination);
				waitASecond();
				success.add("Requested deployment to " + destination.server().name());
			} catch (IntinoException e) {
				errors.add(e);
				if (e instanceof FatalIntinoException) return new DeployResult.Fail(errors);
			}
		if (errors.size() == deployments.size()) return new DeployResult.Fail(errors);
		return !errors.isEmpty() ? new DeployResult.DoneWithErrors(errors, success) : new DeployResult.Done(success);
	}

	private void deploy(Deployment deployment) throws IntinoException {
		try {
			final Artifact.Package aPackage = safe(() -> configuration.artifact().packageConfiguration());
			if (aPackage == null) throw new FatalIntinoException("Package configuration not found");
			if (!aPackage.isRunnable())
				throw new FatalIntinoException("Packaging must be runnable and have Main Class");
			final Map.Entry<String, String> cesar = getSafeInstance(module.getProject()).cesar();
			if (deployment.server() == null)
				throw new IntinoException("Server " + deployment.server().name() + " not found");
			List<Configuration.Parameter> incorrectParameters = incorrectParameters(deployment.runConfiguration().finalArguments());
			if (!incorrectParameters.isEmpty())
				throw new IntinoException("Parameters missed: " + incorrectParameters.stream().map(Configuration.Parameter::name).collect(Collectors.joining("; ")));
			new ApiAccessor(urlOf(cesar.getKey()), cesar.getValue()).postDeployProcess(createDeployment(aPackage, deployment));
		} catch (Forbidden | BadRequest | InternalServerError | Unauthorized e) {
			throw new IntinoException(e.getMessage());
		}
	}

	private List<Configuration.Parameter> incorrectParameters(Map<String, String> arguments) {
		return configuration.artifact().parameters().stream().filter(p -> !arguments.containsKey(p.name()) || arguments.get(p.name()) == null).collect(Collectors.toList());
	}

	private ProcessDeployment createDeployment(Artifact.Package packageConfiguration, Deployment destination) {
		final String classpathPrefix = packageConfiguration.classpathPrefix();
		return new ProcessDeployment().
				groupId(configuration.artifact().groupId()).artifactId(configuration.artifact().name().toLowerCase()).version(configuration.artifact().version()).
				vcs(new ProcessDeployment.Vcs().
						url(safe(() -> GitUtil.repository(module).getRemotes().iterator().next().getFirstUrl())).
						commit(safe(() -> GitUtil.repository(module).getCurrentRevision()))).
				jvmOptions(destination.runConfiguration().vmOptions()).
				artifactoryList(artifactories()).
				requirements(requirements(destination)).
				packaging(new ProcessDeployment.Packaging().mainClass(packageConfiguration.mainClass()).parameterList(extractParameters(destination.runConfiguration())).classpathPrefix(classpathPrefix == null || classpathPrefix.isEmpty() ? "dependency" : classpathPrefix)).
				destinationServer(destination.server().name());
	}

	@NotNull
	private ProcessDeployment.Requirements requirements(Deployment destination) {
		final ProcessDeployment.Requirements requirements = new ProcessDeployment.Requirements();
		Deployment.Requirements r = destination.requirements();
		if (r != null) {
			if (r.minMemory() != 0) requirements.minMemory(r.minMemory());
			if (r.maxMemory() != 0) requirements.maxMemory(r.maxMemory());
			if (r.minHdd() != 0) requirements.hdd(r.minHdd());
		}
		return requirements;
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
		List<Configuration.Repository> repositories = new ArrayList<>(configuration.repositories());
		for (Module dependant : ModuleRootManager.getInstance(module).getDependencies()) {
			final Configuration dependantConf = IntinoUtil.configurationOf(dependant);
			if (dependantConf == null) continue;
			dependantConf.repositories().stream().filter(r -> !contains(repositories, r)).forEach(repositories::add);
		}
		return repositories;
	}

	private boolean contains(List<Configuration.Repository> repositories, Configuration.Repository repository) {
		return repositories.stream().anyMatch(r -> r != null && r.url() != null && r.url().equals(repository.url()));
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

	public abstract static class DeployResult {

		public static class Fail extends DeployResult {
			public List<IntinoException> errors;

			public Fail(List<IntinoException> errors) {
				this.errors = errors;
			}

			public List<IntinoException> errors() {
				return errors;
			}
		}

		public static class DoneWithErrors extends Fail {
			private final List<String> success;

			public DoneWithErrors(List<IntinoException> errors, List<String> success) {
				super(errors);
				this.success = success;
			}

			public List<String> success() {
				return success;
			}
		}

		public static class Done extends DeployResult {
			private final List<String> successMessages;

			public Done(List<String> successMessages) {
				this.successMessages = successMessages;
			}

			public List<String> successMessages() {
				return successMessages;
			}
		}

	}
}
