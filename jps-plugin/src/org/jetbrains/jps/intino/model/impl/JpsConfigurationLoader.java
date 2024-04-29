package org.jetbrains.jps.intino.model.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration.Dsl;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration.Dsl.Builder;
import org.jetbrains.jps.maven.model.JpsMavenExtensionService;
import org.jetbrains.jps.maven.model.impl.MavenModuleResourceConfiguration;
import org.jetbrains.jps.maven.model.impl.MavenProjectConfiguration;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.JpsModelSerializationDataService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.builder.BuildConstants.*;

class JpsConfigurationLoader {
	private static final String TARA = "tara.";
	private final JpsModule module;
	private final CompileContext context;

	JpsConfigurationLoader(JpsModule module, CompileContext context) {
		this.module = module;
		this.context = context;
	}

	JpsModuleConfiguration load() throws ProjectBuildException {
		File confFile = new File(JpsModelSerializationDataService.getBaseDirectory(module.getProject()), ".intino/artifacts/" + module.getName() + ".conf");
		if (confFile.exists()) {
			return loadFromLegio(confFile);
		}
		final MavenProjectConfiguration maven = JpsMavenExtensionService.getInstance().getMavenProjectConfiguration(context.getProjectDescriptor().dataManager.getDataPaths());
		if (maven == null) return new JpsModuleConfiguration();
		final MavenModuleResourceConfiguration moduleMaven = maven.moduleConfigurations.get(module.getName());
		return loadFromMaven(moduleMaven);
	}

	private JpsModuleConfiguration loadFromLegio(File confFile) throws ProjectBuildException {
		final JpsModuleConfiguration conf = new JpsModuleConfiguration();
		try {
			Map<String, String> parameters = Files.readAllLines(confFile.toPath()).stream().filter(l -> l.contains("=")).
					collect(Collectors.toMap(s -> s.split("=")[0], s -> {
						String[] split = s.split("=");
						return split.length > 1 ? split[1] : "";
					}));
			if (parameters.isEmpty())
				throw new ProjectBuildException("Module configuration not found. Please reload artifact.");
			conf.groupId = parameters.getOrDefault(GROUP_ID, "");
			conf.artifactId = parameters.getOrDefault(ARTIFACT_ID, "");
			conf.version = parameters.get(VERSION);
			JsonArray elements = new Gson().fromJson(parameters.get(DSL), JsonArray.class);
			if (elements != null)
				elements.asList().stream().map(e -> (JsonObject) e).map(o -> toDsl(parameters, o)).forEach(d -> conf.dsls.add(d));
			conf.parameters = parameters.getOrDefault(PARAMETERS, "");
			conf.parentInterface = parameters.getOrDefault(PARENT_INTERFACE, "");
			conf.datahub = parameters.getOrDefault(DATAHUB, "");
			conf.archetype = parameters.getOrDefault(ARCHETYPE, "");
			conf.dependencies = parameters.getOrDefault(CURRENT_DEPENDENCIES, "");
		} catch (IOException ignored) {
		}
		return conf;
	}

	private Dsl toDsl(Map<String, String> parameters, JsonObject o) {
		return new Dsl(o.get(DSL).getAsString(),
				o.get(DSL_VERSION).getAsString(),
				o.get(LEVEL).getAsString(),
				o.has(DSL_GENERATION_PACKAGE) ? o.get(DSL_GENERATION_PACKAGE).getAsString() : null,
				o.has(BUILDER) ? builderOf(parameters, o.get(BUILDER).getAsJsonObject(), "") : null,
				o.has(RUNTIME) ? runtimeOf(o.get(RUNTIME).getAsJsonObject(), "") : null,
				o.has(OUT_DSL) ? outDslOf(o.get(OUT_DSL).getAsJsonObject()) : null);
	}

	private Dsl.Runtime runtimeOf(JsonObject object, String prefix) {
		if (object == null) return null;
		return new Dsl.Runtime(
				object.get(prefix + RUNTIME_GROUP_ID).getAsString(),
				object.get(prefix + RUNTIME_ARTIFACT_ID).getAsString(),
				object.get(prefix + RUNTIME_VERSION).getAsString());
	}

	private Builder builderOf(Map<String, String> parameters, JsonObject builderObj, String prefix) {
		if (builderObj == null) return null;
		return new Builder(
				builderObj.get(prefix + BUILDER_GROUP_ID).getAsString(),
				builderObj.get(prefix + BUILDER_ARTIFACT_ID).getAsString(),
				builderObj.get(prefix + BUILDER_VERSION).getAsString(),
				parameters.isEmpty() ? "" : generationPackage(parameters, builderObj),
				excludedPhases(builderObj));
	}

	@NotNull
	private static String generationPackage(Map<String, String> parameters, JsonObject builderObj) {
		String pack = parameters.get(GENERATION_PACKAGE);
		String builderPack = builderObj.get(BUILDER_GENERATION_PACKAGE).getAsString();
		return pack + (!builderPack.isEmpty() ? "." + builderPack : "");
	}

	private Dsl.OutDsl outDslOf(JsonObject object) {
		if (object == null) return null;
		String prefix = OUT_DSL + ".";
		return new Dsl.OutDsl(
				object.get(OUT_DSL).getAsString(),
				object.has(OUT_DSL + "." + BUILDER_GROUP_ID) ? builderOf(Map.of(), object, prefix) : null,
				object.has(OUT_DSL + "." + RUNTIME_GROUP_ID) ? runtimeOf(object, prefix) : null
		);
	}

	private static List<Integer> excludedPhases(JsonObject object) {
		JsonElement jsonElement = object.get(EXCLUDED_PHASES);
		return jsonElement == null ? null : jsonElement.getAsJsonArray().asList().stream().map(e -> e.getAsJsonPrimitive().getAsInt()).toList();
	}

	private JpsModuleConfiguration loadFromMaven(MavenModuleResourceConfiguration pom) {
		final JpsModuleConfiguration conf = new JpsModuleConfiguration();
		if (pom == null) return conf;
		final Map<String, String> props = pom.properties;
		conf.groupId = pom.id.groupId;
		conf.artifactId = pom.id.artifactId;
		conf.version = pom.id.version;
		conf.dsls = List.of(new Dsl(props.getOrDefault(TARA + DSL, ""),
				props.getOrDefault(TARA + DSL_VERSION, ""),
				props.getOrDefault(TARA + LEVEL, ""),
				props.getOrDefault(TARA + DSL_GENERATION_PACKAGE, ""),
				builderOf(props),
				null,
				new Dsl.OutDsl(props.getOrDefault(TARA + OUT_DSL, ""), null, null)
		));
		return conf;
	}

	private Builder builderOf(Map<String, String> props) {
		String outDsl = props.getOrDefault(TARA + OUT_DSL, "");
		String groupId = props.getOrDefault(TARA + BUILDER_GROUP_ID, "");
		String artifactId = props.getOrDefault(TARA + BUILDER_ARTIFACT_ID, "");
		String version = props.getOrDefault(TARA + BUILDER_VERSION, "");
		String generationPackage = props.get(TARA + GENERATION_PACKAGE) + "." + props.getOrDefault(TARA + BUILDER_GENERATION_PACKAGE, outDsl);
		String phases = props.getOrDefault(TARA + EXCLUDED_PHASES, "");
		List<Integer> excludedPhases = phases != null && !phases.isEmpty() ? Arrays.stream(phases.split(",")).map(Integer::parseInt).toList() : null;
		return new Builder(groupId, artifactId, version, generationPackage, excludedPhases);
	}
}
