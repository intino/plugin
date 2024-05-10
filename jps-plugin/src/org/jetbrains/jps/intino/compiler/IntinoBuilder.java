package org.jetbrains.jps.intino.compiler;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaBuilderUtil;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.builders.storage.SourceToOutputMapping;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
import org.jetbrains.jps.incremental.*;
import org.jetbrains.jps.incremental.fs.CompilationRound;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.storage.BuildDataManager;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.jetbrains.jps.intino.compiler.Directories.GEN;
import static org.jetbrains.jps.intino.compiler.Directories.SRC;
import static org.jetbrains.jps.intino.compiler.tara.IntinoPaths.getResourcesDirectory;
import static org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE;

public abstract class IntinoBuilder extends ModuleLevelBuilder {
	protected static final Key<Boolean> CHUNK_REBUILD_ORDERED = Key.create("CHUNK_REBUILD_ORDERED");

	private static final Logger LOG = Logger.getInstance(IntinoBuilder.class.getName());

	protected IntinoBuilder(BuilderCategory category) {
		super(category);
	}


	protected void finish(CompileContext context, ModuleChunk chunk, OutputConsumer outputConsumer, List<OutputItem> outputItems) throws IOException {
		Map<ModuleBuildTarget, List<String>> generationOutputs = getGenerationOutputs(chunk);
		Map<ModuleBuildTarget, List<OutputItem>> compiled = processCompiledFiles(context, chunk, generationOutputs, generationOutputs.get(chunk.representativeTarget()), outputItems);
		commit(context, outputConsumer, compiled);
	}

	private void commit(CompileContext context,
						OutputConsumer outputConsumer,
						Map<ModuleBuildTarget, List<OutputItem>> compiled) throws IOException {
		registerOutputs(context, outputConsumer, compiled);
		removeOldClasses(context, compiled);
	}

	protected boolean checkChunkRebuildNeeded(CompileContext context, boolean shouldRetry) {
		if (JavaBuilderUtil.isForcedRecompilationAllJavaModules(context) || !shouldRetry) return false;
		if (CHUNK_REBUILD_ORDERED.get(context) != null) {
			CHUNK_REBUILD_ORDERED.set(context, null);
			return false;
		}
		CHUNK_REBUILD_ORDERED.set(context, Boolean.TRUE);
		LOG.info("Order chunk rebuild");
		return true;
	}

	protected Map<File, Boolean> collectChangedFiles(ModuleChunk chunk, DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder) throws IOException {
		final Map<File, Boolean> toCompile = new LinkedHashMap<>();
		dirtyFilesHolder.processDirtyFiles((target, file, sourceRoot) -> {
			if (isSuitableFile(file.getPath())) toCompile.put(file, true);
			return true;
		});
		if (chunk.containsTests() || toCompile.isEmpty()) return toCompile;
		for (JpsModule module : chunk.getModules())
			module.getSourceRoots().stream().filter(s -> s.getRootType().equals(SOURCE) && !s.getRootType().isForTests())
					.forEach(root -> collectAllSuitableFilesIn(root.getFile(), toCompile));
		return toCompile;
	}

	protected abstract boolean isSuitableFile(String path);

	private void collectAllSuitableFilesIn(File dir, Map<File, Boolean> fileList) {
		File[] files = dir.listFiles();
		for (File file : files != null ? files : new File[0])
			if (isSuitableFile(file.getPath()) && !fileList.containsKey(file)) fileList.put(file, false);
			else if (file.isDirectory()) collectAllSuitableFilesIn(file, fileList);
	}

	private void registerOutputs(CompileContext context, OutputConsumer outputConsumer, Map<ModuleBuildTarget, List<OutputItem>> compiled) throws IOException {
		for (Map.Entry<ModuleBuildTarget, List<OutputItem>> entry : compiled.entrySet())
			for (OutputItem outputItem : entry.getValue()) {
				final File generatedFile = new File(outputItem.getOutputPath());
				if (isGen(generatedFile, entry.getKey().getModule()))
					outputConsumer.registerOutputFile(entry.getKey(), generatedFile, Collections.singleton(outputItem.getSourcePath()));
				FSOperations.markDirty(context, CompilationRound.CURRENT, generatedFile);
			}
	}

	private boolean isGen(File generatedFile, @NotNull JpsModule module) {
		String genDir = getGenDir(module);
		return generatedFile.getAbsolutePath().startsWith(genDir);
	}

	private void removeOldClasses(CompileContext context, Map<ModuleBuildTarget, List<OutputItem>> compiled) {
		for (Map.Entry<ModuleBuildTarget, List<OutputItem>> entry : compiled.entrySet())
			try {
				BuildDataManager dm = context.getProjectDescriptor().dataManager;
				SourceToOutputMapping mapping = dm.getSourceToOutputMap(entry.getKey());
				for (String source : mapping.getSources()) {
					if (new File(source).exists()) continue;
					mapping.remove(source);
					FSOperations.markDeleted(context, new File(source));
				}
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
	}

	@Nullable
	protected Map<ModuleBuildTarget, String> getCanonicalModuleOutputs(CompileContext context, ModuleChunk chunk) {
		Map<ModuleBuildTarget, String> finalOutputs = new HashMap<>();
		for (ModuleBuildTarget target : chunk.getTargets()) {
			File moduleOutputDir = target.getOutputDir();
			if (moduleOutputDir == null) {
				context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.ERROR,
						"Output directory not specified for module " + target.getModule().getName()));
				return null;
			}
			String moduleOutputPath = FileUtil.toCanonicalPath(moduleOutputDir.getPath());
			finalOutputs.put(target, moduleOutputPath.endsWith("/") ? moduleOutputPath : moduleOutputPath + "/");
		}
		return finalOutputs;
	}

	private Map<ModuleBuildTarget, List<OutputItem>> processCompiledFiles(CompileContext context,
																		  ModuleChunk chunk,
																		  Map<ModuleBuildTarget, List<String>> generationOutputs,
																		  List<String> compilerOutput,
																		  List<OutputItem> successfullyCompiled) throws IOException {
		ProjectDescriptor pd = context.getProjectDescriptor();
		final Map<ModuleBuildTarget, List<OutputItem>> compiled = new HashMap<>();
		for (final OutputItem item : successfullyCompiled)
			processOutputItem(context, chunk, generationOutputs, compilerOutput, pd, compiled, item);
		if (Utils.IS_TEST_MODE || LOG.isDebugEnabled()) LOG.info("Chunk " + chunk + " compilation finished");
		return compiled;
	}

	private void processOutputItem(CompileContext context,
								   ModuleChunk chunk,
								   Map<ModuleBuildTarget, List<String>> generationOutputs,
								   List<String> compilerOutputs,
								   ProjectDescriptor pd,
								   Map<ModuleBuildTarget, List<OutputItem>> compiled,
								   OutputItem item) throws IOException {
		if (Utils.IS_TEST_MODE || LOG.isDebugEnabled()) LOG.info("compiled=" + item);
		final JavaSourceRootDescriptor rd = pd.getBuildRootIndex().findJavaRootDescriptor(context, new File(item.getSourcePath()));
		if (rd != null) {
			ensureCorrectOutput(chunk, item, generationOutputs, compilerOutputs, rd.target);
			List<OutputItem> items = compiled.computeIfAbsent(rd.target, k -> new ArrayList<>());
			if (new File(item.getOutputPath()).exists())
				items.add(new OutputItem(item.getOutputPath(), item.getSourcePath()));
		} else if (Utils.IS_TEST_MODE || LOG.isDebugEnabled())
			LOG.info("No java source root descriptor for the item found =" + item);
	}


	private void ensureCorrectOutput(ModuleChunk chunk,
									 OutputItem item,
									 Map<ModuleBuildTarget, List<String>> generationOutputs,
									 List<String> compilerOutput,
									 @NotNull ModuleBuildTarget srcTarget) throws IOException {
		if (chunk.getModules().size() > 1 && !srcTarget.equals(chunk.representativeTarget())) {
			File output = new File(item.getSourcePath());
			String srcTargetOutput = generationOutputs.get(srcTarget).get(0);
			if (srcTargetOutput == null) {
				LOG.info("No output for " + srcTarget + "; outputs=" + generationOutputs + "; targets = " + chunk.getTargets());
				return;
			}
			File correctRoot = new File(srcTargetOutput);
			File correctOutput = new File(correctRoot, FileUtil.getRelativePath(new File(compilerOutput.get(0)), output));
			FileUtil.rename(output, correctOutput);
			correctOutput.getPath();
		}
	}

	private Map<ModuleBuildTarget, List<String>> getGenerationOutputs(ModuleChunk chunk) {
		final ModuleBuildTarget buildTarget = chunk.getTargets().iterator().next();
		Map<ModuleBuildTarget, List<String>> generationOutputs = new HashMap<>();
		File genRoot = new File(getGenDir(chunk.getModules().iterator().next()));
		genRoot.mkdirs();
		add(generationOutputs, buildTarget, genRoot.getPath());
		File resRoot = getResourcesDirectory(chunk.getModules().iterator().next());
		resRoot.mkdirs();
		add(generationOutputs, buildTarget, resRoot.getPath());
		File srcRoot = new File(getSrcDir(chunk.getModules().iterator().next()));
		add(generationOutputs, buildTarget, srcRoot.getPath());
		return generationOutputs;
	}

	protected String getGenDir(JpsModule module) {
		return getDir(module, GEN);
	}

	protected String getSrcDir(JpsModule module) {
		return getDir(module, SRC);
	}

	protected String getDir(JpsModule module, String name) {
		for (JpsModuleSourceRoot moduleSourceRoot : module.getSourceRoots())
			if (name.equals(moduleSourceRoot.getFile().getName())) return moduleSourceRoot.getFile().getAbsolutePath();
		File moduleFile = module.getSourceRoots().get(0).getFile().getParentFile();
		File gen = new File(moduleFile, name);
		gen.mkdir();
		return gen.getAbsolutePath();
	}

	private void add(Map<ModuleBuildTarget, List<String>> outputs, ModuleBuildTarget target, String path) {
		outputs.putIfAbsent(target, new ArrayList<>());
		outputs.get(target).add(path);
	}
}
