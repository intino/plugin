package io.intino.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Primitive;
import io.intino.magritte.lang.model.Variable;
import io.intino.plugin.lang.psi.TaraStringValue;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level.WARNING;
import static io.intino.plugin.messages.MessageProvider.message;
import static java.util.Collections.emptyList;

public class ResourceAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (!Valued.class.isInstance(element) || ((Valued) element).getValue() == null) return;
		Valued valued = (Valued) element;
		this.holder = holder;
		if (Primitive.RESOURCE.equals(typeOf(valued)))
			check(valued.getValue() != null ? valued.getValue().getStringValueList() : emptyList(), resources(element));
	}

	private Primitive typeOf(Valued valued) {
		if (valued instanceof Variable) return valued.type();
		return valued.type();
	}

	private void check(List<TaraStringValue> values, File resources) {
		values.stream().
				filter(v -> resources == null || !resources.exists() || !new File(resources.getPath(), v.getValue()).exists()).
				forEach(v -> annotateAndFix(Collections.singletonMap(v, new AnnotateAndFix(WARNING, message("warning.resource.not.found")))));
	}

	private File resources(PsiElement element) {
		final VirtualFile resourcesRoot = TaraUtil.getResourcesRoot(element);
		return resourcesRoot == null ? null : new File(resourcesRoot.getPath());
	}

}
