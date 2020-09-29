package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import io.intino.plugin.lang.psi.TaraFlag;
import io.intino.plugin.lang.psi.TaraFlags;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FlagsMixin extends ASTWrapperPsiElement {

	public FlagsMixin(ASTNode node) {
		super(node);
	}

	public List<String> asStringList() {
		return Collections.unmodifiableList(((TaraFlags) this).getFlagList().stream().map(TaraFlag::getText).collect(Collectors.toList()));
	}
}
