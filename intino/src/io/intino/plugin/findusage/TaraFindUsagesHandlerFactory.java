package io.intino.plugin.findusage;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.lang.psi.Signature;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaraFindUsagesHandlerFactory extends FindUsagesHandlerFactory {

	@Override
	public boolean canFindUsages(@NotNull PsiElement element) {
		return element instanceof Mogram || element instanceof Identifier;
	}

	@Nullable
	@Override
	public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
		if (element instanceof TaraModel) return new TaraFileFindUsagesHandler((TaraModel) element);
		if (element instanceof Identifier && element.getParent() instanceof Signature)
			return new TaraNodeFindUsagesHandler(TaraPsiUtil.getContainerNodeOf(element));
		return null;
	}
}
