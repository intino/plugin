package io.intino.plugin.actions.box;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.Configuration.Parameter;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.plugin.toolwindows.output.IntinoTopics;
import io.intino.plugin.toolwindows.output.MavenListener;
import org.jetbrains.jps.incremental.ExternalProcessUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.konos.compiler.shared.KonosBuildConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class KonosRunner {
	private static final com.intellij.openapi.diagnostic.Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(KonosRunner.class);

	private static final char NL = '\n';
	private static final int COMPILER_MEMORY = 600;
	private static File argsFile;
	private final Module module;
	private List<String> classpath;

	public KonosRunner(Module module, LegioConfiguration conf, Mode mode, String outputPath) throws IOException {
		this.module = module;
		argsFile = FileUtil.createTempFile("ideaKonosToCompile", ".txt", false);
		this.classpath = Arrays.asList(Files.readString(InterfaceBuilderManager.classpathFile(IntinoDirectory.boxDirectory(module))).replace("$HOME", System.getProperty("user.home")).split(":"));
		Logger.info("Classpath: " + String.join(":", classpath));
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argsFile), UTF_8))) {
			writer.write(SRC_FILE + NL);
			for (File file : sources(module)) writer.write(file.getAbsolutePath() + "#true" + NL);
			writer.write(NL);
			writer.write(PROJECT + NL + module.getProject().getName() + NL);
			writer.write(MODULE + NL + module.getName() + NL);
			writePaths(collectPaths(module, outputPath), writer);
			if (conf != null) fillConfiguration(conf, mode, writer);
			writer.write(ENCODING + NL + UTF_8 + NL);
		}
	}

	private void fillConfiguration(LegioConfiguration conf, Mode mode, Writer writer) throws IOException {
		writer.write(GROUP_ID + NL + conf.artifact().groupId() + NL);
		writer.write(ARTIFACT_ID + NL + conf.artifact().name() + NL);
		writer.write(VERSION + NL + conf.artifact().version() + NL);
		writer.write(PARAMETERS + NL + conf.artifact().parameters().stream().map(Parameter::name).collect(Collectors.joining(";")) + NL);
		writer.write(BOX_GENERATION_PACKAGE + NL + conf.artifact().box().targetPackage() + NL);
		writer.write(COMPILATION_MODE + NL + mode.name() + NL);
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

	public void runKonosCompiler() throws IOException {
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

	private Map<String, String> collectPaths(Module module, String outputPath) {
		File projectDirectory = new File(Objects.requireNonNull(module.getProject().getBasePath()));
		Map<String, String> map = new LinkedHashMap<>();
		map.put(PROJECT_PATH, projectDirectory.getAbsolutePath());
		map.put(MODULE_PATH, new File(module.getModuleFilePath()).getParent());
		VirtualFile resourcesRoot = IntinoUtil.getResourcesRoot(module, false);
		if (resourcesRoot == null) resourcesRoot = IntinoUtil.createResourceRoot(module, "res").getVirtualFile();
		map.put(RES_PATH, resourcesRoot.getPath());
		List<VirtualFile> sourceRoots = IntinoUtil.getSourceRoots(module);
		sourceRoots.stream().filter(f -> new File(f.getPath()).getName().equals("src")).findFirst().ifPresent(src -> map.put(SRC_PATH, src.getPath()));
		if (outputPath == null)
			sourceRoots.stream().filter(f -> new File(f.getPath()).getName().equals("gen")).findFirst().ifPresent(gen -> map.put(OUTPUTPATH, gen.getPath()));
		else map.put(OUTPUTPATH, outputPath);
		map.put(FINAL_OUTPUTPATH, CompilerModuleExtension.getInstance(module).getCompilerOutputUrl().replace("file://", ""));
		File intinoDirectory = IntinoDirectory.of(module.getProject());
		if (intinoDirectory.exists()) map.put(INTINO_PROJECT_PATH, intinoDirectory.getAbsolutePath());
		return map;
	}

	public List<File> sources(Module module) {
		if (module == null) {
			return Collections.emptyList();
		} else {
			Application application = ApplicationManager.getApplication();
			return application.isReadAccessAllowed() ? konosFiles(module) : application.runReadAction((Computable<List<File>>) () -> konosFiles(module));
		}
	}

	private List<File> konosFiles(Module module) {
		List<File> konosFiles = new ArrayList<>();
		Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(ID.create("filetypes"), KonosFileType.instance(), GlobalSearchScope.moduleScope(module));
		files.stream().filter((o) -> o != null && !o.getCanonicalFile().getName().contains("Misc")).forEach((file) -> {
			TaraModel konosFile = (TaraModel) PsiManager.getInstance(module.getProject()).findFile(file);
			if (konosFile != null) konosFiles.add(new File(konosFile.getVirtualFile().getPath()));
		});
		return konosFiles;
	}

	private String getJavaExecutable() {
		return SystemProperties.getJavaHome() + "/bin/java";
	}

	private String getJavaVersion() {
		return SystemInfo.JAVA_VERSION;
	}
}