package io.intino.plugin.lang.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Flags extends TaraPsiElement {


	@NotNull
	<T extends Flag> List<T> getFlagList();

	List<String> asStringList();
}
