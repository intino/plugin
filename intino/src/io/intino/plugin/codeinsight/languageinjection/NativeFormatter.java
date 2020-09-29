package io.intino.plugin.codeinsight.languageinjection;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.FrameBuilderContext;
import io.intino.magritte.Language;
import io.intino.magritte.dsl.Meta;
import io.intino.magritte.dsl.Proteo;
import io.intino.magritte.lang.model.*;
import io.intino.magritte.lang.model.rules.variable.NativeObjectRule;
import io.intino.magritte.lang.model.rules.variable.NativeReferenceRule;
import io.intino.magritte.lang.model.rules.variable.NativeRule;
import io.intino.magritte.lang.model.rules.variable.NativeWordRule;
import io.intino.magritte.lang.semantics.Constraint;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.codeinsight.languageinjection.helpers.QualifiedNameFormatter;
import io.intino.plugin.codeinsight.languageinjection.helpers.TemplateTags;
import io.intino.plugin.codeinsight.languageinjection.imports.Imports;
import io.intino.plugin.lang.psi.TaraRuleContainer;
import io.intino.plugin.lang.psi.TaraVariable;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.intino.magritte.lang.model.Primitive.*;
import static io.intino.magritte.lang.model.Tag.Feature;
import static io.intino.magritte.lang.model.Tag.Instance;
import static io.intino.plugin.codeinsight.languageinjection.helpers.QualifiedNameFormatter.cleanQn;
import static io.intino.plugin.codeinsight.languageinjection.helpers.QualifiedNameFormatter.qn;
import static io.intino.plugin.lang.psi.resolve.ReferenceManager.resolveRule;
import static io.intino.plugin.project.Safe.safe;
import static java.util.Collections.emptySet;

@SuppressWarnings("Duplicates")
public class NativeFormatter implements TemplateTags {

	private final Imports allImports;
	private final String workingPackage;
	private final Language language;
	private final boolean m0;
	private Set<String> imports = new HashSet<>();

	NativeFormatter(Module module, String workingPackage, Language language) {
		this.workingPackage = workingPackage;
		allImports = new Imports(module.getProject());
		this.language = language;
		final Configuration conf = IntinoUtil.configurationOf(module);
		this.m0 = conf != null && safe(() -> conf.artifact().model().level().isSolution(), false);
	}

	private static String getLanguageScope(Parameter parameter, Language language) {
		if (!parameter.scope().isEmpty()) return parameter.scope();
		else return language.languageName();
	}

	private static String buildContainerPathOfExpression(Variable variable, String workingPackage, boolean m0) {
		return qn(firstNoFeatureAndNamed(variable.container()), workingPackage, m0);
	}

	private static String buildContainerPathOfExpression(Parameter parameter, String outDsl) {
		return buildExpressionContainerPath(parameter.scope(), parameter.container(), outDsl);
	}

	public static String getSignature(Parameter parameter) {
		if (!(parameter.rule() instanceof NativeRule)) return "";
		final NativeRule rule = (NativeRule) parameter.rule();
		return rule != null ? rule.signature() : null;
	}

	private static String getInterface(Parameter parameter) {
		final NativeRule rule = (NativeRule) parameter.rule();
		if (rule == null)
			return null;//throw new SemanticException(new SemanticError("reject.native.signature.notfound", new LanguageParameter(parameter)));
		return rule.interfaceClass();
	}

	public static String getSignature(Variable variable) {
		return ((NativeRule) variable.rule()).signature();
	}

	public static String buildContainerPath(Node node, String scopeLanguage, String workingPackage) {
		if (node != null) {
			final Node scope = node.is(Instance) ? firstNoFeature(node) : firstNoFeatureAndNamed(node);
			if (scope == null) return "";
			if (scope.is(Instance)) return getTypeAsScope(scope, scopeLanguage);
			return qn(scope, workingPackage, false);
		} else return null;
	}

	private static String buildExpressionContainerPath(String languageWorkingPackage, Node owner, String workingPackage) {
		final String trueWorkingPackage = extractWorkingPackage(languageWorkingPackage, workingPackage);
		if (owner != null) {
			final Node scope = owner.is(Instance) ? firstNoFeature(owner) : firstNoFeatureAndNamed(owner);
			if (scope == null) return "";
			if (scope.is(Instance)) return getTypeAsScope(scope, trueWorkingPackage);
			else return qn(scope, workingPackage, false);
		} else return null;
	}

	private static String extractWorkingPackage(String scope, String language) {
		return scope != null && !scope.isEmpty() ? scope : language;
	}

	private static String getTypeAsScope(Node scope, String languageWorkingPackage) {
		if (languageWorkingPackage == null || scope == null) return "";
		return languageWorkingPackage.toLowerCase() + QualifiedNameFormatter.DOT + cleanQn(scope.type());
	}

	private static Node firstNoFeature(NodeContainer owner) {
		NodeContainer container = owner;
		while (container != null) {
			if (container instanceof Node && !(container instanceof NodeRoot) && !((Node) container).is(Feature))
				return (Node) container;
			if (container.container() instanceof NodeRoot) return (Node) container;
			container = container.container();
		}
		return null;
	}

	private static Node firstNoFeatureAndNamed(NodeContainer owner) {
		NodeContainer container = owner;
		while (container != null) {
			if (container instanceof Node && !(container instanceof NodeRoot) && !((Node) container).isAnonymous() &&
					!((Node) container).is(Feature))
				return (Node) container;
			if (container.container() instanceof NodeRoot) return (Node) container;
			container = container.container();
		}
		return owner instanceof Node && ((Node) owner).isAnonymous() ? (Node) owner : TaraPsiUtil.getContainerNodeOf((PsiElement) owner);
	}

	public static String getSignature(PsiClass nativeInterface) {
		if (nativeInterface.getMethods().length == 0) return "void NoSignatureFound()";
		final String text = nativeInterface.getMethods()[0].getText();
		return text.substring(0, text.length() - 1);
	}

	private static String getReturn(PsiClass nativeInterface, String body) {
		if (nativeInterface.getAllMethods().length == 0) return "";
		if (body.isEmpty()) return body;
		body = body.endsWith(";") || body.endsWith("}") ? body : body + ";";
		if (nativeInterface.getMethods()[0].getReturnType() != null &&
				!("void".equals(nativeInterface.getMethods()[0].getReturnType().getCanonicalText())) &&
				!body.contains("\n") && body.split(";").length == 1 && !body.startsWith(RETURN))
			return RETURN + " ";
		return "";
	}

	private static String getReturn(String body, String signature) {
		final String returnText = RETURN + " ";
		body = body.endsWith(";") || body.endsWith("}") ? body : body + ";";
		if (!signature.contains(" void ") && !body.contains("\n") && !body.startsWith(returnText))
			return returnText;
		return "";
	}

	private static String getReturn(String body) {
		final String returnText = RETURN + " ";
		body = body.endsWith(";") || body.endsWith("}") ? body : body + ";";
		if (!body.contains("\n") && !body.startsWith(returnText))
			return returnText;
		return "";
	}

	void fillFrameForNativeVariable(FrameBuilderContext context, Variable variable, boolean isMultiline) {
		final TaraRuleContainer ruleContainer = ((TaraVariable) variable).getRuleContainer();
		if (ruleContainer == null || ruleContainer.getRule() == null) return;
		PsiElement nativeInterface = resolveRule(ruleContainer.getRule());
		if (nativeInterface == null) return;
		imports.addAll(collectImports((Valued) variable));
		imports.addAll(collectImports((PsiClass) nativeInterface));
		context.add(IMPORTS, imports.toArray(new String[0]));
		context.add(NAME, variable.name());
		context.add(SIGNATURE, getSignature((PsiClass) nativeInterface));
		context.add(GENERATED_LANGUAGE, workingPackage.toLowerCase());
		context.add(NATIVE_CONTAINER, cleanQn(buildContainerPath(variable.container(), variable.scope(), workingPackage)));
		if (!(language instanceof Proteo) && !(language instanceof Meta))
			context.add(LANGUAGE, language.languageName());
		if (ruleContainer.getRule() != null) context.add(RULE, ruleContainer.getRule().getText());
		final String aReturn = getReturn((PsiClass) nativeInterface, variable.values().get(0).toString());
		if (!aReturn.isEmpty() && !isMultiline) context.add(RETURN, aReturn);
	}

	void fillFrameForFunctionParameter(FrameBuilderContext context, Parameter parameter, String body, boolean isMultiLine) {
		if (parameter.rule() == null) return;
		final String signature = getSignature(parameter);
		final List<String> imports = ((NativeRule) parameter.rule()).imports();
		imports.addAll(collectImports((Valued) parameter));
		context.add(IMPORTS, imports.toArray(new String[0]));
		context.add(NAME, parameter.name());
		context.add(GENERATED_LANGUAGE, workingPackage.toLowerCase());
		context.add(SCOPE, parameter.scope());
		context.add(NATIVE_CONTAINER, cleanQn(buildContainerPath(parameter.container(), parameter.scope(), workingPackage)));
		if (!(language instanceof Proteo) && !(language instanceof Meta))
			context.add(LANGUAGE, getLanguageScope(parameter, language));
		if (signature != null) context.add(SIGNATURE, signature);
		final String anInterface = getInterface(parameter);
		if (anInterface != null) context.add(RULE, cleanQn(anInterface));
		if (signature != null) {
			final String aReturn = NativeFormatter.getReturn(body, signature);
			if (!aReturn.isEmpty() && !isMultiLine) context.add(RETURN, aReturn);
		}
	}

	void fillFrameExpressionVariable(FrameBuilderContext context, Variable variable, String body, boolean isMultiline) {
		final List<String> imports = new ArrayList<>(collectImports((Valued) variable));
		context.add(NAME, variable.name());
		context.add(IMPORTS, imports.toArray(new String[0]));
		context.add(GENERATED_LANGUAGE, workingPackage);
		context.add(NATIVE_CONTAINER, buildContainerPathOfExpression(variable, workingPackage, m0));
		context.add(TYPE, typeFrame(type(variable), variable.isMultiple()));
		if (!isMultiline) context.add(RETURN, NativeFormatter.getReturn(body));
	}

	void fillFrameExpressionParameter(FrameBuilderContext context, Parameter parameter, String body, boolean isMultiline) {
		final List<String> imports = new ArrayList<>(collectImports((Valued) parameter));
		context.add(NATIVE);
		context.add(NAME, parameter.name());
		context.add(IMPORTS, imports.toArray(new String[0]));
		context.add(GENERATED_LANGUAGE, workingPackage);
		context.add(NATIVE_CONTAINER, buildContainerPathOfExpression(parameter, workingPackage));
		context.add(TYPE, typeFrame(type(parameter), isMultiple(parameter)));
		if (!isMultiline) context.add(RETURN, NativeFormatter.getReturn(body));
	}

	private boolean isMultiple(Parameter parameter) {
		final Constraint.Parameter constraint = IntinoUtil.parameterConstraintOf(parameter);
		return constraint != null && !constraint.size().isSingle();
	}

	private Frame typeFrame(String type, boolean multiple) {
		return (multiple ?
				new FrameBuilder("list").add("value", type) :
				new FrameBuilder("type").add("value", type))
				.toFrame();
	}

	private String type(Variable variable) {
		if (variable.isReference())
			return QualifiedNameFormatter.qn(variable.destinyOfReference(), workingPackage, false);
		if (variable.type().equals(WORD)) return wordType(variable);
		else if (OBJECT.equals(variable.type()))
			return variable.rule() == null ? "" : ((NativeObjectRule) variable.rule()).type();
		else return variable.type().javaName();
	}

	private String type(Parameter parameter) {
		if (parameter.type().equals(REFERENCE)) return referenceType(parameter);
		if (parameter.type().equals(WORD)) return wordType(parameter);
		else if (OBJECT.equals(parameter.type())) return ((NativeObjectRule) parameter.rule()).type();
		else return parameter.type().javaName();
	}

	private Set<String> collectImports(Valued valued) {
		final Node containerOf = TaraPsiUtil.getContainerNodeOf(valued);
		if (containerOf == null || allImports.get(IntinoUtil.importsFile(valued)) == null ||
				!allImports.get(IntinoUtil.importsFile(valued)).containsKey(composeQn(valued, containerOf)))
			return emptySet();
		else {
			if (allImports.get(IntinoUtil.importsFile(valued)) == null) return emptySet();
			final Set<String> set = allImports.get(IntinoUtil.importsFile(valued)).get(composeQn(valued, containerOf));
			return set == null ? emptySet() : set;
		}
	}

	private Set<String> collectImports(PsiClass nativeInterface) {
		if (nativeInterface.getDocComment() == null) return emptySet();
		final String[] lines = nativeInterface.getDocComment().getText().split("\n");
		Set<String> set = new HashSet<>();
		for (String line : lines)
			if (line.contains("import "))
				set.add(line.trim().startsWith("*") ? line.trim().substring(1).trim() : line.trim());
		return set;
	}

	private String composeQn(Valued valued, Node containerOf) {
		return containerOf.qualifiedName() + "." + valued.name();
	}

	private String referenceType(Parameter parameter) {
		if (parameter.rule() instanceof NativeReferenceRule)
			return workingPackage.toLowerCase() + DOT + ((NativeReferenceRule) parameter.rule()).allowedTypes().get(0);
		return "";
	}

	private String wordType(Variable variable) {
		return workingPackage.toLowerCase() + DOT + variable.container().qualifiedName() + "." + Format.firstUpperCase().format(variable.name());
	}

	private String wordType(Parameter parameter) {
		if (parameter.rule() instanceof NativeWordRule)
			return workingPackage.toLowerCase() + DOT + ((NativeWordRule) parameter.rule()).words().get(0);
		return "";
	}

}
