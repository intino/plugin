package org.jetbrains.jps.intino.compiler.tara;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.java.JavaResourceRootProperties;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootProperties;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.model.module.JpsTypedModuleSourceRoot;
import org.jetbrains.jps.model.serialization.JpsModelSerializationDataService;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.jetbrains.jps.intino.compiler.Directories.*;
import static org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE;
import static org.jetbrains.jps.model.java.JavaSourceRootType.TEST_SOURCE;
import static org.jetbrains.jps.model.serialization.JpsModelSerializationDataService.getBaseDirectory;

public class IntinoPaths {
	public String[] srcRoots;
	public String resRoot;
	public String genRoot;
	public String finalOutput;
	public String repository;
	public String intinoConfDirectory;
	public String projectPath;
	public String modulePath;

	public static IntinoPaths load(ModuleChunk chunk, Map<ModuleBuildTarget, String> finalOutputs, JpsProject project) {
		IntinoPaths paths = new IntinoPaths();
		final JpsModule module = chunk.getModules().iterator().next();
		var testGen = getTestGenRoot(module);
		var testSourceRoot = getTestSourceRoot(module);
		paths.genRoot = chunk.containsTests() ? testGen == null ? createTestGen(module).getAbsolutePath() : testGen.getFile().getAbsolutePath() : getGenDir(module);
		paths.finalOutput = FileUtil.toSystemDependentName(finalOutputs.get(chunk.representativeTarget()));
		paths.resRoot = chunk.containsTests() ? testResourcesDirectory(module).getPath() : getResourcesDirectory(module).getPath();
		paths.repository = new File(new File(System.getProperty("user.home")), M2_DIRECTORY).getAbsolutePath();
		paths.intinoConfDirectory = intinoDirectoryPath(project);
		paths.projectPath = JpsModelSerializationDataService.getBaseDirectory(project).getAbsolutePath();
		paths.modulePath = getBaseDirectory(module).getAbsolutePath();
		if (chunk.containsTests())
			paths.srcRoots = new String[]{testSourceRoot != null ? testSourceRoot.getFile().getAbsolutePath() : null};
		else paths.srcRoots = getSourceRoots(module).stream()
				.map(root -> root.getFile().getAbsolutePath())
				.toArray(String[]::new);
		return paths;
	}

	public static String intinoDirectoryPath(JpsProject project) {
		return new File(JpsModelSerializationDataService.getBaseDirectory(project), INTINO_DIRECTORY).getAbsolutePath();
	}


	public static File getResourcesDirectory(JpsModule module) {
		final Iterator<JpsTypedModuleSourceRoot<JavaResourceRootProperties>> iterator = module.getSourceRoots(JavaResourceRootType.RESOURCE).iterator();
		return iterator.hasNext() ? iterator.next().getFile() : new File(module.getSourceRoots().get(0).getFile().getParentFile(), RES);
	}

	static File testResourcesDirectory(JpsModule module) {
		final Iterator<JpsTypedModuleSourceRoot<JavaResourceRootProperties>> iterator = module.getSourceRoots(JavaResourceRootType.TEST_RESOURCE).iterator();
		return iterator.hasNext() ? iterator.next().getFile() : new File(module.getSourceRoots().get(0).getFile().getParentFile(), TEST_RES);
	}

	private static File createTestGen(JpsModule root) {
		return new File(root.getSourceRoots().get(0).getFile().getParentFile(), TEST_GEN);
	}

	private static List<JpsModuleSourceRoot> getSourceRoots(JpsModule module) {
		return module.getSourceRoots().stream()
				.filter(root -> root.getRootType().equals(SOURCE) && !((JavaSourceRootProperties) root.getProperties()).isForGeneratedSources())
				.toList();
	}

	private static JpsModuleSourceRoot getTestSourceRoot(JpsModule module) {
		return module.getSourceRoots().stream()
				.filter(root -> root.getRootType().equals(TEST_SOURCE) && TEST.equals(root.getFile().getName()))
				.findFirst()
				.orElse(null);
	}

	@Nullable
	private static JpsModuleSourceRoot getTestGenRoot(JpsModule module) {
		return module.getSourceRoots().stream()
				.filter(root -> root.getRootType().equals(TEST_SOURCE) && TEST_GEN.equals(root.getFile().getName()))
				.findFirst()
				.orElse(null);
	}

	private static String getGenDir(JpsModule module) {
		return getDir(module, GEN);
	}


	private static String getDir(JpsModule module, String name) {
		for (JpsModuleSourceRoot moduleSourceRoot : module.getSourceRoots())
			if (name.equals(moduleSourceRoot.getFile().getName())) return moduleSourceRoot.getFile().getAbsolutePath();
		File moduleFile = module.getSourceRoots().get(0).getFile().getParentFile();
		File gen = new File(moduleFile, name);
		gen.mkdir();
		return gen.getAbsolutePath();
	}

}
