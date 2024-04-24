package org.jetbrains.jps.intino.model.impl;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

import java.util.ArrayList;
import java.util.List;

public class JpsModuleConfiguration extends JpsElementBase<JpsModuleConfiguration> implements JpsElement {
	static final JpsElementChildRole<JpsModuleConfiguration> ROLE = JpsElementChildRoleBase.create("tara-conf");


	public String groupId = "";
	public String artifactId = "";
	public String version = "1.0.0";
	public List<Dsl> dsls = new ArrayList<>();
	public String parameters = "";
	public String parentInterface = "";
	public String datahub = "";
	public String archetype = "";
	public String dependencies = "";

	public record Dsl(String name, String version, String level, String generationPackage, Builder builder,
					  Runtime runtime, OutDsl outDsl) {
		public record Builder(String groupId, String artifactId, String version, String generationPackage,
							  List<Integer> excludedPhases) {
		}

		public record Runtime(String groupId, String artifactId, String version) {
		}

		public record OutDsl(String name, Builder builder, Runtime runtime) {
		}
	}

	@NotNull
	@Override
	public JpsModuleConfiguration createCopy() {
		return new JpsModuleConfiguration();
	}

	@Override
	public void applyChanges(@NotNull JpsModuleConfiguration modified) {

	}

}