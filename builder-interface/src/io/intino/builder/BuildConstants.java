package io.intino.builder;

public class BuildConstants {
	public static final String TARAC = "Tarac";
	public static final String MAKE = "make";
	public static final String TEST = "test.module";
	public static final String ENCODING = "encoding";
	public static final String OUTPUTPATH = "outputpath";
	public static final String PROJECT = "project";
	public static final String REFRESH_MESSAGE = "%%refresh%%";
	public static final String REFRESH_MESSAGE_SEPARATOR = "#";
	public static final String SRC_PATH = "src.path";
	public static final String TMP_PATH = "tmp.path";
	public static final String FINAL_OUTPUTPATH = "final_outputpath";
	public static final String RES_PATH = "res.path";
	public static final String SRC_FILE = "def.file";
	public static final String COMPILED_START = "%%c";
	public static final String COMPILED_END = "/%c";
	public static final String BUILD_END = "%end%";
	public static final String TO_RECOMPILE_START = "%%rc";
	public static final String MESSAGE_ACTION_START = "%%action";
	public static final String MESSAGE_ACTION_END = "/%action";
	public static final String MESSAGE_ACTION_SEPARATOR = "#%%####%%%#%#####%#";
	public static final String ACTION_MESSAGE = "%%postaction%%";
	public static final String START_ACTIONS_MESSAGE = "%%postaction%%";
	public static final String END_ACTIONS_MESSAGE = "/%postaction%%";
	public static final String TO_RECOMPILE_END = "/%rc";
	public static final String MESSAGES_START = "%%m";
	public static final String ERROR_MESSAGE_START = "%%merror";
	public static final String ERROR_MESSAGE_END = "/%%merror";
	public static final String MESSAGES_END = "/%m";
	public static final String SEPARATOR = "#%%#%%%#%%%%%%%%%#";
	public static final String PRESENTABLE_MESSAGE = "@#$%@# Presentable:";
	public static final String CLEAR_PRESENTABLE = "$@#$%^ CLEAR_PRESENTABLE";
	public static final String INVOKED_PHASE = "phase";
	public static final String MODULE = "module";
	public static final String MODULE_PATH = "module.path";
	public static final String PROJECT_PATH = "project.path";
	public static final String REPOSITORY_PATH = "repository.path";
	public static final String PARAMETERS = "module.parameters";
	public static final String DATAHUB = "datahub";
	public static final String ARCHETYPE = "archetype";
	public static final String CURRENT_DEPENDENCIES = "current.dependencies";
	public static final String DSL = "dsl";
	public static final String BUILDER = "builder";
	public static final String RUNTIME = "runtime";
	public static final String NO_BUILDER = "Cannot compile Tara files: No Tara builder is defined";
	public static final String DSL_VERSION = "dsl.version";
	public static final String OUT_DSL = "out.dsl";
	public static final String OUT_DSL_VERSION = "out.dsl.version";
	public static final String EXCLUDED_PHASES = "excluded_phases";
	public static final String DSL_GENERATION_PACKAGE = "dsl.generation.package";
	public static final String PARENT_INTERFACE = "parent.interface";
	public static final String RELEASE_DISTRIBUTION = "distribution.release";
	public static final String REPOSITORY = "repository";
	public static final String SNAPSHOT_DISTRIBUTION = "distribution.snapshot";
	public static final String INTINO_PROJECT_PATH = "intino.project.path";
	public static final String GENERATION_PACKAGE = "generation.package";
	public static final String COMPILATION_MODE = "compilation.mode";
	public static final String LEVEL = "level";
	public static final String GROUP_ID = "groupId";
	public static final String ARTIFACT_ID = "artifactId";
	public static final String VERSION = "version";
	public static final String BUILDER_GROUP_ID = "builder.groupId";
	public static final String BUILDER_ARTIFACT_ID = "builder.artifactId";
	public static final String BUILDER_VERSION = "builder.version";
	public static final String BUILDER_GENERATION_PACKAGE = "builder.generation.package";
	public static final String RUNTIME_GROUP_ID = "runtime.groupId";
	public static final String RUNTIME_ARTIFACT_ID = "runtime.artifactId";
	public static final String RUNTIME_VERSION = "runtime.version";
	public static final String BUILD_FAILED = "error building";

	private BuildConstants() {
	}

	public static String normalizeForManifest(String key) {
		return key.replace(".", "-");
	}

	public enum Mode {
		Build, OnlyElements, Export
	}
}