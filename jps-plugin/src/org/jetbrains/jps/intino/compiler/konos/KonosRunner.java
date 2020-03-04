package org.jetbrains.jps.intino.compiler.konos;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ExternalProcessUtil;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration;
import org.jetbrains.jps.service.SharedThreadPool;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static io.intino.konos.compiler.shared.KonosBuildConstants.*;

class KonosRunner {
	private static final char NL = '\n';
	private static final Logger LOG = Logger.getInstance(KonosRunner.class.getName());
	private static final int COMPILER_MEMORY = 600;
	private static final String MINIMUM_VERSION = "8.0.0";
	private static File argsFile;
	private List<String> classpath;

	KonosRunner(final String projectName, final String moduleName, JpsModuleConfiguration conf,
				final Map<String, Boolean> sources,
				final String encoding,
				Map<String, String> paths) throws IOException {
		argsFile = FileUtil.createTempFile("ideaKonosToCompile", ".txt", false);
		loadClassPath(paths.get(INTINO_PROJECT_PATH), moduleName);
		LOG.info("args file: " + argsFile.getAbsolutePath());
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argsFile), Charset.forName(encoding)))) {
			writer.write(SRC_FILE + NL);
			for (Map.Entry<String, Boolean> file : sources.entrySet())
				writer.write(file.getKey() + "#" + file.getValue() + NL);
			writer.write(NL);
			writer.write(PROJECT + NL + projectName + NL);
			writer.write(MODULE + NL + moduleName + NL);
			writePaths(paths, writer);
			if (conf != null) fillConfiguration(conf, writer);
			writer.write(ENCODING + NL + encoding + NL);
		}
	}

	private void loadClassPath(String intinoDirectory, String moduleName) throws IOException {
		final File classPathFile = new File(new File(intinoDirectory, "box" + File.separator + moduleName), "compiler.classpath");
		if (!classPathFile.exists()) new File(intinoDirectory, moduleName);
		if (!classPathFile.exists())
			throw new IOException("Unable to find builder classpath. Please reload configuration");
		String text = new String(Files.readAllBytes(classPathFile.toPath()));
		this.classpath = Arrays.stream(text.split(":")).map(c -> c.replace("$HOME", System.getProperty("user.home"))).collect(Collectors.toList());
		if (versionOf(new File(classpath.get(0))).compareTo(MINIMUM_VERSION) < 0)
			throw new IOException("Version of Builder in " + moduleName + " is incompatible with this plugin version. Minimum version: " + MINIMUM_VERSION);
	}

	private String versionOf(File file) {
		return file.getParentFile().getName();
	}

	private void fillConfiguration(JpsModuleConfiguration conf, Writer writer) throws IOException {
		if (!conf.level.isEmpty()) writer.write(LEVEL + NL + conf.level + NL);
		if (!conf.groupId.isEmpty()) writer.write(GROUP_ID + NL + conf.groupId + NL);
		if (!conf.artifactId.isEmpty()) writer.write(ARTIFACT_ID + NL + conf.artifactId + NL);
		if (!conf.version.isEmpty()) writer.write(VERSION + NL + conf.version + NL);
		if (!conf.parameters.isEmpty()) writer.write(PARAMETERS + NL + conf.parameters + NL);
		if (!conf.parentInterface.isEmpty()) writer.write(PARENT_INTERFACE + NL + conf.parentInterface + NL);
		if (!conf.library.isEmpty()) writer.write(LIBRARY + NL + conf.library + NL);
		if (!conf.languageGenerationPackage.isEmpty())
			writer.write(LANGUAGE_GENERATION_PACKAGE + NL + conf.languageGenerationPackage + NL);
		writer.write(GENERATION_PACKAGE + NL + (conf.generationPackage.isEmpty() ? conf.outDsl : conf.generationPackage) + ".box" + NL);
	}

	private void writePaths(Map<String, String> paths, Writer writer) throws IOException {
		writer.write(OUTPUTPATH + NL + paths.get(OUTPUTPATH) + NL);
		writer.write(FINAL_OUTPUTPATH + NL + paths.get(FINAL_OUTPUTPATH) + NL);
		writer.write(RES_PATH + NL + paths.get(RES_PATH) + NL);
		if (paths.containsKey(INTINO_PROJECT_PATH))
			writer.write(INTINO_PROJECT_PATH + NL + paths.get(INTINO_PROJECT_PATH) + NL);
		writer.write(PROJECT_PATH + NL + paths.get(PROJECT_PATH) + NL);
		writer.write(MODULE_PATH + NL + paths.get(MODULE_PATH) + NL);
		writer.write(SRC_PATH + NL + paths.get(SRC_PATH) + NL);
		writer.write(NL);
	}

	KonoscOSProcessHandler runKonosCompiler(final CompileContext context) throws IOException {
		List<String> programParams = List.of(argsFile.getPath());
		List<String> vmParams = new ArrayList<>(getJavaVersion().startsWith("1.8") ? new ArrayList<>() : List.of("--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED"));
		vmParams.add("-Xmx" + COMPILER_MEMORY + "m");
		vmParams.add("-Dfile.encoding=" + System.getProperty("file.encoding"));
		final List<String> cmd = ExternalProcessUtil.buildJavaCommandLine(
				getJavaExecutable(), "io.intino.konos.KonoscRunner", Collections.emptyList(), classpath, vmParams, programParams);
		final Process process = Runtime.getRuntime().exec(ArrayUtil.toStringArray(cmd));
		final KonoscOSProcessHandler handler = new KonoscOSProcessHandler(process, String.join(" ", cmd), statusUpdater -> context.processMessage(new ProgressMessage(statusUpdater))) {
			@Override
			protected Future<?> executeOnPooledThread(@NotNull Runnable task) {
				return SharedThreadPool.getInstance().executeOnPooledThread(task);
			}
		};
		handler.startNotify();
		handler.waitFor();
		return handler;
	}

	private String getJavaExecutable() {
		return SystemProperties.getJavaHome() + "/bin/java";
	}

	private String getJavaVersion() {
		return SystemInfo.JAVA_VERSION;
	}
}
