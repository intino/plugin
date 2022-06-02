package org.jetbrains.jps.intino.compiler.konos;

import com.intellij.openapi.diagnostic.LogLevel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaBuilderUtil;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.BuilderCategory;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.messages.CustomBuilderMessage;
import org.jetbrains.jps.intino.compiler.Directories;
import org.jetbrains.jps.intino.compiler.IntinoBuilder;
import org.jetbrains.jps.intino.model.JpsIntinoExtensionService;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.java.JavaSourceRootProperties;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.konos.compiler.shared.KonosBuildConstants.*;
import static org.jetbrains.jps.builders.java.JavaBuilderUtil.isCompileJavaIncrementally;
import static org.jetbrains.jps.incremental.ModuleLevelBuilder.ExitCode.*;
import static org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE;
import static org.jetbrains.jps.model.serialization.JpsModelSerializationDataService.getBaseDirectory;

public class KonosBuilder extends IntinoBuilder {
	private static final Logger LOG = Logger.getInstance(KonosBuilder.class.getName());
	private static final String KONOS_EXTENSION = "konos";
	private final String builderName;
	private JpsModuleConfiguration conf;

	public KonosBuilder() {
		super(BuilderCategory.SOURCE_GENERATOR);
		LOG.setLevel(LogLevel.WARNING);
		builderName = "Konos compiler";
	}

	public ExitCode build(CompileContext context,
						  ModuleChunk chunk,
						  DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder,
						  OutputConsumer outputConsumer) throws ProjectBuildException {
		long start = System.currentTimeMillis();
		try {
			final JpsIntinoExtensionService service = JpsIntinoExtensionService.instance();
			conf = service.getConfiguration(chunk.getModules().iterator().next(), context);
			return doBuild(context, chunk, dirtyFilesHolder, outputConsumer);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			if (e.getStackTrace().length != 0) {
				LOG.error(e.getMessage());
				LOG.error("ERROR IN -> " + e.getStackTrace()[0].getClassName() + " " + e.getStackTrace()[0].getLineNumber());
			}
			throw new ProjectBuildException(e.getMessage());
		} finally {
			if (start > 0 && LOG.isDebugEnabled())
				LOG.debug(builderName + " took " + (System.currentTimeMillis() - start) + " on " + chunk.getName());
		}
	}

	private ExitCode doBuild(CompileContext context, ModuleChunk chunk, DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder, OutputConsumer outputConsumer) throws IOException {
		JpsProject project = context.getProjectDescriptor().getProject();
		Map<ModuleBuildTarget, String> finalOutputs = getCanonicalModuleOutputs(context, chunk);
		if (finalOutputs == null) return ExitCode.ABORT;
		final Map<File, Boolean> toCompile = collectChangedFiles(chunk, dirtyFilesHolder);
		if (conf == null || toCompile.isEmpty()) return NOTHING_DONE;
		if (toCompile.values().stream().noneMatch(t -> t) && isCompileJavaIncrementally(context)) return NOTHING_DONE;
		final String encoding = context.getProjectDescriptor().getEncodingConfiguration().getPreferredModuleChunkEncoding(chunk);
		Map<String, String> paths = collectPaths(chunk, finalOutputs, context.getProjectDescriptor().getProject());
		KonosRunner runner = new KonosRunner(project.getName(), chunk.getName(), conf, files(toCompile), encoding, paths);
		final KonoscOSProcessHandler handler = runner.runKonosCompiler(context);
		processMessages(chunk, context, handler);
		if (handler.shouldRetry()) return ABORT;
		if (checkChunkRebuildNeeded(context, handler.shouldRetry())) return ABORT;
		finish(context, chunk, outputConsumer, finalOutputs, handler.getSuccessfullyCompiled());
		context.processMessage(new CustomBuilderMessage(KONOSC, REFRESH_MESSAGE, chunk.getName() + REFRESH_BUILDER_MESSAGE_SEPARATOR + getGenDir(chunk.getModules().iterator().next())));
		context.setDone(1);
		return OK;
	}

	@NotNull
	@Override
	public List<String> getCompilableFileExtensions() {
		return Collections.singletonList(KONOS_EXTENSION);
	}

	private Map<String, String> collectPaths(ModuleChunk chunk, Map<ModuleBuildTarget, String> finalOutputs, JpsProject project) {
		File projectDirectory = getBaseDirectory(project);
		final JpsModule module = chunk.getModules().iterator().next();
		String finalOutput = FileUtil.toSystemDependentName(finalOutputs.get(chunk.representativeTarget()));
		Map<String, String> map = new LinkedHashMap<>();
		map.put(PROJECT_PATH, projectDirectory.getAbsolutePath());
		map.put(MODULE_PATH, getBaseDirectory(module).getAbsolutePath());
		map.put(RES_PATH, getResourcesDirectory(module).getPath());
		getSourceRoots(module).stream().filter(f -> f.getFile().getName().equals("src")).findFirst().ifPresent(src -> map.put(SRC_PATH, src.getFile().getAbsolutePath()));
		map.put(OUTPUTPATH, getGenDir(module));
		map.put(FINAL_OUTPUTPATH, finalOutput);
		File file = new File(projectDirectory, Directories.INTINO_DIRECTORY);
		if (file.exists()) map.put(INTINO_PROJECT_PATH, file.getAbsolutePath());
		return map;
	}

	private List<JpsModuleSourceRoot> getSourceRoots(JpsModule module) {
		return module.getSourceRoots().stream().filter(root -> root.getRootType().equals(SOURCE) && !((JavaSourceRootProperties) root.getProperties()).isForGeneratedSources()).collect(Collectors.toList());
	}

	private void processMessages(ModuleChunk chunk, CompileContext context, KonoscOSProcessHandler handler) {
		handler.getCompilerMessages(chunk.getName()).forEach(context::processMessage);
	}

	private Map<String, Boolean> files(Map<File, Boolean> toCompile) {
		Map<String, Boolean> map = new LinkedHashMap<>();
		for (Map.Entry<File, Boolean> file : toCompile.entrySet()) {
			if (LOG.isDebugEnabled()) LOG.debug("Path to compile: " + file.getKey().getPath());
			map.put(FileUtil.toSystemIndependentName(file.getKey().getPath()), file.getValue());
		}
		return map;
	}


	protected void copyGeneratedResources(ModuleChunk chunk, Map<ModuleBuildTarget, String> finalOutputs) {
	}

	@Override
	public void buildStarted(CompileContext context) {
	}

	@Override
	public void chunkBuildFinished(CompileContext context, ModuleChunk chunk) {
		JavaBuilderUtil.cleanupChunkResources(context);
	}

	@Override
	public String toString() {
		return builderName;
	}

	@NotNull
	public String getPresentableName() {
		return builderName;
	}

	protected boolean isSuitableFile(String path) {
		return conf != null && path.endsWith("." + KONOS_EXTENSION);
	}
}