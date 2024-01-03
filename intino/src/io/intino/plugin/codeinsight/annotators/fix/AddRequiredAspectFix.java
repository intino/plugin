package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraMogramImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.semantics.Constraint;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class AddRequiredAspectFix implements IntentionAction {

	private final Mogram mogram;

	public AddRequiredAspectFix(PsiElement element) {
		this.mogram = element instanceof Mogram ? (Mogram) element : (Mogram) TaraPsiUtil.getContainerOf(element);
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
		List<Constraint.Facet> requires = findConstraints().stream().
				filter(constraint -> constraint instanceof Constraint.Facet && ((Constraint.Facet) constraint).isRequired()).
				map(constraint -> (Constraint.Facet) constraint).toList();
		filterPresentFacets(requires);
		for (Constraint.Facet require : requires) {
			((TaraMogramImpl) mogram).applyFacet(require.type());
		}
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private List<Constraint> findConstraints() {
		final List<Constraint> constraintsOf = new ArrayList<>(Objects.requireNonNull(IntinoUtil.constraintsOf(mogram)));
		List<Constraint> facetConstraints = new ArrayList<>();
		final List<String> facets = facetTypes(mogram);
		constraintsOf.stream().
				filter(c -> c instanceof Constraint.Facet && facets.contains(((Constraint.Facet) c).type())).
				forEach(c -> facetConstraints.addAll(((Constraint.Facet) c).constraints()));
		constraintsOf.addAll(facetConstraints);
		return constraintsOf;
	}

	private List<String> facetTypes(Mogram node) {
		return node.appliedFacets().stream().map(io.intino.tara.language.model.Facet::type).toList();
	}

	private void filterPresentFacets(List<Constraint.Facet> requires) {
		for (io.intino.tara.language.model.Facet aspect : mogram.appliedFacets()) {
			Constraint.Facet require = findInConstraints(requires, aspect.type());
			if (require != null) requires.remove(require);
		}
	}

	@Nullable
	private Constraint.Facet findInConstraints(List<Constraint.Facet> constraints, String name) {
		for (Constraint.Facet require : constraints) if (require.type().equals(name)) return require;
		return null;
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}
}
