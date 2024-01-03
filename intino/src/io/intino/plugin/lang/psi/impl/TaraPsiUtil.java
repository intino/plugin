package io.intino.plugin.lang.psi.impl;

import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.lang.psi.*;
import io.intino.tara.language.model.*;
import io.intino.tara.language.model.Primitive.Reference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class TaraPsiUtil {

	private static final Logger LOG = Logger.getInstance(TaraPsiUtil.class.getName());

	private TaraPsiUtil() {
	}

	public static String simpleType(Mogram node) {
		return simpleType(node.type());
	}

	@NotNull
	public static String simpleType(String nodeType) {
		String type = nodeType;
		if (type.contains(":")) type = type.substring(nodeType.indexOf(":") + 1);
		if (type.contains(".")) {
			if (type.endsWith(".")) type = type.substring(0, type.length() - 1);
			else type = type.substring(type.indexOf(".") + 1);
		}
		return type;
	}

	public static String getIdentifier(Identifier keyNode) {
		return keyNode != null ? keyNode.getText() : null;
	}

	public static String getIdentifier(Mogram element) {
		if (((TaraMogram) element).getSignature().getIdentifier() != null) {
			ASTNode valueNode = ((TaraMogram) element).getSignature().getIdentifier().getNode();
			return valueNode.getText();
		}
		return null;
	}

	static Identifier getIdentifierNode(Mogram element) {
		return ((TaraMogram) element).getSignature().getIdentifier() != null ? ((TaraMogram) element).getSignature().getIdentifier() : null;
	}

	static PsiElement setName(Signature signature, String newName) {
		TaraIdentifier oldId = signature.getIdentifier();
		if (oldId != null)
			signature.getNode().replaceChild(oldId.getNode(), TaraElementFactoryImpl.getInstance(signature.getProject()).createNameIdentifier(newName).getNode());
		return signature;
	}

	static List<Mogram> getBodyComponents(Body body) {
		if (body == null) return emptyList();
		List<Mogram> nodes = new ArrayList<>();
		nodes.addAll(body.getMogramList());
		nodes.addAll(body.getNodeLinks());
		return nodes;
	}

	static List<Variable> getVariablesInBody(Body body) {
		return body != null ? (List<Variable>) body.getVariableList() : emptyList();
	}

	public static List<Mogram> componentsOf(Mogram mogram) {
		List<Mogram> components = new ArrayList<>();
		if (mogram != null) {
			bodyComponents((TaraMogram) mogram, components);
			final Mogram parent = mogram.parent();
			if (parent != null) components.addAll(parent.components());
			return components;
		}
		return emptyList();
	}

	public static List<Mogram> componentsOfType(MogramContainer mogram, String type) {
		return mogram != null ?
				getComponentsOfType(mogram, type) :
				Collections.emptyList();
	}

	@NotNull
	private static List<Mogram> getComponentsOfType(MogramContainer mogram, String type) {
		List<Mogram> nodes = new ArrayList<>(mogram.components().stream().filter(c -> ((TaraMogramImpl) c).simpleType().equals(type)).toList());
		if (mogram instanceof TaraMogram)
			nodes.addAll(stream(((TaraMogram) mogram).getChildren()).filter(c -> c instanceof TaraMogram && ((TaraMogramImpl) c).simpleType().equals(type)).map(c -> (Mogram) c).toList());
		return nodes;
	}

	public static Mogram componentOfType(MogramContainer mogram, String type) {
		return mogram == null ? null : mogram.components().stream().filter(c -> ((TaraMogramImpl) c).simpleType().equals(type)).findFirst().orElse(null);
	}

	public static String parameterValue(Mogram mogram, String name) {
		if (mogram == null) return null;
		Parameter parameter = mogram.parameters().stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null ? clean(parameter.values().get(0).toString()) : null;
	}

	public static String parameterValue(Facet facet, String name, int position) {
		if (facet == null) return null;
		List<Parameter> parameters = facet.parameters();
		Parameter parameter = parameters.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null ? clean(parameter.values().get(0).toString()) : (parameters.size() > position ? clean(parameters.get(position).values().get(0).toString()) : null);
	}

	public static String parameterValue(Mogram node, String name, int position) {
		if (node == null) return null;
		List<Parameter> parameters = node.parameters();
		Parameter parameter = parameters.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null ? clean(read(() -> parameter.values().get(0).toString())) :
				parameterValueFromPosition(parameters, position, name);
	}

	public static List<String> parameterValues(Mogram mogram, String name, int position) {
		if (mogram == null) return null;
		List<Parameter> parameters = mogram.parameters();
		Parameter parameter = parameters.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null ?
				read(parameter::values).stream().map(s -> clean(s.toString())).collect(toList()) :
				parameterValuesFromPosition(parameters, position, name);
	}

	@Nullable
	private static String parameterValueFromPosition(List<Parameter> parameters, int position, String name) {
		return parameters.size() > position ? read(() -> {
			Parameter parameter = parameters.get(position);
			if (parameter.name() != null && !parameter.name().isEmpty() && !parameter.name().equals(name)) return null;
			Object v = parameter.values().get(0);
			if (v instanceof EmptyNode) return null;
			return clean(v.toString());
		}) : null;
	}

	@Nullable
	private static List<String> parameterValuesFromPosition(List<Parameter> parameters, int position, String name) {
		return parameters.size() > position ? read(() -> {
			Parameter parameter = parameters.get(position);
			if (parameter.name() != null && !parameter.name().isEmpty() && !parameter.name().equals(name)) return null;
			return parameter.values().stream().map(s -> clean(s.toString())).collect(toList());
		}) : null;
	}

	private static String clean(String string) {
		return string == null ? null : string.replace("\"", "");
	}

	public static Reference referenceParameterValue(Mogram node, String name) {
		if (node == null) return null;
		Parameter parameter = node.parameters().stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		return parameter != null && !parameter.values().isEmpty() ? (Reference) parameter.values().get(0) : null;
	}

	public static Mogram referenceParameterValue(Mogram node, String name, int position) {
		if (node == null) return null;
		List<Parameter> parameters = node.parameters();
		return referenceParameterValue(parameters, name, position);
	}

	public static Mogram referenceParameterValue(List<Parameter> parameters, String name, int position) {
		Parameter parameter = parameters.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
		if (parameter != null && !parameter.values().isEmpty()) {
			Object o = parameter.values().get(0);
			return o instanceof Reference ? ((Reference) o).reference() : (Mogram) o;
		}
		if (parameters.size() > position) {
			parameters.get(position).type(Primitive.REFERENCE);
			Object o = parameters.get(position).values().get(0);
			return o instanceof Reference ? ((Reference) o).reference() : (Mogram) o;
		}
		return null;
	}


	public static <T> T read(Computable<T> t) {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return t.compute();
		return application.runReadAction(t);
	}

	public static void write(Runnable t) {
		Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed()) t.run();
		application.runWriteAction(t);
	}

	private static void bodyComponents(TaraMogram node, List<Mogram> components) {
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

	private static void addSubsOfComponent(List<Mogram> inner) {
		List<Mogram> toAdd = new ArrayList<>();
		for (Mogram mogram : inner) toAdd.addAll(mogram.subs());
		inner.addAll(toAdd);
	}

	public static List<Mogram> getNodeReferencesOf(Mogram node) {
		return ((TaraMogram) node).getBody() == null ? Collections.emptyList() : ((TaraMogram) node).getBody().getNodeLinks();
	}


	private static void removeSubs(List<Mogram> children) {
		List<Mogram> list = children.stream().filter(Mogram::isSub).toList();
		children.removeAll(list);
	}

	@Nullable
	public static Mogram getContainerNodeOf(PsiElement element) {
		try {
			if (element == null) return null;
			PsiElement aElement = element.getOriginalElement();
			while ((aElement.getParent() != null)
					&& !(aElement.getParent() instanceof TaraModel)
					&& !(aElement.getParent() instanceof Mogram))
				aElement = aElement.getParent();
			return (aElement.getParent() != null) ? (Mogram) aElement.getParent() : null;
		} catch (NullPointerException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	@Nullable
	public static MogramContainer getContainerOf(PsiElement element) {
		PsiElement aElement = element;
		while (aElement != null && aElement.getParent() != null && isNotNodeOrFile(aElement))
			aElement = aElement.getParent();
		return (MogramContainer) (aElement != null ? aElement.getParent() : null);
	}

	private static boolean isNotNodeOrFile(PsiElement aElement) {
		return !(aElement.getParent() instanceof TaraModel) && !(aElement.getParent() instanceof Mogram);
	}

	public static Body getBodyContextOf(PsiElement element) {
		PsiElement aElement = element;
		while ((aElement.getParent() != null)
				&& !(aElement.getParent() instanceof Body))
			aElement = aElement.getParent();
		return (Body) aElement.getParent();
	}

	static Mogram getParentOf(Mogram node) {
		if (node.isSub()) return getContainerNodeOf((PsiElement) node);
		return ((TaraMogram) node).getSignature().parent();
	}


	static boolean isAnnotatedAsComponent(Mogram node) {
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
