package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.lang.psi.*;
import io.intino.tara.lang.model.*;
import io.intino.tara.lang.model.Primitive.Reference;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class TaraPsiUtil {

	private static final Logger LOG = Logger.getInstance(TaraPsiUtil.class.getName());

	private TaraPsiUtil() {
	}

	public static String getIdentifier(Identifier keyNode) {
		return keyNode != null ? keyNode.getText() : null;
	}

	public static String getIdentifier(Node element) {
		if (((TaraNode) element).getSignature().getIdentifier() != null) {
			ASTNode valueNode = ((TaraNode) element).getSignature().getIdentifier().getNode();
			return valueNode.getText();
		}
		return null;
	}

	static Identifier getIdentifierNode(Node element) {
		return ((TaraNode) element).getSignature().getIdentifier() != null ? ((TaraNode) element).getSignature().getIdentifier() : null;
	}

	static PsiElement setName(Signature signature, String newName) {
		TaraIdentifier oldId = signature.getIdentifier();
		if (oldId != null)
			signature.getNode().replaceChild(oldId.getNode(), TaraElementFactoryImpl.getInstance(signature.getProject()).createNameIdentifier(newName).getNode());
		return signature;
	}

	static List<Node> getBodyComponents(Body body) {
		if (body == null) return emptyList();
		List<Node> nodes = new ArrayList<>();
		nodes.addAll(body.getNodeList());
		nodes.addAll(body.getNodeLinks());
		return nodes;
	}

	static List<Variable> getVariablesInBody(Body body) {
		return body != null ? (List<Variable>) body.getVariableList() : emptyList();
	}

	public static List<Node> componentsOf(Node node) {
		List<Node> components = new ArrayList<>();
		if (node != null) {
			bodyComponents((TaraNode) node, components);
			final Node parent = node.parent();
			if (parent != null) components.addAll(parent.components());
			return components;
		}
		return emptyList();
	}

	public static List<Node> componentsOfType(NodeContainer node, String type) {
		return node == null ? Collections.emptyList() : node.components().stream().filter(c -> ((TaraNode) c).simpleType().equals(type)).collect(Collectors.toList());
	}

	public static Node componentOfType(NodeContainer node, String type) {
		return node == null ? null : node.components().stream().filter(c -> ((TaraNode) c).simpleType().equals(type)).findFirst().orElse(null);
	}

	public static String parameterValue(Node node, String name) {
		if (node == null) return null;
		Parameter parameter = node.parameters().stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null ? clean(parameter.values().get(0).toString()) : null;
	}

	public static String parameterValue(Aspect aspect, String name, int position) {
		if (aspect == null) return null;
		List<Parameter> parameters = aspect.parameters();
		Parameter parameter = parameters.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null ? clean(parameter.values().get(0).toString()) : (parameters.size() > position ? clean(parameters.get(position).values().get(0).toString()) : null);
	}

	public static String parameterValue(Node node, String name, int position) {
		if (node == null) return null;
		List<Parameter> parameters = node.parameters();
		Parameter parameter = parameters.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null ? clean(parameter.values().get(0).toString()) : (parameters.size() > position ? clean(parameters.get(position).values().get(0).toString()) : null);
	}

	private static String clean(String string) {
		return string.replace("\"", "");
	}

	public static Reference referenceParameterValue(Node node, String name) {
		if (node == null) return null;
		Parameter parameter = node.parameters().stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null && !parameter.values().isEmpty() ? (Reference) parameter.values().get(0) : null;
	}

	public static Reference referenceParameterValue(Node node, String name, int position) {
		if (node == null) return null;
		List<Parameter> parameters = node.parameters();
		Parameter parameter = parameters.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		if (parameter != null && !parameter.values().isEmpty()) {
			return (Reference) parameter.values().get(0);
		}
		if (parameters.size() > position) {
			parameters.get(position).type(Primitive.REFERENCE);
			return (Reference) parameters.get(position).values().get(0);
		}
		return null;
	}


	public static List<String> parameterValues(Node node, String name) {
		if (node == null) return Collections.emptyList();
		Parameter parameter = node.parameters().stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null ? parameter.values().stream().map(Object::toString).collect(Collectors.toList()) : Collections.emptyList();
	}

	public static <T> T read(Computable<T> t) {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return t.compute();
		return application.runReadAction(t);
	}

	private static void bodyComponents(TaraNode node, List<Node> components) {
		if (node.getBody() != null) {
			components.addAll(getBodyComponents(node.getBody()));
			removeSubs(components);
			addSubsOfComponent(components);
		}
	}

	public static int getIndentation(PsiElement element) {
		PsiElement container = (PsiElement) TaraPsiUtil.getContainerOf(element);
		if (container == null) return 0;
		if (is(container.getPrevSibling(), TaraTypes.NEWLINE) || is(container.getPrevSibling(), TaraTypes.NEW_LINE_INDENT))
			return 1 + countTabs(container.getPrevSibling().getText());
		return 0;
	}

	private static boolean is(PsiElement element, IElementType type) {
		return element != null && element.getNode() != null && element.getNode().getElementType().equals(type);
	}

	private static int countTabs(String text) {
		int i = text.length() - text.replace("\t", "").length();
		return i + (text.length() - text.replace(" ", "").length()) / 4;
	}


	public static <T> T getContainerByType(PsiElement element, Class<T> tClass) {
		PsiElement parent = element;
		while (parent != null)
			if (tClass.isInstance(parent)) return (T) parent;
			else parent = parent.getParent();
		return null;
	}

	public static <T> T contextOf(PsiElement element, Class<T> tClass) {
		PsiElement context = element;
		while (context != null)
			if (tClass.isInstance(context)) return (T) context;
			else context = context.getContext();
		return null;
	}

	private static void addSubsOfComponent(List<Node> inner) {
		List<Node> toAdd = new ArrayList<>();
		for (Node node : inner) toAdd.addAll(node.subs());
		inner.addAll(toAdd);
	}

	public static List<Node> getNodeReferencesOf(Node node) {
		return ((TaraNode) node).getBody() == null ? Collections.EMPTY_LIST : ((TaraNode) node).getBody().getNodeLinks();
	}


	private static void removeSubs(List<Node> children) {
		List<Node> list = children.stream().filter(Node::isSub).collect(Collectors.toList());
		children.removeAll(list);
	}

	@Nullable
	public static Node getContainerNodeOf(PsiElement element) {
		try {
			if (element == null) return null;
			PsiElement aElement = element.getOriginalElement();
			while ((aElement.getParent() != null)
					&& !(aElement.getParent() instanceof TaraModel)
					&& !(aElement.getParent() instanceof Node))
				aElement = aElement.getParent();
			return (aElement.getParent() != null) ? (Node) aElement.getParent() : null;
		} catch (NullPointerException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	@Nullable
	public static NodeContainer getContainerOf(PsiElement element) {
		PsiElement aElement = element;
		while (aElement != null && aElement.getParent() != null && isNotNodeOrFile(aElement))
			aElement = aElement.getParent();
		return (NodeContainer) (aElement != null ? aElement.getParent() : null);
	}

	private static boolean isNotNodeOrFile(PsiElement aElement) {
		return !(aElement.getParent() instanceof TaraModel) && !(aElement.getParent() instanceof Node);
	}

	public static Body getBodyContextOf(PsiElement element) {
		PsiElement aElement = element;
		while ((aElement.getParent() != null)
				&& !(aElement.getParent() instanceof Body))
			aElement = aElement.getParent();
		return (Body) aElement.getParent();
	}

	static Node getParentOf(Node node) {
		if (node.isSub()) return getContainerNodeOf((PsiElement) node);
		return ((TaraNode) node).getSignature().parent();
	}


	static boolean isAnnotatedAsComponent(Node node) {
		for (Tag flag : node.flags())
			if (flag.equals(Tag.Component)) return true;
		return false;
	}

	static PsiElement setType(Signature signature, String type) {
		TaraMetaIdentifier oldType = signature.getMetaIdentifier();
		if (oldType != null)
			return oldType.replace(TaraElementFactoryImpl.getInstance(signature.getProject()).createMetaIdentifier(type).copy());
		return null;
	}
}
