package io.intino.plugin.build.plugins;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.ArrayUtil;
import io.intino.Configuration;
import io.intino.Configuration.Parameter;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.eclipse.aether.graph.Dependency;
import org.jetbrains.jps.incremental.ExternalProcessUtil;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static io.intino.plugin.BuildConstants.*;
import static io.intino.plugin.project.Safe.safe;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jetbrains.jps.incremental.messages.BuildMessage.Kind.ERROR;

public class PluginRunner {
	private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(PluginRunner.class);

	private static final char NL = '\n';
	private static final int COMPILER_MEMORY = 1024;
	private static File argsFile;
	private final Module module;
	private final Configuration.Artifact.Plugin plugin;
	private final FactoryPhase phase;
	private final ProgressIndicator indicator;
	private final List<String> classpath;


	public PluginRunner(Module module, ArtifactLegioConfiguration conf, Configuration.Artifact.Plugin plugin, FactoryPhase phase, List<Dependency> classpath, ProgressIndicator indicator) throws IOException, IntinoException {
		this.module = module;
		this.plugin = plugin;
		this.phase = phase;
		this.indicator = indicator;
		argsFile = FileUtil.createTempFile("ideaPluginToCompile", ".txt", false);
		this.classpath = classpath.stream().map(d -> d.getArtifact().getFile().getAbsolutePath()).collect(Collectors.toList());
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argsFile), UTF_8))) {
			writer.write(SRC_FILE + NL);
			for (File file : sources(module)) writer.write(file.getAbsolutePath() + "#true" + NL);
			writer.write(NL);
			writer.write(PROJECT + NL + module.getProject().getName() + NL);
			writer.write(MODULE + NL + module.getName() + NL);
			writePaths(collectPaths(module), writer);
			if (conf != null) fillConfiguration(conf, writer);
			writer.write(ENCODING + NL + UTF_8 + NL);
		}
	}

	private void fillConfiguration(ArtifactLegioConfiguration conf, Writer writer) throws IOException {
		writer.write(GROUP_ID + NL + conf.artifact().groupId() + NL);
		writer.write(ARTIFACT_ID + NL + conf.artifact().name() + NL);
		writer.write(VERSION + NL + conf.artifact().version() + NL);
		writer.write(PARAMETERS + NL + conf.artifact().parameters().stream().map(Parameter::name).collect(Collectors.joining(";")) + NL);
		writer.write(GENERATION_PACKAGE + NL + conf.artifact().code().generationPackage() + NL);
		writer.write(LANGUAGE_GENERATION_PACKAGE + NL + conf.artifact().code().modelPackage() + NL);
		writer.write(INVOKED_PHASE + NL + phase.name() + NL);
		for (Configuration.Repository repository : conf.repositories())
			writer.write(REPOSITORY + NL + repository.identifier() + "#" + repository.url() + NL);
		if (safe(() -> conf.artifact().distribution().snapshot()) != null) {
			final Configuration.Repository snapshot = conf.artifact().distribution().snapshot();
			writer.write(SNAPSHOT_DISTRIBUTION + NL + snapshot.identifier() + "#" + snapshot.url() + NL);
		}
		if (safe(() -> conf.artifact().distribution().release()) != null) {
			final Configuration.Repository release = conf.artifact().distribution().release();
			writer.write(SNAPSHOT_DISTRIBUTION + NL + release.identifier() + "#" + release.url() + NL);
		}
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

	public void run() throws IOException, IntinoException, InterruptedException {
		List<String> programParams = List.of(argsFile.getPath());
		List<String> vmParams = new ArrayList<>(getJavaVersion().startsWith("1.8") ? new ArrayList<>() : List.of("--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED"));
		vmParams.add("-Xmx" + COMPILER_MEMORY + "m");
		vmParams.add("-Dfile.encoding=" + System.getProperty("file.encoding"));
		String mainClass = mainClass();
		if (mainClass == null) throw new IntinoException("Main class of plugin not found. Please declare in manifest");
		final List<String> cmd = ExternalProcessUtil.buildJavaCommandLine(
				getJavaExecutable(), mainClass, Collections.emptyList(), classpath, vmParams, programParams);
		final Process process = Runtime.getRuntime().exec(ArrayUtil.toStringArray(cmd));
		final PluginOSProcessHandler handler = new PluginOSProcessHandler(process, module, plugin, indicator);
		handler.listen();
		handler.waitFor();
		for (CompilerMessage compilerMessage : handler.compilerMessages())
			if (compilerMessage.getKind().equals(ERROR))
				throw new IntinoException(compilerMessage.getMessageText());
		indicator.setText("Executing post compile actions...");
		handler.postCompileActions().parallelStream().forEach(PostCompileAction::execute);
//		for (PostCompileAction action : handler.postCompileActions()) {
//			PostCompileAction.FinishStatus finishStatus = action.execute();
////			if (finishStatus == PostCompileAction.FinishStatus.Error) break;
//		}
		indicator.setText(null);
	}

	private Map<String, String> collectPaths(Module module) {
		File projectDirectory = new File(Objects.requireNonNull(module.getProject().getBasePath()));
		Map<String, String> map = new LinkedHashMap<>();
		map.put(PROJECT_PATH, projectDirectory.getAbsolutePath());
		map.put(MODULE_PATH, IntinoUtil.moduleRoot(module).getAbsolutePath());
		VirtualFile resourcesRoot = IntinoUtil.getResourcesRoot(module, false);
		if (resourcesRoot == null) {
			final PsiDirectory res = IntinoUtil.createResourceRoot(module, "res");
			if (res != null) resourcesRoot = res.getVirtualFile();
		}
		if (resourcesRoot != null) map.put(RES_PATH, resourcesRoot.getPath());
		List<VirtualFile> sourceRoots = IntinoUtil.getSourceRoots(module);
		sourceRoots.stream().filter(f -> new File(f.getPath()).getName().equals("src")).findFirst().ifPresent(src -> map.put(SRC_PATH, src.getPath()));
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
			return application.isReadAccessAllowed() ? src(module) : application.runReadAction((Computable<List<File>>) () -> src(module));
		}
	}

	private List<File> src(Module module) {
		List<File> srcFiles = new ArrayList<>();
		//TODO put files of specified language
		return srcFiles;
	}

	private String getJavaExecutable() throws IntinoException {
		Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
		if (sdk == null) throw new IntinoException("Sdk not found");
		return sdk.getHomePath() + "/bin/java";
	}

	private String mainClass() {
		final String mainJar = classpath.get(0).replace("$HOME", System.getProperty("user.home"));
		try (JarFile jarFile = new JarFile(new File(mainJar))) {
			return jarFile.getManifest().getMainAttributes().getValue("Main-Class");
		} catch (IOException e) {
			return null;
		}
	}


	private String getJavaVersion() {
		return SystemInfo.JAVA_VERSION;
	}
}
