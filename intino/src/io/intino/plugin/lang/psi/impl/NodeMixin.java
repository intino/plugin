package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.source.tree.ChangeUtil;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import io.intino.Configuration;
import io.intino.magritte.Language;
import io.intino.magritte.Resolver;
import io.intino.magritte.dsl.ProteoConstants;
import io.intino.magritte.lang.model.Rule;
import io.intino.magritte.lang.model.*;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.documentation.TaraDocumentationFormatter;
import io.intino.plugin.lang.psi.Flags;
import io.intino.plugin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.magritte.lang.model.Node.ANONYMOUS;
import static io.intino.magritte.lang.model.Tag.Abstract;
import static io.intino.magritte.lang.model.Tag.Terminal;
import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.firstUpperCase;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

public class NodeMixin extends ASTWrapperPsiElement {

	private String fullType;
	private String prevType;
	private Set<Tag> inheritedFlags = new HashSet<>();
	private List<String> metaTypes = new ArrayList<>();

	NodeMixin(@NotNull ASTNode node) {
		super(node);
	}

	@Override
	public String getName() {
		return qualifiedName();
	}

	public ItemPresentation getPresentation() {
		return new ItemPresentation() {
			public String getPresentableText() {
				return getName();
			}

			public String getLocationString() {
				return "";
			}

			public Icon getIcon(final boolean open) {
				return IntinoIcons.NODE;
			}
		};
	}

	@NotNull
	public SearchScope getUseScope() {
		return GlobalSearchScope.allScope(getProject());
	}

	public void delete() throws IncorrectOperationException {
		final ASTNode parentNode = getParent().getNode();
		assert parentNode != null;
		ASTNode node = getNode();
		ASTNode prev = node.getTreePrev();
		ASTNode next = node.getTreeNext();
		parentNode.removeChild(node);
		if ((prev == null || prev.getElementType() == TokenType.WHITE_SPACE) && next != null &&
				next.getElementType() == TokenType.WHITE_SPACE) {
			parentNode.removeChild(next);
		}
	}

	public String simpleType() {
		return shortType();
	}

	@NotNull
	public String type() {
		if (prevType == null) prevType = shortType();
		if (fullType == null) fullType = shortType();
		if (!prevType.equals(shortType())) {
			fullType = shortType();
			prevType = shortType();
		}
		return fullType;
	}

	private String shortType() {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return sType();
		return application.<String>runReadAction(this::sType);
	}

	private String sType() {
		MetaIdentifier type = getSignature().getType();
		if (type == null && this.isSub()) {
			Node parent = parent();
			return parent != null ? parent.type() : "";
		} else return type == null || type.getText() == null ? "" : type.getText();
	}

	public void type(String fullType) {
		this.fullType = fullType;
	}

	public void setShortType(String type) {
		setType(type);
	}

	public Node resolve() {
		Language language = IntinoUtil.getLanguage(this.getOriginalElement());
		if (language == null) return (Node) this;
		new Resolver(language).resolve((Node) this);
		return (Node) this;
	}

	public List<Node> siblings() {
		NodeContainer container = container();
		if (container == null) return unmodifiableList(((TaraModel) this.getContainingFile()).components());
		return unmodifiableList(container.components());
	}

	public List<Node> components() {
		return unmodifiableList(IntinoUtil.getComponentsOf((Node) this));
	}

	public List<Rule> rulesOf(Node component) {
		return emptyList();
	}

	public List<Variable> variables() {
		return TaraPsiUtil.getVariablesInBody(this.getBody());
	}

	public List<Node> referenceComponents() {
		return unmodifiableList(TaraPsiUtil.getNodeReferencesOf((Node) this));
	}

	@Nullable
	public String parentName() {
		Signature signature = this.getSignature();
		return signature.getParentReference() != null ? signature.getParentReference().getText() : null;
	}

	public boolean isMetaAspect() {
		return isAspect();
	}

	public Node parent() {
		return TaraPsiUtil.getParentOf((Node) this);
	}

	public MetaIdentifier getMetaIdentifier() {
		MetaIdentifier[] childrenOfType = PsiTreeUtil.getChildrenOfType(this.getSignature(), MetaIdentifier.class);
		return childrenOfType == null ? null : childrenOfType[0];
	}

	@NotNull
	public String name() {
		Identifier identifierNode = TaraPsiUtil.getIdentifierNode((Node) this);
		return identifierNode != null ? getText(identifierNode) : "";
	}

	private String getText(Identifier identifierNode) {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return identifierNode.getText();
		return application.<String>runReadAction(identifierNode::getText);
	}

	public List<Parameter> parameters() {
		List<Parameter> parameterList = new ArrayList<>();
		final Parameters parameters = getSignature().getParameters();
		if (parameters != null) parameterList.addAll(parameters.getParameters());
		parameterList.addAll(getVarInits());
		for (Aspect aspect : appliedAspects()) {
			final TaraParameters p = ((TaraAspectApply) aspect).getParameters();
			if (p != null) parameterList.addAll(p.getParameterList());
		}
		return parameterList;
	}

	private List<Parameter> getVarInits() {
		if (this.getBody() == null) return emptyList();
		return unmodifiableList(this.getBody().getVarInitList());
	}

	public String qualifiedName() {
		if (container() == null) return name();
		String container = container().qualifiedName();
		return (container.isEmpty() ? "" : container + ".") +
				(name().isEmpty() ?
						"[" + ANONYMOUS + shortType() + "]" :
						name());
	}

	public String layerQualifiedName() {
		if (container() == null) return firstUpperCase().format(name()).toString();
		String container = container().qualifiedName();
		return new StringBuilder().append(container.isEmpty() ? "" : container + "$").
				append(name().isEmpty() ?
						"[" + ANONYMOUS + shortType() + "]" :
						firstUpperCase().format(name()).toString()).toString();
	}

	public TaraModelImpl getFile() throws PsiInvalidElementAccessException {
		return (TaraModelImpl) super.getContainingFile();
	}

	public Icon getIcon(@IconFlags int i) {
		return IntinoIcons.NODE;
	}

	public void name(String name) {
		setName(name);
	}

	private PsiElement setName(String name) {
		return TaraPsiUtil.setName(this.getSignature(), name);
	}

	private PsiElement setType(String type) {
		return TaraPsiUtil.setType(this.getSignature(), type);
	}

	@Nullable
	public Body getBody() {
		return findChildByClass(Body.class);
	}

	public boolean isSub() {
		return this.getSignature().isSub();
	}

	public boolean isAspect() {
		return ProteoConstants.ASPECT.equals(type()) || (metaTypes() != null && metaTypes().contains(ProteoConstants.META_ASPECT));
	}

	public List<String> metaTypes() {
		return metaTypes;
	}

	public void metaTypes(List<String> types) {
		this.metaTypes = types;
	}

	public boolean isAbstract() {
		return is(Abstract) || !subs().isEmpty();
	}

	public boolean isTerminal() {
		Configuration.Artifact.Model.Level level = IntinoUtil.level(this);
		return is(Terminal) || (level != null && level.isProduct());
	}

	public boolean is(Tag tag) {
		Node parent = parentName() != null ? parent() : null;
		return hasFlag(tag) || (parent != null && parent.is(tag));
	}

	public boolean into(Tag tag) {
		Node parent = parentName() != null ? parent() : null;
		return hasAnnotation(tag) || parent != null && parent.is(tag);
	}

	private boolean hasFlag(Tag tags) {
		for (Tag a : flags())
			if (a.equals(tags)) return true;
		return false;
	}

	private boolean hasAnnotation(Tag tag) {
		for (Tag a : annotations())
			if (a.equals(tag)) return true;
		return false;
	}

	public List<Node> subs() {
		ArrayList<Node> subs = new ArrayList<>();
		List<Node> children = TaraPsiUtil.getBodyComponents(this.getBody());
		children.stream().filter(Node::isSub).forEach(child -> {
			subs.add(child);
			subs.addAll(child.subs());
		});
		return unmodifiableList(subs);
	}

	public Node container() {
		return isSub() ? containerOfSub((Node) this) : TaraPsiUtil.getContainerNodeOf(this);
	}

	private Node containerOfSub(Node node) {
		Node container = node;
		while (container != null && container.isSub())
			container = TaraPsiUtil.getContainerNodeOf((PsiElement) container);
		return container != null ? container.container() : null;
	}

	public List<Aspect> appliedAspects() {
		return unmodifiableList(((TaraNode) this).getSignature().appliedAspects());
	}

	public List<String> types() {
		Set<String> types = new HashSet<>();
		types.add(type());
		types.addAll(secondaryTypes());
		return new ArrayList<>(types);
	}

	public List<String> secondaryTypes() {
		Set<String> types = appliedAspects().stream().map(f -> f.fullType() + ":" + this.type()).collect(Collectors.toSet());
		if (parent() != null && !parent().equals(this)) types.addAll(parent().types());
		return new ArrayList<>(types);
	}

	@NotNull
	public TaraSignature getSignature() {
		return findNotNullChildByClass(TaraSignature.class);
	}


	@NotNull
	private List<Annotation> getAnnotations() {
		Annotations annotations = this.getAnnotationsNode();
		return annotations == null ? emptyList() : unmodifiableList(annotations.getAnnotationList());
	}

	@NotNull
	private List<Flag> getFlags() {
		Flags flags = this.getFlagsElement();
		return flags == null ? emptyList() : flags.getFlagList();
	}


	public List<Tag> annotations() {
		return getAnnotations().stream().map(a -> Tag.valueOf(firstUpperCase().format(a.getText()).toString())).collect(toList());
	}

	public List<Tag> flags() {
		final List<Tag> tags = new ArrayList<>();
		tags.addAll(getFlags().stream().
				map(f -> Tag.valueOf(firstUpperCase().format(f.getText()).toString())).collect(toList()));
		tags.addAll(inheritedFlags());
		return tags;
	}

	private synchronized Set<Tag> inheritedFlags() {
		return this.inheritedFlags;
	}

	@Nullable
	public Annotations getAnnotationsNode() {
		return this.getSignature().getAnnotations();
	}

	@Nullable
	public Flags getFlagsNode() {
		return this.getSignature().getFlags();
	}

	@Nullable
	public Flags getFlagsElement() {
		return this.getSignature().getFlags();
	}

	public void addFlags(List<Tag> flags) {
		inheritedFlags().clear();
		inheritedFlags().addAll(flags);
	}

	public void addFlags(Tag... flag) {
		Collections.addAll(inheritedFlags(), flag);
	}

	public void addAnnotations(Tag... annotations) {

	}

	public void add(Variable... variables) {
		for (Variable variable : variables) {
			final TreeElement copy = ChangeUtil.copyToElement((PsiElement) variable);
			PsiElement psi = copy.getPsi();
			if (getBody() == null) TaraElementFactory.getInstance(this.getProject()); //TODO
			final PsiElement add = getBody().add((psi));
		}
	}

	public boolean contains(String type) {
		for (Node node : components())
			if (type.equals(node.type())) return true;
		return true;
	}

	public boolean isReference() {
		return false;
	}

	public Node destinyOfReference() {
		return null;
	}

	public String languageName() {
		return null;
	}

	public void languageName(String language) {
	}

	public List<Node> children() {
		return emptyList();
	}

	public boolean isAnonymous() {
		return name().isEmpty();
	}

	public <T extends Node> boolean contains(T node) {
		return components().contains(node);
	}

	public String doc() {
		return buildDocText();
	}

	public String file() {
		final PsiFile containingFile = this.getContainingFile();
		return containingFile == null || containingFile.getVirtualFile() == null ? null : containingFile.getVirtualFile().getPath();
	}

	public List<String> uses() {
		return emptyList();
	}


	public String buildDocText() {
		StringBuilder text = new StringBuilder();
		TaraDoc doc = ((TaraNode) this).getDoc();
		if (doc == null) return "";
		String comment = doc.getText();
		String trimmed = StringUtil.trimStart(comment, "!!");
		text.append(trimmed.trim()).append("\n");
		return TaraDocumentationFormatter.doc2Html(this, text.toString());
	}

	public void addParameter(String name, String facet, int position, String metric, int line, int column, List<Object> values) {
		final TaraElementFactory factory = TaraElementFactory.getInstance(this.getProject());
		Map<String, String> params = new HashMap();
		params.put(name, String.join(" ", toString(values, metric)));
		final Parameters newParameters = factory.createExplicitParameters(params);
		final Parameters parameters = parametersAnchor(facet);
		if (parameters == null) getSignature().addAfter(newParameters, metaidentifier(facet));
		else {
			PsiElement anchor = calculateAnchor();
			if (anchor == null) parameters.add((PsiElement) newParameters.getParameters().get(0));
			else {
				final PsiElement separator = parameters.addAfter(factory.createParameterSeparator(), anchor);
				parameters.addAfter((PsiElement) newParameters.getParameters().get(0), separator);
			}
		}
	}

	public void applyAspect(String type) {
		final TaraElementFactory factory = TaraElementFactory.getInstance(this.getProject());
		if (appliedAspects().isEmpty()) {
			final PsiElement anchor = anchor();
			final PsiElement psiElement = getSignature().addAfter(factory.createWhiteSpace(), anchor);
			getSignature().addAfter(factory.createAspects(type), psiElement);
		} else
			getSignature().addAfter(factory.createFacet(type), (PsiElement) getSignature().appliedAspects().get(getSignature().appliedAspects().size() - 1));
	}

	@Nullable
	private PsiElement anchor() {
		final Signature signature = getSignature();
		if (signature.getIdentifier() != null) return signature.getIdentifier();
		if (signature.getParameters() != null) return signature.getParameters();
		else return signature.getMetaIdentifier();
	}

	private Parameters parametersAnchor(String facet) {
		PsiElement metaidentifier = metaidentifier(facet);
		if (metaidentifier == null) return null;
		final PsiElement nextSibling = metaidentifier.getNextSibling();
		return nextSibling instanceof Parameters ? (Parameters) nextSibling : null;
	}

	private TaraMetaIdentifier metaidentifier(String facet) {
		return facet.isEmpty() ? getSignature().getMetaIdentifier() : findFacet(facet);
	}

	private TaraMetaIdentifier findFacet(String facet) {
		for (Aspect f : getSignature().appliedAspects())
			if (f.type().equals(facet)) return ((TaraAspectApply) f).getMetaIdentifier();
		return null;
	}

	public List<String> toString(List<Object> values, String metric) {
		final List<String> list = new ArrayList<>(values.stream().map(v -> {
			final String quote = mustBeQuoted(v);
			return quote + (v instanceof Node ? ((Node) v).qualifiedName() : v.toString()) + quote;
		}).collect(toList()));
		if (metric != null && !metric.isEmpty()) list.add(metric);
		return list;
	}

	private String mustBeQuoted(Object v) {
		if (v instanceof Primitive.Expression) return "'";
		else if (v instanceof String && !((String) v).startsWith("\"")) return "\"";
		else return "";
	}

	private PsiElement calculateAnchor() {
		Parameters parameters = getSignature().getParameters();
		if (parameters == null) return null;
		List<Parameter> parameterList = parameters.getParameters();
		return parameterList.isEmpty() ? null : (PsiElement) parameterList.get(parameterList.size() - 1);
	}

	public void stashNodeName(String name) {

	}

	public String toString() {
		return (isAnonymous() ? "unNamed" : name()) + "@" + type();
	}

}
