package io.intino.plugin.codeinsight.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.JavaCompletionSorting;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.Language;
import io.intino.tara.language.model.Facet;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramContainer;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.rules.Size;
import io.intino.tara.language.semantics.Constraint;
import io.intino.tara.language.semantics.Documentation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.codeInsight.lookup.LookupElementBuilder.create;
import static com.intellij.openapi.util.io.FileUtil.getNameWithoutExtension;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.aspectParameterConstraintsOf;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.parameterConstraintsOf;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerNodeOf;
import static java.util.stream.Collectors.toList;

public class CompletionUtils {

	private final CompletionParameters parameters;
	private final CompletionResultSet resultSet;
	private final Language language;

	CompletionUtils(CompletionParameters parameters, CompletionResultSet resultSet) {
		this.parameters = parameters;
		this.resultSet = resultSet;
		language = IntinoUtil.getLanguage(parameters.getOriginalFile());

	}

	void collectAllowedComponents() {
		if (language == null) return;
		Mogram container = getContainerNodeOf((PsiElement) getContainerNodeOf(parameters.getPosition()));
		final List<Constraint> nodeConstraints = language.constraints(container == null ? "" : container.resolve().type());
		if (nodeConstraints == null) return;
		List<Constraint> constraints = new ArrayList<>(nodeConstraints);
		if (container != null)
			constraints.addAll(constraintsOf(aspectConstraints(nodeConstraints, container.appliedFacets())));
		List<Constraint.Component> components = new ArrayList<>();
		if (container != null) {
			components.addAll(componentConstraints(constraints).stream().filter(c -> isSizeAccepted(c, container)).toList());
			components.addAll(constraints.stream()
					.filter(c -> c instanceof Constraint.Facet && hasAspect(container, (Constraint.Facet) c))
					.flatMap(c -> ((Constraint.Facet) c).constraints().stream().filter(cs -> cs instanceof Constraint.Component).map(cst -> (Constraint.Component) cst))
					.filter(c -> isSizeAccepted(c, container)).toList());
		}
		if (components.isEmpty()) return;
		List<LookupElementBuilder> elementBuilders = createComponentLookUps(fileName(language, container), components, container);
		resultSet.addAllElements(elementBuilders);
		JavaCompletionSorting.addJavaSorting(parameters, resultSet);
	}

	@NotNull
	private static List<Constraint.Component> componentConstraints(List<Constraint> constraints) {
		return constraints.stream().filter(c -> c instanceof Constraint.Component).map(c -> (Constraint.Component) c).collect(toList());
	}

	private List<Constraint> constraintsOf(List<Constraint> constraints) {
		List<Constraint> list = new ArrayList<>();
		constraints.stream().map(constraint -> ((Constraint.Facet) constraint).constraints()).forEach(list::addAll);
		return list;
	}


	void collectAllowedAspects() {
		Mogram mogram = getContainerNodeOf(parameters.getPosition().getContext());
		if (language == null) return;
		List<Constraint> constraints = language.constraints(mogram == null ? "" : mogram.resolve().type());
		if (constraints == null || mogram == null || mogram.type() == null) return;
		final String fileName = language.doc(mogram.type()) != null ? getNameWithoutExtension(new File(language.doc(mogram.type()).file())) : "";
		List<LookupElementBuilder> elementBuilders = buildCompletionForAspects(fileName, constraints, mogram);
		resultSet.addAllElements(elementBuilders);
		JavaCompletionSorting.addJavaSorting(parameters, resultSet);
	}

	void collectBodyParameters() {
		if (language == null) return;
		Mogram mogram = getContainerNodeOf((PsiElement) getContainerNodeOf(parameters.getPosition()));
		if (mogram == null) return;
		mogram.resolve();
		List<LookupElementBuilder> elementBuilders = buildCompletionForParameters(parameterConstraintsOf(mogram), mogram.parameters());
		resultSet.addAllElements(elementBuilders);
		JavaCompletionSorting.addJavaSorting(parameters, resultSet);
	}

	void collectSignatureParameters() {
		if (language == null) return;
		Mogram mogram = getContainerNodeOf(parameters.getPosition());
		if (mogram == null) return;
		mogram.resolve();
		List<Constraint.Parameter> constraints = isInAspect() ? aspectParameterConstraintsOf(mogram) : parameterConstraintsOf(mogram);
		List<LookupElementBuilder> elementBuilders = buildCompletionForParameters(constraints, mogram.parameters());
		resultSet.addAllElements(elementBuilders);
		JavaCompletionSorting.addJavaSorting(parameters, resultSet);
	}

	private boolean isInAspect() {
		return TaraPsiUtil.contextOf(parameters.getPosition(), Facet.class) != null;
	}

	private boolean isSizeAccepted(Constraint.Component component, Mogram container) {
		long count = container.components().stream().filter(c -> component.type().equals(c.type()) || shortType(component.type()).equals(c.type())).count();
		return component.rules().stream()
				.filter(r -> r instanceof Size)
				.allMatch(r -> ((Size) r).max() > count);
	}

	private String fileName(Language language, Mogram mogram) {
		final Documentation doc = language.doc(mogram == null ? null : mogram.type());
		final String file = doc == null ? null : doc.file();
		return file == null ? "" : getNameWithoutExtension(new File(file));
	}

	private List<Constraint> aspectConstraints(List<Constraint> nodeConstraints, List<Facet> aspects) {
		List<String> facetTypes = aspects.stream().map(Facet::type).toList();
		List<Constraint> list = new ArrayList<>();
		if (nodeConstraints == null) return list;
		for (Constraint constraint : nodeConstraints)
			if (constraint instanceof Constraint.Facet && facetTypes.contains(((Constraint.Facet) constraint).type()))
				list.add(constraint);
		return list;
	}

	private List<LookupElementBuilder> createComponentLookUps(String fileName, List<Constraint.Component> constraints, MogramContainer container) {
		Set<String> added = new HashSet<>();
		List<LookupElementBuilder> builders = new ArrayList<>();
		for (Constraint.Component constraint : constraints)
			if (constraint instanceof Constraint.OneOf)
				builders.addAll(createElement(fileName, (Constraint.OneOf) constraint, container));
			else builders.add(createElement(fileName, constraint, container));
		return builders.stream().filter(c -> added.add(c.getLookupString())).collect(toList());
	}

	private List<LookupElementBuilder> buildCompletionForAspects(String fileName, List<Constraint> constraints, Mogram node) {
		Set<String> added = new HashSet<>();
		return constraints.stream().
				filter(c -> c instanceof Constraint.Facet && !hasAspect(node, (Constraint.Facet) c)).
				map(c -> createElement(fileName, (Constraint.Facet) c, node)).filter(l -> added.add(l.getLookupString())). //TODO pasar el container
						collect(toList());
	}

	private boolean hasAspect(Mogram node, Constraint.Facet c) {
		return node.appliedFacets().stream().anyMatch(facet -> facet.type().equals(c.type()) || facet.fullType().equals(c.type()));
	}

	private LookupElementBuilder createElement(String fileName, Constraint.Component constraint, MogramContainer container) {
		return create(new FakeElement(constraint.type(), (PsiElement) container), lastTypeOf(constraint.type()) + " ").withIcon(IntinoIcons.MOGRAM).withCaseSensitivity(true).withTypeText(fileName);
	}

	private List<LookupElementBuilder> createElement(String fileName, Constraint.OneOf constraint, MogramContainer container) {
		return constraint.components().stream().map(component -> createElement(fileName, component, container)).collect(toList());
	}

	private String lastTypeOf(String fullType) {
		String[] splittedType = fullType.split(":");
		String type = splittedType[splittedType.length - 1];
		return type.contains(".") ? type.substring(type.lastIndexOf('.') + 1) : type;
	}

	private LookupElementBuilder createElement(String language, Constraint.Facet aspect, MogramContainer container) {
		return create(new FakeElement(aspect.type(), (PsiElement) container), lastTypeOf(aspect.type()) + " ").withIcon(IntinoIcons.MODEL_16).withCaseSensitivity(true).withTypeText(language);
	}

	private List<LookupElementBuilder> buildCompletionForParameters(List<Constraint.Parameter> constraints, List<Parameter> parameterList) {
		Set<String> added = new HashSet<>();
		return constraints.stream().
				filter(c -> c != null && !contains(parameterList, c.name())).
				map(this::createElement).filter(l -> added.add(l.getLookupString())).
				collect(toList());
	}

	private boolean contains(List<Parameter> parameters, String name) {
		return parameters.stream().anyMatch(parameter -> name.equals(parameter.name()));
	}

	private LookupElementBuilder createElement(Constraint.Parameter allow) {
		return create(allow.name() + " ").withIcon(IntinoIcons.MOGRAM).withCaseSensitivity(true).withTypeText(allow.type().getName());
	}


	private static String shortType(String type) {
		final String[] s = type.split("\\.");
		return s[s.length - 1];
	}


	public static class FakeElement extends FakePsiElement implements NavigatablePsiElement {
		private final String type;
		private final PsiElement parent;

		FakeElement(String type, PsiElement parent) {
			this.type = type;
			this.parent = parent;
		}

		public String getType() {
			return type;
		}

		@Override
		public PsiElement getParent() {
			return parent;
		}

		@Override
		public String toString() {
			return type;
		}

		@Override
		public String getPresentableText() {
			return toString();
		}

		@Override
		public String getName() {
			return toString();
		}
	}
}
