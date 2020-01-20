package io.intino.plugin;

import io.intino.tara.compiler.shared.Configuration;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

public abstract class PluginLauncher {
	protected Configuration moduleConfiguration;
	protected ModuleStructure moduleStructure;
	protected File moduleDirectory;
	protected SystemProperties systemProperties;
	protected PrintStream log;
	protected Notifier notifier;
	protected Phase invokedPhase;

	public abstract void run();

	public PluginLauncher moduleConfiguration(Configuration moduleConfiguration) {
		this.moduleConfiguration = moduleConfiguration;
		return this;
	}

	public PluginLauncher moduleDirectory(File moduleDirectory) {
		this.moduleDirectory = moduleDirectory;
		return this;
	}

	public PluginLauncher systemProperties(SystemProperties systemProperties) {
		this.systemProperties = systemProperties;
		return this;
	}

	public PluginLauncher moduleStructure(ModuleStructure moduleStructure) {
		this.moduleStructure = moduleStructure;
		return this;
	}

	public PluginLauncher logger(PrintStream log) {
		this.log = log;
		return this;
	}

	public Phase invokedPhase() {
		return invokedPhase;
	}

	public PluginLauncher invokedPhase(Phase invokedPhase) {
		this.invokedPhase = invokedPhase;
		return this;
	}

	protected Configuration configuration() {
		return moduleConfiguration;
	}

	protected File moduleDirectory() {
		return moduleDirectory;
	}

	protected SystemProperties systemProperties() {
		return systemProperties;
	}

	protected ModuleStructure moduleStructure() {
		return moduleStructure;
	}

	protected PrintStream logger() {
		return log;
	}

	protected Notifier notifier() {
		return notifier;
	}

	public PluginLauncher notifier(Notifier notifier) {
		this.notifier = notifier;
		return this;
	}

	public enum Phase {
		COMPILE, PACKAGE, INSTALL, DISTRIBUTE, DEPLOY
	}

	public interface Notifier {
		public void notify(String text);

		public void notifyError(String text);
	}

	public static class SystemProperties {
		public File mavenHome;
		public File javaHome;

		public SystemProperties(File mavenHome, File javaHome) {
			this.mavenHome = mavenHome;
			this.javaHome = javaHome;
		}
	}

	public static class ModuleStructure {
		public List<File> sourceDirectories;
		public List<File> resDirectories;
		public File outDirectory;

		public ModuleStructure(List<File> sourceDirectories, List<File> resDirectories, File outDirectory) {
			this.sourceDirectories = sourceDirectories;
			this.resDirectories = resDirectories;
			this.outDirectory = outDirectory;
		}
	}
}
