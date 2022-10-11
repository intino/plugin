package io.intino.plugin.codeinsight.intentions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.codeinsight.annotators.fix.ClassCreationIntention;
import io.intino.plugin.codeinsight.languageinjection.imports.Imports;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.intino.plugin.codeinsight.languageinjection.helpers.QualifiedNameFormatter.qnOf;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.importsFile;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerNodeOf;

public class ConvertToMethodReference extends ClassCreationIntention {

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		final Valued valued = TaraPsiUtil.getContainerByType(element, Valued.class);
		return valued != null && expressionContext(valued) != null && valued.name() != null && !valued.name().isEmpty();
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final Node node = getContainerNodeOf(element);
		if (node == null) return;
		final Valued valued = TaraPsiUtil.getContainerByType(element, Valued.class);
		if (valued == null) return;
		final String name = valued.name();
		final TaraMethodReference methodReference = TaraElementFactory.getInstance(valued.getProject()).createMethodReference(name);
		new MethodReferenceCreator(valued, name).create(expressionContext(valued).getValue());
		substitute(methodReference, valued);
		removeOldImports(valued);
		PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		PsiDocumentManager.getInstance(project).commitAllDocuments();
//		MemberInplaceRenameHandler handler = new MemberInplaceRenameHandler();
//		if (substitute != null)
//			handler.doRename(substitute.getMethodReferenceList().get(0).getIdentifierReference().getIdentifierList().get(0), editor, null);
	}

	private void removeOldImports(Valued valued) {
		Imports imports = new Imports(valued.getProject());
		imports.save(importsFile(valued), qnOf(valued), Collections.emptySet());
	}

	private PsiElement substitute(TaraMethodReference methodReference, Valued valued) {
		if (valued.getBodyValue() != null) {
			valued.getBodyValue().delete();
			return valued.getLastChild() instanceof TaraFlags ? add(methodReference, valued, findName(valued)) : addToTheEnd(methodReference, valued);
		} else if (valued.getValue() != null) return valued.getValue().replace(methodReference.getParent().copy());
		return null;
	}

	private Identifier findName(Valued valued) {
		final List<Identifier> childrenOfType = new ArrayList<>(PsiTreeUtil.findChildrenOfType(valued, Identifier.class));
		return childrenOfType.get(childrenOfType.size() - 1);
	}

	private PsiElement addToTheEnd(TaraMethodReference methodReference, PsiElement valued) {
		valued.add(methodReference.getParent().getPrevSibling().copy());
		valued.add(methodReference.getParent().getPrevSibling().getPrevSibling().copy());
		valued.add(methodReference.getParent().getPrevSibling().copy());
		return valued.add(methodReference);
	}

	private PsiElement add(TaraMethodReference methodReference, Valued valued, PsiElement anchor) {
		valued.addAfter(methodReference.getParent().getPrevSibling().copy(), anchor);
		valued.addAfter(methodReference.getParent().getPrevSibling().getPrevSibling().copy(), anchor.getNextSibling());
		valued.addAfter(methodReference.getParent().getPrevSibling().copy(), anchor.getNextSibling().getNextSibling());
		valued.addAfter(methodReference.getParent().copy(), anchor.getNextSibling().getNextSibling().getNextSibling());
		return methodReference;
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Convert to method reference";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	private Expression expressionContext(@NotNull Valued element) {
		if (element.getValue() == null) return null;
		final List<TaraExpression> expressionList = element.getValue().getExpressionList();
		return expressionList.isEmpty() ? null : expressionList.get(0);
	}
}
