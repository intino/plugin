package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.semantics.Constraint;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class AddRequiredAspectFix implements IntentionAction {

	private final Node node;

	public AddRequiredAspectFix(PsiElement element) {
		this.node = element instanceof Node ? (Node) element : (Node) TaraPsiUtil.getContainerOf(element);
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Add required facet";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		return file.isValid();
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		List<Constraint.Aspect> requires = findConstraints().stream().
				filter(constraint -> constraint instanceof Constraint.Aspect && ((Constraint.Aspect) constraint).isRequired()).
				map(constraint -> (Constraint.Aspect) constraint).collect(Collectors.toList());
		filterPresentFacets(requires);
		for (Constraint.Aspect require : requires) {
			((TaraNode) node).applyAspect(require.type());
		}
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private List<Constraint> findConstraints() {
		final List<Constraint> constraintsOf = new ArrayList<>(Objects.requireNonNull(IntinoUtil.constraintsOf(node)));
		List<Constraint> facetConstraints = new ArrayList<>();
		final List<String> facets = facetTypes(node);
		constraintsOf.stream().
				filter(c -> c instanceof Constraint.Aspect && facets.contains(((Constraint.Aspect) c).type())).
				forEach(c -> facetConstraints.addAll(((Constraint.Aspect) c).constraints()));
		constraintsOf.addAll(facetConstraints);
		return constraintsOf;
	}

	private List<String> facetTypes(Node node) {
		return node.appliedAspects().stream().map(io.intino.magritte.lang.model.Aspect::type).collect(Collectors.toList());
	}

	private void filterPresentFacets(List<Constraint.Aspect> requires) {
		for (io.intino.magritte.lang.model.Aspect aspect : node.appliedAspects()) {
			Constraint.Aspect require = findInConstraints(requires, aspect.type());
			if (require != null) requires.remove(require);
		}
	}

	@Nullable
	private Constraint.Aspect findInConstraints(List<Constraint.Aspect> constraints, String name) {
		for (Constraint.Aspect require : constraints) if (require.type().equals(name)) return require;
		return null;
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}
}
