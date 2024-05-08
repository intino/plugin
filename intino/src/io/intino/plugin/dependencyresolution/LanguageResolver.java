package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dsl;
import io.intino.Configuration.Repository;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.DslBuilderManager;
import org.eclipse.aether.graph.Dependency;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static io.intino.builder.BuildConstants.*;
import static io.intino.plugin.project.Safe.safe;

public class LanguageResolver {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);
	private final Module module;
	private final List<Repository> repositories;

	public LanguageResolver(Module module, List<Repository> repositories) {
		this.module = module;
		this.repositories = repositories;
	}

	public List<Dependency> resolve(Dsl dsl) {
		if (dsl == null) return null;
		List<Dependency> languageDependencies = new DslImporter(module, repositories).importDsl(dsl);
		new DslBuilderManager(module, repositories, dsl).resolveBuilder();
		return languageDependencies;
	}

	public String runtimeCoors(Dsl dsl) {
		return runtimeCoors(dsl.name(), dsl.version());
	}

	public String runtimeCoors(Dsl dsl, String version) {
		return runtimeCoors(dsl.name(), version);
	}

	public static Module moduleDependencyOf(Module languageModule, String language, String version) {
		final List<Module> modules = Arrays.stream(ModuleManager.getInstance(languageModule.getProject()).getModules()).filter(m -> !m.equals(languageModule)).toList();
		for (Module m : modules) {
			final Configuration configuration = IntinoUtil.configurationOf(m);
			if (configuration != null || safe(() -> configuration.artifact().dsls().stream().anyMatch(d -> language.equalsIgnoreCase(d.outputDsl().name())), false))
				return m;
		}
		return null;
	}

	public static String runtimeCoors(String language, String version) {
		final File languageFile = LanguageManager.getLanguageFile(language, version);
		if (!languageFile.exists()) return null;
		else try (JarFile jarFile = new JarFile(languageFile)) {
			Manifest manifest = jarFile.getManifest();
			final Attributes tara = manifest.getAttributes("tara");
			if (tara == null) return null;
			String framework = tara.getValue("framework");
			return framework != null ? framework : coors(tara);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	private static String coors(Attributes tara) {
		return String.join(":", tara.getValue(normalizeForManifest(RUNTIME_GROUP_ID)),
				tara.getValue(normalizeForManifest(RUNTIME_ARTIFACT_ID)),
				tara.getValue(normalizeForManifest(RUNTIME_VERSION)));
	}
}
