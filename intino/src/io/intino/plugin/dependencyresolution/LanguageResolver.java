package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.jcabi.aether.Aether;
import io.intino.legio.graph.Repository;
import io.intino.legio.graph.level.LevelArtifact;
import io.intino.plugin.dependencyresolution.DependencyCatalog.Dependency;
import io.intino.plugin.project.builders.ModelBuilderManager;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.dsl.Meta;
import io.intino.tara.dsl.Proteo;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static io.intino.plugin.dependencyresolution.DependencyCatalog.DependencyScope;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;

public class LanguageResolver {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);

	private final Module module;
	private final DependencyAuditor auditor;
	private final List<Repository.Type> repositories;
	private final LevelArtifact.Model model;
	private String version;
	private File localRepository = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");

	public LanguageResolver(Module module, DependencyAuditor auditor, LevelArtifact.Model model, String version, List<Repository.Type> repositories) {
		this.module = module;
		this.auditor = auditor;
		this.repositories = repositories;
		this.model = model;
		this.version = version;
	}

	public static Module moduleDependencyOf(Module languageModule, String language, String version) {
		final List<Module> modules = Arrays.stream(ModuleManager.getInstance(languageModule.getProject()).getModules()).filter(m -> !m.equals(languageModule)).collect(Collectors.toList());
		for (Module m : modules) {
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration == null) continue;
			if (language.equalsIgnoreCase(configuration.artifactId())) return m;
		}
		return null;
	}

	public static String languageId(String language, String version) {
		if (isMagritteLibrary(language)) return magritteID(version);
		final File languageFile = LanguageManager.getLanguageFile(language, version);
		if (!languageFile.exists()) return null;
		else try {
			Manifest manifest = new JarFile(languageFile).getManifest();
			final Attributes tara = manifest.getAttributes("tara");
			if (tara == null) return null;
			return tara.getValue("framework");
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	@NotNull
	private static String magritteID(String version) {
		return Proteo.GROUP_ID + ":" + Proteo.ARTIFACT_ID + ":" + version;
	}

	private static boolean isMagritteLibrary(String language) {
		return Proteo.class.getSimpleName().equals(language) || Meta.class.getSimpleName().equals(language);
	}

	public DependencyCatalog resolve() {
		if (model == null) return new DependencyCatalog();
		LanguageManager.silentReload(this.module.getProject(), model.language(), version);
		final DependencyCatalog libraries = languageFramework();
		new ModelBuilderManager(module.getProject(), model).resolveBuilder();
		return libraries;
	}

	private DependencyCatalog languageFramework() {
		ResolutionCache cache = ResolutionCache.instance(module.getProject());
		String languageId = languageId(model.language(), version);
		if (!auditor.isModified(model.core$())) {
			List<Dependency> dependencies = cache.get(languageId);
			if (dependencies != null && !dependencies.isEmpty()) {
				model.effectiveVersion(dependencies.get(0).version);
				return new DependencyCatalog(dependencies);
			}
		}
		final Module dependency = moduleDependencyOf(this.module, model.language(), version);
		DependencyCatalog catalog = dependency != null ? resolveModuleLanguage(dependency) : resolveExternalLanguage();
		cache.put(languageId, catalog.dependencies());
		cache.save();
		return catalog;
	}

	private DependencyCatalog resolveModuleLanguage(Module dependency) {
		DependencyCatalog catalog = new ModuleDependencyResolver().resolveDependencyTo(dependency);
		final Configuration configuration = TaraUtil.configurationOf(dependency);
		if (configuration != null) model.effectiveVersion(configuration.version());
		return catalog;
	}

	private DependencyCatalog resolveExternalLanguage() {
		DependencyCatalog catalog = new DependencyCatalog();
		if (!LanguageManager.getLanguageFile(model.language(), version).exists()) version = importLanguage();
		final Map<Artifact, DependencyScope> framework = findLanguageFramework(languageId(model.language(), version));
		model.effectiveVersion(version);
		if (framework.isEmpty()) return catalog;
		resolveSources(catalog, framework);
		return catalog;
	}

	private void resolveSources(DependencyCatalog catalog, Map<Artifact, DependencyScope> framework) {
		Artifact mainArtifact = framework.keySet().iterator().next();
		framework.forEach((a, scope) -> catalog.add(new Dependency(a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion() + ":" + scope.name(), a.getFile())));
		catalog.dependencies().get(0).sources(sourcesOf(mainArtifact));
		model.effectiveVersion(!framework.isEmpty() ? framework.keySet().iterator().next().getVersion() : "");
	}

	private Map<Artifact, DependencyScope> findLanguageFramework(String languageId) {
		try {
			if (languageId == null) return Collections.emptyMap();
			Aether aether = new Aether(frameworkRemotes(), localRepository);
			List<Artifact> resolve = aether.resolve(new DefaultArtifact(languageId), JavaScopes.COMPILE);
			return toMap(resolve, DependencyScope.COMPILE);
		} catch (DependencyResolutionException e) {
			return Collections.emptyMap();
		}
	}

	private boolean sourcesOf(Artifact artifact) {
		try {
			DefaultArtifact root = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "sources", "jar", artifact.getVersion());
			return !new Aether(collectRemotes(), localRepository).resolve(root, JavaScopes.COMPILE).isEmpty();
		} catch (DependencyResolutionException ignored) {
			return false;
		}
	}

	private Map<Artifact, DependencyScope> toMap(List<Artifact> artifacts, DependencyScope scope) {
		Map<Artifact, DependencyScope> map = new LinkedHashMap<>();
		artifacts.forEach(a -> map.put(a, scope));
		return map;
	}

	private String importLanguage() {
		return new LanguageImporter(module, TaraUtil.configurationOf(module)).importLanguage(model.language(), version);
	}

	@NotNull
	private List<RemoteRepository> collectRemotes() {
		List<RemoteRepository> remotes = new ArrayList<>(repositories.stream().map(this::remoteFrom).filter(Objects::nonNull).collect(Collectors.toList()));
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/").
				setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		return remotes;
	}

	@NotNull
	private List<RemoteRepository> frameworkRemotes() {
		List<RemoteRepository> remotes = new ArrayList<>(repositories.stream().filter(r -> !r.i$(Repository.Language.class)).map(this::remoteFrom).filter(Objects::nonNull).collect(Collectors.toList()));
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/").
				setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		return remotes;
	}

	private RemoteRepository remoteFrom(Repository.Type remote) {
		if (remote.core$().variables().get("mavenID").get(0) == null) return null;
		return new RemoteRepository(remote.mavenID(), "default", remote.url()).
				setAuthentication(provideAuthentication(remote.mavenID())).
				setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY));
	}

	private Authentication provideAuthentication(String mavenId) {
		final IntinoSettings settings = IntinoSettings.getSafeInstance(module.getProject());
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(mavenId))
				return new Authentication(credential.username, credential.password);
		return null;
	}
}
