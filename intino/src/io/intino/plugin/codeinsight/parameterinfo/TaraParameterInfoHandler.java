package io.intino.plugin.codeinsight.parameterinfo;

import com.intellij.lang.parameterInfo.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.Language;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.rules.Size;
import io.intino.tara.language.model.rules.variable.ReferenceRule;
import io.intino.tara.language.semantics.Constraint;
import io.intino.tara.language.semantics.constraints.parameter.ReferenceParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TaraParameterInfoHandler implements ParameterInfoHandlerWithTabActionSupport<Parameters, Object, TaraParameter> {

	private static final Set<Class<?>> STOP_SEARCHING_CLASSES = ContainerUtil.newHashSet(TaraModel.class);

	@NotNull
	@Override
	public TaraParameter @NotNull [] getActualParameters(@NotNull Parameters o) {
		return o.getParameters().toArray(new TaraParameter[0]);
	}

	@NotNull
	@Override
	public IElementType getActualParameterDelimiterType() {
		return TaraTypes.COMMA;
	}

	@NotNull
	@Override
	public IElementType getActualParametersRBraceType() {
		return TaraTypes.RIGHT_PARENTHESIS;
	}

	@NotNull
	@Override
	public Set<Class<?>> getArgumentListAllowedParentClasses() {
		return new HashSet<>(Collections.singletonList(Signature.class));
	}

	@NotNull
	@Override
	public Set<? extends Class<?>> getArgListStopSearchClasses() {
		return STOP_SEARCHING_CLASSES;
	}

	@NotNull
	@Override
	public Class<Parameters> getArgumentListClass() {
		return Parameters.class;
	}

	@Nullable
	@Override
	public Parameters findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
		Parameters parameters = getParameters(context.getFile(), context.getOffset());
		if (parameters == null) return null;
		int index = ParameterInfoUtils.getCurrentParameterIndex(parameters.getNode(), context.getOffset(), getActualParameterDelimiterType());
		if (!parameters.getParameters().isEmpty() && parameters.getParameters().size() > index)
			context.setHighlightedElement((PsiElement) parameters.getParameters().get(index));
		return parameters;
	}

	@Nullable
	private Parameters getParameters(PsiFile file, int offset) {
		Parameters parameters = ParameterInfoUtils.findParentOfType(file, offset, Parameters.class);
		if (parameters == null) {
			Signature signature = PsiTreeUtil.findElementOfClassAtOffset(file, offset, Signature.class, false);
			if (signature != null) parameters = signature.getParameters();
		}
		return parameters;
	}

	@Override
	public void showParameterInfo(@NotNull Parameters parameters, @NotNull CreateParameterInfoContext context) {
		Language language = IntinoUtil.getLanguage(parameters);
		if (language == null) return;
		List<Constraint> constraints = language.constraints(TaraPsiUtil.getContainerNodeOf(parameters).resolve().type());
		if (constraints == null) return;
		List<Constraint.Parameter> parameterConstraints = collectParameterConstraints(constraints, parameters.isInFacet());
		if (!parameterConstraints.isEmpty())
			context.setItemsToShow(parameterConstraints.toArray(Constraint.Parameter[]::new));
		context.showHint(parameters, parameters.getTextRange().getStartOffset(), this);
	}

	private List<Constraint.Parameter> collectParameterConstraints(List<Constraint> nodeConstraints, TaraFacetApply inFacet) {
		List<Constraint> scopeAllows = nodeConstraints;
		if (inFacet != null) scopeAllows = collectFacetParameterConstraints(nodeConstraints, inFacet.fullType());
		return scopeAllows.stream().
				filter(constraint -> constraint instanceof Constraint.Parameter).
				map(constraint -> (Constraint.Parameter) constraint).toList();
	}

	private List<Constraint> collectFacetParameterConstraints(List<Constraint> constraints, String type) {
		for (Constraint constraint : constraints)
			if ((constraint instanceof Constraint.Facet) && ((Constraint.Facet) constraint).type().equals(type))
				return ((Constraint.Facet) constraint).constraints();
		return Collections.emptyList();
	}


	@NotNull
	private String parameterInfo(Constraint.Parameter constraint) {
		return Primitive.REFERENCE.equals(constraint.type()) ?
				asReferenceParameter(constraint) :
				asWordParameter(constraint);
	}

	@NotNull
	private String asWordParameter(Constraint.Parameter constraint) {
		return constraint.type().getName().toLowerCase() + (size(constraint)) + constraint.name();
	}

	@NotNull
	private String asReferenceParameter(Constraint.Parameter constraint) {
		return presentableText((ReferenceParameter) constraint) + size(constraint) + constraint.name();
	}

	@NotNull
	private String size(Constraint.Parameter constraint) {
		Size size = constraint.size();
		return "[" + size.min() + ".." + (size.max() == Integer.MAX_VALUE ? "*" : size.max()) + "] ";
	}

	@NotNull
	private String presentableText(ReferenceParameter constraint) {
		return constraint.rule() instanceof ReferenceRule ? "{" + String.join(", ", ((ReferenceRule) constraint.rule()).allowedReferences()) + "}" : constraint.referenceType();
	}

	@Nullable
	@Override
	public Parameters findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
		return ParameterInfoUtils.findParentOfType(context.getFile(), context.getOffset(), Parameters.class);
	}

	@Override
	public void updateParameterInfo(@NotNull Parameters parameters, @NotNull UpdateParameterInfoContext context) {
		if (context.getParameterOwner() != parameters) {
			context.removeHint();
			return;
		}
		final List<Constraint.Parameter> constraints = Arrays.asList((Constraint.Parameter[]) context.getObjectsToView());
		int index = ParameterInfoUtils.getCurrentParameterIndex(parameters.getNode(), context.getOffset(), getActualParameterDelimiterType());
		Parameter parameter = parameters.getParameters().get(index);
		Constraint.Parameter constraint = IntinoUtil.parameterConstraintOf(parameter);
		int constraintIndex = constraints.indexOf(constraint);
		context.setParameterOwner(parameters);
		context.setCurrentParameter(constraintIndex);
		context.setHighlightedParameter(constraintIndex < constraints.size() && constraintIndex >= 0 ? constraints.get(constraintIndex) : null);

	}

	@Override
	public void updateUI(Object parameter, @NotNull ParameterInfoUIContext context) {
		Constraint.Parameter constraint = (Constraint.Parameter) parameter;
		String s = parameterInfo(constraint);
		boolean required = constraint.size().min() > 0;
		context.setupUIComponentPresentation(s,
				0, required ? s.length() : 0, false, false, false, context.getDefaultParameterColor());
	}
}
