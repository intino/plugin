package org.jetbrains.jps.intino.model.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

public class IntinoJpsCompilerSettings extends JpsCompositeElementBase<IntinoJpsCompilerSettings> {
	public static final JpsElementChildRole<IntinoJpsCompilerSettings> ROLE = JpsElementChildRoleBase.create("Intino Compiler Configuration");

	private JpsIntinoSettings mySettings;

	public IntinoJpsCompilerSettings(@NotNull JpsIntinoSettings settings) {
		mySettings = settings;
	}

	@NotNull
	@Override
	public IntinoJpsCompilerSettings createCopy() {
		return new IntinoJpsCompilerSettings(mySettings);
	}

	@Override
	public void applyChanges(@NotNull IntinoJpsCompilerSettings modified) {
		mySettings = modified.mySettings;
	}


	public int modelMemory() {
		return mySettings.modelMemory;
	}


	public int boxMemory() {
		return mySettings.boxMemory;
	}
}
