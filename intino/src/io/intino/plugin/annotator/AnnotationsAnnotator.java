package io.intino.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
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
		final Configuration.Artifact.Model.Level level = IntinoUtil.level(element);
		if (level == null) return;
		if ((element instanceof Annotations && (level.isProduct() || level.isSolution())) || (element instanceof Flags && (level.isSolution())))
			holder.createErrorAnnotation(element, MessageProvider.message("reject.annotations.in.level"));
	}
}
