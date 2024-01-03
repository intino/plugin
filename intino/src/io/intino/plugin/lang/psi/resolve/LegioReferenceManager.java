package io.intino.plugin.lang.psi.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.ConfigurationManager;
import io.intino.plugin.project.configuration.ProjectLegioConfiguration;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramContainer;
import io.intino.tara.language.model.Tag;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LegioReferenceManager {

	private LegioReferenceManager() {
	}

	@NotNull
	public static List<PsiElement> resolve(Identifier identifier) {
		PsiElement reference = internalResolve(identifier);
		return Collections.singletonList(reference instanceof Mogram && !(reference instanceof TaraModel) ? ((TaraMogram) reference).getSignature().getIdentifier() : reference);
	}

	@Nullable
	public static Mogram resolveToNode(IdentifierReference identifierReference) {
		if (identifierReference == null) return null;
		List<? extends Identifier> identifierList = identifierReference.getIdentifierList();
		return (Mogram) resolveNode(identifierList.get(identifierList.size() - 1), (List<Identifier>) identifierList);
	}

	@Nullable
	public static PsiElement resolve(IdentifierReference identifierReference) {
		List<? extends Identifier> identifierList = identifierReference.getIdentifierList();
		PsiElement reference = resolveNode(identifierList.get(identifierList.size() - 1), (List<Identifier>) identifierList);
		if (reference instanceof Mogram) reference = ((TaraMogram) reference).getSignature().getIdentifier();
		return reference;
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
		TaraModel containingFile = (TaraModel) identifier.getContainingFile().getOriginalFile();
		if (containingFile.getVirtualFile() == null) return null;
		Module moduleOfDocument = ModuleProvider.moduleOf(containingFile);
		TaraModel projectFile = ((ProjectLegioConfiguration) ConfigurationManager.projectConfigurationOf(moduleOfDocument)).legioFile();
		return projectFile.getPresentableName().equals(identifier.getText()) ? projectFile : null;
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
		element = tryToResolveAsQN(subPath);
		if (element != null) return element;
		return tryToResolveInProject(subPath);
	}

	private static PsiElement tryToResolveInProject(List<Identifier> subPath) {
		return null;
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
		TaraModel model = null;
		Identifier identifier = path.get(0);
		TaraModel containingFile = (TaraModel) identifier.getContainingFile().getOriginalFile();
		if (containingFile.getVirtualFile() != null) {
			Module moduleOfDocument = ModuleProvider.moduleOf(containingFile);
			if (moduleOfDocument == null) return null;
			TaraModel projectFile = ((ProjectLegioConfiguration) ConfigurationManager.projectConfigurationOf(moduleOfDocument)).legioFile();
			if (projectFile != null)
				model = projectFile.getPresentableName().equals(identifier.getText()) ? projectFile : null;
		}
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
				toList());
	}

	private static void collectContextNodes(Identifier identifier, Set<Mogram> set, Mogram node) {
		Mogram container = node;
		final Mogram containerNode = TaraPsiUtil.getContainerNodeOf(identifier);
		while (container != null) {
			set.addAll(collectCandidates(container).stream().
					filter(sibling -> areNamesake(identifier, sibling) && !sibling.equals(containerNode)).
					toList());
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

	private static PsiElement tryToResolveOnImportedModels(List<Identifier> path) {
		TaraModel context = (TaraModel) path.get(0).getContainingFile();
		Collection<Import> imports = context.getImports();
		return (PsiElement) searchInImport(path, imports);
	}

	private static MogramContainer searchInImport(List<Identifier> path, Collection<Import> imports) {
		for (Import anImport : imports) {
			PsiElement resolve = resolveImport(anImport);
			if (resolve == null || !(resolve.getContainingFile() instanceof TaraModel)) continue;
			MogramContainer mogram = tryToResolveInBox((TaraModel) resolve.getContainingFile(), path);
			if (mogram != null) return mogram;
		}
		return null;
	}

	private static PsiElement resolveImport(Import anImport) {
		List<TaraIdentifier> importIdentifiers = anImport.getHeaderReference().getIdentifierList();
		return resolve(importIdentifiers.get(importIdentifiers.size() - 1)).get(0);
	}

}