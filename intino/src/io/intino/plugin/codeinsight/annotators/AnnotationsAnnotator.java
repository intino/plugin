package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.plugin.lang.psi.Annotations;
import io.intino.plugin.lang.psi.Flags;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.messages.MessageProvider;
import org.jetbrains.annotations.NotNull;

public class AnnotationsAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (!(element instanceof Annotations) && !(element instanceof Flags)) return;
		final Configuration.Artifact.Dsl.Level level = IntinoUtil.level(element);
		if (level == null) return;
		if ((element instanceof Annotations && (level.isMetaModel() || level.isModel())) || (element instanceof Flags && (level.isModel())))
			holder.newAnnotation(HighlightSeverity.ERROR, MessageProvider.message("reject.annotations.in.level")).range(element).create();
	}
}
