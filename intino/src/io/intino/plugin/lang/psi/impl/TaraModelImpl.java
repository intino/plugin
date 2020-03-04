package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.impl.source.tree.ChangeUtil;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.Language;
import io.intino.magritte.Resolver;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Rule;
import io.intino.magritte.lang.model.rules.Size;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

public class TaraModelImpl extends PsiFileBase implements TaraModel {

	public TaraModelImpl(@NotNull FileViewProvider viewProvider) {
		super(viewProvider, TaraLanguage.INSTANCE);
	}

	@NotNull
	public FileType getFileType() {
		VirtualFile vFile = this.getVirtualFile();
		return vFile != null ? vFile.getFileType() : this.getOriginalFile().getVirtualFile().getFileType();
	}

	public String toString() {
		return getPresentableName();
	}

	@NotNull
	public String getPresentableName() {
		return getName().contains(".") ? getName().substring(0, getName().lastIndexOf(".")) : getName();
	}

	public ItemPresentation getPresentation() {
		return new ItemPresentation() {
			public String getPresentableText() {
				return getName().substring(0, getName().lastIndexOf("."));
			}

			public String getLocationString() {
				final PsiDirectory psiDirectory = getParent();
				if (psiDirectory != null) {
					return psiDirectory.getVirtualFile().getPresentableUrl();
				}
				return null;
			}

			public Icon getIcon(final boolean open) {
				return getFileType().getIcon();
			}
		};
	}

	@Nullable
	public Icon getIcon(int flags) {
		return getFileType().getIcon();
	}

	public Node container() {
		return null;
	}

	@Override
	public boolean isMetaAspect() {
		return false;
	}

	@Override
	public List<String> uses() {
		return getImports().stream().map(anImport -> anImport.getHeaderReference().toString()).collect(Collectors.toList());
	}

	@NotNull
	@Override
	public List<Node> components() {
		return IntinoUtil.getMainNodesOfFile(this);
	}

	public Node addNode(String identifier) {
		return (Node) addNode(TaraElementFactory.getInstance(getProject()).createNode(identifier));
	}

	public Import addImport(String reference) {
		TaraImports imports = TaraElementFactory.getInstance(getProject()).createImport(reference);
		return (Import) addImport(imports);
	}

	private PsiElement addImport(TaraImports imports) {
		final TreeElement copy = ChangeUtil.copyToElement(imports);
		TaraImports psi = (TaraImports) copy.getPsi();
		return this.getImports().isEmpty() ? addTaraImport(psi) : addImportToList(psi);
	}

	private TaraAnImport addTaraImport(TaraImports psi) {
		TaraAnImport anImport = ((TaraImports) this.addBefore(psi, findImportAnchor())).getAnImportList().get(0);
		anImport.add(TaraElementFactoryImpl.getInstance(psi.getProject()).createNewLine());
		return anImport;
	}

	private PsiElement findImportAnchor() {
		for (PsiElement psiElement : this.getChildren())
			if (psiElement.getNode().getElementType().equals(TaraTypes.NEWLINE)) return psiElement.getNextSibling();
		return this.getLastChild();
	}

	private Import addImportToList(TaraImports psi) {
		TaraImports[] imports = PsiTreeUtil.getChildrenOfType(this, TaraImports.class);
		if (imports == null || imports.length == 0) return null;
		return (Import) imports[0].addBefore(psi.getAnImportList().get(0), imports[0].getAnImportList().get(0));
	}

	public void updateDSL(String dsl) {
		setDSL(dsl == null || dsl.isEmpty() ? null : dsl);
	}

	private void setDSL(String dslName) {
		TaraDslDeclaration dslDeclaration = getDSLDeclaration();
		if (dslName != null && !dslName.isEmpty()) {
			TaraDslDeclaration dsl = TaraElementFactory.getInstance(getProject()).createDslDeclaration(dslName);
			if (dslDeclaration != null) dslDeclaration.replace(dsl.copy());
			else this.addBefore(dsl, getFirstChild());
		}
	}

	@Nullable
	public TaraDslDeclaration getDSLDeclaration() {
		TaraDslDeclaration[] childrenOfType = PsiTreeUtil.getChildrenOfType(this, TaraDslDeclaration.class);
		return childrenOfType != null && childrenOfType.length > 0 ? childrenOfType[0] : null;
	}


	public TaraModelImpl getFile() throws PsiInvalidElementAccessException {
		return this;
	}

	@Override
	public String doc() {
		return "";
	}

	public String dsl() {
		TaraDslDeclaration dslDeclaration = getDSLDeclaration();
		if (dslDeclaration == null) return null;
		return dslDeclaration.getHeaderReference().getText();
	}

	@NotNull
	public List<Import> getImports() {
		TaraImports[] taraImports = PsiTreeUtil.getChildrenOfType(this, TaraImports.class);
		if (taraImports == null) return Collections.EMPTY_LIST;
		Import[] imports = PsiTreeUtil.getChildrenOfType(taraImports[0], TaraAnImport.class);
		return imports != null ? unmodifiableList(Arrays.asList(imports)) : Collections.EMPTY_LIST;
	}

	private void insertLineBreakBefore(final ASTNode anchorBefore) {
		getNode().addChild(ASTFactory.whitespace("\n"), anchorBefore);
	}

	private boolean haveToAddNewLine() {
		ASTNode lastChild = getNode().getLastChildNode();
		return lastChild != null && !lastChild.getText().endsWith("\n");
	}


	@NotNull
	public PsiElement addNode(@NotNull Node node) throws IncorrectOperationException {
		if (haveToAddNewLine()) insertLineBreakBefore(null);
		final TreeElement copy = ChangeUtil.copyToElement((PsiElement) node);
		getNode().addChild(copy);
		return copy.getPsi();
	}

	public String type() {
		return "";
	}

	@Override
	public List<Rule> rulesOf(Node component) {
		final List<Node> components = components();
		final TaraNode node = (TaraNode) components.get(components.indexOf(component));
		final List<TaraRuleContainer> ruleContainerList = node.getSignature().getRuleContainerList();
		if (ruleContainerList.isEmpty() || ruleContainerList.get(0) == null) return singletonList(Size.MULTIPLE());
		return ruleContainerList.stream().map(ruleContainer -> createSize(ruleContainer.getRule())).collect(Collectors.toList());

	}

	private Rule createSize(TaraRule rule) {
		if (rule == null) Size.MULTIPLE();
		final TaraRange range = rule.getRange();
		if (!rule.isLambda() || range == null) return Size.MULTIPLE();
		return new Size(min(range), max(range));
	}

	private int min(TaraRange range) {
		final PsiElement psiElement = range.getFirstChild();
		if (psiElement.getNode().getElementType().equals(TaraTypes.STAR)) return Integer.MIN_VALUE;
		return Integer.parseInt(psiElement.getText());
	}

	private int max(TaraRange range) {
		final PsiElement psiElement = range.getLastChild();
		if (psiElement.getNode().getElementType().equals(TaraTypes.STAR)) return Integer.MAX_VALUE;
		return Integer.parseInt(psiElement.getText());
	}

	public <T extends Node> boolean contains(T node) {
		return components().contains(node);
	}


	public String simpleType() {
		return "";
	}

	@Override
	public void stashNodeName(String name) {

	}


	public void name(String name) {
	}

	@Override
	public Language language() {
		return IntinoUtil.getLanguage(this.getOriginalElement());
	}

	@NotNull
	public Node resolve() {
		Language language = IntinoUtil.getLanguage(this.getOriginalElement());
		if (language == null) return this;
		new Resolver(language).resolve(this);
		return this;
	}

	@Override
	public String file() {
		return this.getContainingFile().getVirtualFile().getPath();
	}
}