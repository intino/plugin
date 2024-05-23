package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.Parameters;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraFacetApply;
import io.intino.plugin.lang.psi.TaraParameters;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.rules.MogramRule;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FacetApplyMixin extends ASTWrapperPsiElement {
	private String fullType = type();

	public FacetApplyMixin(@NotNull ASTNode node) {
		super(node);
	}

	public List<Parameter> parameters() {
		List<Parameter> parameterList = new ArrayList<>();
		final TaraParameters parameters = ((TaraFacetApply) this).getParameters();
		if (parameters != null) parameterList.addAll(parameters.getParameters());
		return parameterList;
	}

	public void fullType(String type) {
		fullType = type;
	}

	public String fullType() {
		return fullType;
	}

	public void addParameter(String name, int position, List<Object> values) {
		final TaraElementFactory factory = TaraElementFactory.getInstance(this.getProject());
		Map<String, String> params = new HashMap<>();
		params.put(name, String.join(" ", toString(values)));
		final Parameters newParameters = factory.createExplicitParameters(params);
		final TaraParameters parameters = ((TaraFacetApply) this).getParameters();
		if (parameters == null) this.addAfter(newParameters, ((TaraFacetApply) this).getMetaIdentifier());
		else {
			PsiElement anchor = calculateAnchor(parameters, position);
			parameters.addBefore((PsiElement) newParameters.getParameters().get(0), anchor);
			parameters.addBefore(factory.createParameterSeparator(), anchor);
		}
	}

	public List<String> toString(List<Object> values) {
		return values.stream().map(v -> {
			final String quote = mustBeQuoted(v);
			return quote + v.toString() + quote;
		}).toList();
	}

	@Override
	public String toString() {
		return "as " + type();
	}

	private String mustBeQuoted(Object v) {
		if (v instanceof Primitive.Expression) return "'";
		else if (v instanceof String) return "\"";
		else return "";
	}

	private PsiElement calculateAnchor(TaraParameters parameters, int position) {
		return parameters.getParameters().size() <= position ?
				parameters.getLastChild() :
				(PsiElement) parameters.getParameters().get(position);
	}

	public String type() {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return ((TaraFacetApply) this).getMetaIdentifier().getText();
		return application.<String>runReadAction(() -> ((TaraFacetApply) this).getMetaIdentifier().getText());
	}

	public MogramRule ruleOf(Mogram component) {
		return null;//TODO
	}

	public Mogram container() {
		return TaraPsiUtil.getContainerNodeOf(this);
	}

	public String doc() {
		return null;
	}

	public String file() {
		return this.getContainingFile().getVirtualFile().getPath();
	}

	public List<String> uses() {
		return Collections.emptyList();
	}

}
