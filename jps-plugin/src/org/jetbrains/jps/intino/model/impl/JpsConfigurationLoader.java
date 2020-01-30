package org.jetbrains.jps.intino.model.impl;

import io.intino.konos.compiler.shared.KonosBuildConstants;
import io.intino.tara.compiler.shared.TaraBuildConstants;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.maven.model.JpsMavenExtensionService;
import org.jetbrains.jps.maven.model.impl.MavenModuleResourceConfiguration;
import org.jetbrains.jps.maven.model.impl.MavenProjectConfiguration;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.JpsModelSerializationDataService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.konos.compiler.shared.KonosBuildConstants.LIBRARY;
import static io.intino.konos.compiler.shared.KonosBuildConstants.PARENT_INTERFACE;
import static io.intino.tara.compiler.shared.TaraBuildConstants.*;

class JpsConfigurationLoader {
	private final JpsModule module;
	private final CompileContext context;

	private static String TARA = "tara.";

	JpsConfigurationLoader(JpsModule module, CompileContext context) {
		this.module = module;
		this.context = context;
	}

	JpsModuleConfiguration load() {
		final JpsModuleConfiguration conf = new JpsModuleConfiguration();
		File confFile = new File(JpsModelSerializationDataService.getBaseDirectory(module.getProject()), ".intino/artifacts/" + module.getName() + ".conf");
		if (confFile.exists()) {
			fillFromTara(conf, confFile);
		}
		final MavenProjectConfiguration maven = JpsMavenExtensionService.getInstance().getMavenProjectConfiguration(context.getProjectDescriptor().dataManager.getDataPaths());
		if (maven == null) return conf;
		final MavenModuleResourceConfiguration moduleMaven = maven.moduleConfigurations.get(module.getName());
		fillFromMaven(conf, moduleMaven);
		return conf;
	}

	private void fillFromTara(JpsModuleConfiguration conf, File confFile) {
		try {
			Map<String, String> parameters = Files.readAllLines(confFile.toPath()).stream().
					collect(Collectors.toMap(s -> s.split("=")[0], s -> s.split("=")[1]));
			conf.groupId = parameters.get(GROUP_ID);
			conf.artifactId = parameters.get(ARTIFACT_ID);
			conf.version = parameters.get(VERSION);
			conf.language = parameters.get(LANGUAGE);
			conf.languageVersion = parameters.get(LANGUAGE_VERSION);
			conf.level = parameters.get(LEVEL);
			conf.outDsl = parameters.get(OUT_DSL);
			conf.generationPackage = parameters.get(TaraBuildConstants.GENERATION_PACKAGE);
			conf.languageGenerationPackage = parameters.getOrDefault(LANGUAGE_GENERATION_PACKAGE, "");
			conf.parameters = parameters.getOrDefault(KonosBuildConstants.PARAMETERS, "");
			conf.parentInterface = parameters.getOrDefault(PARENT_INTERFACE, "");
			conf.library = parameters.getOrDefault(LIBRARY, "");
		} catch (IOException ignored) {
		}
	}

	private void fillFromMaven(JpsModuleConfiguration conf, MavenModuleResourceConfiguration pom) {
		if (pom == null) return;
		final Map<String, String> props = pom.properties;
		conf.groupId = pom.id.groupId;
		conf.artifactId = pom.id.artifactId;
		conf.version = pom.id.version;
		conf.level = props.getOrDefault(TARA + LEVEL, "");
		conf.language = props.getOrDefault(TARA + LANGUAGE, "") + ":" + props.getOrDefault(TARA + LANGUAGE_VERSION, "");
		conf.languageVersion = props.getOrDefault(TARA + LANGUAGE_VERSION, "");
		conf.outDsl = props.getOrDefault(TARA + OUT_DSL, "");
		conf.generationPackage = props.getOrDefault(TARA + GENERATION_PACKAGE, props.getOrDefault(TARA + OUT_DSL, ""));
	}
}
