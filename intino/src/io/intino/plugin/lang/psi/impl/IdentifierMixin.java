package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.resolve.*;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.Variable;
import io.intino.tara.language.semantics.Constraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static io.intino.tara.language.model.Primitive.REFERENCE;
import static io.intino.tara.language.model.Primitive.WORD;

public class IdentifierMixin extends ASTWrapperPsiElement {

	public IdentifierMixin(@NotNull ASTNode node) {
		super(node);
	}

	public String getIdentifier() {
		return TaraPsiUtil.getIdentifier((Identifier) this);
	}

	@NotNull
	@Override
	public PsiReference[] getReferences() {
		PsiReference reference = getReference();
		return reference == null ? new PsiReference[0] : new PsiReference[]{reference};
	}

	@Nullable
	@Override
	public PsiReference getReference() {
		PsiElement element = (PsiElement) asParameterReference();
		if (element != null && !isMethodReference()) return createResolverForParameter((Parameter) element);
		else if (isMethodReference()) return createMethodReferenceResolver();
		else if (isContract()) return createOutDefinedResolver();
		else if (isWordDefaultValue()) return null;
		else if (isFileReference()) return createFileResolver();
		else if (isNodeReference()) return createNodeResolver();
		else return null;
	}

	private Parameter asParameterReference() {
		PsiElement parent = this.getParent();
		while (!(parent instanceof PsiFile)) {
			if (parent instanceof Parameter) return (Parameter) parent;
			parent = parent.getParent();
		}
		return null;
	}

	private boolean isContract() {
		PsiElement parent = this.getParent();
		while (!(parent instanceof PsiFile))
			if (parent instanceof Rule) return true;
			else parent = parent.getParent();
		return false;
	}

	private boolean isWordDefaultValue() {
		PsiElement parent = this.getParent();
		while (!(parent instanceof PsiFile))
			if (parent instanceof Variable && WORD.equals(((Variable) parent).type())) return true;
			else parent = parent.getParent();
		return false;
	}


	private PsiReference createOutDefinedResolver() {
		return new OutDefinedReferenceSolver(this, getRange());
	}

	private PsiReference createMethodReferenceResolver() {
		return new MethodReferenceSolver((Identifier) this, getRange());
	}

	private PsiReference createFileResolver() {
		return new TaraFileReferenceSolver((HeaderReference) this.getParent(), getRange());
	}

	private PsiReference createNodeResolver() {
		return new TaraMogramReferenceSolver(this, getRange());
	}

	private PsiReference createResolverForParameter(Parameter parameter) {
		Constraint.Parameter constraint = IntinoUtil.parameterConstraintOf(parameter);
		if (constraint == null) return null;
		if (constraint.type().equals(REFERENCE))
			return new TaraMogramReferenceSolver(this, getRange());
		if (constraint.type().equals(WORD) || !Primitive.isPrimitive(constraint.type().getName()))
			return new TaraWordReferenceSolver(this, getRange(), constraint);
		return null;
	}

	private TextRange getRange() {
		return new TextRange(0, getIdentifier().length());
	}

	public PsiElement setName(String name) {
		Identifier identifier = TaraElementFactoryImpl.getInstance(this.getProject()).createNameIdentifier(name);
		ASTNode mogram = identifier.getNode();
		this.getParent().getNode().replaceChild(getNode(), mogram);
		return identifier;
	}


	@Override
	public Icon getIcon(@IconFlags int i) {
		return IntinoIcons.MOGRAM;
	}

	@Override
	public String getName() {
		return this.getText();
	}

	public String toString() {
		return this.getName();
	}

	private boolean isFileReference() {
		return this.getParent() instanceof TaraHeaderReference;
	}

	@Nullable
	public PsiElement getNameIdentifier() {
		return this;
	}

	private boolean isNodeReference() {
		return this.getParent() instanceof TaraIdentifierReference && inReferenceValued();
	}

	private boolean inReferenceValued() {
		final Valued valued = TaraPsiUtil.contextOf(this, Valued.class);
		return valued == null || valued.type().equals(REFERENCE);
	}

	private boolean isMethodReference() {
		return TaraPsiUtil.getContainerByType(this, TaraMethodReference.class) != null;
	}
}
