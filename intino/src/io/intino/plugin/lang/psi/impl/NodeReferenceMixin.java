package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import io.intino.tara.lang.model.Rule;
import io.intino.tara.lang.model.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.firstUpperCase;
import static java.util.Collections.unmodifiableList;


public class NodeReferenceMixin extends ASTWrapperPsiElement {
	private List<Tag> inheritedFlags = new ArrayList<>();

	public NodeReferenceMixin(ASTNode node) {
		super(node);
	}

	public Node destinyOfReference() {
		return ReferenceManager.resolveToNode(((TaraNodeReference) this).getIdentifierReference());
	}

	public List<String> secondaryTypes() {
		final Node destiny = destinyOfReference();
		if (destiny == null) return Collections.emptyList();
		return Collections.unmodifiableList(destiny.appliedAspects().stream().map(Aspect::type).collect(Collectors.toList()));
	}

	public boolean isReference() {
		return true;
	}

	public boolean isSub() {
		return false;
	}

	@Nullable
	private Annotations getAnnotationsNode() {
		TaraTags tags = ((TaraNodeReference) this).getTags();
		if (tags == null || tags.getAnnotations() == null) return null;
		return tags.getAnnotations();
	}

	@Nullable
	private TaraFlags getFlagsNode() {
		TaraTags tags = ((TaraNodeReference) this).getTags();
		if (tags == null || tags.getFlags() == null) return null;
		return tags.getFlags();
	}

	public Node resolve() {
		final Node node = destinyOfReference();
		return node != null ? node.resolve() : null;
	}

	public String file() {
		return this.getContainingFile().getVirtualFile().getPath();
	}

	public List<Rule> rulesOf(Node component) {
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

	public boolean isAspect() {
		final Node node = destinyOfReference();
		return node != null && node.isAspect();
	}

	public boolean isMetaAspect() {
		return isAspect();
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
		final Node node = destinyOfReference();
		if (node == null) return Collections.emptyList();
		List<Tag> annotations = node.annotations();
		if (getAnnotationsNode() != null)
			annotations.addAll(getAnnotationsNode().getAnnotationList().stream().
					map(a -> Tag.valueOf(a.getText().toUpperCase())).collect(Collectors.toList()));
		return annotations;
	}

	public List<Tag> flags() {
		final Node node = destinyOfReference();
		if (node == null) return Collections.emptyList();
		List<Tag> flags = node.flags();
		flags.addAll(inheritedFlags);
		if (getFlagsNode() != null)
			flags.addAll(getFlagsNode().getFlagList().stream().
					map(a -> Tag.valueOf(firstUpperCase().format(a.getText()).toString())).collect(Collectors.toList()));
		return flags;
	}

	public String name() {
		return "";
	}

	public void name(String name) {
	}

	public List<Node> subs() {
		return Collections.emptyList();
	}

	public <T extends Node> boolean contains(T node) {
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
		final Node node = destinyOfReference();
		return node != null ? node.types() : Collections.emptyList();
	}

	public List<Parameter> parameters() {
		return Collections.emptyList();
	}

	public List<Node> components() {
		return Collections.emptyList();
	}

	public Node container() {
		return TaraPsiUtil.getContainerNodeOf(this);
	}

	public String type() {
		return destinyOfReference() == null ? "" : destinyOfReference().type();
	}

	public List<Node> component(String name) {
		return Collections.emptyList();
	}

	public List<Node> siblings() {
		NodeContainer contextOf = container();
		if (contextOf == null) return unmodifiableList(((TaraModel) this.getContainingFile()).components());
		return Collections.unmodifiableList(contextOf.components());
	}

	public List<Node> children() {
		return Collections.emptyList();
	}

	public List<Variable> variables() {
		return Collections.emptyList();
	}

	public Node parent() {
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

	public List<Node> referenceComponents() {
		return Collections.emptyList();
	}

	public List<Aspect> appliedAspects() {
		return Collections.emptyList();
	}

	public List<String> uses() {
		return Collections.emptyList();
	}

	public String toString() {
		return "Reference -> " + ((TaraNodeReference) this).getIdentifierReference().getText() + "@" + type();
	}

	public void stashNodeName(String name) {
	}
}
