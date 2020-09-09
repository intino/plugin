package io.intino.plugin.build;

import java.util.Arrays;
import java.util.List;

public enum FactoryPhase {
	PACKAGE("packaging", "packaged", "clean", "package"),
	INSTALL("installing", "installed", "clean", "install"),
	DISTRIBUTE("distributing", "distributed", "clean", "deploy"),
	DEPLOY("deploying", "deployed", "clean", "install", "deploy"),
	MANAGE("managing", "managed");

	private final String gerund;
	private final String participle;
	private final List<String> mavenActions;


	public List<String> mavenActions() {
		return mavenActions;
	}

	FactoryPhase(String gerund, String participle, String... mavenActions) {
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
