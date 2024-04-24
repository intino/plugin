package org.jetbrains.jps.intino.compiler.tara;

import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration.Dsl;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration.Dsl.Builder;
import org.jetbrains.jps.model.JpsProject;

import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.builder.BuildConstants.*;

public class RunConfigurationRenderer {
	private static final char NL = '\n';
	private final JpsProject project;
	private final ModuleChunk module;
	private final JpsModuleConfiguration conf;
	private final Map<String, Boolean> sources;
	private final String dsl;
	private final IntinoPaths paths;
	private final boolean make;
	private final String encoding;

	public RunConfigurationRenderer(JpsProject project, ModuleChunk module, JpsModuleConfiguration conf, Map<String, Boolean> sources, String dsl, IntinoPaths paths, boolean make, String encoding) {
		this.project = project;
		this.module = module;
		this.conf = conf;
		this.sources = sources;
		this.dsl = dsl;
		this.paths = paths;
		this.make = make;
		this.encoding = encoding;
	}

	public String build() {
		StringWriter writer = new StringWriter();
		writer.write(SRC_FILE + NL);
		for (Map.Entry<String, Boolean> file : sources.entrySet())
			writer.write(file.getKey() + "#" + file.getValue() + NL);
		writer.write(NL);
		writer.write(PROJECT + NL + project.getName() + NL);
		writer.write(MODULE + NL + module.getName() + NL);
		writePaths(paths, writer);
		if (conf != null) fillConfiguration(conf, writer);
		writer.write(MAKE + NL + make + NL);
		writer.write(TEST + NL + module.containsTests() + NL);
		writer.write(ENCODING + NL + encoding + NL);
		if (!conf.parameters.isEmpty()) writer.write(PARAMETERS + NL + conf.parameters + NL);
		if (!conf.parentInterface.isEmpty()) writer.write(PARENT_INTERFACE + NL + conf.parentInterface + NL);
		if (!conf.datahub.isEmpty()) {
			writer.write(DATAHUB + NL + conf.datahub + NL);
			writer.write("library" + NL + conf.datahub + NL);
		}
		if (!conf.archetype.isEmpty()) writer.write(ARCHETYPE + NL + conf.archetype + NL);
		if (!conf.dependencies.isEmpty()) writer.write(CURRENT_DEPENDENCIES + NL + conf.dependencies + NL);
		return writer.toString();
	}

	private void fillConfiguration(JpsModuleConfiguration conf, StringWriter writer) {
		if (!conf.groupId.isEmpty()) writer.write(GROUP_ID + NL + conf.groupId + NL);
		if (!conf.artifactId.isEmpty()) writer.write(ARTIFACT_ID + NL + conf.artifactId + NL);
		if (!conf.version.isEmpty()) writer.write(VERSION + NL + conf.version + NL);
		Dsl dslConf = conf.dsls.stream().filter(d -> d.name().equalsIgnoreCase(dsl)).findFirst().orElse(null);
		if (dslConf != null) fillDslConfiguration(dslConf, writer);
	}

	private void fillDslConfiguration(Dsl conf, StringWriter writer) {
		if (conf == null) return;
		writer.write(DSL + NL + conf.name() + ":" + conf.version() + NL);
		writer.write(LEVEL + NL + conf.level() + NL);
		Builder builder = conf.builder();
		if (builder != null) {
			if (builder.excludedPhases() != null)
				writer.write(EXCLUDED_PHASES + NL + builder.excludedPhases().stream().map(Object::toString).collect(Collectors.joining(" ")) + NL);
			writer.write(GENERATION_PACKAGE + NL + builder.generationPackage() + NL);
		}
		Dsl.OutDsl outDsl = conf.outDsl();
		if (outDsl != null) {
			writer.write(OUT_DSL + NL + outDsl.name() + NL);
			Builder outBuilder = outDsl.builder();
			if (outBuilder != null) {
				writer.write(OUT_DSL + "." + BUILDER_GROUP_ID + NL + outBuilder.groupId() + NL);
				writer.write(OUT_DSL + "." + BUILDER_ARTIFACT_ID + NL + outBuilder.artifactId() + NL);
				writer.write(OUT_DSL + "." + BUILDER_VERSION + NL + outBuilder.version() + NL);
			}
			Dsl.Runtime runtime = outDsl.runtime();
			if (runtime != null) {
				writer.write(OUT_DSL + "." + RUNTIME_GROUP_ID + NL + runtime.groupId() + NL);
				writer.write(OUT_DSL + "." + RUNTIME_ARTIFACT_ID + NL + runtime.artifactId() + NL);
				writer.write(OUT_DSL + "." + RUNTIME_VERSION + NL + runtime.version() + NL);
			}
		}
	}

	private void writePaths(IntinoPaths paths, StringWriter writer) {
		writer.write(PROJECT_PATH + NL + paths.projectPath + NL);
		writer.write(MODULE_PATH + NL + paths.modulePath + NL);
		writer.write(OUTPUTPATH + NL + paths.genRoot + NL);
		writer.write(FINAL_OUTPUTPATH + NL + paths.finalOutput + NL);
		writer.write(RES_PATH + NL + paths.resRoot + NL);
		writer.write(REPOSITORY_PATH + NL + paths.repository + NL);
		writer.write(INTINO_PROJECT_PATH + NL + paths.intinoConfDirectory + NL);
		writer.write(SRC_PATH + NL);
		writer.write(String.join("\n", paths.srcRoots));
		writer.write(NL);
		writer.write(NL);
	}

}

