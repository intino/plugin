package io.intino.plugin.project.configuration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import io.intino.Configuration;
import io.intino.ProjectConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ConfigurationManager {
	private static final Map<Module, Configuration> registeredModules = new HashMap<>();
	private static final Map<Project, ProjectConfiguration> projectConfigurations = new HashMap<>();
	private static final Set<Class<? extends Configuration>> providers = new LinkedHashSet<>();

	public static Configuration register(Module module, Configuration configuration) {
		registeredModules.put(module, configuration);
		register(module.getProject());
		return configuration.init();
	}

	public static ProjectConfiguration register(Project project) {
		if (projectConfigurations.containsKey(project)) return projectConfigurations.get(project);
		ProjectLegioConfiguration configuration = new ProjectLegioConfiguration(project);
		projectConfigurations.put(project, configuration);
		return configuration.init();

	}

	public static Configuration configurationOf(Module module) {
		return registeredModules.get(module);
	}

	public static ProjectConfiguration projectConfigurationOf(@NotNull Module module) {
		return projectConfigurationOf(module.getProject());
	}

	public static ProjectConfiguration projectConfigurationOf(@NotNull Project project) {
		return projectConfigurations.get(project);
	}

	public static void unregister(Module module) {
		registeredModules.remove(module);
	}

	public static void registerProvider(Class<? extends Configuration> configuration) {
		providers.add(configuration);
	}

	public static void unregisterProvider(Class<? extends Configuration> configuration) {
		providers.remove(configuration);
	}

	public static boolean hasExternalProviders() {
		return !providers.isEmpty();
	}

	public static Configuration newExternalProvider(Module module) {
		if (providers.isEmpty()) return null;
		final Class<? extends Configuration> provider = providers.iterator().next();
		try {
			return (Configuration) provider.getDeclaredConstructors()[0].newInstance(module);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Configuration newSuitableProvider(Module module) {
		for (Class<? extends Configuration> provider : providers) {
			try {
				final Configuration configuration = (Configuration) provider.getDeclaredConstructors()[0].newInstance(module);
				if (configuration.isSuitable()) return configuration;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
			}
		}
		return null;
	}
}
