package io.intino.builder;

import io.intino.Configuration;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static io.intino.builder.BuildConstants.*;
import static java.io.File.separator;

public class CompilationInfoExtractor {
	private static final Logger LOG = Logger.getGlobal();

	public static void getInfoFromArgsFile(File argsFile, CompilerConfiguration configuration, Map<File, Boolean> srcFiles) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(argsFile)));
			processArgs(configuration, reader, readSrc(srcFiles, SRC_FILE, reader));
		} catch (IOException e) {
			LOG.log(java.util.logging.Level.SEVERE, "Error getting Args IO: " + e.getMessage(), e);
		} finally {
			try {
				assert reader != null;
				reader.close();
			} catch (IOException e) {
				LOG.log(java.util.logging.Level.SEVERE, "Error getting Args IO2: " + e.getMessage(), e);
			}
		}
	}

	private static String readSrc(Map<File, Boolean> srcFiles, String type, BufferedReader reader) throws IOException {
		String line;
		while (!"".equals(line = reader.readLine())) {
			if (type.equals(line)) continue;
			final String[] split = line.split("#");
			final File file = new File(split[0]);
			srcFiles.put(file, Boolean.valueOf(split[1]));
		}
		return line;
	}

	private static void processArgs(CompilerConfiguration configuration, BufferedReader reader, String line) throws IOException {
		String aLine = line;
		while (aLine != null) {
			processLine(configuration, reader, aLine);
			aLine = reader.readLine();
		}
	}

	private static void processLine(CompilerConfiguration configuration, BufferedReader reader, String aLine) throws IOException {
		switch (aLine) {
			case ENCODING:
				configuration.sourceEncoding(reader.readLine());
				break;
			case OUTPUTPATH:
				configuration.genDirectory(new File(reader.readLine()));
				break;
			case FINAL_OUTPUTPATH:
				configuration.outDirectory(new File(reader.readLine()));
				break;
			case SRC_PATH:
				configuration.srcDirectory(new File(reader.readLine()));
				break;
			case RES_PATH:
				configuration.resDirectory(new File(reader.readLine()));
				break;
			case PROJECT:
				configuration.setProject(reader.readLine());
				break;
			case MODULE:
				configuration.module(reader.readLine());
				break;
			case DSL:
				configuration.dsl().name(reader.readLine());
				break;
			case DSL_GENERATION_PACKAGE:
				configuration.dsl().generationPackage(reader.readLine());
				break;
			case PARENT_INTERFACE:
				configuration.parentInterface(reader.readLine());
				break;
			case DATAHUB:
				configuration.datahubLibrary(findLibraryInRepository(reader.readLine()));
				break;
			case CURRENT_DEPENDENCIES:
				configuration.currentDependencies(Arrays.asList(reader.readLine().split(",")));
				break;
			case SNAPSHOT_IMPORT:
				configuration.addRepository(snapshotConfOf(reader.readLine().split("#")));
				break;
			case RELEASE_IMPORT:
				configuration.addRepository(releaseConfOf(reader.readLine().split("#")));
				break;
			case SNAPSHOT_DISTRIBUTION:
				configuration.snapshotDistributionRepository(snapshotConfOf(reader.readLine().split("#")));
				break;
			case RELEASE_DISTRIBUTION:
				configuration.releaseDistributionRepository(releaseConfOf(reader.readLine().split("#")));
				break;
			case ARCHETYPE:
				configuration.archetypeLibrary(findLibraryInRepository(reader.readLine()));
				break;
			case PROJECT_PATH:
				configuration.projectDirectory(new File(reader.readLine()));
				break;
			case MODULE_PATH:
				configuration.moduleDirectory(new File(reader.readLine()));
				break;
			case LEVEL:
				configuration.dsl().level(Configuration.Artifact.Dsl.Level.valueOf(reader.readLine()));
				break;
			case GROUP_ID:
				configuration.groupId(reader.readLine());
				break;
			case ARTIFACT_ID:
				configuration.artifactId(reader.readLine());
				break;
			case VERSION:
				configuration.version(reader.readLine());
				break;
			case PARAMETERS:
				configuration.parameters(reader.readLine().split(";"));
				break;
			case INVOKED_PHASE:
				configuration.invokedPhase(io.intino.builder.CompilerConfiguration.Phase.valueOf(reader.readLine()));
				break;
			case GENERATION_PACKAGE:
				configuration.generationPackage(reader.readLine());
				break;
			case SRC_FILE:
				readSrcPaths(configuration.sources(), reader);
				break;
			case INTINO_PROJECT_PATH:
				configuration.intinoProjectDirectory(new File(reader.readLine()));
				break;
			case COMPILATION_MODE:
				configuration.mode(reader.readLine());
				break;
			default:
				break;
		}
	}

	private static Configuration.Repository releaseConfOf(String[] split) {
		return new Configuration.Repository.Release() {
			@Override
			public String identifier() {
				return split[0];
			}

			@Override
			public String url() {
				return split[1];
			}

			@Override
			public String user() {
				return null;
			}

			@Override
			public String password() {
				return null;
			}

			@Override
			public UpdatePolicy updatePolicy() {
				return null;
			}

			@Override
			public Configuration root() {
				return null;
			}

			@Override
			public Configuration.ConfigurationNode owner() {
				return null;
			}
		};
	}

	private static Configuration.Repository snapshotConfOf(String[] split) {
		return new Configuration.Repository.Snapshot() {
			@Override
			public String identifier() {
				return split[0];
			}

			@Override
			public String url() {
				return split[1];
			}

			@Override
			public String user() {
				return null;
			}

			@Override
			public String password() {
				return null;
			}

			@Override
			public UpdatePolicy updatePolicy() {
				return null;
			}

			@Override
			public Configuration root() {
				return null;
			}

			@Override
			public Configuration.ConfigurationNode owner() {
				return null;
			}
		};
	}

	private static File findLibraryInRepository(String library) {
		String[] split = library.split(":");
		File directory = new File(System.getProperty("user.home") + separator + ".m2" + separator + "repository" +
								  separator + split[0].replace(".", separator) + separator + split[1] + separator + split[2]);
		return new File(directory, split[1] + "-" + split[2] + ".jar");
	}

	private static void readSrcPaths(List<File> srcPaths, BufferedReader reader) throws IOException {
		String line;
		while (!"".equals(line = reader.readLine()))
			srcPaths.add(new File(line));
	}
}
