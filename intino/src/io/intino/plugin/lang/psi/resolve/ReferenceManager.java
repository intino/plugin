package io.intino.plugin.lang.psi.resolve;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import io.intino.plugin.codeinsight.JavaHelper;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.Rule;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ReferenceManager {

	@NotNull
	public static List<PsiElement> resolve(Identifier identifier) {
		if (identifier.getContainingFile().getFileType().equals(LegioFileType.instance()))
			return LegioReferenceManager.resolve(identifier);
		PsiElement reference = internalResolve(identifier);
		return Collections.singletonList(reference instanceof Mogram && !(reference instanceof TaraModel) ? ((TaraMogram) reference).getSignature().getIdentifier() : reference);
	}

	@Nullable
	public static Mogram resolveToNode(IdentifierReference identifierReference) {
		if (identifierReference == null) return null;
		if (identifierReference.getContainingFile().getFileType().equals(LegioFileType.instance()))
			return LegioReferenceManager.resolveToNode(identifierReference);
		List<? extends Identifier> identifierList = identifierReference.getIdentifierList();
		return (Mogram) resolveNode(identifierList.get(identifierList.size() - 1), (List<Identifier>) identifierList);
	}

	@Nullable
	public static PsiElement resolve(IdentifierReference ref) {
		if (ref.getContainingFile().getFileType().equals(LegioFileType.instance()))
			return LegioReferenceManager.resolve(ref);
		List<? extends Identifier> identifierList = ref.getIdentifierList();
		PsiElement reference = resolveNode(identifierList.get(identifierList.size() - 1), (List<Identifier>) identifierList);
		if (reference instanceof Mogram) reference = ((TaraMogram) reference).getSignature().getIdentifier();
		return reference;
	}

	@Nullable
	public static PsiElement resolveJavaClassReference(Project project, String path) {
		if (project == null || path == null || path.isEmpty()) return null;
		return JavaHelper.getJavaHelper(project).findClass(path.trim());
	}

	private static PsiElement internalResolve(Identifier identifier) {
		if (identifier.getParent() instanceof IdentifierReference)
			return resolveNode(identifier, getIdentifiersOfReference(identifier));
		if (identifier.getParent() instanceof HeaderReference)
			return identifier.getParent().getParent() instanceof TaraDslDeclaration ? identifier : resolveHeaderReference(identifier);
		if (identifier.getParent() instanceof Signature) return identifier;
		return null;
	}

	private static PsiElement resolveHeaderReference(Identifier identifier) {
		return resolveModelPath(identifier);
	}

	private static List<Identifier> getIdentifiersOfReference(Identifier identifier) {
		List<Identifier> path = (List<Identifier>) ((IdentifierReference) (identifier.getParent())).getIdentifierList();
		path = path.subList(0, path.indexOf(identifier) + 1);
		return path;
	}

	private static PsiElement resolveNode(Identifier identifier, List<Identifier> path) {
		List<Identifier> subPath = path.subList(0, path.indexOf(identifier) + 1);
		PsiElement element = (PsiElement) tryToResolveInBox((TaraModel) identifier.getContainingFile(), subPath);
		if (element != null) return element;
		element = tryToResolveOnImportedModels(subPath);
		if (element != null) return element;
		return tryToResolveAsQN(subPath);
	}

	private static MogramContainer tryToResolveInBox(TaraModel file, List<Identifier> path) {
		Mogram[] roots = getPossibleRoots(file, path.get(0));
		if (roots.length == 0) return null;
		if (roots.length == 1 && path.size() == 1) return roots[0];
		for (Mogram possibleRoot : roots) {
			if (possibleRoot.is(Tag.Enclosed)) continue;
			MogramContainer mogram = resolvePathInMogram(path, possibleRoot);
			if (mogram != null) return mogram;
		}
		return null;
	}

	private static Mogram[] getPossibleRoots(TaraModel file, Identifier identifier) {
		Set<Mogram> set = new LinkedHashSet<>();
		if (file.equals(identifier.getContainingFile())) addNodesInContext(identifier, set);
		if (isVariableReference(identifier)) addNodeSiblings(identifier, set);
		addRootNodes(file, identifier, set);
		return set.toArray(new Mogram[0]);
	}

	private static boolean isVariableReference(Identifier identifier) {
		return TaraPsiUtil.getContainerByType(identifier, Variable.class) != null;
	}

	private static void addNodeSiblings(Identifier identifier, Set<Mogram> set) {
		final MogramContainer container = TaraPsiUtil.getContainerOf(identifier);
		if (container == null) return;
		set.addAll(container.components().stream().filter(mogram -> areNamesake(identifier, mogram)).toList());
	}

	private static PsiElement tryToResolveAsQN(List<Identifier> path) {
		TaraModel model = resolveModelPath(path.get(0));
		if (model == null || path.isEmpty()) return null;
		List<Identifier> qn = path.subList(1, path.size());
		if (qn.isEmpty()) return null;
		return (PsiElement) tryToResolveInBox(model, qn);
	}

	private static void addRootNodes(TaraModel model, Identifier identifier, Set<Mogram> set) {
		List<Mogram> nodes = model.components();
		set.addAll(nodes.stream().filter(mogram -> areNamesake(identifier, mogram)).toList());
	}

	private static void addNodesInContext(Identifier identifier, Set<Mogram> set) {
		Mogram container = TaraPsiUtil.getContainerNodeOf(identifier);
		if (container != null && !isExtendsOrParameterReference(identifier) && areNamesake(identifier, container))
			set.add(container);
		if (container != null) {
			collectContextNodes(identifier, set, container);
			if (isExtendsOrParameterReference(identifier) && container.container() != null) {
				final Mogram parent = container.container().parent();
				if (parent != null) collectParentComponents(identifier, set, parent);
			}
		}
	}

	private static void collectParentComponents(Identifier identifier, Set<Mogram> set, Mogram parent) {
		final Mogram containerNode = TaraPsiUtil.getContainerNodeOf(identifier);
		set.addAll(parent.components().stream().
				filter(sibling -> areNamesake(identifier, sibling) && !sibling.equals(containerNode)).
				collect(Collectors.toList()));
	}

	private static void collectContextNodes(Identifier identifier, Set<Mogram> set, Mogram node) {
		Mogram container = node;
		final Mogram containerNode = TaraPsiUtil.getContainerNodeOf(identifier);
		while (container != null) {
			set.addAll(collectCandidates(container).stream().
					filter(sibling -> areNamesake(identifier, sibling) && !sibling.equals(containerNode)).
					collect(Collectors.toList()));
			container = container.container();
		}
	}

	private static List<Mogram> collectCandidates(Mogram container) {
		List<? extends Mogram> siblings = container.siblings();
		List<Mogram> nodes = new ArrayList<>(siblings);
		for (Mogram mogram : siblings) nodes.addAll(mogram.subs());
		return nodes;
	}

	private static boolean isExtendsOrParameterReference(Identifier reference) {
		PsiElement parent = reference.getParent();
		while (parent != null && !(parent instanceof Signature) && !(parent instanceof Mogram))
			parent = parent.getParent();
		return parent instanceof Signature;
	}

	private static boolean areNamesake(Identifier identifier, Mogram node) {
		return identifier.getText().equals(node.name());
	}

	private static MogramContainer resolvePathInMogram(List<Identifier> path, Mogram mogram) {
		Mogram reference = null;
		for (Identifier identifier : path) {
			reference = reference == null ? areNamesake(identifier, mogram) ? mogram : null :
					findIn(reference, identifier);
			if (reference == null || reference.is(Tag.Enclosed) && !isLast(identifier, path))
				return null;
		}
		return reference;
	}

	private static Mogram findIn(Mogram node, Identifier identifier) {
		return IntinoUtil.findComponent(node, identifier.getText());
	}

	private static boolean isLast(Identifier identifier, List<Identifier> path) {
		return path.indexOf(identifier) == path.size() - 1;
	}

	private static TaraModel resolveModelPath(Identifier identifier) {
		TaraModel containingFile = (TaraModel) identifier.getContainingFile().getOriginalFile();
		if (containingFile.getVirtualFile() == null) return null;
		Module moduleOfDocument = ModuleProvider.moduleOf(containingFile);
		for (TaraModel taraBoxFile : IntinoUtil.getFilesOfModuleByFileType(moduleOfDocument, containingFile.getFileType()))
			if (taraBoxFile.getPresentableName().equals(identifier.getText())) return taraBoxFile;
		return null;
	}

	private static PsiElement tryToResolveOnImportedModels(List<Identifier> path) {
		TaraModel context = (TaraModel) path.get(0).getContainingFile();
		Collection<Import> imports = context.getImports();
		return (PsiElement) searchInImport(path, imports);
	}

	private static MogramContainer searchInImport(List<Identifier> path, Collection<Import> imports) {
		for (Import anImport : imports) {
			PsiElement resolve = resolveImport(anImport);
			if (resolve == null || !TaraModel.class.isInstance(resolve.getContainingFile())) continue;
			MogramContainer mogram = tryToResolveInBox((TaraModel) resolve.getContainingFile(), path);
			if (mogram != null) return mogram;
		}
		return null;
	}

	private static PsiElement resolveImport(Import anImport) {
		List<TaraIdentifier> importIdentifiers = anImport.getHeaderReference().getIdentifierList();
		return resolve(importIdentifiers.get(importIdentifiers.size() - 1)).get(0);
	}

	public static PsiElement resolveRule(Rule rule) {
		if (rule == null) return null;
		return isNative(rule) ? resolveNativeClass(rule, rule.getProject()) : resolveRuleToClass(rule);
	}

	private static boolean isNative(Rule rule) {
		Variable variable = TaraPsiUtil.getContainerByType(rule, Variable.class);
		return variable != null && Primitive.FUNCTION.equals(variable.type());
	}

	private static PsiElement resolveRuleToClass(Rule rule) {
		return resolveJavaClassReference(rule.getProject(), IntinoUtil.dslGenerationPackage(rule).toLowerCase() + ".rules." + rule.getText());
	}

	private static PsiElement resolveNativeClass(Rule rule, Project project) {
		if (rule == null) return null;
		String aPackage = IntinoUtil.dslGenerationPackage(rule) + '.' + "functions";
		return resolveJavaClassReference(project, aPackage.toLowerCase() + '.' + capitalize(rule.getText()));
	}

	private static final String DOC_SEPARATOR = "#";

	public static PsiElement resolveJavaNativeImplementation(PsiClass psiClass) {
		if (psiClass.isInterface() || psiClass.getDocComment() == null) return null;
		String data = findData(psiClass.getDocComment().getChildren());
		if (data.isEmpty()) return null;
		String[] nativeInfo = data.split(DOC_SEPARATOR);
		if (nativeInfo.length < 2) return null;
		File destinyFile = new File(nativeInfo[1]);
		final List<TaraModel> filesOfModule = IntinoUtil.getTaraFilesOfModule(ModuleProvider.moduleOf(psiClass));
		for (TaraModel taraModel : filesOfModule)
			if (FileUtil.compareFiles(destinyFile, new File(taraModel.getVirtualFile().getPath())) == 0)
				return searchNodeIn(taraModel, nativeInfo);
		return null;
	}

	public static PsiElement resolveTaraNativeImplementationToJava(Valued valued) {
		String workingPackage = IntinoUtil.dslGenerationPackage(valued);
		if (ModuleProvider.moduleOf(valued) == null) return null;
		if (workingPackage.isEmpty())
			workingPackage = ModuleProvider.moduleOf(valued).getName();
		for (PsiClass aClass : getCandidates(valued, workingPackage.toLowerCase()))
			if (valued.equals(TaraPsiUtil.getContainerByType(resolveJavaNativeImplementation(aClass), Valued.class)))
				return aClass;
		return null;
	}

	@NotNull
	private static List<PsiClass> getCandidates(Valued valued, String generatedDSL) {
		final PsiPackage aPackage = (PsiPackage) JavaHelper.getJavaHelper(valued.getProject()).findPackage(generatedDSL.toLowerCase() + ".natives");
		if (aPackage == null || valued.name() == null) return Collections.emptyList();
		return getAllClasses(aPackage).stream().filter(c -> c.getName() != null && c.getName().startsWith(Format.firstUpperCase().format(valued.name()) + "_")).toList();
	}

	private static List<PsiClass> getAllClasses(PsiPackage aPackage) {
		List<PsiClass> psiClasses = new ArrayList<>(Arrays.asList(aPackage.getClasses()));
		Arrays.asList(aPackage.getSubPackages()).forEach(p -> psiClasses.addAll(ReferenceManager.getAllClasses(p)));
		return psiClasses;
	}

	private static String findData(PsiElement[] elements) {
		for (PsiElement element : elements) {
			final String comment = element.getNode().getElementType().toString();
			if ("DOC_COMMENT_DATA".equals(comment) || "GDOC_COMMENT_DATA".equals(comment)) return element.getText();
		}
		return "";
	}

	private static PsiElement searchNodeIn(TaraModel taraModel, String[] nativeInfo) {
		final Document document = PsiDocumentManager.getInstance(taraModel.getProject()).getDocument(taraModel);
		if (document == null) return null;
		final int start = Integer.parseInt(nativeInfo[2]) - 1;
		if (document.getLineCount() <= start) return null;
		final PsiElement elementAt = taraModel.findElementAt(document.getLineStartOffset(start) + Integer.parseInt(nativeInfo[3]));
		return elementAt != null && (elementAt.getNode().getElementType().equals(TaraTypes.NEWLINE) ||
				elementAt.getNode().getElementType().equals(TaraTypes.NEW_LINE_INDENT)) ? elementAt.getNextSibling() : elementAt;
	}

	private static String capitalize(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

}