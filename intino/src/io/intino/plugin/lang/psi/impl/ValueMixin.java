package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import io.intino.magritte.Language;
import io.intino.magritte.lang.model.EmptyNode;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Primitive;
import io.intino.magritte.lang.model.Primitive.Reference;
import io.intino.magritte.lang.semantics.InstanceContext;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ValueMixin extends ASTWrapperPsiElement {

	ValueMixin(ASTNode node) {
		super(node);
	}

	@NotNull
	public List<Object> values() {
		List<Object> values = new ArrayList<>();
		for (PsiElement element : getChildren())
			if (!(element instanceof TaraMetric)) values.add(cast(element));
		return unmodifiableList(values);
	}

	private Object cast(PsiElement element) {
		String value = getText(element);
		if (element instanceof TaraStringValue) return value;
		else if (element instanceof TaraBooleanValue) return Boolean.parseBoolean(value);
		else if (element instanceof TaraDoubleValue) return Double.parseDouble(value);
		else if (element instanceof TaraIntegerValue) return toInt(value);
		else if (element instanceof TaraTupleValue) {
			final TaraTupleValue tuple = (TaraTupleValue) element;
			return new AbstractMap.SimpleEntry<>(tuple.getStringValue().getValue(), Double.parseDouble(getText(tuple.getDoubleValue())));
		} else if (element instanceof TaraEmptyField) return new EmptyNode();
		else if (element instanceof TaraExpression)
			return new Primitive.Expression(((TaraExpression) element).getValue());
		else if (element instanceof IdentifierReference) {
			Node node = ReferenceManager.resolveToNode((IdentifierReference) element);
			return node != null ? node : createReference(element);
		} else if (element instanceof TaraMethodReference) {
			return createMethodReference((TaraMethodReference) element);
		}
		return "";
	}

	private String getText(PsiElement element) {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return element.getText();
		return application.<String>runReadAction(element::getText);
	}

	private Object toInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException ex) {
				return "";
			}
		}
	}

	private Primitive.MethodReference createMethodReference(TaraMethodReference element) {
		return new Primitive.MethodReference(element.getIdentifierReference() != null ? getText(element.getIdentifierReference()) : "");
	}

	private Reference createReference(PsiElement element) {
		final Reference reference = new Reference(getText(element));
		final Language language = TaraUtil.getLanguage(element);
		if (language == null) return reference;
		final InstanceContext instance = language.instances().get(getText(element));
		if (instance == null) return reference;
		reference.setToInstance(true);
		reference.instanceTypes(instance.types());
		reference.path(instance.path());
		return reference;
	}
}
