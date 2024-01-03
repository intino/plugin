package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import io.intino.tara.language.model.Rule;
import io.intino.tara.language.model.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.firstUpperCase;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;


public class MogramReferenceMixin extends ASTWrapperPsiElement {
	private final List<Tag> inheritedFlags = new ArrayList<>();

	public MogramReferenceMixin(ASTNode node) {
		super(node);
	}

	public Mogram targetOfReference() {
		return ReferenceManager.resolveToNode(((TaraMogramReference) this).getIdentifierReference());
	}

	public List<String> secondaryTypes() {
		final Mogram target = targetOfReference();
		if (target == null) return emptyList();
		return target.appliedFacets().stream().map(Facet::type).toList();
	}

	public boolean isReference() {
		return true;
	}

	public boolean isSub() {
		return false;
	}

	@Nullable
	private Annotations getAnnotationsNode() {
		TaraTags tags = ((TaraMogramReference) this).getTags();
		if (tags == null || tags.getAnnotations() == null) return null;
		return tags.getAnnotations();
	}

	@Nullable
	private TaraFlags getFlagsNode() {
		TaraTags tags = ((TaraMogramReference) this).getTags();
		if (tags == null || tags.getFlags() == null) return null;
		return tags.getFlags();
	}

	public Mogram resolve() {
		final Mogram mogram = targetOfReference();
		return mogram != null ? mogram.resolve() : null;
	}

	public String file() {
		return this.getContainingFile().getVirtualFile().getPath();
	}

	public List<Rule> rulesOf(Mogram component) {
		return null;
	}

	public void addFlags(List<Tag> flags) {
		this.inheritedFlags.clear();
		this.inheritedFlags.addAll(flags);
	}


	public void addFlags(Tag... flag) {
		Collections.addAll(inheritedFlags, flag);
	}

	public void addAnnotations(Tag... annotations) {
	}

	public boolean isFacet() {
		final Mogram mogram = targetOfReference();
		return mogram != null && mogram.isFacet();
	}

	public boolean isMetaFacet() {
		return isFacet();
	}

	public boolean isAbstract() {
		return flags().contains(Tag.Abstract);
	}

	public boolean isTerminal() {
		return flags().contains(Tag.Terminal);
	}

	public boolean is(Tag tag) {
		return flags().contains(tag);
	}

	public boolean into(Tag tag) {
		return annotations().contains(tag);
	}

	public List<Tag> annotations() {
		final Mogram mogram = targetOfReference();
		if (mogram == null) return emptyList();
		List<Tag> annotations = mogram.annotations();
		if (getAnnotationsNode() != null)
			annotations.addAll(getAnnotationsNode().getAnnotationList().stream().
					map(a -> Tag.valueOf(a.getText().toUpperCase())).toList());
		return annotations;
	}

	public List<Tag> flags() {
		final Mogram mogram = targetOfReference();
		if (mogram == null) return emptyList();
		List<Tag> flags = mogram.flags();
		flags.addAll(inheritedFlags);
		if (getFlagsNode() != null)
			flags.addAll(getFlagsNode().getFlagList().stream().
					map(a -> Tag.valueOf(firstUpperCase().format(a.getText()).toString()))
					.toList());
		return flags;
	}

	public String name() {
		return "";
	}

	public void name(String name) {
	}

	public List<Mogram> subs() {
		return emptyList();
	}

	public <T extends Mogram> boolean contains(T node) {
		return false;
	}

	public String qualifiedName() {
		return null;
	}

	public String layerQualifiedName() {
		return null;
	}

	public String doc() {
		return "null";
	}

	public List<String> types() {
		final Mogram mogram = targetOfReference();
		return mogram != null ? mogram.types() : emptyList();
	}

	public List<Parameter> parameters() {
		return emptyList();
	}

	public List<Mogram> components() {
		return emptyList();
	}

	public Mogram container() {
		return TaraPsiUtil.getContainerNodeOf(this);
	}

	public String type() {
		return targetOfReference() == null ? "" : targetOfReference().type();
	}

	public List<Mogram> component(String name) {
		return emptyList();
	}

	public List<Mogram> siblings() {
		MogramContainer contextOf = container();
		if (contextOf == null) return unmodifiableList(((TaraModel) this.getContainingFile()).components());
		return Collections.unmodifiableList(contextOf.components());
	}

	public List<Mogram> children() {
		return emptyList();
	}

	public List<Variable> variables() {
		return emptyList();
	}

	public Mogram parent() {
		return null;
	}

	public String parentName() {
		return null;
	}

	public boolean isAnonymous() {
		return false;
	}

	public void type(String type) {
	}

	public List<Mogram> referenceComponents() {
		return emptyList();
	}

	public List<Facet> appliedFacets() {
		return emptyList();
	}

	public List<String> uses() {
		return emptyList();
	}

	public String toString() {
		return "Reference -> " + ((TaraMogramReference) this).getIdentifierReference().getText() + "@" + type();
	}

	public void stashNodeName(String name) {
	}
}
