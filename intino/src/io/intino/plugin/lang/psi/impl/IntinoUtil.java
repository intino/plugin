package io.intino.plugin.lang.psi.impl;

import com.intellij.ide.util.DirectoryUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.project.ProjectKt;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Code;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.project.configuration.ConfigurationManager;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.Language;
import io.intino.tara.language.model.*;
import io.intino.tara.language.semantics.Constraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.read;
import static io.intino.plugin.project.Safe.safe;
import static java.util.Collections.emptyList;
import static org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE;
import static org.jetbrains.jps.model.java.JavaResourceRootType.TEST_RESOURCE;

public class IntinoUtil {
	private static final String FUNCTIONS = "functions";

	private IntinoUtil() {
	}

	public static void commitDocument(PsiFile file) {
		final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(file.getProject());
		FileDocumentManager fileDocManager = FileDocumentManager.getInstance();
		Document doc = documentManager.getDocument(file);
		if (doc == null) return;
		documentManager.commitDocument(doc);
		fileDocManager.saveDocument(doc);
	}

	public static List<Mogram> findRootNode(PsiElement element, String identifier) {
		List<Mogram> result = new ArrayList<>();
		for (TaraModel taraFile : getModuleFiles(element.getContainingFile())) {
			Collection<Mogram> nodes = taraFile.components();
			extractNodesByName(identifier, result, nodes);
		}
		return result;
	}

	private static void extractNodesByName(String identifier, List<Mogram> result, Collection<Mogram> nodes) {
		result.addAll(nodes.stream().filter(mogram -> identifier.equals(mogram.name())).toList());
	}

	public static File moduleRoot(Module module) {
		VirtualFile file = ProjectUtil.guessModuleDir(module);
		if (file != null) return VfsUtil.virtualToIoFile(file);
		VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
		return contentRoots.length == 0 ? new File(module.getModuleFilePath()).getParentFile() : VfsUtil.virtualToIoFile(contentRoots[0]);
	}

	public static File projectRoot(Project project) {
		return ProjectKt.getStateStore(project).getProjectBasePath().toFile();
	}

	@Nullable
	public static Language getLanguage(PsiElement element) {
		if (element == null) return null;
		PsiFile file = element.getContainingFile();
		if (file == null) return null;
		return LanguageManager.getLanguage(file.getVirtualFile() == null ? file.getOriginalFile() : file);
	}

	public static String modelPackage(@NotNull PsiElement element) {
		if (!(element.getContainingFile() instanceof TaraModel)) return "";
		final Module module = ModuleProvider.moduleOf(element);
		final Configuration conf = configurationOf(module);
		if (conf == null) return "";
		return safe(() -> conf.artifact().code().generationPackage()) + (isTest(element.getContainingFile(), module) ? ".test" : "." + conf.artifact().code().modelPackage());
	}

	public static boolean isTest(PsiElement dir, Module module) {
		final List<VirtualFile> roots = testContentRoot(module);
		for (VirtualFile root : roots) if (isIn(root, dir)) return true;
		return false;
	}

	private static boolean isIn(VirtualFile modelSourceRoot, PsiElement dir) {
		if (modelSourceRoot == null) return false;
		PsiElement parent = dir;
		while (parent != null && !modelSourceRoot.equals(virtualFileOf(parent)))
			parent = parent.getParent();
		return parent != null && virtualFileOf(parent).equals(modelSourceRoot);
	}

	private static VirtualFile virtualFileOf(PsiElement element) {
		return element instanceof PsiDirectory ? ((PsiDirectory) element).getVirtualFile() : ((PsiFile) element).getVirtualFile();
	}

	private static List<VirtualFile> testContentRoot(Module module) {
		return ModuleRootManager.getInstance(module).getSourceRoots(JavaModuleSourceRootTypes.TESTS);
	}

	public static String languageGraphPackage(@NotNull PsiElement element) {
		if (!(element.getContainingFile() instanceof TaraModel)) return "";
		final Configuration conf = configurationOf(element);
		if (conf == null) return "";
		Configuration.Artifact.Model.Language language = safe(() -> conf.artifact().model().language());
		if (language == null) return null;
		return language.generationPackage();
	}

	public static String modelPackage(@NotNull Module module) {
		final Configuration conf = configurationOf(module);
		if (conf == null) return "";
		return safe(() -> {
			Code code = conf.artifact().code();
			return code.generationPackage() + "." + code.modelPackage();
		});
	}

	public static Configuration.Artifact.Model.Level level(@NotNull PsiElement element) {
		final Configuration configuration = configurationOf(element);
		return safe(() -> configuration.artifact().model().level());
	}

	public static Configuration configurationOf(@NotNull PsiElement element) {
		return configurationOf(ModuleProvider.moduleOf(element));
	}

	public static Configuration configurationOf(@Nullable Module module) {
		return ConfigurationManager.configurationOf(module);
	}

	private static boolean isTestModelFile(PsiFile file) {
		final Module moduleOf = ModuleProvider.moduleOf(file);
		final VirtualFile definitions = getContentRoot(moduleOf, "test");
		return definitions != null && file.getVirtualFile() != null && file.getVirtualFile().getPath().startsWith(definitions.getPath());
	}

	@Nullable
	public static List<Constraint> constraintsOf(Mogram mogram) {
		Language language = getLanguage((PsiElement) mogram);
		if (language == null) return null;
		return language.constraints(mogram.resolve().type());
	}

	@NotNull
	public static List<Constraint.Parameter> parameterConstraintsOf(Mogram node) {
		Language language = getLanguage((PsiElement) node);
		if (language == null) return emptyList();
		final List<Constraint> nodeConstraints = language.constraints(node.resolve().type());
		if (nodeConstraints == null) return emptyList();
		List<Constraint.Parameter> parameters = new ArrayList<>();
		for (Constraint constraint : nodeConstraints)
			if (constraint instanceof Constraint.Parameter) parameters.add((Constraint.Parameter) constraint);
			else if (constraint instanceof Constraint.Facet && hasAspect(node, (Constraint.Facet) constraint))
				parameters.addAll(((Constraint.Facet) constraint).constraints().stream().filter(c -> c instanceof Constraint.Parameter).map(c -> (Constraint.Parameter) c).toList());
		return parameters;
	}

	@NotNull
	public static List<Constraint.Parameter> aspectParameterConstraintsOf(Mogram node) {
		Language language = getLanguage((PsiElement) node);
		if (language == null) return emptyList();
		final List<Constraint> nodeConstraints = language.constraints(node.resolve().type());
		if (nodeConstraints == null) return emptyList();
		List<Constraint.Parameter> parameters = new ArrayList<>();
		nodeConstraints.stream().
				filter(constraint -> constraint instanceof Constraint.Facet && hasAspect(node, (Constraint.Facet) constraint)).
				map(constraint -> ((Constraint.Facet) constraint).constraints().stream().filter(c -> c instanceof Constraint.Parameter).map(c -> (Constraint.Parameter) c).
						collect(Collectors.toList())).forEach(parameters::addAll);
		return parameters;
	}

	private static boolean hasAspect(Mogram node, Constraint.Facet constraint) {
		return node.appliedFacets().stream().anyMatch(facet -> facet.fullType().equalsIgnoreCase(constraint.type()));
	}

	@Nullable
	public static List<Constraint> constraintsOf(Facet facet) {
		final Mogram node = TaraPsiUtil.getContainerNodeOf((PsiElement) facet);
		final List<Constraint> nodeConstraints = constraintsOf(node);
		if (nodeConstraints == null || nodeConstraints.isEmpty()) return emptyList();
		return collectAspectConstrains(facet, nodeConstraints);
	}

	private static List<Constraint> collectAspectConstrains(Facet aspect, List<Constraint> constraints) {
		return constraints.stream().
				filter(c -> c instanceof Constraint.Facet && ((Constraint.Facet) c).type().equals(aspect.fullType())).
				findFirst().
				map(c -> ((Constraint.Facet) c).constraints()).
				orElse(emptyList());
	}

	@Nullable
	public static TaraVariable getOverriddenVariable(Variable variable) {
		Mogram mogram = TaraPsiUtil.getContainerNodeOf((PsiElement) variable);
		if (mogram == null) return null;
		Mogram parent = mogram.parent();
		while (parent != null) {
			for (Variable parentVar : parent.variables())
				if (isOverridden(variable, parentVar))
					return (TaraVariable) parentVar;
			parent = parent.parent();
		}

		return null;
	}

	private static boolean isOverridden(Variable variable, Variable parentVar) {
		return parentVar.type() != null && parentVar.type().equals(variable.type()) && parentVar.name() != null && parentVar.name().equals(variable.name());
	}

	public static Constraint.Parameter parameterConstraintOf(Parameter parameter) {
		List<Constraint.Parameter> parameters = parameterConstraintsOf(parameter.container());
		if (parameters.isEmpty() || parameters.size() <= parameter.position()) return null;
		return !parameter.name().isEmpty() || parameter instanceof TaraVarInit ?
				findParameter(parameters, parameter.name()) : getParameterByIndex(parameter, parameters);
	}

	private static Constraint.Parameter getParameterByIndex(Parameter parameter, List<Constraint.Parameter> parameterConstraints) {
		for (Constraint.Parameter constraint : parameterConstraints)
			if (constraint.position() == getIndexInParent(parameter)) return constraint;
		return null;
	}

	private static int getIndexInParent(Parameter parameter) {
		return parameter.position();
	}

	private static Constraint.Parameter findParameter(List<Constraint.Parameter> parameters, String name) {
		for (Constraint.Parameter variable : parameters)
			if (variable.name().equals(name))
				return variable;
		return null;
	}

	public static List<Mogram> getMainNodesOfFile(TaraModel file) {
		Set<Mogram> list = new LinkedHashSet<>();
		Mogram[] nodes = components(file);
		if (nodes == null) return new ArrayList<>(list);
		for (Mogram mogram : nodes) {
			list.add(mogram);
			list.addAll(mogram.subs());
		}
		List<Mogram> mainNodes = findMainNodes(file);
		for (Mogram main : mainNodes) {
			if (list.contains(main)) continue;
			list.add(main);
		}
		return new ArrayList<>(list);
	}

	@NotNull
	private static TaraModel[] getModuleFiles(PsiFile psiFile) {
		Module module = ModuleProvider.moduleOf(psiFile);
		if (module == null) return new TaraModelImpl[0];
		List<TaraModel> taraFiles = getFilesOfModuleByFileType(module, psiFile.getFileType());
		return taraFiles.toArray(new TaraModel[0]);
	}

	public static List<TaraModel> getTaraFilesOfModule(Module module) {
		return filesOf(module, TaraFileType.instance());
	}

	public static List<TaraModel> getFilesOfModuleByFileType(Module module, FileType fileType) {
		return filesOf(module, fileType);
	}

	@NotNull
	private static List<TaraModel> filesOf(Module module, FileType fileType) {
		List<TaraModel> taraFiles = new ArrayList<>();
		if (module == null) return taraFiles;
		Collection<VirtualFile> files = FileTypeIndex.getFiles(fileType, GlobalSearchScope.moduleScope(module));
		files.stream().filter(Objects::nonNull).forEach(file -> {
			TaraModel taraFile = (TaraModel) PsiManager.getInstance(module.getProject()).findFile(file);
			if (taraFile != null) taraFiles.add(taraFile);
		});
		return taraFiles;
	}

	public static List<Mogram> getAllNodesOfFile(TaraModel model) {
		Set<Mogram> all = new HashSet<>();
		final Mogram[] rootNodes = components(model);
		if (rootNodes == null) return emptyList();
		for (Mogram include : rootNodes) all.addAll(include.subs());
		for (Mogram root : rootNodes) getRecursiveComponentsOf(root, all);
		return new ArrayList<>(all);
	}

	private static void getRecursiveComponentsOf(Mogram root, Set<Mogram> all) {
		all.add(root);
		TaraMogram[] components = PsiTreeUtil.getChildrenOfType(((TaraMogram) root).getBody(), TaraMogram.class);
		if (components != null) for (Mogram include : components) getRecursiveComponentsOf(include, all);
	}

	@NotNull
	public static List<Mogram> getComponentsOf(MogramContainer container) {
		return TaraPsiUtil.componentsOf((Mogram) container);
	}

	@Nullable
	public static Mogram findComponent(MogramContainer mogram, String name) {
		for (Mogram include : mogram.components())
			if (include.name() != null && include.name().equals(name)) return include;
		if (!(mogram instanceof Mogram) || ((Mogram) mogram).parent() == null) return null;
		return findInParentComponents((Mogram) mogram, name);
	}

	@Nullable
	private static Mogram findInParentComponents(Mogram node, String name) {
		for (Mogram include : node.parent().components())
			if (include.name() != null && include.name().equals(name))
				return include;
		return null;
	}

	public static List<VirtualFile> getSourceRoots(@NotNull PsiElement foothold) {
		final Module module = ModuleUtilCore.findModuleForPsiElement(foothold);
		if (module != null) return getSourceRoots(module);
		return emptyList();
	}

	public static List<VirtualFile> getSourceRoots(@NotNull Module module) {
		final Set<VirtualFile> result = new LinkedHashSet<>();
		final ModuleRootManager manager = ModuleRootManager.getInstance(module);
		result.addAll(Arrays.asList(manager.getSourceRoots()));
		result.addAll(Arrays.asList(manager.getContentRoots()));
		return new ArrayList<>(result);
	}

	@NotNull
	public static String importsFile(Valued valued) {
		return ModuleProvider.moduleOf(valued).getName() + LanguageManager.JSON;
	}

	public static String methodReference(PsiElement valued) {
		final PsiDirectory aPackage = valued.getContainingFile().getContainingDirectory();
		final PsiJavaFile file = (PsiJavaFile) aPackage.findFile(((TaraModel) valued.getContainingFile()).getPresentableName() + ".java");
		if (file == null || file.getClasses().length == 0) return "";
		return file.getClasses()[0].getQualifiedName();
	}

	public static PsiDirectory findFunctionsDirectory(Module module, String workingPackage) {
		return findOrCreateDirectory(module, workingPackage, FUNCTIONS);
	}

	private static PsiDirectory findOrCreateDirectory(Module module, String workingPackage, String dirName) {
		if (module == null) return null;
		final VirtualFile srcRoot = getSrcRoot(module);
		final PsiDirectory srcDirectory = srcRoot == null ? null : new PsiDirectoryImpl((com.intellij.psi.impl.PsiManagerImpl) PsiManager.getInstance(module.getProject()), srcRoot);
		final List<String> path = new ArrayList<>(Arrays.asList(workingPackage.toLowerCase().split("\\.")));
		path.add(dirName);
		PsiDirectory destinyDir = srcDirectory;
		if (destinyDir == null) return null;
		for (String name : path) {
			if (destinyDir == null) break;
			destinyDir = destinyDir.findSubdirectory(name) == null ? createDirectory(destinyDir, name) : destinyDir.findSubdirectory(name);
		}
		return destinyDir;
	}

	private static PsiDirectory createDirectory(final PsiDirectory basePath, final String name) {
		final com.intellij.openapi.application.Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			return application.<PsiDirectory>runWriteAction(() -> DirectoryUtil.createSubdirectories(name, basePath, "."));
		else {
			PsiDirectory[] directory = new PsiDirectory[1];
			application.invokeLater(() -> directory[0] = application.<PsiDirectory>runWriteAction(() -> DirectoryUtil.createSubdirectories(name, basePath, ".")));
			return directory[0];
		}
	}

	public static VirtualFile getResourcesRoot(PsiElement element) {
		final Module module = ModuleProvider.moduleOf(element);
		return getResourcesRoot(module, isTestModelFile(element.getContainingFile()));
	}

	public static VirtualFile getResourcesRoot(Module module, boolean test) {
		if (module == null) return null;
		final List<VirtualFile> roots = ModuleRootManager.getInstance(module).getSourceRoots(test ? TEST_RESOURCE : RESOURCE);
		if (roots.isEmpty()) return null;
		return roots.stream().filter(r -> r.getName().equals(test ? "test-res" : "res")).findAny().orElse(null);
	}

	public static VirtualFile getSrcRoot(Module module) {
		if (module == null) return null;
		for (VirtualFile file : getSourceRoots(module))
			if (file.isDirectory() && "src".equals(file.getName())) return file;
		return null;
	}

	public static VirtualFile getContentRoot(Module module, String name) {
		if (module == null) return null;
		final VirtualFile[] roots = ModuleRootManager.getInstance(module).getSourceRoots();
		for (VirtualFile file : roots)
			if (file.isDirectory() && name.equals(file.getName())) return file;
		return null;
	}

	private static List<Mogram> findMainNodes(TaraModel file) {
		final TaraMogram[] childrenOfType = components(file);
		if (childrenOfType == null) return emptyList();
		final List<Mogram> rootNodes = Arrays.asList(childrenOfType);
		return rootNodes.stream().filter((node) -> !TaraPsiUtil.isAnnotatedAsComponent(node)).toList();
	}

	private static TaraMogram[] components(TaraModel file) {
		try {
			Application application = ApplicationManager.getApplication();
			if (application.isReadAccessAllowed()) return PsiTreeUtil.getChildrenOfType(file, TaraMogram.class);
			return application.<TaraMogram[]>runReadAction(() -> PsiTreeUtil.getChildrenOfType(file, TaraMogram.class));
		} catch (RuntimeException e) {
			return new TaraMogram[0];
		}
	}

	public static PsiDirectory createResourceRoot(Module module, String name) {
		VirtualFile moduleDir = ProjectUtil.guessModuleDir(module);
		if (moduleDir == null) return null;
		return createDirectory(read(() -> PsiManager.getInstance(module.getProject()).findDirectory(moduleDir.getParent())), name);
	}
}