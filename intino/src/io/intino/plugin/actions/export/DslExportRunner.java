package io.intino.plugin.actions.export;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SystemProperties;
import io.intino.Configuration;
import io.intino.Configuration.Parameter;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.file.KonosFileType;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.DslBuilderManager;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.jetbrains.jps.incremental.ExternalProcessUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static io.intino.builder.BuildConstants.*;
import static io.intino.plugin.project.Safe.safe;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DslExportRunner {
	private static final com.intellij.openapi.diagnostic.Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(DslExportRunner.class);

	private static final char NL = '\n';
	private static final int COMPILER_MEMORY = 1024;
	private static File argsFile;
	private final StringBuilder output = new StringBuilder();
	private final List<String> classpath;
	private final Module module;
	private final Configuration.Artifact.Dsl dsl;

	public DslExportRunner(Module module, ArtifactLegioConfiguration conf, Configuration.Artifact.Dsl dsl, Mode mode, FactoryPhase factoryPhase, String outputPath) throws IOException, IntinoException {
		this.module = module;
		this.dsl = dsl;
		argsFile = FileUtil.createTempFile("idea" + dsl.name() + "ToCompile", ".txt", false);
		Path path = new DslBuilderManager(module, conf.repositories(), dsl).classpathFile();
		if (!path.toFile().exists())
			throw new IntinoException("Classpath of compiler not found. Please Reload configuration in order to attach it.");
		this.classpath = Arrays.asList(Files.readString(path).replace("$HOME", System.getProperty("user.home")).split(":"));
		Logger.info("Classpath: " + String.join(":", classpath));
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argsFile), UTF_8))) {
			writer.write(SRC_FILE + NL);
			for (File file : sources(module)) writer.write(file.getAbsolutePath() + "#true" + NL);
			writer.write(NL);
			writer.write(PROJECT + NL + module.getProject().getName() + NL);
			writer.write(MODULE + NL + module.getName() + NL);
			writePaths(collectPaths(module, outputPath), writer);
			fillConfiguration(conf, mode, writer);
			writer.write(ENCODING + NL + UTF_8 + NL);
			writer.write(INVOKED_PHASE + NL + factoryPhase.name() + NL);
		}
	}

	private void fillConfiguration(ArtifactLegioConfiguration conf, Mode mode, Writer writer) throws IOException {
		writer.write(GROUP_ID + NL + conf.artifact().groupId() + NL);
		writer.write(ARTIFACT_ID + NL + conf.artifact().name() + NL);
		writer.write(VERSION + NL + conf.artifact().version() + NL);
		writer.write(PARAMETERS + NL + conf.artifact().parameters().stream().map(Parameter::name).collect(Collectors.joining(";")) + NL);
		writer.write(GENERATION_PACKAGE + NL + conf.artifact().code().generationPackage() + "." + dsl.builder().generationPackage() + NL);
		writer.write(COMPILATION_MODE + NL + mode + NL);
		if (safe(() -> conf.artifact().distribution().snapshot()) != null) {
			final Configuration.Repository snapshot = conf.artifact().distribution().snapshot();
			writer.write(SNAPSHOT_DISTRIBUTION + NL + snapshot.identifier() + "#" + snapshot.url() + NL);
		}
		if (safe(() -> conf.artifact().distribution().release()) != null) {
			final Configuration.Repository release = conf.artifact().distribution().release();
			writer.write(RELEASE_DISTRIBUTION + NL + release.identifier() + "#" + release.url() + NL);
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

	public void runExport() throws IOException {
		List<String> programParams = List.of(argsFile.getPath());
		List<String> vmParams = new ArrayList<>(getJavaVersion().startsWith("1.8") ? new ArrayList<>() : List.of("--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED"));
		vmParams.add("-Xmx" + COMPILER_MEMORY + "m");
		vmParams.add("-Dfile.encoding=" + Charset.defaultCharset().displayName());
		final String mainClass = mainClass(classpath.get(0));
		if (mainClass == null) throw new IOException("Main Class of runner not found");
		final List<String> cmd = ExternalProcessUtil.buildJavaCommandLine(getJavaExecutable(), mainClass, Collections.emptyList(), classpath, vmParams, programParams);
		final Process process = Runtime.getRuntime().exec(ArrayUtil.toStringArray(cmd));
		final ExportOSProcessHandler handler = new ExportOSProcessHandler(process, this::save);
		handler.startNotify();
		try {
			handler.waitFor();
		} catch (InterruptedException e) {
			Logger.error(e);
		}
		Path tempFile = Files.createTempFile("export", "runner");
		Files.writeString(tempFile, output.toString());
		Logger.info("Output of export execution saved in: " + tempFile.toFile().getAbsolutePath());
	}

	private String mainClass(String path) {
		try (JarFile jarFile = new JarFile(new File(path))) {
			String mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
			if (mainClass != null) return mainClass;
		} catch (IOException ignored) {
		}
		return null;
	}

	private void save(String line) {
		output.append(line).append("\n");
	}

	private Map<String, String> collectPaths(Module module, String outputPath) {
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
		if (outputPath == null)
			sourceRoots.stream().filter(f -> new File(f.getPath()).getName().equals("gen")).findFirst().ifPresent(gen -> map.put(OUTPUTPATH, gen.getPath()));
		else map.put(OUTPUTPATH, outputPath);
		map.put(FINAL_OUTPUTPATH, CompilerModuleExtension.getInstance(module).getCompilerOutputUrl().replace("file://", ""));
		File intinoDirectory = IntinoDirectory.of(module.getProject());
		if (intinoDirectory.exists()) map.put(INTINO_PROJECT_PATH, intinoDirectory.getAbsolutePath());
		return map;
	}

	public List<File> sources(Module module) {
		if (module == null) return Collections.emptyList();
		else {
			Application application = ApplicationManager.getApplication();
			return application.isReadAccessAllowed() ?
					dslFiles(module) :
					application.runReadAction((Computable<List<File>>) () -> dslFiles(module));
		}
	}

	private List<File> dslFiles(Module module) {
		List<TaraModel> models = IntinoUtil.getFilesOfModuleByFileType(module, TaraFileType.instance());
		models.addAll(IntinoUtil.getFilesOfModuleByFileType(module, KonosFileType.instance()));
		return models.stream().map(model -> new File(model.getVirtualFile().getPath())).toList();
	}

	private String getJavaExecutable() {
		String homePath = safe(() -> ModuleRootManager.getInstance(this.module).getSdk().getHomePath(), SystemProperties.getJavaHome());
		return homePath + "/bin/java";
	}

	private String getJavaVersion() {
		return SystemInfo.JAVA_VERSION;
	}
}
