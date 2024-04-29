package org.jetbrains.jps.intino.compiler.tara;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ExternalProcessUtil;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.intino.model.impl.IntinoJpsCompilerSettings;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.service.SharedThreadPool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.jar.JarFile;

import static java.nio.charset.Charset.defaultCharset;

class TaraRunner {
	private static final Logger LOG = Logger.getInstance(TaraRunner.class.getName());
	private final File argsFile;
	private final int compilerMemory;
	private final List<String> classpath;
	private final String dsl;
	private final String runConfiguration;
	private final String encoding;
	private final ModuleChunk module;

	TaraRunner(final JpsProject project, final ModuleChunk module, String dsl,  String runConfiguration, String encoding) throws IOException {
		this.module = module;
		this.dsl = dsl;
		this.runConfiguration = runConfiguration;
		this.encoding = encoding;
		this.argsFile = FileUtil.createTempFile("ideaTaraToCompile", ".txt", false);
		this.compilerMemory = project.getContainer().getChild(IntinoJpsCompilerSettings.ROLE).modelMemory();
		this.classpath = loadClassPath(IntinoPaths.intinoDirectoryPath(project), module.getName());
		LOG.info("args file: " + argsFile.getAbsolutePath());
	}

	private List<String> loadClassPath(String intinoDirectory, String moduleName) throws IOException {
		File classPathFile = new File(intinoDirectory, dsl + File.separator + moduleName + File.separator + "compiler.classpath");
		if (!classPathFile.exists()) classPathFile = new File(intinoDirectory, "compiler.classpath");
		if (!classPathFile.exists())
			throw new IOException("Unable to find builder classpath. Please reload configuration. If the error persists, check your your internet connection and the defined artifactories");
		return Arrays.asList(new String(Files.readAllBytes(classPathFile.toPath())).split(":"));
	}

	CompilationResult runTaraCompiler(final CompileContext context) throws IOException {
		Files.writeString(argsFile.toPath(), runConfiguration, Charset.forName(encoding));
		List<String> programParams = new ArrayList<>(Collections.singletonList(argsFile.getPath()));
		List<String> vmParams = vmParams();
		List<String> finalClasspath = classpath.stream().map(j -> j.replace("$HOME", System.getProperty("user.home"))).toList();
		final List<String> cmd = ExternalProcessUtil.buildJavaCommandLine(getJavaExecutable(), mainClass(), List.of(), finalClasspath, vmParams, programParams);
		final Process process = Runtime.getRuntime().exec(ArrayUtil.toStringArray(cmd));
		final TaracOSProcessHandler handler = processHandler(context, process, cmd);
		handler.startNotify();
		handler.waitFor();
		return new CompilationResult(handler.getCompilerMessages(module.getName()), handler.shouldRetry(), handler.getSuccessfullyCompiled());
	}

	private String mainClass() throws IOException {
		final String mainJar = classpath.get(0).replace("$HOME", System.getProperty("user.home"));
		try (JarFile jarFile = new JarFile(new File(mainJar))) {
			String mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
			if (mainClass != null) return mainClass;
		} catch (IOException ignored) {
		}
		if (mainJar.contains("konos")) return "io.intino.konos.KonoscRunner";
		if (mainJar.contains("magritte")) return "io.intino.magritte.builder.MagrittecRunner";
		throw new IOException("Main class not found in " + mainJar);
	}

	@NotNull
	private List<String> vmParams() {
		return Arrays.asList("--add-opens=java.base/java.nio=ALL-UNNAMED",
				"--add-opens=java.base/java.lang=ALL-UNNAMED",
				"--add-opens=java.base/java.io=ALL-UNNAMED",
				"-Xmx" + compilerMemory + "m",
				"-Dfile.encoding=" + defaultCharset().displayName());
	}

	@NotNull
	private static TaracOSProcessHandler processHandler(CompileContext context, Process process, List<String> cmd) {
		return new TaracOSProcessHandler(process, String.join(" ", cmd), defaultCharset().displayName(), statusUpdater -> context.processMessage(new ProgressMessage(statusUpdater))) {
			@NotNull
			@Override
			public Future<?> executeTask(@NotNull Runnable task) {
				return SharedThreadPool.getInstance().submit(task);
			}
		};
	}

	private String getJavaExecutable() {
		return SystemProperties.getJavaHome() + "/bin/java";
	}
}
