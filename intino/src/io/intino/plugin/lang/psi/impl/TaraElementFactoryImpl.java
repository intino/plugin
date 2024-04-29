package io.intino.plugin.lang.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.*;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.Variable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DataFlowIssue")
public class TaraElementFactoryImpl extends TaraElementFactory {

	private static final String DUMMY_CONCEPT = "Dummy DummyInstance";
	private static final String CONCEPT_DUMMY = "Concept Dummy";
	private final Project project;

	public TaraElementFactoryImpl(Project project) {
		this.project = project;
	}

	public TaraMogram createMogram(String name, String type) {
		final TaraModelImpl file = createDummyFile(
				type + " " + name + "\n"
		);
		return (TaraMogram) file.components().iterator().next();
	}

	public TaraMogram createMogram(String name) {
		final TaraModelImpl file = createDummyFile(
				"Concept" + " " + name + "\n"
		);
		return (TaraMogram) file.components().iterator().next();
	}

	public TaraMogram createFullMogram(String text) {
		final TaraModelImpl file = createDummyFile(text + "\n");
		return (TaraMogram) file.components().iterator().next();
	}

	private TaraMogram createNodeWithType(String type) {
		final TaraModelImpl file = createDummyFile(
				type + " " + "Dummy" + "\n"
		);
		return (TaraMogram) file.components().iterator().next();
	}


	public TaraModelImpl createDummyFile(String text) {
		return (TaraModelImpl) PsiFileFactory.getInstance(project).createFileFromText("dummy." + TaraFileType.instance().getDefaultExtension(), TaraFileType.instance(), text);
	}

	public MetaIdentifier createMetaIdentifier(String type) {
		return PsiTreeUtil.getChildOfType(createNodeWithType(type).getSignature(), MetaIdentifier.class);
	}

	public Identifier createNameIdentifier(String name) {
		return PsiTreeUtil.getChildOfType(createMogram(name).getSignature(), Identifier.class);
	}

	public TaraVariable createVariable(String name, Primitive type) {
		final TaraModelImpl file = createDummyFile(
				CONCEPT_DUMMY + "\n" +
						"\tvar " + type.getName() + " " + name + "\n" +
						"\t" + CONCEPT_DUMMY + "2\n"
		);
		Body body = PsiTreeUtil.getChildOfType(file, TaraMogram.class).getBody();
		return body != null ? (TaraVariable) body.getFirstChild().getNextSibling() : null;
	}

	@Override
	public TaraFacets createFacets(String type) {
		final TaraModelImpl file = createDummyFile(
				CONCEPT_DUMMY + " as " + type + "\n" +
						"\t" + CONCEPT_DUMMY + "2\n"
		);
		return PsiTreeUtil.getChildOfType(file, TaraMogram.class).getSignature().getFacets();
	}

	public TaraFacetApply createFacetApply(String type) {
		final TaraModelImpl file = createDummyFile(
				CONCEPT_DUMMY + " as " + type + "\n" +
						"\t" + CONCEPT_DUMMY + "2\n"
		);
		return PsiTreeUtil.getChildOfType(file, TaraMogram.class).getSignature().getFacets().getFacetApplyList().get(0);
	}

	public TaraVariable createResource(String name, String type) {
		final TaraModelImpl file = createDummyFile(
				CONCEPT_DUMMY + "\n" +
						"\tvar resource:" + type + " " + name + "\n" +
						"\tConcept Ontology\n"
		);
		Body body = PsiTreeUtil.getChildOfType(file, TaraMogram.class).getBody();
		return body != null ? (TaraVariable) body.getFirstChild().getNextSibling() : null;
	}

	@Override
	public PsiElement createMetaWordIdentifier(String module, String mogram, String name) {
		final TaraModelImpl file = createDummyFile(
				mogram + "(" + mogram + "." + name + ")" + " Dummy" + "\n"
		);
		Collection<Parameter> parameters = PsiTreeUtil.getChildOfType(file, TaraMogram.class).getSignature().getParameters().getParameters();
		return ((PsiElement) parameters.iterator().next()).getLastChild().getLastChild();
	}

	@Override
	public TaraVariable createWord(String name, String[] types) {
		final TaraModelImpl file = createDummyFile(
				CONCEPT_DUMMY + "\n" +
						"\tvar word " + name + "\n" +
						getWordTypesToString(types));
		Body body = PsiTreeUtil.getChildOfType(file, TaraMogram.class).getBody();
		return body != null ? (TaraVariable) body.getVariableList().get(0) : null;
	}

	private String getWordTypesToString(String[] types) {
		StringBuilder builder = new StringBuilder();
		for (String type : types) builder.append("\t\t").append(type).append("\n");
		return builder.toString();
	}


	public TaraImports createImport(String reference) {
		final TaraModelImpl file = createDummyFile(
				"dsl Proteo\n\n" +
						"use " + reference + "\n" +
						CONCEPT_DUMMY + "\n"
		);
		return PsiTreeUtil.getChildrenOfType(file, TaraImports.class)[0];
	}

	public TaraDslDeclaration createDslDeclaration(String name) {
		final TaraModelImpl file = createDummyFile(
				"dsl " + name + "\n\n" +
						CONCEPT_DUMMY + "\n"
		);
		return PsiTreeUtil.getChildrenOfType(file, TaraDslDeclaration.class)[0];
	}

	public PsiElement createNewLine() {
		final TaraModelImpl file = createDummyFile("\n");
		return file.getFirstChild();
	}

	@Override
	public Parameters createEmptyParameters() {
		final TaraModelImpl file = createDummyFile(
				"Form(" + ")" + "DummyInstance\n"
		);
		final Mogram next = file.components().iterator().next();
		return ((TaraMogram) next).getSignature().getParameters();
	}

	@Override
	public Parameters createParameters(boolean areStrings) {
		final TaraModelImpl file = createDummyFile(
				"Form(" + (areStrings ? "\"\"" : "") + ")" + "DummyInstance\n"
		);
		final Mogram next = file.components().iterator().next();
		return ((TaraMogram) next).getSignature().getParameters();
	}

	@Override
	public Parameters createParameters(String... names) {
		String parameters = buildParameters(names);
		final TaraModelImpl file = createDummyFile(
				"Dummy(" + parameters + ")" + " DummyInstance\n"
		);
		final Mogram next = file.components().iterator().next();
		return ((TaraMogram) next).getSignature().getParameters();
	}

	private String buildParameters(String[] names) {
		StringBuilder parms = new StringBuilder();
		String separator = ", ";
		for (String name : names) parms.append(separator).append(name);
		return parms.substring(separator.length());
	}

	@Override
	public Parameters createExplicitParameters(Map<String, String> parameters) {
		String assigns = buildExplicitParameters(parameters);
		final TaraModelImpl file = createDummyFile(
				"Concept(" + assigns + ")" + " DummyInstance\n"
		);
		if (file.components().isEmpty()) return null;
		return ((TaraMogram) file.components().get(0)).getSignature().getParameters();
	}

	@Override
	public PsiElement createParameterSeparator() {
		final TaraModelImpl file = createDummyFile(
				"Dummy(" + "x = 1, y = 2" + ")" + " DummyInstance\n"
		);
		final TaraMogram mogram = (TaraMogram) file.components().iterator().next();
		return mogram.getSignature().getParameters().getNode().getChildren(TokenSet.create(TaraTypes.COMMA))[0].getPsi();
	}

	private String buildExplicitParameters(Map<String, String> parameters) {
		StringBuilder params = new StringBuilder();
		String separator = ", ";
		for (Map.Entry<String, String> name : parameters.entrySet())
			params.append(separator).append(name.getKey()).append(" = ").append(name.getValue());
		return params.substring(separator.length());
	}

	@Override
	public TaraFlag createFlag(String flag) {
		final TaraModelImpl file = createDummyFile(
				DUMMY_CONCEPT + " is " + flag + "\n"
		);
		Mogram mogram = file.components().iterator().next();
		return (TaraFlag) ((TaraMogram) mogram).getSignature().getFlags().getFlagList().get(0);
	}

	@Override
	public TaraFlags createFlags(String annotation) {
		final TaraModelImpl file = createDummyFile(DUMMY_CONCEPT + " is " + annotation + "\n");
		Mogram mogram = file.components().iterator().next();
		return (TaraFlags) ((TaraMogram) mogram).getSignature().getFlags();
	}

	@Override
	public PsiElement createNewLineIndent() {
		final TaraModelImpl file = createDummyFile(DUMMY_CONCEPT + "\n\t\tDummy DummyInstance2");
		TaraMogram mogram = (TaraMogram) file.components().iterator().next();
		return mogram.getBody().getFirstChild();
	}

	@Override
	public PsiElement createNewLineIndent(int level) {
		StringBuilder indents = new StringBuilder();
		for (int i = 0; i < level; i++)
			indents.append("\t");
		final TaraModelImpl file = createDummyFile(DUMMY_CONCEPT + "\n" + indents.toString() + DUMMY_CONCEPT);
		TaraMogram mogram = (TaraMogram) file.components().iterator().next();
		return mogram.getBody().getFirstChild();
	}

	@Override
	public PsiElement createInlineNewLineIndent() {
		final TaraModelImpl file = createDummyFile(DUMMY_CONCEPT + " > Dummy DummyInstance2");
		TaraMogram mogram = (TaraMogram) file.components().iterator().next();
		return mogram.getBody().getFirstChild();
	}

	@Override
	public PsiElement createDedent(int level) {
		StringBuilder indents = new StringBuilder();
		for (int i = 0; i < level; i++) indents.append("\t");
		final TaraModelImpl file = createDummyFile(DUMMY_CONCEPT + "\n" + indents.toString() + DUMMY_CONCEPT + "2\n" + indents.toString().substring(1) + DUMMY_CONCEPT + 3);
		TaraMogram mogram = (TaraMogram) file.components().iterator().next();
		return mogram.getBody().getFirstChild();
	}

	@Override
	public PsiElement createWhiteSpace() {
		final TaraModelImpl file = createDummyFile(" " + DUMMY_CONCEPT + " > Dummy DummyInstance2");
		return file.getFirstChild();
	}

	public PsiElement createBodyNewLine() {
		final TaraModelImpl file = createDummyFile(DUMMY_CONCEPT + "\n" +
				"\t" + DUMMY_CONCEPT + "2\n" +
				"\t" + DUMMY_CONCEPT + "3");
		Mogram mogram = file.components().iterator().next();
		LeafPsiElement[] childrenOfType = PsiTreeUtil.getChildrenOfType(((TaraMogram) mogram).getBody(), LeafPsiElement.class);
		return childrenOfType != null ? childrenOfType[1] : null;
	}

	public PsiElement createBodyNewLine(int level) {
		StringBuilder indents = new StringBuilder();
		for (int i = 0; i < level; i++)
			indents.append("\t");
		final TaraModelImpl file = createDummyFile(DUMMY_CONCEPT + "\n" +
				indents + DUMMY_CONCEPT + "2\n" +
				indents + DUMMY_CONCEPT + "3");
		Mogram mogram = file.components().iterator().next();
		LeafPsiElement[] childrenOfType = PsiTreeUtil.getChildrenOfType(((TaraMogram) mogram).getBody(), LeafPsiElement.class);
		return childrenOfType != null ? childrenOfType[1] : null;
	}

	public PsiElement createInlineNewLine() {
		final TaraModelImpl file = createDummyFile(DUMMY_CONCEPT + " >" + DUMMY_CONCEPT + "2; Dummy DummyInstance3");
		Mogram mogram = file.components().iterator().next();
		LeafPsiElement[] childrenOfType = PsiTreeUtil.getChildrenOfType(((TaraMogram) mogram).getBody(), LeafPsiElement.class);
		return childrenOfType != null ? childrenOfType[1] : null;
	}

	public TaraExpression createExpression(String text) {
		char quote = '\'';
		final TaraModelImpl file = createDummyFile(
				CONCEPT_DUMMY + "\n" +
						"\tvar function:Dummy dummy " + "= " + quote + formatted(text) + quote + "\n");
		Mogram mogram = PsiTreeUtil.getChildOfType(file, TaraMogram.class);
		if (mogram == null) return null;
		Body body = ((TaraMogram) mogram).getBody();
		if (body == null) return null;
		Variable variable = (Variable) body.getFirstChild().getNextSibling();
		return ((Valued) variable).getValue().getExpressionList().get(0);
	}

	public PsiElement createMultiLineExpression(String text, String oldIndent, String newIndent, String quote) {
		final TaraModelImpl file = text.trim().startsWith("--") ?
				createDummyFile(CONCEPT_DUMMY + "\n" +
						"\tvar function:Dummy dummy " + "\n" +
						"\t" + formatted(text, oldIndent, newIndent).trim() + "\n") :
				createDummyFile(
						CONCEPT_DUMMY + "\n" +
								"\tvar function:Dummy dummy " + "\n" +
								"\t\t" + quote + "\n" + newIndent + formatted(text, oldIndent, newIndent) + '\n' + newIndent + quote + "\n");
		TaraMogram mogram = PsiTreeUtil.getChildOfType(file, TaraMogram.class);
		if (mogram == null) return null;
		Body body = mogram.getBody();
		if (body == null) return null;
		Variable variable = (Variable) body.getFirstChild().getNextSibling();
		return ((Valued) variable).getBodyValue().getExpression();
	}

	@Override
	public Parameter createVarInit(String name, String value) {
		final TaraModelImpl file = createDummyFile(
				CONCEPT_DUMMY + "\n" +
						"\t" + name + " = " + value + "\n" +
						"\t" + CONCEPT_DUMMY + "2\n"
		);
		Body body = PsiTreeUtil.getChildOfType(file, TaraMogram.class).getBody();
		return body != null ? (TaraVarInit) body.getFirstChild().getNextSibling() : null;
	}

	@Override
	public TaraMetric createMetric(String value) {
		final TaraModelImpl file = createDummyFile(
				"Dummy(value = 12 " + value + ")" + " DummyInstance\n"
		);
		final Mogram next = file.components().iterator().next();
		return ((TaraParameter) ((TaraMogram) next).getSignature().getParameters().getParameters().get(0)).getValue().getMetric();
	}

	@Override
	public TaraMethodReference createMethodReference(String reference) {
		final TaraModelImpl file = createDummyFile(
				CONCEPT_DUMMY + "\n" +
						"\tvalue = @" + reference + "\n" +
						"\t" + CONCEPT_DUMMY + "2\n"
		);
		Body body = PsiTreeUtil.getChildOfType(file, TaraMogram.class).getBody();
		return body != null ? ((TaraVarInit) body.getFirstChild().getNextSibling()).getValue().getMethodReferenceList().get(0) : null;
	}

	@Override
	public TaraValue createTaraValue(List<?> objects) {
		List<String> array = objects.stream().map(o -> "\"" + o.toString() + "\"").toList();
		final TaraVarInit sample = (TaraVarInit) createVarInit("sample", String.join(" ", array));
		return sample.getValue();
	}

	private String formatted(String text) {
		return text.replace("\n", "\\n").replace("'", "\'");
	}

	private String formatted(String text, String oldIndent, String indent) {
		text = text.replace("    ", "\t");
		return text.replaceAll("(\n|\r\n)" + oldIndent, "\n" + indent);
	}

}
