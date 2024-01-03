package io.intino.plugin.structureview;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.navigation.ItemPresentation;
import io.intino.plugin.lang.psi.impl.TaraModelImpl;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class FileStructureViewElement extends PsiTreeElementBase<TaraModelImpl> {

	FileStructureViewElement(TaraModelImpl taraModel) {
		super(taraModel);
	}

	@NotNull
	public Collection<StructureViewTreeElement> getChildrenBase() {
		if (getElement() == null) return Collections.emptyList();
		List<Mogram> nodes = getElement().components().stream().filter(mogram -> !mogram.isReference()).toList();
		List<StructureViewTreeElement> elements = new ArrayList<>(1);
		elements.addAll(nodes.stream().map(StructureViewElement::new).toList());
		return elements;
	}

	public String getPresentableText() {
		return getElement().getPresentableName();
	}

	@NotNull
	public ItemPresentation getPresentation() {
		return new ItemPresentation() {
			public String getPresentableText() {
				return FileStructureViewElement.this.getPresentableText();
			}

			public String getLocationString() {
				return null;
			}

			public Icon getIcon(boolean open) {
				return getElement().getIcon(0);
			}
		};
	}
}