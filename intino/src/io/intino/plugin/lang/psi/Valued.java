package io.intino.plugin.lang.psi;

import com.intellij.pom.Navigatable;
import io.intino.tara.lang.model.Primitive;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.intino.tara.lang.model.Primitive.*;

public interface Valued extends Navigatable, TaraPsiElement, io.intino.tara.lang.model.Valued {

	io.intino.tara.lang.model.Rule rule();

	String name();

	String scope();

	List<Object> values();

	@Nullable
	TaraValue getValue();

	TaraBodyValue getBodyValue();

	Primitive type();

	default Primitive getInferredType() {
		TaraValue value = getValue();
		if (value == null) return null;
		Primitive x = asPrimitive(value);
		if (x != null) return x;
		if (!value.getIdentifierReferenceList().isEmpty()) return REFERENCE;
		if (value.getEmptyField() != null) return REFERENCE;
		return null;
	}

	@Nullable
	default Primitive asPrimitive(TaraValue value) {
		if (!value.getBooleanValueList().isEmpty()) return BOOLEAN;
		if (!value.getDoubleValueList().isEmpty()) return DOUBLE;
		if (!value.getIntegerValueList().isEmpty()) return INTEGER;
		if (!value.getStringValueList().isEmpty()) return STRING;
		if (!value.getExpressionList().isEmpty()) return FUNCTION;
		if (!value.getMethodReferenceList().isEmpty()) return FUNCTION;
		return null;
	}

}
