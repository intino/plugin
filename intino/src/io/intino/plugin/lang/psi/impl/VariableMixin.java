package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import io.intino.Configuration;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.Tag;
import io.intino.tara.language.model.Variable;
import io.intino.tara.language.model.rules.Size;
import io.intino.tara.language.model.rules.variable.VariableRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VariableMixin extends ASTWrapperPsiElement {

	private final Set<Tag> inheritedFlags = new HashSet<>();

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
			this.getNode().replaceChild(keyNode, variable.getIdentifier().getNode().copyElement());
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
		final Configuration conf = IntinoUtil.configurationOf(this);
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

	public boolean isOverridden() {
		return IntinoUtil.getOverriddenVariable((Variable) this) != null;
	}

	public List<Tag> flags() {
		List<Tag> tags = new ArrayList<>(inheritedFlags);
		if (((TaraVariable) this).getFlags() != null)
			tags.addAll(((TaraVariable) this).getFlags().getFlagList().stream().
					map(f -> Tag.valueOf(Format.firstUpperCase().format(f.getText()).toString())).toList());
		return Collections.unmodifiableList(tags);
	}

	public void addFlags(Tag... flags) {
		Collections.addAll(inheritedFlags, flags);
	}

	public Mogram container() {
		return TaraPsiUtil.getContainerNodeOf(this);
	}

	public void container(Mogram container) {
	}

	public Mogram targetOfReference() {
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
		else return Value.makeUp(Objects.requireNonNullElse(bodyValue, value).values(), type(), this);
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
