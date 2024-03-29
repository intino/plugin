package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.TaraStringValue;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static io.intino.plugin.messages.MessageProvider.message;
import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.WARNING;
import static java.util.Collections.emptyList;

public class ResourceAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (!(element instanceof Valued valued) || ((Valued) element).getValue() == null) return;
		if (Primitive.RESOURCE.equals(typeOf(valued)))
			check(holder, valued.getValue() != null ? valued.getValue().getStringValueList() : emptyList(), resources(element));
	}

	private Primitive typeOf(Valued valued) {
		if (valued instanceof Variable) return valued.type();
		return valued.type();
	}

	private void check(@NotNull AnnotationHolder holder, List<TaraStringValue> values, File resources) {
		values.stream().
				filter(v -> resources == null || !resources.exists() || !new File(resources.getPath(), v.getValue()).exists()).
				forEach(v -> annotateAndFix(holder, Collections.singletonMap(v, new AnnotateAndFix(WARNING, message("warning.resource.not.found")))));
	}

	private File resources(PsiElement element) {
		final VirtualFile resourcesRoot = IntinoUtil.getResourcesRoot(element);
		return resourcesRoot == null ? null : new File(resourcesRoot.getPath());
	}

}
