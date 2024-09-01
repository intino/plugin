package io.intino.plugin.project.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.intino.Configuration;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static io.intino.builder.BuildConstants.*;
import static java.util.stream.Collectors.toList;

public class ArtifactSerializer {
	public static final String EQ = "=";
	public static final String NL = "\n";
	private final LegioArtifact artifact;

	public ArtifactSerializer(LegioArtifact artifact) {
		this.artifact = artifact;
	}

	public String serialize() {
		String content = GROUP_ID + EQ + artifact.groupId() + NL +
						 ARTIFACT_ID + EQ + artifact.name() + NL +
						 VERSION + EQ + artifact.version() + NL +
						 PARAMETERS + EQ + artifact.parameters().stream().map(Configuration.Parameter::name).collect(Collectors.joining(";")) + NL +
						 GENERATION_PACKAGE + EQ + artifact.code().generationPackage() + NL;
		content += serializeDsls(artifact.dsls());
		content += serialize(artifact.datahub());
		content += serialize(artifact.archetype());
		String dependencies = serializeDependencies();
		if (!dependencies.isEmpty()) content += CURRENT_DEPENDENCIES + EQ + dependencies;
		return content;
	}

	public String serializeDependencies() {
		List<String> dependencies = artifact.dependencies().stream()
				.filter(d -> d.scope().equalsIgnoreCase(JavaScopes.COMPILE) && d.groupId().startsWith("io.intino"))
				.map(Configuration.Artifact.Dependency::identifier)
				.collect(toList());
		return !dependencies.isEmpty() ? String.join(",", dependencies) + NL : "";
	}

	@NotNull
	private static String serialize(Configuration.Artifact.Dependency.Archetype archetype) {
		return archetype != null ? ARCHETYPE + EQ + archetype.identifier() + NL : "";
	}

	private String serialize(Configuration.Artifact.Dependency.DataHub datahub) {
		String builder = "";
		if (datahub != null) builder += DATAHUB + EQ + datahub.identifier() + NL;
		if (datahub != null)
			builder += "library" + EQ + datahub.identifier() + NL;//FIXME added by retro-compatibility. remove in future
		return builder;
	}

	private String serializeDsls(List<Configuration.Artifact.Dsl> dsls) {
		JsonArray elements = new JsonArray(dsls.size());
		dsls.stream().filter(d -> d.name() != null && d.version() != null).map(this::objectOf).forEach(elements::add);
		return DSL + EQ + elements + NL;

	}

	private JsonObject objectOf(Configuration.Artifact.Dsl dsl) {
		JsonObject object = new JsonObject();
		object.add(DSL, new JsonPrimitive(dsl.name()));
		object.add(DSL_VERSION, new JsonPrimitive(dsl.version()));
		object.add(LEVEL, new JsonPrimitive(dsl.level().name()));
		if (dsl.generationPackage() != null)
			object.add(DSL_GENERATION_PACKAGE, new JsonPrimitive(dsl.generationPackage()));
		if (dsl.builder() != null && dsl.builder().groupId() != null) object.add(BUILDER, objectOf(dsl.builder()));
		if (dsl.runtime() != null && dsl.runtime().groupId() != null) object.add(RUNTIME, objectOf(dsl.runtime()));
		if (dsl.outputDsl() != null) object.add(OUT_DSL, objectOf(dsl.outputDsl()));
		return object;
	}

	private JsonObject objectOf(Configuration.Artifact.Dsl.Builder builder) {
		JsonObject object = new JsonObject();
		if (builder.groupId() == null) return object;
		object.add(BUILDER_GROUP_ID, new JsonPrimitive(builder.groupId()));
		object.add(BUILDER_ARTIFACT_ID, new JsonPrimitive(builder.artifactId()));
		object.add(BUILDER_VERSION, new JsonPrimitive(builder.version()));
		object.add(BUILDER_GENERATION_PACKAGE, new JsonPrimitive(builder.generationPackage()));
		List<Integer> excluded = builder.excludedPhases().stream().map(e -> e.ordinal() + 8).toList();
		if (!excluded.isEmpty()) {
			JsonArray jsonElements = new JsonArray(excluded.size());
			excluded.forEach(jsonElements::add);
			object.add(EXCLUDED_PHASES, jsonElements);
		}
		return object;
	}

	private JsonElement objectOf(Configuration.Artifact.Dsl.Runtime runtime) {
		JsonObject object = new JsonObject();
		if (runtime.groupId() == null) return object;
		object.add(RUNTIME_GROUP_ID, new JsonPrimitive(runtime.groupId()));
		object.add(RUNTIME_ARTIFACT_ID, new JsonPrimitive(runtime.artifactId()));
		object.add(RUNTIME_VERSION, new JsonPrimitive(runtime.version()));
		return object;
	}

	private static JsonObject objectOf(Configuration.Artifact.Dsl.OutputDsl outputDsl) {
		JsonObject object = new JsonObject();
		object.add(OUT_DSL, new JsonPrimitive(outputDsl.name()));
		object.add(OUT_DSL_VERSION, new JsonPrimitive(outputDsl.version()));
		Configuration.Artifact.Dsl.OutputBuilder builder = outputDsl.builder();
		if (builder != null) {
			object.add(OUT_DSL + "." + BUILDER_GROUP_ID, new JsonPrimitive(builder.groupId()));
			object.add(OUT_DSL + "." + BUILDER_ARTIFACT_ID, new JsonPrimitive(builder.artifactId()));
			object.add(OUT_DSL + "." + BUILDER_VERSION, new JsonPrimitive(builder.version()));
		}
		Configuration.Artifact.Dsl.Runtime runtime = outputDsl.runtime();
		if (runtime != null && runtime.groupId() != null && runtime.artifactId() != null && runtime.version() != null) {
			object.add(OUT_DSL + "." + RUNTIME_GROUP_ID, new JsonPrimitive(runtime.groupId()));
			object.add(OUT_DSL + "." + RUNTIME_ARTIFACT_ID, new JsonPrimitive(runtime.artifactId()));
			object.add(OUT_DSL + "." + RUNTIME_VERSION, new JsonPrimitive(runtime.version()));
		}
		return object;
	}
}
