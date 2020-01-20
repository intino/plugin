package io.intino.plugin.lang.psi;

import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Annotations extends Navigatable {

	@NotNull
	<T extends Annotation> List<T> getAnnotationList();

}
