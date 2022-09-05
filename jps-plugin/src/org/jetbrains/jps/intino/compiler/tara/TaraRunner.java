package org.jetbrains.jps.intino.compiler.tara;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.cmdline.ClasspathBootstrap;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ExternalProcessUtil;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration;
import org.jetbrains.jps.service.SharedThreadPool;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Future;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static io.intino.magritte.compiler.shared.TaraBuildConstants.*;

class TaraRunner {
	private static final char NL = '\n';
	private static final Logger LOG = Logger.getInstance(TaraRunner.class.getName());
	private static final int COMPILER_MEMORY = 1024;
	private static File argsFile;
	private List<String> classpath;

	TaraRunner(final String projectName, final String moduleName, JpsModuleConfiguration conf, boolean isMake, final Map<String, Boolean> sources, final String encoding, final boolean isTest, List<String> paths) throws IOException {
		argsFile = FileUtil.createTempFile("ideaTaraToCompile", ".txt", false);
		loadClassPath(paths.get(4), moduleName);
		LOG.info("args file: " + argsFile.getAbsolutePath());
		try (Writer writer = Files.newBufferedWriter(argsFile.toPath(), Charset.forName(encoding))) {
			writer.write(SRC_FILE + NL);
			for (Map.Entry<String, Boolean> file : sources.entrySet())
				writer.write(file.getKey() + "#" + file.getValue() + NL);
			writer.write(NL);
			writer.write(PROJECT + NL + projectName + NL);
			writer.write(MODULE + NL + moduleName + NL);
			writePaths(paths, writer);
			if (conf != null) fillConfiguration(conf, writer);
			writer.write(MAKE + NL + isMake + NL);
			writer.write(TEST + NL + isTest + NL);
			writer.write(ENCODING + NL + encoding + NL);
		}
	}

	private void loadClassPath(String intinoDirectory, String moduleName) throws IOException {
		File classPathFile = new File(intinoDirectory, "model" + File.separator + moduleName + File.separator + "compiler.classpath");
		if (!classPathFile.exists()) classPathFile = new File(intinoDirectory, "compiler.classpath");
		if (!classPathFile.exists())
			throw new IOException("Unable to find builder classpath. Please reload configuration");
		this.classpath = Arrays.asList(new String(Files.readAllBytes(classPathFile.toPath())).split(":"));
	}

	private void fillConfiguration(JpsModuleConfiguration conf, Writer writer) throws IOException {
		if (!conf.level.isEmpty()) writer.write(LEVEL + NL + conf.level + NL);
		if (!conf.language.isEmpty() && !conf.languageVersion.isEmpty())
			writer.write(LANGUAGE + NL + conf.language + ":" + conf.languageVersion + NL);
		if (!conf.outDsl.isEmpty()) writer.write(OUT_DSL + NL + conf.outDsl + NL);
		if (!conf.groupId.isEmpty()) writer.write(GROUP_ID + NL + conf.groupId + NL);
		if (!conf.artifactId.isEmpty()) writer.write(ARTIFACT_ID + NL + conf.artifactId + NL);
		if (!conf.version.isEmpty()) writer.write(VERSION + NL + conf.version + NL);
		if (!conf.excludedPhases.isEmpty())
			writer.write(EXCLUDED_PHASES + NL + conf.excludedPhases.stream().map(Object::toString).collect(Collectors.joining(" ")) + NL);
		writer.write(GENERATION_PACKAGE + NL + (conf.modelGenerationPackage.isEmpty() ? conf.outDsl : conf.modelGenerationPackage) + NL);
	}

	private void writePaths(List<String> paths, Writer writer) throws IOException {
		writer.write(SEMANTIC_LIB + NL + getIntinoJar(ClasspathBootstrap.getResourceFile(TaraBuilder.class)).getAbsolutePath() + NL);
		writer.write(OUTPUTPATH + NL + paths.get(0) + NL);
		writer.write(FINAL_OUTPUTPATH + NL + paths.get(1) + NL);
		writer.write(RESOURCES + NL + paths.get(2) + NL);
		if (paths.get(3) != null) writer.write(TARA_PATH + NL + paths.get(3) + NL);
		if (paths.get(4) != null) writer.write(INTINO_PROJECT_PATH + NL + paths.get(4) + NL);
		writer.write(SRC_PATH + NL);
		for (int i = 5; i < paths.size(); i++)
			writer.write(paths.get(i) + NL);
		writer.write(NL);
	}

	TaracOSProcessHandler runTaraCompiler(final CompileContext context) throws IOException {
		List<String> programParams = ContainerUtil.newArrayList(argsFile.getPath());
		List<String> vmParams = getJavaVersion().startsWith("1.8") ? new ArrayList<>() : ContainerUtil.newArrayList("--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED");
		vmParams.add("-Xmx" + COMPILER_MEMORY + "m");
		String encoding = System.getProperty("file.encoding");
		vmParams.add("-Dfile.encoding=" + encoding);
		List<String> finalClasspath = classpath.stream().map(j -> j.replace("$HOME", System.getProperty("user.home"))).collect(Collectors.toList());
		final List<String> cmd = ExternalProcessUtil.buildJavaCommandLine(getJavaExecutable(), mainClass(), Collections.emptyList(), finalClasspath, vmParams, programParams);
		final Process process = Runtime.getRuntime().exec(ArrayUtil.toStringArray(cmd));
		final TaracOSProcessHandler handler = new TaracOSProcessHandler(process, String.join(" ", cmd), encoding, statusUpdater -> context.processMessage(new ProgressMessage(statusUpdater))) {
			@NotNull
			@Override
			public Future<?> executeTask(@NotNull Runnable task) {
				return SharedThreadPool.getInstance().submit(task);
			}
		};
		handler.startNotify();
		handler.waitFor();
		return handler;
	}

	private String mainClass() {
		final String mainJar = classpath.get(0).replace("$HOME", System.getProperty("user.home"));
		try (JarFile jarFile = new JarFile(new File(mainJar))) {
			String mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
			return mainClass != null ? mainClass : "io.intino.magritte.TaracRunner";
		} catch (IOException e) {
			LOG.warn("Main class not found in " + mainJar);
			return "io.intino.magritte.TaracRunner";
		}
	}

	private String getJavaExecutable() {
		return SystemProperties.getJavaHome() + "/bin/java";
	}

	private String getJavaVersion() {
		return SystemInfo.JAVA_VERSION;
	}

	@NotNull
	private File getIntinoJar(File root) {
		File parentFile = root.getParentFile();
		if (!parentFile.exists()) return parentFile;
		return Arrays.stream(Objects.requireNonNull(parentFile.listFiles())).filter(f -> f.getName().startsWith("language-")).findFirst().orElse(parentFile);
	}
}
