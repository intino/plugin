package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Model;
import io.intino.Configuration.Repository;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.builders.ModelBuilderManager;
import io.intino.tara.dsls.Meta;
import io.intino.tara.dsls.Proteo;
import io.intino.tara.dsls.Tara;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;

public class LanguageResolver {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);
	private final Module module;
	private final List<Repository> repositories;
	private final Model model;
	private final MavenDependencyResolver resolver;
	private String version;

	public LanguageResolver(Module module, Model model, String version, List<Repository> repositories) {
		this.module = module;
		this.repositories = repositories;
		this.model = model;
		this.version = version;
		this.resolver = new MavenDependencyResolver(remotes());
	}

	public void resolve() {
		if (model == null) return;
		LanguageManager.silentReload(this.module.getProject(), model.language().name(), version);
		new ModelBuilderManager(module, repositories, model).resolveBuilder();
	}

	public DependencyCatalog languageFramework() {
		final Module dependency = moduleDependencyOf(this.module, model.language().name(), version);
		return dependency != null ? resolveLanguageModule(dependency) : resolveLanguageLibrary();
	}

	private DependencyCatalog resolveLanguageModule(Module dependency) {
		DependencyCatalog catalog = new ModuleDependencyResolver().resolveDependencyWith(dependency, JavaScopes.COMPILE);
		final Configuration configuration = IntinoUtil.configurationOf(dependency);
		if (configuration != null) model.language().effectiveVersion(configuration.artifact().version());
		return catalog;
	}

	private DependencyCatalog resolveLanguageLibrary() {
		DependencyCatalog catalog = new DependencyCatalog();
		if (!LanguageManager.getLanguageFile(model.language().name(), version).exists()) version = importLanguage();
		catalog.addAll(findLanguageFramework(languageId(model.language().name(), version)));
		model.language().effectiveVersion(version);
		resolveSources(catalog.dependencies());
		return catalog;
	}

	public String frameworkCoors() {
		return languageId(model.language().name(), version);
	}

	private void resolveSources(List<Dependency> framework) {
		resolver.resolveSources(framework.get(0).getArtifact());
	}

	private List<Dependency> findLanguageFramework(String frameworkCoors) {
		try {
			if (frameworkCoors == null) return Collections.emptyList();
			String[] coors = frameworkCoors.split(":");
			DependencyResult resolved = resolver.resolve(new DefaultArtifact(coors[0], coors[1].toLowerCase(), "jar", coors[2]), JavaScopes.COMPILE);
			return MavenDependencyResolver.dependenciesFrom(resolved, false);
		} catch (DependencyResolutionException e) {
			LOG.error(e);
			return Collections.emptyList();
		}
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
		final List<Module> modules = Arrays.stream(ModuleManager.getInstance(languageModule.getProject()).getModules()).filter(m -> !m.equals(languageModule)).toList();
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
		else try (JarFile jarFile = new JarFile(languageFile)) {
			Manifest manifest = jarFile.getManifest();
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
