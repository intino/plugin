package org.jetbrains.jps.intino.compiler.tara;

import com.intellij.openapi.diagnostic.LogLevel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaBuilderUtil;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.BuilderCategory;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.messages.CustomBuilderMessage;
import org.jetbrains.jps.intino.compiler.IntinoBuilder;
import org.jetbrains.jps.intino.model.JpsIntinoExtensionService;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.java.JavaResourceRootProperties;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootProperties;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.model.module.JpsTypedModuleSourceRoot;
import org.jetbrains.jps.model.serialization.JpsModelSerializationDataService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.magritte.builder.shared.TaraBuildConstants.*;
import static org.jetbrains.jps.builders.java.JavaBuilderUtil.isCompileJavaIncrementally;
import static org.jetbrains.jps.incremental.ModuleLevelBuilder.ExitCode.*;
import static org.jetbrains.jps.intino.compiler.CopyResourcesUtil.copy;
import static org.jetbrains.jps.intino.compiler.Directories.TEST;
import static org.jetbrains.jps.intino.compiler.Directories.*;
import static org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE;
import static org.jetbrains.jps.model.java.JavaSourceRootType.TEST_SOURCE;

public class TaraBuilder extends IntinoBuilder {
	private static final Logger LOG = Logger.getInstance(TaraBuilder.class.getName());
	private static final String TARA_EXTENSION = "tara";
	private static final String STASH = ".stash";
	private final String builderName;
	private JpsModuleConfiguration conf;

	public TaraBuilder() {
		super(BuilderCategory.SOURCE_GENERATOR);
		LOG.setLevel(LogLevel.WARNING);
		builderName = "Tara compiler";
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
			if (e.getStackTrace().length != 0) {
				LOG.error(e.getMessage());
				LOG.error(e.getStackTrace()[0].getClassName() + " " + e.getStackTrace()[0].getLineNumber());
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
		final String encoding = context.getProjectDescriptor().getEncodingConfiguration().getPreferredModuleChunkEncoding(chunk);
		List<String> paths = collectPaths(chunk, finalOutputs, context.getProjectDescriptor().getProject());
		TaraRunner runner = new TaraRunner(project, chunk.getName(), conf, isMake(context), files(toCompile), encoding, chunk.containsTests(), paths);
		final TaracOSProcessHandler handler = runner.runTaraCompiler(context);
		processMessages(chunk, context, handler);
		if (checkChunkRebuildNeeded(context, handler.shouldRetry())) return CHUNK_REBUILD_REQUIRED;
		if (handler.shouldRetry()) return ABORT;
		finish(context, chunk, outputConsumer, finalOutputs, handler.getSuccessfullyCompiled());
		context.processMessage(new CustomBuilderMessage(TARAC, REFRESH_BUILDER_MESSAGE, chunk.getName() + REFRESH_BUILDER_MESSAGE_SEPARATOR + getGenDir(chunk.getModules().iterator().next())));
		context.setDone(1);
		return OK;
	}

	private boolean isMake(CompileContext context) {
		return isCompileJavaIncrementally(context);
	}

	@NotNull
	@Override
	public List<String> getCompilableFileExtensions() {
		return Collections.singletonList(TARA_EXTENSION);
	}

	private List<String> collectPaths(ModuleChunk chunk, Map<ModuleBuildTarget, String> finalOutputs, JpsProject project) throws IOException {
		final JpsModule module = chunk.getModules().iterator().next();
		String finalOutput = FileUtil.toSystemDependentName(finalOutputs.get(chunk.representativeTarget()));
		final File testResourcesDirectory = testResourcesDirectory(module);
		final File resourcesDirectory = getResourcesDirectory(module);
		List<String> list = new ArrayList<>();
		final JpsModuleSourceRoot testGen = getTestGenRoot(module);
		list.add(chunk.containsTests() ? testGen == null ? createTestGen(module).getAbsolutePath() : testGen.getFile().getAbsolutePath() : getGenDir(module));
		list.add(finalOutput);
		list.add(chunk.containsTests() ? testResourcesDirectory.getPath() : resourcesDirectory.getPath());
		list.add(new File(new File(System.getProperty("user.home")), LANGUAGES_DIRECTORY).getAbsolutePath());
		File file = new File(JpsModelSerializationDataService.getBaseDirectory(project), INTINO_DIRECTORY);
		if (file.exists()) list.add(file.getAbsolutePath());
		else
			list.add(new File(JpsModelSerializationDataService.getBaseDirectory(project), TARA_DIRECTORY).getAbsolutePath());
		final JpsModuleSourceRoot testSourceRoot = getTestSourceRoot(module);
		if (chunk.containsTests()) list.add(testSourceRoot != null ? testSourceRoot.getFile().getAbsolutePath() : null);
		else
			list.addAll(getSourceRoots(module).stream().map(root -> root.getFile().getAbsolutePath()).collect(Collectors.toList()));
		return list;
	}

	private File createTestGen(JpsModule root) {
		final File file = new File(root.getSourceRoots().get(0).getFile().getParentFile(), TEST_GEN);
		file.mkdirs();
		return file;
	}

	private List<JpsModuleSourceRoot> getSourceRoots(JpsModule module) {
		return module.getSourceRoots().stream().filter(root -> root.getRootType().equals(SOURCE) && !((JavaSourceRootProperties) root.getProperties()).isForGeneratedSources()).collect(Collectors.toList());
	}

	private JpsModuleSourceRoot getTestSourceRoot(JpsModule module) {
		return module.getSourceRoots().stream().filter(root -> root.getRootType().equals(TEST_SOURCE) && TEST.equals(root.getFile().getName())).findFirst().orElse(null);
	}

	@Nullable
	private JpsModuleSourceRoot getTestGenRoot(JpsModule module) {
		return module.getSourceRoots().stream().filter(root -> root.getRootType().equals(TEST_SOURCE) && TEST_GEN.equals(root.getFile().getName())).findFirst().orElse(null);
	}

	protected void copyGeneratedResources(ModuleChunk chunk, Map<ModuleBuildTarget, String> finalOutputs) {
		for (JpsModule module : chunk.getModules()) {
			final File resourcesDirectory = chunk.containsTests() ? testResourcesDirectory(module) : getResourcesDirectory(module);
			if (!resourcesDirectory.exists()) resourcesDirectory.mkdirs();
			File[] files = resourcesDirectory.listFiles((dir, name) -> name.endsWith(STASH));
			for (File file : files == null ? new File[0] : files)
				copy(file, new File(FileUtil.toSystemDependentName(finalOutputs.get(chunk.representativeTarget()))));
		}
	}

	private void processMessages(ModuleChunk chunk, CompileContext context, TaracOSProcessHandler handler) {
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

	private File testResourcesDirectory(JpsModule module) {
		final Iterator<JpsTypedModuleSourceRoot<JavaResourceRootProperties>> iterator = module.getSourceRoots(JavaResourceRootType.TEST_RESOURCE).iterator();
		return iterator.hasNext() ? iterator.next().getFile() : new File(module.getSourceRoots().get(0).getFile().getParentFile(), TEST_RES);
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
		return conf != null && path.endsWith("." + TARA_EXTENSION);
	}
}