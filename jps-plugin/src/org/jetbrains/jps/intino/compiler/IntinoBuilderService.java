package org.jetbrains.jps.intino.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.jetbrains.jps.intino.compiler.tara.TaraBuilder;

import java.util.List;


public class IntinoBuilderService extends BuilderService {

	@NotNull
	public List<? extends ModuleLevelBuilder> createModuleLevelBuilders() {
		return List.of(new TaraBuilder());
	}
}