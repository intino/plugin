package io.intino.builder;

import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dsl.Level;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class CompilerConfiguration {
	private static final Logger LOG = Logger.getGlobal();

	static {
		Logger.getGlobal().setLevel(java.util.logging.Level.INFO);
		LOG.setUseParentHandlers(false);
		for (Handler handler : LOG.getHandlers()) LOG.removeHandler(handler);
		final StreamHandler errorHandler = new StreamHandler(System.err, new SimpleFormatter());
		errorHandler.setLevel(java.util.logging.Level.WARNING);
		LOG.addHandler(errorHandler);
		final StreamHandler infoHandler = new StreamHandler(System.out, new SimpleFormatter());
		infoHandler.setLevel(java.util.logging.Level.INFO);
		LOG.addHandler(infoHandler);
	}


	public enum Phase {
		COMPILE, PACKAGE, INSTALL, DISTRIBUTE;
	}

	private int warningLevel;
	private String sourceEncoding;
	private String project;
	private String module;
	private String parentInterface;
	private boolean debug;
	private final List<File> sources = new ArrayList<>();
	private File srcDirectory;
	private File genDirectory;
	private File resDirectory;
	private File outDirectory;
	private String groupID;
	private String artifactID;
	private String version;
	private Phase phase;
	private final ModelConfiguration model;
	private boolean verbose;
	private File tempDirectory;
	private File datahubLibrary;
	private List<String> currentDependencies;
	private File intinoProjectDirectory;
	private String generationPackage;
	private PrintStream out = System.out;
	private File projectDirectory;
	private File webModuleDirectory;
	private File archetypeLibrary;
	private File serviceDirectory;
	private File configurationDirectory;
	private List<String> parameters = new ArrayList<>();
	private final List<Configuration.Repository> repositories = new ArrayList<>();
	private Configuration.Repository releaseDistributionRepository;
	private Configuration.Repository snapshotDistributionRepository;

	public CompilerConfiguration() {
		setWarningLevel(1);
		setDebug(false);
		String encoding;
		encoding = System.getProperty("file.encoding", "UTF8");
		encoding = System.getProperty("tara.source.encoding", encoding);
		sourceEncoding(encoding);
		this.model = new ModelConfiguration();
		try {
			tempDirectory = Files.createTempDirectory("_intino_").toFile();
		} catch (IOException e) {
			LOG.log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	public int getWarningLevel() {
		return this.warningLevel;
	}

	public void setWarningLevel(int level) {
		if ((level < 0) || (level > 3)) {
			this.warningLevel = 1;
		} else this.warningLevel = level;
	}

	public String sourceEncoding() {
		return this.sourceEncoding;
	}

	public void sourceEncoding(String encoding) {
		if (encoding == null) sourceEncoding = "UTF8";
		this.sourceEncoding = encoding;
	}


	public Phase phase() {
		return phase;
	}

	public CompilerConfiguration phase(Phase phase) {
		this.phase = phase;
		return this;
	}

	public void addRepository(Configuration.Repository repository) {
		this.repositories.add(repository);
	}

	public List<Configuration.Repository> repositories() {
		return repositories;
	}

	public boolean getDebug() {
		return this.debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public File getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public String project() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String groupId() {
		return groupID;
	}

	public void groupId(String groupID) {
		this.groupID = groupID;
	}

	public String artifactId() {
		return artifactID;
	}

	public void artifactId(String artifactID) {
		this.artifactID = artifactID;
	}

	public String version() {
		return version;
	}

	public void version(String version) {
		this.version = version;
	}

	public void generationPackage(String generationPackage) {
		this.generationPackage = generationPackage;
	}

	public String generationPackage() {
		return generationPackage;
	}

	public String module() {
		return module;
	}

	public void module(String module) {
		this.module = module;
	}

	public ModelConfiguration model() {
		return model;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void intinoProjectDirectory(File intinoPath) {
		this.intinoProjectDirectory = intinoPath;
	}

	public File intinoProjectDirectory() {
		return intinoProjectDirectory;
	}

	public List<File> sources() {
		return sources;
	}

	public File srcDirectory() {
		return srcDirectory;
	}

	public void srcDirectory(File srcDirectory) {
		this.srcDirectory = srcDirectory;
	}

	public void genDirectory(File directory) {
		if (directory != null) {
			this.genDirectory = directory;
			this.genDirectory.mkdirs();
		}
	}

	public File genDirectory() {
		return genDirectory;
	}

	public File serviceDirectory() {
		return webModuleDirectory;
	}

	public File moduleDirectory() {
		return serviceDirectory;
	}

	public File projectDirectory() {
		return projectDirectory;
	}

	public void serviceDirectory(File serviceDirectory) {
		this.webModuleDirectory = serviceDirectory;
	}

	public void moduleDirectory(File moduleDirectory) {
		this.serviceDirectory = moduleDirectory;
	}

	public void projectDirectory(File projectDirectory) {
		this.projectDirectory = projectDirectory;
	}

	public File configurationDirectory() {
		return configurationDirectory;
	}

	public File resDirectory() {
		return resDirectory;
	}

	public void resDirectory(File resDirectory) {
		this.resDirectory = resDirectory;
	}

	public File outDirectory() {
		return this.outDirectory;
	}

	public PrintStream out() {
		return out;
	}

	public void out(PrintStream out) {
		this.out = out;
	}

	public String parentInterface() {
		return parentInterface;
	}

	public void parentInterface(String parentInterface) {
		this.parentInterface = parentInterface;
	}

	public File datahubLibrary() {
		return datahubLibrary;
	}

	public void datahubLibrary(File datahubLibrary) {
		this.datahubLibrary = datahubLibrary;
	}

	public File archetypeLibrary() {
		return archetypeLibrary;
	}

	public void archetypeLibrary(File archetypeLibrary) {
		this.archetypeLibrary = archetypeLibrary;
	}

	public void parameters(String[] parameters) {
		this.parameters = List.of(parameters);
	}

	public List<String> parameters() {
		return parameters;
	}

	public void outDirectory(File file) {
		this.outDirectory = file;
	}


	public List<String> currentDependencies() {
		return this.currentDependencies == null ? Collections.emptyList() : currentDependencies;
	}

	public CompilerConfiguration currentDependencies(List<String> currentDependencies) {
		this.currentDependencies = currentDependencies;
		return this;
	}

	public Configuration.Repository releaseDistributionRepository() {
		return this.releaseDistributionRepository;
	}

	public CompilerConfiguration releaseDistributionRepository(Configuration.Repository repository) {
		this.releaseDistributionRepository = repository;
		return this;
	}

	public Configuration.Repository snapshotDistributionRepository() {
		return this.snapshotDistributionRepository;
	}


	public CompilerConfiguration snapshotDistributionRepository(Configuration.Repository repository) {
		this.snapshotDistributionRepository = repository;
		return this;
	}

	public static class ModelConfiguration {

		private String language;
		private Level level;
		private String outDsl;
		private String generationPackage;


		public String language() {
			return language;
		}

		public void outDsl(String outDsl) {
			this.outDsl = outDsl;
		}

		public String outDsl() {
			return outDsl;
		}

		public void language(String language) {
			this.language = language;
		}

		public void level(Level level) {
			this.level = level;
		}

		public Level level() {
			return level;
		}

		public String generationPackage() {
			return generationPackage;
		}

		public void generationPackage(String generationPackage) {
			this.generationPackage = generationPackage;
		}
	}
}
