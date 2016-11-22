package io.intino.legio.plugin.build;

import java.util.Arrays;
import java.util.List;

public enum LifeCyclePhase {
	PACKAGE("packaging", "packaged", "clean", "package"),
	INSTALL("installing", "installed", "clean", "package", "install"),
	DISTRIBUTE("distributing", "distributed", "clean", "package", "install", "deploy"),
	PREDEPLOY("deploying on pre", "pre deployed", "clean", "package", "install", "deploy"),
	DEPLOY("deploying", "deployed", "clean", "package", "install", "deploy");

	private final String gerund;
	private final String participle;
	private final List<String> mavenActions;


	public List<String> mavenActions() {
		return mavenActions;
	}

	LifeCyclePhase(String gerund, String participle, String... mavenActions) {
		this.gerund = gerund;
		this.participle = participle;
		this.mavenActions = Arrays.asList(mavenActions);
	}

	public String gerund() {
		return gerund;
	}

	public String participle() {
		return participle;
	}
}
