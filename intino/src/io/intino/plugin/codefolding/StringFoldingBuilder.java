package io.intino.plugin.codefolding;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.*;
import io.intino.tara.language.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class StringFoldingBuilder {
	static final int VALUE_MAX_SIZE = 5;


	void processMultiValuesParameters(@NotNull List<FoldingDescriptor> descriptors, Mogram node) {
		descriptors.addAll(searchMultiValuedParameters(node).stream().
				map(multivalued -> new FoldingDescriptor((PsiElement) multivalued, getRange(((Valued) multivalued).getValue())) {
					public String getPlaceholderText() {
						return buildHolderText();
					}
				}).toList());
	}

	void processMultiLineValues(@NotNull List<FoldingDescriptor> descriptors, Mogram node) {
		descriptors.addAll(searchStringMultiLineValues(node).stream()
				.map(multiLine -> new FoldingDescriptor(multiLine, getRange(multiLine)) {
					public String getPlaceholderText() {
						return buildHolderText();
					}
				})
				.toList());
	}

	private List<Parameter> searchMultiValuedParameters(Parametrized node) {
		return node.parameters().stream().filter(parameter -> parameter.values().size() >= VALUE_MAX_SIZE).toList();
	}

	private List<PsiElement> searchStringMultiLineValues(Mogram node) {
		List<PsiElement> strings = new ArrayList<>();
		searchMultiLineVariables(node, strings);
		searchMultiLineVarInit(node, strings);
		return strings;
	}

	private void searchMultiLineVariables(Mogram node, List<PsiElement> strings) {
		node.variables().stream().
				filter(variable -> isStringOrNativeType(variable) && hasStringValue(variable)).
				forEach(variable -> addMultiLineString((TaraVariable) variable, strings));
	}

	private void searchMultiLineVarInit(Mogram mogram, List<PsiElement> strings) {
		if (!(mogram instanceof TaraMogram) || ((TaraMogram) mogram).getBody() == null) return;
		((TaraMogram) mogram).getBody().getVarInitList().stream()
				.filter(parameter -> Primitive.STRING.equals(parameter.getInferredType()))
				.forEach(parameter -> addMultiLineString(((Valued) parameter).getValue(), strings));
	}

	private void addMultiLineString(TaraVariable variable, List<PsiElement> strings) {
		if (variable.getValue() == null) return;
		strings.addAll(variable.getValue().getStringValueList().stream().filter(StringValue::isMultiLine).toList());
	}

	private String buildHolderText() {
		return " ...";
	}

	private void addMultiLineString(Value value, List<PsiElement> strings) {
		strings.addAll(((TaraValue) value).getStringValueList().stream().filter(StringValue::isMultiLine).toList());
	}


	private boolean isStringOrNativeType(Variable variable) {
		return variable.type() != null && (variable.type().equals(Primitive.STRING) || variable.type().equals(Primitive.FUNCTION));
	}

	private boolean hasStringValue(Variable variable) {
		return ((Valued) variable).getValue() != null && !((Valued) variable).getValue().getStringValueList().isEmpty();
	}

	private TextRange getRange(PsiElement value) {
		return new TextRange(value.getTextRange().getStartOffset(), value.getTextRange().getEndOffset());
	}
}
