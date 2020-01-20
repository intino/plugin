package io.intino.plugin.lang.psi.resolve;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import io.intino.plugin.lang.psi.MetaIdentifier;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.Language;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.semantics.Documentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class TaraMetaReferenceSolver extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

	public TaraMetaReferenceSolver(MetaIdentifier metaIdentifier, TextRange textRange) {
		super(metaIdentifier, textRange);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode) {
		List<ResolveResult> results = new ArrayList<>();
		final PsiElement destiny = findDestiny();
		if (destiny != null) results.add(new PsiElementResolveResult(destiny));
		return results.toArray(new ResolveResult[results.size()]);
	}

	@Nullable
	private PsiElement findDestiny() {
		Language language = TaraUtil.getLanguage(myElement);
		final Node node = TaraPsiUtil.getContainerNodeOf(myElement);
		if (language == null || node == null) return null;
		final Documentation doc = language.doc(node.resolve().type());
		if (doc == null) return null;
		PsiFile file = findFile(doc.file());
		if (file == null) return null;
		return (PsiElement) searchNodeIn(TaraUtil.getAllNodesOfFile((TaraModel) file), node);
	}

	private Node searchNodeIn(List<Node> nodes, Node instance) {
		if (nodes.isEmpty()) return null;
		final Document document = PsiDocumentManager.getInstance(myElement.getProject()).getDocument(((PsiElement) nodes.get(0)).getContainingFile());
		if (document == null) return null;
		for (Node node : nodes) {
			if (node != null && instance.type().equals(node.qualifiedName()))
				return node;
		}
		return null;
	}

	@Nullable
	private PsiFile findFile(String file) {
		try {
			final VirtualFile virtualFile = VfsUtil.findFileByURL(new File(file).toURI().toURL());
			if (virtualFile == null) return null;
			return PsiManager.getInstance(myElement.getProject()).findFile(virtualFile);
		} catch (MalformedURLException ignored) {
			return null;
		}
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		ResolveResult[] resolveResults = multiResolve(false);
		return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		return new Object[0];
	}
}
