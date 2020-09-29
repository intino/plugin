package io.intino.plugin.codeinsight.imports;

import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.psi.IdentifierReference;
import io.intino.plugin.lang.psi.Import;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TaraImportOptimizer implements ImportOptimizer {
	@Override
	public boolean supports(PsiFile file) {
		return file instanceof TaraModel;
	}

	@NotNull
	@Override
	public Runnable processFile(final PsiFile file) {
		return () -> {
			if (file instanceof TaraModel) new ImportsOptimizer((TaraModel) file).run();
		};
	}

	private static class ImportsOptimizer {
		private final TaraModel file;
		private final Collection<Import> myImportBlock;

		private ImportsOptimizer(TaraModel file) {
			this.file = file;
			myImportBlock = this.file.getImports();
		}

		public void run() {
			deleteDuplicates();
			deleteUnusedImportStatement();
		}


		private void deleteUnusedImportStatement() {
			Collection<IdentifierReference> identifierReferences = PsiTreeUtil.collectElementsOfType(file, IdentifierReference.class);
			Set<String> neededReferences = new HashSet<>();
			for (IdentifierReference reference : identifierReferences) {
				PsiElement resolve = ReferenceManager.resolve(reference);
				if (resolve != null)
					neededReferences.add(FileUtilRt.getNameWithoutExtension(resolve.getContainingFile().getName()));
			}
			myImportBlock.stream().
					filter(anImport -> !neededReferences.contains(anImport.getHeaderReference().getText())).
					forEach(Import::delete);
		}

		private void deleteDuplicates() {
			Set<Import> set = new HashSet<>();
			myImportBlock.stream().filter(anImport -> isInSet(set, anImport)).forEach(Import::delete);
		}

		private boolean isInSet(Set<Import> set, Import anImport) {
			for (Import anImport1 : set) if (anImport1.getText().equals(anImport.getText())) return true;
			set.add(anImport);
			return false;
		}

	}
}
