package io.intino.plugin.lang.psi;

import com.intellij.pom.Navigatable;
import io.intino.tara.language.model.Parameter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Parameters extends Navigatable, TaraPsiElement {

	@NotNull
	List<Parameter> getParameters();

	boolean areExplicit();

	TaraFacetApply isInFacet();

}
