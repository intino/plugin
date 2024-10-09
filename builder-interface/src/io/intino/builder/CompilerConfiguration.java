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

	public static final String LANGUAGE_PACKAGE = "tara.dsl";
	private int warningLevel;
	private String sourceEncoding;
	private String project;
	private String module;
	private boolean test;
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
	private final DslConfiguration dsl;
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
	private BuildConstants.Mode mode = BuildConstants.Mode.Build;
	private final List<Configuration.Repository> repositories = new ArrayList<>();
	private Configuration.Repository releaseDistributionRepository;
	private Configuration.Repository snapshotDistributionRepository;
	private File localRepository;
	private Phase invokedBuildPhase;
	private List<Integer> excludedInternalSteps = new ArrayList<>();

	public CompilerConfiguration() {
		setWarningLevel(1);
		setDebug(false);
		String encoding = System.getProperty("file.encoding", "UTF8");
		encoding = System.getProperty("tara.source.encoding", encoding);
		sourceEncoding(encoding);
		this.dsl = new DslConfiguration();
		this.localRepository = new File(System.getProperty("user.home") + String.join(File.separator, ".m2", "repository"));
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

	public Phase invokedPhase() {
		return invokedBuildPhase;
	}

	public CompilerConfiguration invokedPhase(Phase phase) {
		this.invokedBuildPhase = phase;
		return this;
	}

	public List<Integer> excludedInternalSteps() {
		return excludedInternalSteps;
	}

	public void addExcludedInternalSteps(List<Integer> steps) {
		excludedInternalSteps.addAll(steps);
	}

	public void addRepository(Configuration.Repository repository) {
		this.repositories.add(repository);
	}

	public List<Configuration.Repository> repositories() {
		return repositories;
	}

	public File localRepository() {
		return localRepository;
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

	public CompilerConfiguration localRepository(File localRepository) {
		this.localRepository = localRepository;
		return this;
	}

	public boolean test() {
		return test;
	}

	public void test(boolean b) {
		this.test = b;
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

	public DslConfiguration dsl() {
		return dsl;
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

	public File rulesDirectory() {
		final String rulesPackage = (generationPackage() == null ? module.toLowerCase() : generationPackage().toLowerCase().replace(".", File.separator)) + File.separator + "rules";
		final File file = new File(srcDirectory, rulesPackage);
		if (file.exists()) return file;
		return null;
	}

	public File functionsDirectory() {
		final String functionsPackage = (generationPackage() == null ? module.toLowerCase() : generationPackage().toLowerCase().replace(".", File.separator)) + File.separator + "functions";
		final File file = new File(srcDirectory, functionsPackage);
		if (file.exists()) return file;
		return null;
	}

	@Deprecated
	public File webModuleDirectory() {
		return webModuleDirectory;
	}

	public File moduleDirectory() {
		return serviceDirectory;
	}

	public File projectDirectory() {
		return projectDirectory;
	}

	@Deprecated
	public void webModuleDirectory(File serviceDirectory) {
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

	public CompilerConfiguration configurationDirectory(File configurationDirectory) {
		this.configurationDirectory = configurationDirectory;
		return this;
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

	public void mode(String mode) {
		this.mode = BuildConstants.Mode.valueOf(mode);
	}

	public BuildConstants.Mode mode() {
		return mode;
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

	public static class DslConfiguration {
		private String name;
		private Level level;
		private String outDsl;
		private final Library runtime = new Library();
		private final Library builder = new Library();
		private String generationPackage;
		private String version;

		public String name() {
			return name;
		}

		public void outDsl(String outDsl) {
			this.outDsl = outDsl;
		}

		public String outDsl() {
			return outDsl;
		}

		public DslConfiguration name(String dsl) {
			this.name = dsl;
			return this;
		}

		public DslConfiguration version(String version) {
			this.version = version;
			return this;
		}

		public Library runtime() {
			return runtime;
		}

		public Library builder() {
			return builder;
		}

		public String version() {
			return version;
		}

		@Deprecated
		public void level(Level level) {
			this.level = level;
		}

		@Deprecated
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

	@Override
	public CompilerConfiguration clone() {
		try {
			return (CompilerConfiguration) super.clone();
		} catch (CloneNotSupportedException e) {
			LOG.info(e.getMessage());
			return null;
		}
	}

	public static class Library {
		private String groupId;
		private String artifactId;
		private String version;

		public String groupId() {
			return groupId;
		}

		public String artifactId() {
			return artifactId;
		}

		public String version() {
			return version;
		}

		public Library groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public Library artifactId(String artifactId) {
			this.artifactId = artifactId;
			return this;
		}

		public Library version(String version) {
			this.version = version;
			return this;
		}
	}

}
