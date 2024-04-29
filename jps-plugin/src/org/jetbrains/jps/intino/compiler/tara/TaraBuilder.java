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
import org.jetbrains.jps.intino.compiler.OutputItem;
import org.jetbrains.jps.intino.model.JpsIntinoExtensionService;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration;
import org.jetbrains.jps.model.JpsProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.intino.builder.BuildConstants.*;
import static org.jetbrains.jps.builders.java.JavaBuilderUtil.isCompileJavaIncrementally;
import static org.jetbrains.jps.incremental.ModuleLevelBuilder.ExitCode.*;

public class TaraBuilder extends IntinoBuilder {
	private static final Logger LOG = Logger.getInstance(TaraBuilder.class.getName());
	private static final String TARA_EXTENSION = "tara";
	private final String builderName;
	private JpsModuleConfiguration conf;
	private static final Pattern taraFilePattern = Pattern.compile("\\.([a-z])+\\.tara$" + TARA_EXTENSION);

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
			throw new ProjectBuildException(e.getMessage(), e);
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
		String encoding = encoding(context, chunk);
		Map<String, Map<String, Boolean>> files = files(toCompile);
		IntinoPaths paths = IntinoPaths.load(chunk, finalOutputs, context.getProjectDescriptor().getProject());
		List<OutputItem> compiled = new ArrayList<>();
		for (String dsl : sort(new ArrayList<>(files.keySet()))) {
			String runConfiguration = new RunConfigurationRenderer(project, chunk, conf, files.get(dsl), dsl, paths, isCompileJavaIncrementally(context), encoding).build();
			CompilationResult result = new TaraRunner(project, chunk, dsl, runConfiguration, encoding).runTaraCompiler(context);
			ExitCode exitCode = processResult(context, result);
			if (exitCode != null) return exitCode;
			compiled.addAll(result.successfullyCompiled());
		}
		finish(context, chunk, outputConsumer, compiled);
		context.processMessage(new CustomBuilderMessage(TARAC, REFRESH_MESSAGE, chunk.getName() + REFRESH_MESSAGE_SEPARATOR + getGenDir(chunk.getModules().iterator().next())));
		context.setDone(1);
		return OK;
	}

	private Collection<String> sort(List<String> names) {
		names.sort((s, t1) -> Integer.compare(indexOf(s), indexOf(t1)));
		return names;
	}

	private int indexOf(String dsl) {
		JpsModuleConfiguration.Dsl dslObj = conf.dsls.stream().filter(d -> d.name().equalsIgnoreCase(dsl)).findFirst().orElse(null);
		return dslObj == null ? Integer.MAX_VALUE : conf.dsls.indexOf(dslObj);
	}

	@Nullable
	private ExitCode processResult(CompileContext context, CompilationResult result) {
		result.compilerMessages().forEach(context::processMessage);
		if (checkChunkRebuildNeeded(context, result.shouldRetry())) return CHUNK_REBUILD_REQUIRED;
		if (result.shouldRetry()) return ABORT;
		return null;
	}

	@NotNull
	@Override
	public List<String> getCompilableFileExtensions() {
		return List.of(TARA_EXTENSION, "konos");//FIXME maintaining for retrocompatibility
	}

	@Nullable
	private static String encoding(CompileContext context, ModuleChunk chunk) {
		return context.getProjectDescriptor().getEncodingConfiguration().getPreferredModuleChunkEncoding(chunk);
	}

	private Map<String, Map<String, Boolean>> files(Map<File, Boolean> toCompile) {
		Map<String, Map<String, Boolean>> map = new LinkedHashMap<>();
		for (Map.Entry<File, Boolean> file : toCompile.entrySet()) {
			if (LOG.isDebugEnabled()) LOG.debug("Path to compile: " + file.getKey().getPath());
			String dsl = dslOf(file.getKey());
			if (dsl != null) {
				if (!map.containsKey(dsl)) map.put(dsl, new HashMap<>());
				map.get(dsl).put(FileUtil.toSystemIndependentName(file.getKey().getPath()), file.getValue());
			}
		}
		return map;
	}

	private String dslOf(File file) {
		Matcher matcher = taraFilePattern.matcher(file.getName());
		if (matcher.find()) return matcher.group(1);
		try {
			String line = Files.readString(file.toPath()).trim().lines().findFirst().get();
			return line.contains("dsl ") ? line.split("dsl ")[1] : null;
		} catch (IOException e) {
			return null;
		}
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
		return conf != null && (path.endsWith("." + TARA_EXTENSION)
				|| path.endsWith(".konos"));//FIXME Maintain for retrocompatibility
	}
}