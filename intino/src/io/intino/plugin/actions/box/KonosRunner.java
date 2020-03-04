package io.intino.plugin.actions.box;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.Configuration.Parameter;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.plugin.toolwindows.output.IntinoTopics;
import io.intino.plugin.toolwindows.output.MavenListener;
import org.jetbrains.jps.incremental.ExternalProcessUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.konos.compiler.shared.KonosBuildConstants.*;

class KonosRunner {
	private static final com.intellij.openapi.diagnostic.Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(KonosRunner.class);

	private static final char NL = '\n';
	private static final int COMPILER_MEMORY = 600;
	private static File argsFile;
	private final Module module;
	private List<String> classpath;

	KonosRunner(Module module, LegioConfiguration conf,
				final List<File> sources,
				final Charset encoding,
				Map<String, String> paths) throws IOException {
		this.module = module;
		argsFile = FileUtil.createTempFile("ideaKonosToCompile", ".txt", false);
		this.classpath = Arrays.asList(Files.readString(InterfaceBuilderManager.classpathFile(IntinoDirectory.boxDirectory(module))).replace("$HOME", System.getProperty("user.home")).split(":"));
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argsFile), encoding))) {
			writer.write(SRC_FILE + NL);
			for (File file : sources) writer.write(file.getAbsolutePath() + "#true" + NL);
			writer.write(NL);
			writer.write(PROJECT + NL + module.getProject().getName() + NL);
			writer.write(MODULE + NL + module.getName() + NL);
			writePaths(paths, writer);
			if (conf != null) fillConfiguration(conf, writer);
			writer.write(ENCODING + NL + encoding + NL);
		}
	}

	private void fillConfiguration(LegioConfiguration conf, Writer writer) throws IOException {
		writer.write(GROUP_ID + NL + conf.artifact().groupId() + NL);
		writer.write(ARTIFACT_ID + NL + conf.artifact().name() + NL);
		writer.write(VERSION + NL + conf.artifact().version() + NL);
		writer.write(PARAMETERS + NL + conf.artifact().parameters().stream().map(Parameter::name).collect(Collectors.joining(";")) + NL);
		writer.write(GENERATION_PACKAGE + NL + conf.artifact().code().generationPackage() + ".box" + NL);
		writer.write(ONLY_GENERATE_ELEMENTS + NL + true + NL);
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

	void runKonosCompiler() throws IOException {
		List<String> programParams = List.of(argsFile.getPath());
		List<String> vmParams = new ArrayList<>(getJavaVersion().startsWith("1.8") ? new ArrayList<>() : List.of("--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED"));
		vmParams.add("-Xmx" + COMPILER_MEMORY + "m");
		vmParams.add("-Dfile.encoding=" + System.getProperty("file.encoding"));
		final List<String> cmd = ExternalProcessUtil.buildJavaCommandLine(
				getJavaExecutable(), "io.intino.konos.KonoscRunner", Collections.emptyList(), classpath, vmParams, programParams);
		final Process process = Runtime.getRuntime().exec(ArrayUtil.toStringArray(cmd));

		final KonoscOSProcessHandler handler = new KonoscOSProcessHandler(process, u -> publish(u));
		handler.startNotify();
		try {
			handler.waitFor();
		} catch (InterruptedException e) {
			Logger.error(e);
		}
	}

	private void publish(String line) {
		if (module.getProject().isDisposed()) return;
		final MessageBus messageBus = module.getProject().getMessageBus();
		final MavenListener mavenListener = messageBus.syncPublisher(IntinoTopics.BUILD_CONSOLE);
		mavenListener.println(line);
		final MessageBusConnection connect = messageBus.connect();
		connect.deliverImmediately();
		connect.disconnect();
	}

	private String getJavaExecutable() {
		return SystemProperties.getJavaHome() + "/bin/java";
	}

	private String getJavaVersion() {
		return SystemInfo.JAVA_VERSION;
	}
}
