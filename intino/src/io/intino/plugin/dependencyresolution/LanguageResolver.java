package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.jcabi.aether.Aether;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Model;
import io.intino.Configuration.Repository;
import io.intino.magritte.dsl.Meta;
import io.intino.magritte.dsl.Proteo;
import io.intino.magritte.dsl.Tara;
import io.intino.plugin.dependencyresolution.DependencyCatalog.Dependency;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.builders.ModelBuilderManager;
import io.intino.plugin.project.configuration.model.LegioModel;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
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
	private final List<Repository> repositories;
	private final Model model;
	private final File localRepository = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	private String version;

	public LanguageResolver(Module module, DependencyAuditor auditor, Model model, String version, List<Repository> repositories) {
		this.module = module;
		this.auditor = auditor;
		this.repositories = repositories;
		this.model = model;
		this.version = version;
	}

	public DependencyCatalog resolve() {
		if (model == null) return new DependencyCatalog();
		LanguageManager.silentReload(this.module.getProject(), model.language().name(), version);
		final DependencyCatalog libraries = languageFramework();
		new ModelBuilderManager(module.getProject(), model).resolveBuilder();
		return libraries;
	}

	private DependencyCatalog languageFramework() {
		ResolutionCache cache = ResolutionCache.instance(module.getProject());
		String languageId = languageId(model.language().name(), version);
		if (!auditor.isModified(((LegioModel) model).node())) {
			List<Dependency> dependencies = cache.get(languageId);
			if (dependencies != null && !dependencies.isEmpty()) {
				model.language().effectiveVersion(version.equalsIgnoreCase("LATEST") ? dependencies.get(0).version : version);
				return new DependencyCatalog(dependencies);
			}
		}
		final Module dependency = moduleDependencyOf(this.module, model.language().name(), version);
		DependencyCatalog catalog = dependency != null ? resolveModuleLanguage(dependency) : resolveLibraryLanguage();
		cache.put(languageId, catalog.dependencies());
		cache.save();
		return catalog;
	}

	private DependencyCatalog resolveModuleLanguage(Module dependency) {
		DependencyCatalog catalog = new ModuleDependencyResolver().resolveDependencyTo(dependency, "COMPILE");
		final Configuration configuration = IntinoUtil.configurationOf(dependency);
		if (configuration != null) model.language().effectiveVersion(configuration.artifact().version());
		return catalog;
	}

	private DependencyCatalog resolveLibraryLanguage() {
		DependencyCatalog catalog = new DependencyCatalog();
		if (!LanguageManager.getLanguageFile(model.language().name(), version).exists()) version = importLanguage();
		final Map<Artifact, DependencyScope> framework = findLanguageFramework(languageId(model.language().name(), version));
		model.language().effectiveVersion(version);
		if (framework.isEmpty()) return catalog;
		resolveSources(catalog, framework);
		return catalog;
	}

	private void resolveSources(DependencyCatalog catalog, Map<Artifact, DependencyScope> framework) {
		Artifact mainArtifact = framework.keySet().iterator().next();
		framework.forEach((a, scope) -> catalog.add(new Dependency(a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion() + ":" + scope.name(), a.getFile())));
		catalog.dependencies().get(0).sources(sourcesOf(mainArtifact));
		model.language().effectiveVersion(!framework.isEmpty() ? framework.keySet().iterator().next().getVersion() : "");
	}

	private Map<Artifact, DependencyScope> findLanguageFramework(String frameworkCoors) {
		try {
			if (frameworkCoors == null) return Collections.emptyMap();
			Aether aether = new Aether(remotes(), localRepository);
			String[] coors = frameworkCoors.split(":");
			List<Artifact> resolve = aether.resolve(new DefaultArtifact(coors[0], coors[1], "jar", coors[2]), JavaScopes.COMPILE);
			return toMap(resolve, DependencyScope.COMPILE);
		} catch (DependencyResolutionException e) {
			return Collections.emptyMap();
		}
	}

	private boolean sourcesOf(Artifact artifact) {
		try {
			DefaultArtifact root = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "sources", "jar", artifact.getVersion());
			return !new Aether(remotes(), localRepository).resolve(root, JavaScopes.COMPILE).isEmpty();
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
		return new LanguageImporter(module, IntinoUtil.configurationOf(module)).importLanguage(model.language().name(), version);
	}

	@NotNull
	private List<RemoteRepository> remotes() {
		Repositories repositories = new Repositories(module);
		List<RemoteRepository> remotes = repositories.map(this.repositories);
		remotes.add(repositories.maven(UPDATE_POLICY_DAILY));
		return remotes;
	}

	public static Module moduleDependencyOf(Module languageModule, String language, String version) {
		final List<Module> modules = Arrays.stream(ModuleManager.getInstance(languageModule.getProject()).getModules()).filter(m -> !m.equals(languageModule)).collect(Collectors.toList());
		for (Module m : modules) {
			final Configuration configuration = IntinoUtil.configurationOf(m);
			if (configuration == null || configuration.artifact().model() == null) continue;
			if (language.equalsIgnoreCase(configuration.artifact().model().outLanguage())) return m;
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
		return Tara.GROUP_ID + ":" + Tara.ARTIFACT_ID + ":" + version;
	}

	private static boolean isMagritteLibrary(String language) {
		return Proteo.class.getSimpleName().equals(language) || Meta.class.getSimpleName().equals(language);
	}
}
