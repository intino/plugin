package io.intino.plugin.lang.psi.resolve;

import com.intellij.psi.PsiElement;
import io.intino.magritte.Checker;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.model.Tag;
import io.intino.magritte.lang.model.rules.variable.ReferenceRule;
import io.intino.magritte.lang.semantics.Constraint;
import io.intino.magritte.lang.semantics.constraints.parameter.ReferenceParameter;
import io.intino.magritte.lang.semantics.errorcollector.SemanticFatalException;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class VariantsManager {

	private final Set<Node> variants = new LinkedHashSet<>();
	private final Identifier myElement;
	private final List<Identifier> context;

	VariantsManager(Identifier myElement) {
		this.myElement = myElement;
		this.context = solveIdentifierContext();
	}

	Set<Node> resolveVariants() {
		if (hasContext()) addContextVariants();
		else {
			addInModelVariants();
			addImportVariants();
		}
		final Node node = TaraPsiUtil.getContainerNodeOf(myElement);
		if (node == null || node.type() == null) return variants;
		if (isParentReference((IdentifierReference) myElement.getParent()))
			variants.removeAll(collectUnacceptableNodes(singletonList(node.type())));
		else if (isParameterReference(myElement))
			variants.removeAll(collectUnacceptableNodes(filterTypes(myElement)));
		return variants;
	}

	private List<String> filterTypes(PsiElement element) {
		final Node node = TaraPsiUtil.getContainerNodeOf(element);
		check(node);
		final List<Constraint> constraints = IntinoUtil.getConstraintsOf(node);
		final Parameter parameter = TaraPsiUtil.getContainerByType(element, Parameter.class);
		if (constraints == null || parameter == null || parameter.name() == null) return emptyList();
		Constraint.Parameter constraint = findParameter(constraints, parameter);
		if (constraint == null && !node.appliedAspects().isEmpty())
			constraint = searchInFacets(node.appliedAspects(), constraints, parameter);
		if (constraint == null || !(constraint.rule() instanceof ReferenceRule)) return emptyList();
		return ((ReferenceRule) constraint.rule()).allowedReferences();
	}

	private Constraint.Parameter searchInFacets(List<io.intino.magritte.lang.model.Aspect> aspects, List<Constraint> constraints, Parameter parameter) {
		for (Constraint c : constraints)
			if (c instanceof Constraint.Aspect && facetOf((Constraint.Aspect) c, aspects) != null)
				return findParameter(((Constraint.Aspect) c).constraints(), parameter);
		return null;
	}

	private io.intino.magritte.lang.model.Aspect facetOf(Constraint.Aspect c, List<io.intino.magritte.lang.model.Aspect> aspects) {
		for (io.intino.magritte.lang.model.Aspect aspect : aspects)
			if (aspect.type().equals(c.type())) return aspect;
		return null;
	}

	private Constraint.Parameter findParameter(List<Constraint> constraints, Parameter parameter) {
		return (Constraint.Parameter) constraints.stream().
				filter(c -> c instanceof ReferenceParameter && isConstraintOf(parameter, c)).findFirst().orElse(null);
	}

	private boolean isConstraintOf(Parameter parameter, Constraint constraint) {
		final ReferenceParameter c = (ReferenceParameter) constraint;
		return c.name().equals(parameter.name()) || c.isConstraintOf(parameter);
	}

	private void check(Node node) {
		Checker checker = new Checker(LanguageManager.getLanguage(myElement.getContainingFile()));
		try {
			checker.check(node);
		} catch (SemanticFatalException ignored) {
		}
	}

	private boolean isParameterReference(PsiElement element) {
		return TaraPsiUtil.getContainerByType(element, Parameter.class) != null;
	}

	private List<Node> collectUnacceptableNodes(List<String> expectedTypes) {
		if (expectedTypes.isEmpty()) return emptyList();
		return variants.stream().
				filter(variant -> {
					variant.resolve();
					return variant.type() != null && variant.types().stream().noneMatch(t -> expectedTypes.contains(t.split(":")[0]));
				}).collect(Collectors.toList());
	}

	private boolean hasContext() {
		return getContext().indexOf(myElement) != 0;
	}

	@NotNull
	private List<? extends Identifier> getContext() {
		return ((IdentifierReference) myElement.getParent()).getIdentifierList();
	}

	private void addContextVariants() {
		final List<Identifier> aContext = (List<Identifier>) getContext();
		final List<PsiElement> resolve = ReferenceManager.resolve(aContext.get(aContext.size() - 2));
		if (resolve.isEmpty()) return;
		final Node containerNodeOf = TaraPsiUtil.getContainerNodeOf(resolve.get(0));
		if (containerNodeOf == null) return;
		variants.addAll(containerNodeOf.components());
	}

	private void addInModelVariants() {
		TaraModel model = (TaraModel) myElement.getContainingFile();
		if (model == null) return;
		model.components().stream().
				filter(node -> !node.equals(TaraPsiUtil.getContainerNodeOf(myElement))).
				forEach(node -> resolvePathFor(node, context));
		addMainConcepts(model);
	}

	private void addImportVariants() {
		Collection<Import> imports = ((TaraModel) myElement.getContainingFile()).getImports();
		for (Import anImport : imports) {
			PsiElement resolve = resolveImport(anImport);
			if (resolve == null || !TaraModel.class.isInstance(resolve)) continue;
			((TaraModel) resolve).components().stream().filter(node -> !node.equals(TaraPsiUtil.getContainerNodeOf(myElement))).forEach(node -> resolvePathFor(node, context));
			addMainConcepts((TaraModel) resolve);
		}
	}

	private PsiElement resolveImport(Import anImport) {
		List<TaraIdentifier> importIdentifiers = anImport.getHeaderReference().getIdentifierList();
		return ReferenceManager.resolve(importIdentifiers.get(importIdentifiers.size() - 1)).get(0);
	}

	private void addMainConcepts(TaraModel model) {
		IntinoUtil.getAllNodesOfFile(model).stream().
				filter(node -> !variants.contains(node) && !node.is(Tag.Component) && !node.is(Tag.Feature)).
				forEach(node -> resolvePathFor(node, context));
	}

	private void resolvePathFor(Node node, List<Identifier> path) {
		List<Node> childrenOf = TaraPsiUtil.componentsOf(node);
		if (node == null || node.type() == null) return;
		if (path.isEmpty()) variants.add(node);
		else if (path.get(0).getText().equals(node.name()))
			for (Node child : childrenOf)
				resolvePathFor(child, path.subList(1, path.size()));
	}

	private List<Identifier> solveIdentifierContext() {
		List<? extends Identifier> list = ((IdentifierReference) myElement.getParent()).getIdentifierList();
		return (List<Identifier>) list.subList(0, list.size() - 1);
	}

	private boolean isParentReference(IdentifierReference reference) {
		return reference.getParent() instanceof Signature;
	}
}
