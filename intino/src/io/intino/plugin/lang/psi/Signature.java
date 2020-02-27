package io.intino.plugin.lang.psi;

import com.intellij.psi.PsiInvalidElementAccessException;
import io.intino.magritte.lang.model.Aspect;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.impl.TaraModelImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Signature extends TaraPsiElement {

	TaraModelImpl getFile() throws PsiInvalidElementAccessException;

	TaraMetaIdentifier getMetaIdentifier();

	@Nullable
	TaraIdentifier getIdentifier();

	boolean isSub();

	@Nullable
	TaraIdentifierReference getParentReference();

	@Nullable
	MetaIdentifier getType();

	Node parent();

	@Nullable
	Parameters getParameters();

	List<? extends Aspect> appliedAspects();

	@Nullable
	Tags getTags();

	Flags getFlags();

	@Nullable
	Annotations getAnnotations();

	int hashcode();
}