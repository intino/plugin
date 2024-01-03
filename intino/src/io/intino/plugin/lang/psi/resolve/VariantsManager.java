package io.intino.plugin.lang.psi.resolve;

import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.Checker;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Tag;
import io.intino.tara.language.model.rules.variable.ReferenceRule;
import io.intino.tara.language.semantics.Constraint;
import io.intino.tara.language.semantics.constraints.parameter.ReferenceParameter;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class VariantsManager {

	private final Set<Mogram> variants = new LinkedHashSet<>();
	private final Identifier myElement;
	private final List<Identifier> context;

	VariantsManager(Identifier myElement) {
		this.myElement = myElement;
		this.context = solveIdentifierContext();
	}

	Set<Mogram> resolveVariants() {
		if (hasContext()) addContextVariants();
		else {
			addInModelVariants();
			addImportVariants();
		}
		final Mogram mogram = TaraPsiUtil.getContainerNodeOf(myElement);
		if (mogram == null || mogram.type() == null) return variants;
		if (isParentReference((IdentifierReference) myElement.getParent()))
			collectUnacceptableNodes(singletonList(mogram.type())).forEach(variants::remove);
		else if (isParameterReference(myElement))
			collectUnacceptableNodes(filterTypes(myElement)).forEach(variants::remove);
		return variants;
	}

	private List<String> filterTypes(PsiElement element) {
		final Mogram mogram = TaraPsiUtil.getContainerNodeOf(element);
		check(mogram);
		final List<Constraint> constraints = IntinoUtil.constraintsOf(mogram);
		final Parameter parameter = TaraPsiUtil.getContainerByType(element, Parameter.class);
		if (constraints == null || parameter == null || parameter.name() == null) return emptyList();
		Constraint.Parameter constraint = findParameter(constraints, parameter);
		if (constraint == null && !mogram.appliedFacets().isEmpty())
			constraint = searchInFacets(mogram.appliedFacets(), constraints, parameter);
		if (constraint == null || !(constraint.rule() instanceof ReferenceRule)) return emptyList();
		return ((ReferenceRule) constraint.rule()).allowedReferences();
	}

	private Constraint.Parameter searchInFacets(List<io.intino.tara.language.model.Facet> aspects, List<Constraint> constraints, Parameter parameter) {
		for (Constraint c : constraints)
			if (c instanceof Constraint.Facet && facetOf((Constraint.Facet) c, aspects) != null)
				return findParameter(((Constraint.Facet) c).constraints(), parameter);
		return null;
	}

	private io.intino.tara.language.model.Facet facetOf(Constraint.Facet c, List<io.intino.tara.language.model.Facet> aspects) {
		for (io.intino.tara.language.model.Facet aspect : aspects)
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

	private void check(Mogram node) {
		Checker checker = new Checker(LanguageManager.getLanguage(myElement.getContainingFile()));
		try {
			checker.check(node);
		} catch (SemanticFatalException ignored) {
		}
	}

	private boolean isParameterReference(PsiElement element) {
		return TaraPsiUtil.getContainerByType(element, Parameter.class) != null;
	}

	private List<Mogram> collectUnacceptableNodes(List<String> expectedTypes) {
		if (expectedTypes.isEmpty()) return emptyList();
		return variants.stream().
				filter(variant -> {
					variant.resolve();
					return variant.type() != null && variant.types().stream().noneMatch(t -> expectedTypes.contains(t.split(":")[0]));
				}).toList();
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
		final Mogram containerNodeOf = TaraPsiUtil.getContainerNodeOf(resolve.get(0));
		if (containerNodeOf == null) return;
		variants.addAll(containerNodeOf.components());
	}

	private void addInModelVariants() {
		TaraModel model = (TaraModel) myElement.getContainingFile();
		if (model == null) return;
		model.components().stream().
				filter(mogram -> !mogram.equals(TaraPsiUtil.getContainerNodeOf(myElement))).
				forEach(mogram -> resolvePathFor(mogram, context));
		addMainConcepts(model);
	}

	private void addImportVariants() {
		Collection<Import> imports = ((TaraModel) myElement.getContainingFile()).getImports();
		for (Import anImport : imports) {
			PsiElement resolve = resolveImport(anImport);
			if (!(resolve instanceof TaraModel)) continue;
			((TaraModel) resolve).components().stream()
					.filter(m -> !m.equals(TaraPsiUtil.getContainerNodeOf(myElement)))
					.forEach(mogram -> resolvePathFor(mogram, context));
			addMainConcepts((TaraModel) resolve);
		}
	}

	private PsiElement resolveImport(Import anImport) {
		List<TaraIdentifier> importIdentifiers = anImport.getHeaderReference().getIdentifierList();
		return ReferenceManager.resolve(importIdentifiers.get(importIdentifiers.size() - 1)).get(0);
	}

	private void addMainConcepts(TaraModel model) {
		IntinoUtil.getAllNodesOfFile(model).stream().
				filter(mogram -> !variants.contains(mogram) && !mogram.is(Tag.Component) && !mogram.is(Tag.Feature)).
				forEach(mogram -> resolvePathFor(mogram, context));
	}

	private void resolvePathFor(Mogram mogram, List<Identifier> path) {
		List<Mogram> childrenOf = TaraPsiUtil.componentsOf(mogram);
		if (mogram == null || mogram.type() == null) return;
		if (path.isEmpty()) variants.add(mogram);
		else if (path.get(0).getText().equals(mogram.name()))
			for (Mogram child : childrenOf)
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
