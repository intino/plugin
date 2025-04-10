package io.intino.plugin.actions.export;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
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
import io.intino.Configuration.Repository.Snapshot;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.file.KonosFileType;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.DslBuilderManager;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.ArtifactSerializer;
import org.jetbrains.jps.incremental.ExternalProcessUtil;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

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
import static org.jetbrains.jps.incremental.messages.BuildMessage.Kind.ERROR;

public class DslExportRunner {
	private static final com.intellij.openapi.diagnostic.Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(DslExportRunner.class);

	private static final char NL = '\n';
	private static final int COMPILER_MEMORY = 1024;
	private static File argsFile;
	private final ProgressIndicator indicator;
	private final List<String> classpath;
	private final Module module;
	private final Configuration.Artifact.Dsl dsl;
	private final List<File> sources;

	public DslExportRunner(Module module, ArtifactLegioConfiguration conf, Configuration.Artifact.Dsl dsl, Mode mode, FactoryPhase factoryPhase, String outputPath, ProgressIndicator indicator) throws IOException, IntinoException {
		this.module = module;
		this.dsl = dsl;
		argsFile = FileUtil.createTempFile("idea" + dsl.name() + "ToCompile", ".txt", false);
		Logger.info("DslExporter argsFile: " + argsFile.getAbsolutePath());
		sources = sources(module, dsl);
		this.indicator = indicator;
		Path path = new DslBuilderManager(module, conf.repositories(), dsl).classpathFile();
		if (!path.toFile().exists()) {
			throw new IntinoException("Classpath of compiler not found. Please Reload configuration in order to attach it.");
		}
		this.classpath = Arrays.stream(Files.readString(path).split(":")).map(f -> f.replace("$HOME", System.getProperty("user.home"))).toList();
		Logger.info("Classpath: " + String.join(":", classpath));
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argsFile), UTF_8))) {
			writer.write(SRC_FILE + NL);
			for (File file : sources) writer.write(file.getAbsolutePath() + "#true" + NL);
			writer.write(NL);
			writer.write(PROJECT + NL + module.getProject().getName() + NL);
			writer.write(MODULE + NL + module.getName() + NL);
			writePaths(collectPaths(module, outputPath), writer);
			fillConfiguration(conf, mode, writer);
			writer.write(ENCODING + NL + UTF_8 + NL);
			writer.write(INVOKED_PHASE + NL + factoryPhase.name() + NL);
		}
	}

	public void runExport() throws IOException, IntinoException, InterruptedException {
		if (sources.isEmpty()) return;
		List<String> programParams = List.of(argsFile.getPath());
		List<String> vmParams = new ArrayList<>(getJavaVersion().startsWith("1.8") ? new ArrayList<>() : List.of("--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED"));
		vmParams.add("-Xmx" + COMPILER_MEMORY + "m");
		vmParams.add("-Dfile.encoding=" + Charset.defaultCharset().displayName());
		String mainClass = mainClass(classpath.get(0));
		if (mainClass == null && dsl.name().equalsIgnoreCase("konos")) mainClass = "io.intino.konos.KonoscRunner";
		if (mainClass == null) throw new IOException("Main Class of runner not found");
		final List<String> cmd = ExternalProcessUtil.buildJavaCommandLine(getJavaExecutable(), mainClass, Collections.emptyList(), classpath, vmParams, programParams);
		final Process process = Runtime.getRuntime().exec(ArrayUtil.toStringArray(cmd));
		final ExportOSProcessHandler handler = new ExportOSProcessHandler(process, module, dsl, indicator);
		handler.listen();
		handler.waitFor();
		for (CompilerMessage compilerMessage : handler.compilerMessages())
			if (compilerMessage.getKind().equals(ERROR))
				throw new IntinoException(compilerMessage.getMessageText());
		indicator.setText("Executing post compile actions...");
		List<PostCompileAction> postCompileActions = handler.postCompileActions();
		if (!postCompileActions.isEmpty()) {
			postCompileActions.get(0).execute();
			postCompileActions.stream().skip(1).parallel().forEach(PostCompileAction::execute);
		}
		indicator.setText(null);
	}

	private String mainClass(String path) {
		Logger.info("Main Class of: " + path);
		try (JarFile jarFile = new JarFile(new File(path))) {
			String mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
			if (mainClass != null) return mainClass;
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	private void fillConfiguration(ArtifactLegioConfiguration conf, Mode mode, Writer writer) throws IOException {
		writer.write(GROUP_ID + NL + conf.artifact().groupId() + NL);
		writer.write(ARTIFACT_ID + NL + conf.artifact().name() + NL);
		writer.write(VERSION + NL + conf.artifact().version() + NL);
		writer.write(DSL + NL + dsl.name() + ":" + dsl.version() + NL);
		writer.write("language" + NL + dsl.name() + ":" + dsl.version() + NL);//FIXME retrocompatibility. Remove in following versions
		writer.write(LEVEL + NL + dsl.level() + NL);
		writer.write(PARAMETERS + NL + conf.artifact().parameters().stream().map(Parameter::name).collect(Collectors.joining(";")) + NL);
		writer.write(GENERATION_PACKAGE + NL + conf.artifact().code().generationPackage() + "." + dsl.builder().generationPackage() + NL);
		writer.write(COMPILATION_MODE + NL + mode.name() + NL);
		if (safe(() -> conf.artifact().distribution().onArtifactory()) != null) {
			if (safe(() -> conf.artifact().distribution().onArtifactory().snapshot()) != null) {
				final Configuration.Repository snapshot = conf.artifact().distribution().onArtifactory().snapshot();
				writer.write(SNAPSHOT_DISTRIBUTION + NL + snapshot.identifier() + "#" + snapshot.url() + NL);
			}
			if (safe(() -> conf.artifact().distribution().onArtifactory().release()) != null) {
				final Configuration.Repository release = conf.artifact().distribution().onArtifactory().release();
				writer.write(RELEASE_DISTRIBUTION + NL + release.identifier() + "#" + release.url() + NL);
			}
		}
		for (Configuration.Repository repository : conf.repositories())
			writer.write((repository instanceof Snapshot ? SNAPSHOT_IMPORT : RELEASE_IMPORT) + NL + repository.identifier() + "#" + repository.url() + NL);
		if (!conf.artifact().dependencies().isEmpty()) {
			String content = new ArtifactSerializer(conf.artifact()).serializeDependencies();
			if (!content.isEmpty()) writer.write(CURRENT_DEPENDENCIES + NL + content);
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

	public List<File> sources(Module module, Configuration.Artifact.Dsl dsl) {
		if (module == null) return Collections.emptyList();
		else {
			Application application = ApplicationManager.getApplication();
			return application.isReadAccessAllowed() ?
					dslFiles(module, dsl) :
					application.runReadAction((Computable<List<File>>) () -> dslFiles(module, dsl));
		}
	}

	private List<File> dslFiles(Module module, Configuration.Artifact.Dsl dsl) {
		String name = dsl.name();
		Set<TaraModel> files = new HashSet<>(IntinoUtil.getFilesOfModuleByFileType(module, TaraFileType.instance()));
		files.addAll(IntinoUtil.getFilesOfModuleByFileType(module, KonosFileType.instance()));
		return files.stream().filter(m -> IntinoUtil.dslOf(m).equalsIgnoreCase(name)).map(model -> new File(model.getVirtualFile().getPath())).toList();
	}

	private String getJavaExecutable() {
		String homePath = safe(() -> ModuleRootManager.getInstance(this.module).getSdk().getHomePath(), SystemProperties.getJavaHome());
		return homePath + "/bin/java";
	}

	private String getJavaVersion() {
		return SystemInfo.JAVA_VERSION;
	}
}