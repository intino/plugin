package io.intino.plugin.project.configuration.maven;

public interface MavenTags {
	String VERSION = "version";
	String DEPENDENCY = "dependency";
	String DEPENDENCIES = "dependencies";
	String REPOSITORY = "repository";
	String GROUP_ID = "groupId";
	String ARTIFACT_ID = "artifactId";
	String URL = "url";
	String ID = "id";

	String LEVEL = "tara.level";

	String DSL = "tara.dsl";
	String DSL_VERSION = "tara.dsl.version";
	String DSL_BUILDER_GROUP_ID = "tara.dsl.builder.groupId";
	String DSL_BUILDER_ARTIFACT_ID = "tara.dsl.builder.artifactId";
	String DSL_BUILDER_VERSION = "tara.dsl.builder.version";
	String DSL_BUILDER_GENERATION_PACKAGE = "tara.dsl.builder.generation.package";
	String DSL_RUNTIME_GROUP_ID = "tara.dsl.builder.groupId";
	String DSL_RUNTIME_ARTIFACT_ID = "tara.dsl.builder.artifactId";
	String DSL_RUNTIME_VERSION = "tara.dsl.builder.version";
	String DSL_OUTPUT_BUILDER_GROUP_ID = "tara.dsl.output.builder.groupId";
	String DSL_OUTPUT_BUILDER_ARTIFACT_ID = "tara.dsl.output.builder.artifactId";
	String DSL_OUTPUT_BUILDER_VERSION = "tara.dsl.output.builder.version";
	String OUT_DSL = "tara.out.dsl";
	String INTINO_GENERATION_PACKAGE = "tara.generation.package";
	String OUT_DSL_VERSION = "tara.out.dsl.version";
}
