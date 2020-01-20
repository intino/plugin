package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.tree.TokenSet;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Primitive;
import io.intino.tara.lang.model.Tag;
import io.intino.tara.lang.model.Variable;
import io.intino.tara.lang.model.rules.Size;
import io.intino.tara.lang.model.rules.variable.VariableRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class VariableMixin extends ASTWrapperPsiElement {

	private Set<Tag> inheritedFlags = new HashSet<>();

	public VariableMixin(@NotNull ASTNode node) {
		super(node);
	}

	public void name(String newName) {
		setName(newName);
	}

	@NotNull
	public PsiElement setName(String newName) {
		ASTNode keyNode = getNode().findChildByType(TaraTypes.IDENTIFIER);
		if (keyNode != null) {
			TaraVariable variable = TaraElementFactoryImpl.getInstance(this.getProject()).createVariable(newName, type());
			ASTNode node = variable.getIdentifier().getNode().copyElement();
			this.getNode().replaceChild(keyNode, node);
		}
		return this;
	}

	public Rule getRule() {
		TaraRuleContainer attributeType = ((TaraVariable) this).getRuleContainer();
		if (attributeType == null) return null;
		return attributeType.getRule();
	}

	public VariableRule rule() {
		final Primitive type = type();
		return this.getRule() != null ? RuleFactory.createRule((TaraVariable) this) : (type != null ? type.defaultRule() : null);
	}

	public void rule(VariableRule rule) {
	}

	public String scope() {
		final Configuration conf = TaraUtil.configurationOf(this);
		return conf != null ? conf.artifact().model().outLanguage() : "";
	}

	@Nullable
	public String name() {
		ASTNode[] child = this.getNode().getChildren(TokenSet.create(TaraTypes.IDENTIFIER));
		if (child.length == 0) return null;
		return child[0].getText();
	}

	@Nullable
	public String getName() {
		return name();
	}

	@Nullable
	public Primitive type() {
		if (isReference()) return Primitive.REFERENCE;
		TaraVariableType type = ((TaraVariable) this).getVariableType();
		return type == null ? null : Primitive.value(type.getText());
	}

	public boolean isReference() {
		TaraVariableType type = ((TaraVariable) this).getVariableType();
		return type != null && type.getIdentifierReference() != null;
	}

	public boolean isMultiple() {
		return size().max() > 1;
	}

	public void size(Size size) {
	}

	public Size size() {
		final TaraSizeRange sizeRange = ((TaraVariable) this).getSizeRange();
		if (sizeRange == null) return new Size(1, 1);
		if (sizeRange.getSize() == null) return new Size(1, Integer.MAX_VALUE);
		return parseRange(sizeRange.getSize());
	}

	private Size parseRange(TaraSize size) {
		final TaraListRange range = size.getListRange();
		if (range != null)
			return new Size(Integer.parseInt(range.getChildren()[0].getText()), Integer.parseInt(range.getChildren()[range.getChildren().length - 1].getText()));
		final int minMax = Integer.parseInt(size.getText());
		return new Size(minMax, minMax);
	}

	public boolean isOverriden() {
		return TaraUtil.getOverriddenVariable((Variable) this) != null;
	}

	public List<Tag> flags() {
		List<Tag> tags = new ArrayList<>();
		tags.addAll(inheritedFlags);
		if (((TaraVariable) this).getFlags() != null)
			tags.addAll(((TaraVariable) this).getFlags().getFlagList().stream().
					map(f -> Tag.valueOf(Format.firstUpperCase().format(f.getText()).toString())).collect(Collectors.toList()));
		return Collections.unmodifiableList(tags);
	}

	public void addFlags(Tag... flags) {
		Collections.addAll(inheritedFlags, flags);
	}

	private String extractFields(PsiClass psiClass) {
		String fields = "";
		for (PsiField psiField : psiClass.getFields())
			if (psiField instanceof PsiEnumConstant) fields += ", " + psiField.getNameIdentifier().getText();
		return fields.isEmpty() ? "" : fields.substring(2);
	}

	public Node container() {
		return TaraPsiUtil.getContainerNodeOf(this);
	}

	public void container(Node container) {
	}

	public Node destinyOfReference() {
		if (!isReference()) return null;
		TaraVariableType type = ((TaraVariable) this).getVariableType();
		if (type == null || type.getIdentifierReference() == null) return null;
		return ReferenceManager.resolveToNode(type.getIdentifierReference());
	}

	public void type(Primitive type) {
	}

	public boolean isTerminal() {
		return flags().contains(Tag.Terminal);
	}

	public boolean isFinal() {
		return flags().contains(Tag.Final);
	}

	public boolean isPrivate() {
		return flags().contains(Tag.Private);
	}

	public boolean isInherited() {
		return false;
	}

	public List<Object> values() {
		Value value = ((Valued) this).getValue();
		Value bodyValue = ((TaraVariable) this).getBodyValue();
		if (value == null && bodyValue == null) return Collections.emptyList();
		else if (bodyValue != null) return Value.makeUp(bodyValue.values(), type(), this);
		else return Value.makeUp(value.values(), type(), this);
	}

	public String defaultMetric() {
		final TaraValue value = ((TaraVariable) this).getValue();
		if (value == null) return "";
		TaraMetric metric = value.getMetric();
		return metric != null ? metric.getText() : "";
	}

	public void defaultMetric(String defaultExtension) {
	}

	public void overriden(boolean overriden) {

	}

	public String getUID() {
		return null;
	}

	public String file() {
		return this.getContainingFile().getVirtualFile().getPath();
	}

	@Override
	public String toString() {
		return type() + " " + name();
	}
}
