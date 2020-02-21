package io.intino.plugin.codeinsight.intentions;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.codeinsight.languageinjection.helpers.NativeExtractor;
import io.intino.plugin.codeinsight.languageinjection.helpers.QualifiedNameFormatter;
import io.intino.plugin.codeinsight.languageinjection.imports.Imports;
import io.intino.plugin.lang.psi.TaraRule;
import io.intino.plugin.lang.psi.TaraVariable;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.lang.psi.impl.TaraVariableImpl;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import io.intino.plugin.project.IntinoModuleType;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.Checker;
import io.intino.tara.Language;
import io.intino.tara.lang.model.*;
import io.intino.tara.lang.model.rules.CustomRule;
import io.intino.tara.lang.model.rules.Size;
import io.intino.tara.lang.model.rules.variable.NativeObjectRule;
import io.intino.tara.lang.model.rules.variable.NativeRule;
import io.intino.tara.lang.semantics.Constraint;
import io.intino.tara.lang.semantics.errorcollector.SemanticFatalException;

import java.util.*;

import static com.intellij.openapi.util.io.FileUtilRt.getNameWithoutExtension;
import static com.intellij.pom.java.LanguageLevel.JDK_1_8;
import static com.intellij.psi.search.GlobalSearchScope.*;
import static io.intino.plugin.codeinsight.languageinjection.NativeFormatter.buildContainerPath;
import static io.intino.plugin.codeinsight.languageinjection.helpers.QualifiedNameFormatter.cleanQn;
import static io.intino.plugin.codeinsight.languageinjection.helpers.QualifiedNameFormatter.qnOf;
import static io.intino.tara.lang.model.Primitive.FUNCTION;

public class MethodReferenceCreator {
	private final Valued valued;
	private final String reference;
	private final Module module;
	private final String workingPackage;
	private final String languageWorkingPackage;

	public MethodReferenceCreator(Valued valued, String reference) {
		this.valued = valued;
		this.reference = reference.replace("@", "");
		module = valued != null ? ModuleProvider.moduleOf(valued) : null;
		workingPackage = TaraUtil.graphPackage(valued);
		languageWorkingPackage = TaraUtil.languageGraphPackage(valued);
	}

	public PsiMethod create(String methodBody) {
		if (valued == null || module == null) return null;
		resolve(valued);
		PsiClass aClass = findClass();
		return addMethod(aClass != null ? aClass : createClass(), methodBody);
	}

	private PsiClass createClass() {
		PsiDirectory destiny = valued.getContainingFile().getParent();
		if (destiny == null) return null;
		return JavaDirectoryService.getInstance().createClass(destiny, Format.javaValidName().format(getNameWithoutExtension(valued.getContainingFile().getName())).toString());
	}

	private PsiMethod addMethod(PsiClass aClass, String body) {
		if (aClass == null || !aClass.canNavigateToSource()) return null;
		final JavaPsiFacade facade = JavaPsiFacade.getInstance(aClass.getProject());
		final PsiMethod method = facade.getElementFactory().createMethodFromText(buildMethodWith(body), null, JDK_1_8);
		final PsiElement newMethod = aClass.add(method);
		addImports(aClass);
		return (PsiMethod) newMethod;
	}

	private String buildMethodWith(String methodBody) {
		FrameBuilder builder = new FrameBuilder("method");
		Size size = valued instanceof Parameter ? parameterSize() : ((Variable) valued).size();
		final String type = type();
		if (size != null && !size.isSingle() && !"void".equals(type)) builder.add("multiple");
		builder.add("name", reference);
		builder.add("type", type);
		final String[] parameters = findParameters();
		if (parameters.length != 0 && !parameters[0].isEmpty()) builder.add("parameter", parameters);
		if (valued.getValue() != null) {
			if (!type.equalsIgnoreCase("void") && !methodBody.startsWith("return "))
				methodBody = "return " + (methodBody.isEmpty() ? "null" : methodBody);
			if (!methodBody.endsWith(";")) methodBody += ";";
		}
		builder.add("body", methodBody);
		builder.add("scope", cleanQn(buildContainerPath(TaraPsiUtil.getContainerNodeOf(valued), valued.scope(), workingPackage)));
		return new MethodTemplate().render(builder.toFrame());
	}

	private String[] findParameters() {
		if (FUNCTION.equals(valued.type())) if (valued instanceof Parameter) {
			final NativeRule rule = (NativeRule) valued.rule();
			if (rule.signature() == null || rule.signature().isEmpty()) return new String[0];
			return new String[]{new NativeExtractor(rule.interfaceClass(), valued.name(), rule.signature()).parameters()};
		} else return resolveInterfaceParameters();
		else return new String[0];
	}

	private String[] resolveInterfaceParameters() {
		final TaraRule rule = ((TaraVariable) valued).getRuleContainer().getRule();
		if (rule == null) return new String[0];
		final PsiElement reference = ReferenceManager.resolveRule(rule);
		if (!(reference instanceof PsiClass)) return new String[0];
		final PsiParameterList parameterList = ((PsiClass) reference).getAllMethods()[0].getParameterList();
		return Arrays.stream(parameterList.getParameters()).map(PsiParameter::getText).toArray(String[]::new);
	}

	private Size parameterSize() {
		final Constraint.Parameter constraint = TaraUtil.parameterConstraintOf((Parameter) valued);
		return constraint != null ? constraint.size() : Size.MULTIPLE();
	}

	private String type() {
		try {
			Node node = TaraPsiUtil.getContainerNodeOf(valued);
			if (node != null) new Checker(TaraUtil.getLanguage(valued)).check(node.resolve());
		} catch (SemanticFatalException ignored) {
		}
		if (valued.flags().contains(Tag.Concept)) return "io.intino.tara.magritte.Concept";
		if (FUNCTION.equals(valued.type()) && valued.rule() instanceof NativeRule)
			return getFunctionReturnType().getPresentableText();
		else if (Primitive.OBJECT.equals(valued.type())) return getObjectReturnType();
		else if (Primitive.REFERENCE.equals(valued.type())) return getReferenceReturnType(valued);
		else if (Primitive.WORD.equals(valued.type())) return getWordReturnType(valued);
		else return valued.type() == null ? "void" : valued.type().javaName();
	}

	private String getWordReturnType(Valued valued) {
		final Rule rule = valued.rule();
		if (rule instanceof CustomRule)
			return ((CustomRule) rule).externalClass();
		return valued.name();
	}

	private String getReferenceReturnType(Valued valued) {
		final Node node = ((TaraVariableImpl) valued).destinyOfReference();
		return QualifiedNameFormatter.qn(node, workingPackage, false);
	}

	private PsiType getFunctionReturnType() {
		final String workingPackage = valued instanceof Variable ? this.workingPackage : valued.scope().toLowerCase();
		final JavaPsiFacade facade = JavaPsiFacade.getInstance(valued.getProject());
		PsiClass aClass = facade.findClass(workingPackage + ".functions." + ((NativeRule) valued.rule()).interfaceClass(), moduleWithDependenciesScope(module));
		if (aClass == null)
			aClass = facade.findClass(languageWorkingPackage + ".functions." + ((NativeRule) valued.rule()).interfaceClass(), moduleWithDependenciesAndLibrariesScope(module));
		if (aClass == null || !aClass.isInterface()) return PsiType.VOID;
		return aClass.getMethods()[0].getReturnType();
	}

	private String getObjectReturnType() {
		return ((NativeObjectRule) valued.rule()).type();
	}

	private PsiClass findClass() {
		Module module = ModuleProvider.moduleOf(valued);
		final JavaPsiFacade instance = JavaPsiFacade.getInstance(valued.getProject());
		final String qualifiedName = TaraUtil.methodReference(valued);
		return IntinoModuleType.isIntino(module) && !qualifiedName.isEmpty() ? instance.findClass(qualifiedName, moduleWithDependenciesScope(module)) : null;
	}

	private void addImports(PsiClass aClass) {
		if (valued.type().equals(FUNCTION))
			addImports(aClass, valued instanceof Variable ? findFunctionImports() : ((NativeRule) valued.rule()).imports());
		Imports imports = new Imports(module.getProject());
		String qn = qnOf(valued);
		final Map<String, Set<String>> map = imports.get(TaraUtil.importsFile(valued));
		if (map == null) return;
		imports.save(TaraUtil.importsFile(valued), qn, map.get(qn));
		if (map.get(qn) == null) return;
		addImports(aClass, map.get(qn));
		map.remove(qn);
	}

	private Collection<String> findFunctionImports() {
		final String genLanguage = workingPackage.isEmpty() ? module.getName() : workingPackage;
		final PsiClass aClass = JavaPsiFacade.getInstance(valued.getProject()).findClass(genLanguage.toLowerCase() + ".functions." + ((NativeRule) valued.rule()).interfaceClass(), allScope(module.getProject()));
		if (aClass == null || !aClass.isInterface()) return Collections.emptyList();
		List<String> imports = new ArrayList<>();
		if (((PsiJavaFile) aClass.getContainingFile()).getImportList() == null) return Collections.emptyList();
		for (PsiImportStatementBase psiImportStatementBase : Objects.requireNonNull(((PsiJavaFile) aClass.getContainingFile()).getImportList()).getAllImportStatements())
			imports.add(psiImportStatementBase.getText());
		return imports;
	}

	private void addImports(PsiClass aClass, Collection<String> imports) {
		final PsiJavaFile file = (PsiJavaFile) aClass.getContainingFile();
		for (String statement : imports)
			if (statement.contains(" static ")) addStaticImport(aClass, file, statement.split(" ")[2].replace(";", ""));
			else addOnDemandImport(aClass, file, statement.split(" ")[1].replace(";", ""));
	}

	private void addOnDemandImport(PsiClass aClass, PsiJavaFile file, String importReference) {
		final PsiClass reference = JavaPsiFacade.getInstance(valued.getProject()).findClass(importReference, allScope(module.getProject()));
		if (reference != null) {
			final PsiImportStatement importStatement = JavaPsiFacade.getElementFactory(aClass.getProject()).createImportStatement(reference);
			if (file.getImportList() != null) file.getImportList().add(importStatement);
			else file.addAfter(importStatement, file.getPackageStatement());
		}
	}

	private void addStaticImport(PsiClass aClass, PsiJavaFile file, String reference) {
		final PsiClass classReference = JavaPsiFacade.getInstance(valued.getProject()).findClass(reference.substring(0, reference.lastIndexOf(".")), allScope(module.getProject()));
		final PsiImportStaticStatement importStaticStatement = JavaPsiFacade.getElementFactory(aClass.getProject()).createImportStaticStatement(classReference, reference.substring(reference.lastIndexOf(".") + 1));
		if (file.getImportList() != null) file.getImportList().add(importStaticStatement);
		else file.addAfter(importStaticStatement.getParent().copy(), file.getPackageStatement());
	}

	private void resolve(Valued valued) {
		final List<Node> tree = tree(valued);
		final Language language = TaraUtil.getLanguage(valued);
		if (!tree.isEmpty() && language != null) for (Node node : tree) {
			try {
				new Checker(language).check(node.resolve());
			} catch (SemanticFatalException ignored) {
			}
		}
	}

	private List<Node> tree(Valued valued) {
		List<Node> list = new ArrayList<>();
		Node container = TaraPsiUtil.getContainerNodeOf(valued);
		list.add(container);
		while ((TaraPsiUtil.getContainerNodeOf((PsiElement) container)) != null) {
			container = TaraPsiUtil.getContainerNodeOf((PsiElement) container);
			list.add(container);
		}
		return list;
	}
}
