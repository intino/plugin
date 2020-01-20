package io.intino.plugin.findusage;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.NotNull;

public class TaraFileFindUsagesHandler extends FindUsagesHandler {
	private final TaraModel boxFile;

	public TaraFileFindUsagesHandler(TaraModel boxFile) {
		super(boxFile);
		this.boxFile = boxFile;
	}

	@NotNull
	@Override
	public PsiElement[] getPrimaryElements() {
		return new PsiElement[]{boxFile};
	}

	@NotNull
	@Override
	public PsiElement[] getSecondaryElements() {
		return PsiElement.EMPTY_ARRAY;
	}


	@Override
	protected boolean isSearchForTextOccurrencesAvailable(@NotNull PsiElement psiElement, boolean isSingleFile) {
		return true;
	}
}
