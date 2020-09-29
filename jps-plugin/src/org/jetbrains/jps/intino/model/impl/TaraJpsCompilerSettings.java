package org.jetbrains.jps.intino.model.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

public class TaraJpsCompilerSettings extends JpsCompositeElementBase<TaraJpsCompilerSettings> {
	public static final JpsElementChildRole<TaraJpsCompilerSettings> ROLE = JpsElementChildRoleBase.create("Tara Compiler Configuration");

	private JpsTaraSettings mySettings;

	public TaraJpsCompilerSettings(@NotNull JpsTaraSettings settings) {
		mySettings = settings;
	}

	@NotNull
	@Override
	public TaraJpsCompilerSettings createCopy() {
		return new TaraJpsCompilerSettings(mySettings);
	}

	@Override
	public void applyChanges(@NotNull TaraJpsCompilerSettings modified) {
		mySettings = modified.mySettings;
	}


}
