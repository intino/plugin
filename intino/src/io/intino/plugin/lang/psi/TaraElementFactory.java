package io.intino.plugin.lang.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.impl.TaraModelImpl;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Primitive;

import java.util.List;
import java.util.Map;

public abstract class TaraElementFactory {

	public static TaraElementFactory getInstance(Project project) {
		return project.getService(TaraElementFactory.class);
	}

	public abstract Mogram createNode(String name);

	public abstract Mogram createFullMogram(String code);

	public abstract TaraMogram createNode(String name, String type);

	public abstract TaraModelImpl createDummyFile(String text);

	public abstract MetaIdentifier createMetaIdentifier(String type);

	public abstract Identifier createNameIdentifier(String name);

	public abstract TaraVariable createVariable(String name, Primitive type);

	public abstract TaraFacetApply createFacetApply(String type);

	public abstract TaraFacets createFacets(String type);

	public abstract TaraImports createImport(String reference);

	public abstract TaraDslDeclaration createDslDeclaration(String name);

	public abstract PsiElement createNewLine();

	public abstract PsiElement createEmptyParameters();

	public abstract Parameters createParameters(boolean string);

	public abstract Parameters createParameters(String... string);

	public abstract Parameters createExplicitParameters(Map<String, String> parameters);

	public abstract PsiElement createParameterSeparator();

	public abstract TaraVariable createWord(String name, String[] types);

	public abstract TaraVariable createResource(String name, String types);

	public abstract PsiElement createMetaWordIdentifier(String module, String node, String name);

	public abstract TaraFlag createFlag(String name);

	public abstract TaraFlags createFlags(String name);

	public abstract PsiElement createNewLineIndent();

	public abstract PsiElement createNewLineIndent(int level);

	public abstract PsiElement createInlineNewLineIndent();

	public abstract PsiElement createDedent(int i);

	public abstract PsiElement createWhiteSpace();

	public abstract PsiElement createBodyNewLine();

	public abstract PsiElement createBodyNewLine(int level);

	public abstract PsiElement createInlineNewLine();

	public abstract TaraExpression createExpression(String text);

	public abstract PsiElement createMultiLineExpression(String text, String oldIndent, String indent, String quote);

	public abstract Parameter createVarInit(String name, String value);

	public abstract TaraMetric createMetric(String value);

	public abstract TaraMethodReference createMethodReference(String reference);

	public abstract TaraValue createTaraValue(List<?> objects);
}