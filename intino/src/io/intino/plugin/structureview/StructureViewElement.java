package io.intino.plugin.structureview;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class StructureViewElement implements StructureViewTreeElement {

	private final TaraNode node;

	StructureViewElement(Node taraNode) {
		this.node = (TaraNode) taraNode;
	}

	@Override
	public Object getValue() {
		return node;
	}

	public void navigate(boolean requestFocus) {
		node.navigate(requestFocus);
	}

	public boolean canNavigate() {
		return node.canNavigate();
	}

	public boolean canNavigateToSource() {
		return node.canNavigateToSource();
	}


	@NotNull
	@Override
	public TreeElement[] getChildren() {
		if (node != null) {
			Collection<Node> nodes = IntinoUtil.getComponentsOf(node);
			if (!nodes.isEmpty()) {
				List<TreeElement> treeElements = new ArrayList<>(nodes.size());
				treeElements.addAll(nodes.stream().filter(n -> !n.isReference()).map(StructureViewElement::new).collect(Collectors.toList()));
				return treeElements.toArray(new TreeElement[treeElements.size()]);
			}
		}
		return EMPTY_ARRAY;
	}

	@NotNull
	public ItemPresentation getPresentation() {
		return new ItemPresentation() {
			public String getPresentableText() {
				return node.name() == null ? "Anonymous" : node.name();
			}

			public String getLocationString() {
				return null;
			}

			public Icon getIcon(boolean open) {
				return node.getIcon(0);
			}
		};
	}

}
