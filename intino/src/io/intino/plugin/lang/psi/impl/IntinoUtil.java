package io.intino.plugin.lang.psi.impl;

import com.intellij.ide.util.DirectoryUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.Configuration;
import io.intino.magritte.Language;
import io.intino.magritte.lang.model.*;
import io.intino.magritte.lang.semantics.Constraint;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.project.configuration.ConfigurationManager;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

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

	public static List<Node> findRootNode(PsiElement element, String identifier) {
		List<Node> result = new ArrayList<>();
		for (TaraModel taraFile : getModuleFiles(element.getContainingFile())) {
			Collection<Node> nodes = taraFile.components();
			extractNodesByName(identifier, result, nodes);
		}
		return result;
	}

	private static void extractNodesByName(String identifier, List<Node> result, Collection<Node> nodes) {
		result.addAll(nodes.stream().filter(node -> identifier.equals(node.name())).collect(Collectors.toList()));
	}

	@Nullable
	public static Language getLanguage(PsiElement element) {
		if (element == null) return null;
		PsiFile file = element.getContainingFile();
		if (file == null) return null;
		return LanguageManager.getLanguage(file.getVirtualFile() == null ? file.getOriginalFile() : file);
	}

	public static String graphPackage(@NotNull PsiElement element) {
		if (!(element.getContainingFile() instanceof TaraModel)) return "";
		final Module module = ModuleProvider.moduleOf(element);
		final Configuration conf = configurationOf(module);
		if (conf == null) return "";
		return safe(() -> conf.artifact().code().generationPackage()) + (isTest(element.getContainingFile(), module) ? ".test" : "") + ".graph";
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
		Configuration.Artifact.Model.Language language = conf.artifact().model().language();
		if (language == null) return null;
		return language.generationPackage();
	}

	public static String graphPackage(@NotNull Module module) {
		final Configuration conf = configurationOf(module);
		if (conf == null) return "";
		return safe(() -> conf.artifact().code().generationPackage()) + ".graph";
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
	public static List<Constraint> constraintsOf(Node node) {
		Language language = getLanguage((PsiElement) node);
		if (language == null) return null;
		return language.constraints(node.resolve().type());
	}

	@NotNull
	public static List<Constraint.Parameter> parameterConstraintsOf(Node node) {
		Language language = getLanguage((PsiElement) node);
		if (language == null) return emptyList();
		final List<Constraint> nodeConstraints = language.constraints(node.resolve().type());
		if (nodeConstraints == null) return emptyList();
		List<Constraint.Parameter> parameters = new ArrayList<>();
		for (Constraint constraint : nodeConstraints)
			if (constraint instanceof Constraint.Parameter) parameters.add((Constraint.Parameter) constraint);
			else if (constraint instanceof Constraint.Aspect && hasAspect(node, (Constraint.Aspect) constraint))
				parameters.addAll(((Constraint.Aspect) constraint).constraints().stream().filter(c -> c instanceof Constraint.Parameter).map(c -> (Constraint.Parameter) c).collect(Collectors.toList()));
		return parameters;
	}

	@NotNull
	public static List<Constraint.Parameter> aspectParameterConstraintsOf(Node node) {
		Language language = getLanguage((PsiElement) node);
		if (language == null) return emptyList();
		final List<Constraint> nodeConstraints = language.constraints(node.resolve().type());
		if (nodeConstraints == null) return emptyList();
		List<Constraint.Parameter> parameters = new ArrayList<>();
		nodeConstraints.stream().
				filter(constraint -> constraint instanceof Constraint.Aspect && hasAspect(node, (Constraint.Aspect) constraint)).
				map(constraint -> ((Constraint.Aspect) constraint).constraints().stream().filter(c -> c instanceof Constraint.Parameter).map(c -> (Constraint.Parameter) c).
						collect(Collectors.toList())).forEach(parameters::addAll);
		return parameters;
	}

	private static boolean hasAspect(Node node, Constraint.Aspect constraint) {
		return node.appliedAspects().stream().anyMatch(facet -> facet.fullType().equalsIgnoreCase(constraint.type()));
	}

	@Nullable
	public static List<Constraint> constraintsOf(Aspect aspect) {
		final Node node = TaraPsiUtil.getContainerNodeOf((PsiElement) aspect);
		final List<Constraint> nodeConstraints = constraintsOf(node);
		if (nodeConstraints == null || nodeConstraints.isEmpty()) return emptyList();
		return collectAspectConstrains(aspect, nodeConstraints);
	}

	private static List<Constraint> collectAspectConstrains(Aspect aspect, List<Constraint> constraints) {
		return constraints.stream().
				filter(c -> c instanceof Constraint.Aspect && ((Constraint.Aspect) c).type().equals(aspect.fullType())).
				findFirst().
				map(c -> ((Constraint.Aspect) c).constraints()).
				orElse(emptyList());
	}

	@Nullable
	public static TaraVariable getOverriddenVariable(Variable variable) {
		Node node = TaraPsiUtil.getContainerNodeOf((PsiElement) variable);
		if (node == null) return null;
		Node parent = node.parent();
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

	public static List<Node> getMainNodesOfFile(TaraModel file) {
		Set<Node> list = new LinkedHashSet<>();
		Node[] nodes = components(file);
		if (nodes == null) return new ArrayList<>(list);
		for (Node node : nodes) {
			list.add(node);
			list.addAll(node.subs());
		}
		List<Node> mainNodes = findMainNodes(file);
		for (Node main : mainNodes) {
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

	public static List<Node> getAllNodesOfFile(TaraModel model) {
		Set<Node> all = new HashSet<>();
		final Node[] rootNodes = components(model);
		if (rootNodes == null) return emptyList();
		for (Node include : rootNodes) all.addAll(include.subs());
		for (Node root : rootNodes) getRecursiveComponentsOf(root, all);
		return new ArrayList<>(all);
	}

	private static void getRecursiveComponentsOf(Node root, Set<Node> all) {
		all.add(root);
		TaraNode[] components = PsiTreeUtil.getChildrenOfType(((TaraNode) root).getBody(), TaraNode.class);
		if (components != null) for (Node include : components) getRecursiveComponentsOf(include, all);
	}

	@NotNull
	public static List<Node> getComponentsOf(NodeContainer container) {
		return TaraPsiUtil.componentsOf((Node) container);
	}

	@Nullable
	public static Node findComponent(NodeContainer node, String name) {
		for (Node include : node.components())
			if (include.name() != null && include.name().equals(name)) return include;
		if (!(node instanceof Node) || ((Node) node).parent() == null) return null;
		return findInParentComponents((Node) node, name);
	}

	@Nullable
	private static Node findInParentComponents(Node node, String name) {
		for (Node include : node.parent().components())
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

	private static List<Node> findMainNodes(TaraModel file) {
		final TaraNode[] childrenOfType = components(file);
		if (childrenOfType == null) return emptyList();
		final List<Node> rootNodes = Arrays.asList(childrenOfType);
		return rootNodes.stream().filter((node) -> !TaraPsiUtil.isAnnotatedAsComponent(node)).collect(Collectors.toList());
	}

	private static TaraNode[] components(TaraModel file) {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return PsiTreeUtil.getChildrenOfType(file, TaraNode.class);
		return application.<TaraNode[]>runReadAction(() -> PsiTreeUtil.getChildrenOfType(file, TaraNode.class));
	}

	public static PsiDirectory createResourceRoot(Module module, String name) {
		return createDirectory(read(() -> PsiManager.getInstance(module.getProject()).findDirectory(module.getModuleFile().getParent())), name);
	}
}