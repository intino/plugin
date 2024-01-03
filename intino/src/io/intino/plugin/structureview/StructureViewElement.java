package io.intino.plugin.structureview;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class StructureViewElement implements StructureViewTreeElement {

	private final TaraMogram mogram;

	StructureViewElement(Mogram taraNode) {
		this.mogram = (TaraMogram) taraNode;
	}

	@Override
	public Object getValue() {
		return mogram;
	}

	public void navigate(boolean requestFocus) {
		mogram.navigate(requestFocus);
	}

	public boolean canNavigate() {
		return mogram.canNavigate();
	}

	public boolean canNavigateToSource() {
		return mogram.canNavigateToSource();
	}


	@NotNull
	@Override
	public TreeElement[] getChildren() {
		if (mogram != null) {
			Collection<Mogram> nodes = IntinoUtil.getComponentsOf(mogram);
			if (!nodes.isEmpty()) {
				List<TreeElement> treeElements = new ArrayList<>(nodes.size());
				treeElements.addAll(nodes.stream().filter(n -> !n.isReference()).map(StructureViewElement::new).toList());
				return treeElements.toArray(new TreeElement[0]);
			}
		}
		return EMPTY_ARRAY;
	}

	@NotNull
	public ItemPresentation getPresentation() {
		return new ItemPresentation() {
			public String getPresentableText() {
				return mogram.name() == null ? "Anonymous" : mogram.name();
			}

			public String getLocationString() {
				return null;
			}

			public Icon getIcon(boolean open) {
				return mogram.getIcon(0);
			}
		};
	}

}
