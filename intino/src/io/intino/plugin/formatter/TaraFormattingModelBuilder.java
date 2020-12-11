package io.intino.plugin.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import io.intino.magritte.lang.model.NodeContainer;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaraFormattingModelBuilder implements CustomFormattingModelBuilder {

	@Override
	public boolean isEngagedToFormat(PsiElement context) {
		PsiFile file = context.getContainingFile();
		return file != null && file.getLanguage() == TaraLanguage.INSTANCE;
	}

//	@NotNull
//	private FormattingModel createModel(@NotNull PsiElement element, @NotNull CodeStyleSettings settings, @NotNull FormattingMode mode) {
//		settings.AUTODETECT_INDENTS = false;
//		final ASTNode fileNode = element.getContainingFile().getNode();
//		final TaraBlock block = new TaraBlock(fileNode, Alignment.createAlignment(), Indent.getNormalIndent(true), Wrap.createWrap(WrapType.NONE, false), new TaraBlockContext(settings, createSpacingBuilder(settings), mode));
//		return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(), block, settings);
//	}

	private SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
		return new SpacingBuilder(settings.getCommonSettings(TaraLanguage.INSTANCE));
	}

	@Nullable
	@Override
	public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
		final NodeContainer containerByType = TaraPsiUtil.getContainerByType(elementAtOffset.getPsi(), NodeContainer.class);
		return containerByType != null ? ((PsiElement) containerByType).getTextRange() : null;
	}
}