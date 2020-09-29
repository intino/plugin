package io.intino.plugin.project.configuration;

import com.intellij.openapi.module.Module;
import io.intino.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ConfigurationManager {

	private static final Map<Module, Configuration> registeredModules = new HashMap<>();
	private static final Set<Class<? extends Configuration>> providers = new LinkedHashSet<>();

	public static Configuration register(Module module, Configuration configuration) {
		registeredModules.put(module, configuration);
		return configuration.init();
	}

	public static Configuration configurationOf(Module module) {
		return registeredModules.get(module);
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
		return providers.size() > 0;
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
