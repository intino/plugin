package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SignatureMixin extends ASTWrapperPsiElement {
	private int hashcode = -1;

	public SignatureMixin(@NotNull ASTNode node) {
		super(node);
	}

	@NotNull
	public SearchScope getUseScope() {
		return GlobalSearchScope.allScope(getProject());
	}

	@Override
	public String getName() {
		return TaraPsiUtil.getIdentifier((Node) this);
	}

	public TaraModelImpl getFile() throws PsiInvalidElementAccessException {
		return (TaraModelImpl) super.getContainingFile();
	}

	@Override
	public Icon getIcon(@IconFlags int i) {
		return IntinoIcons.NODE;
	}

	public boolean isSub() {
		return getNode().findChildByType(TaraTypes.SUB) != null;
	}

	@Nullable
	public MetaIdentifier getType() {
		return findChildByClass(MetaIdentifier.class);
	}

	@Nullable
	public Node parent() {
		IdentifierReference parentReference = findChildByClass(IdentifierReference.class);
		if (parentReference == null) return null;
		return ReferenceManager.resolveToNode(parentReference);
	}

	@NotNull
	public List<TaraAspectApply> appliedAspects() {
		if (((TaraSignature) this).getAspects() == null) return Collections.emptyList();
		return ((TaraSignature) this).getAspects().getAspectApplyList();
	}

	public Flags getFlags() {
		TaraTags tags = ((TaraSignature) this).getTags();
		return tags != null ? tags.getFlags() : null;
	}

	public Annotations getAnnotations() {
		TaraTags tags = ((TaraSignature) this).getTags();
		return tags != null ? tags.getAnnotations() : null;
	}

	@Nullable
	public TaraIdentifierReference getParentReference() {
		return findChildByClass(TaraIdentifierReference.class);
	}

	@Nullable
	public Parameters getParameters() {
		return findChildByClass(Parameters.class);
	}


	public int hashcode() {
		return this.hashcode != -1 ? this.hashcode : calculateHashCode();
	}

	private int calculateHashCode() {
		Parameters parameters = getParameters();
		return this.hashcode = parameters == null ? getText().hashCode() :
				parameters.getParameters().stream().
						map(p -> p.values().stream().map(Object::toString).collect(Collectors.joining())).
						collect(Collectors.joining(":")).hashCode();
	}
}
