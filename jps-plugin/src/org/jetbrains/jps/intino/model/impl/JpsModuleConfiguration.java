package org.jetbrains.jps.intino.model.impl;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

import java.util.Collections;
import java.util.List;

public class JpsModuleConfiguration extends JpsElementBase<JpsModuleConfiguration> implements JpsElement {

	static final JpsElementChildRole<JpsModuleConfiguration> ROLE = JpsElementChildRoleBase.create("tara-conf");

	public String level = "";
	public String language = "";
	public String languageVersion = "";
	public String outDsl = "";
	public List<Integer> excludedPhases = Collections.emptyList();

	public String modelGenerationPackage = "";
	public String boxGenerationPackage = "";

	public String groupId = "";
	public String artifactId = "";
	public String version = "1.0.0";
	public String languageGenerationPackage = "";
	public String parameters = "";
	public String parentInterface = "";
	public String datahub = "";
	public String archetype = "";
	public String dependencies = "";


	@NotNull
	@Override
	public JpsModuleConfiguration createCopy() {
		return new JpsModuleConfiguration();
	}

	@Override
	public void applyChanges(@NotNull JpsModuleConfiguration modified) {

	}

}