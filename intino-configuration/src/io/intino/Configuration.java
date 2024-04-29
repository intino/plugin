package io.intino;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Configuration {

	default Configuration init() {
		return null;
	}

	default boolean isSuitable() {
		return false;
	}

	default boolean isReloading() {
		return false;
	}

	default void reload() {
	}

	Artifact artifact();

	List<Server> servers();

	List<RunConfiguration> runConfigurations();

	List<Repository> repositories();

	interface Artifact extends ConfigurationNode {
		String groupId();

		String name();

		void name(String newName);

		String version();

		void version(String newVersion);

		String description();

		String url();

		License license();

		Artifact.Scm scm();

		List<Developer> developers();

		List<Dsl> dsls();

		default Dsl dsl(String name) {
			return dsls().stream().filter(d -> d.name().equalsIgnoreCase(name)).findFirst().orElse(null);
		}

		Artifact.Dependency.DataHub datahub();

		Artifact.Dependency.Archetype archetype();

		List<Artifact.Dependency> dependencies();

		List<Artifact.WebComponent> webComponents();

		List<Artifact.WebResolution> webResolutions();

		List<Artifact.WebArtifact> webArtifacts();

		Artifact.Code code();

		List<Plugin> plugins();

		Artifact.QualityAnalytics qualityAnalytics();

		Package packageConfiguration();

		Distribution distribution();

		List<Parameter> parameters();

		default List<Deployment> deployments() {
			return Collections.emptyList();
		}

		interface QualityAnalytics {
			String url();

			String token();
		}

		interface Dsl extends ConfigurationNode {
			enum Level {
				Model, MetaModel, MetaMetaModel;

				public int compareLevelWith(Level type) {
					return type.ordinal() - this.ordinal();
				}

				public boolean is(Level type, int level) {
					return type.ordinal() == level;
				}

				public boolean isModel() {
					return Model.equals(this);
				}

				public boolean isMetaModel() {
					return MetaModel.equals(this);
				}

				public boolean isMetaMetaModel() {
					return MetaMetaModel.equals(this);
				}
			}

			String groupId();

			default String artifactId() {
				return name();
			}

			String name();

			String version();

			Level level();

			String generationPackage();

			String effectiveVersion();

			void effectiveVersion(String version);

			void version(String version);

			Builder builder();

			Runtime runtime();

			OutputDsl outputDsl();

			interface OutputDsl extends ConfigurationNode {

				String name();

				String version();

				OutputBuilder builder();

				Runtime runtime();
			}

			interface Builder extends Library {
				String generationPackage();

				List<ExcludedPhases> excludedPhases();

				enum ExcludedPhases {
					ExcludeCodeBaseGeneration, ExcludeLanguageGeneration
				}
			}

			interface OutputBuilder extends Library {
			}

			interface Runtime extends Library {
			}
		}

		interface Code extends ConfigurationNode {
			String generationPackage();

			default String webDirectory() {
				return "lib";
			}

			String nativeLanguage();
		}

		interface Dependency extends Library {
			default String identifier() {
				return groupId() + ":" + artifactId() + ":" + version();
			}

			default String name() {
				return "Intino: " + identifier();
			}


			void version(String newVersion);

			String scope();

			List<Exclude> excludes();

			String effectiveVersion();

			void effectiveVersion(String version);

			boolean transitive();

			boolean toModule();

			void toModule(boolean toModule);

			interface Exclude {
				String groupId();

				String artifactId();
			}

			interface Compile extends Dependency {
			}

			interface Test extends Dependency {
			}

			interface Runtime extends Dependency {
			}

			interface Provided extends Dependency {
			}

			interface DataHub extends Dependency {
			}

			interface Archetype extends Dependency {
			}

			interface Web extends Dependency {
			}
		}

		interface License {
			enum LicenseType {
				GLP, BSD, LGPL
			}

			LicenseType type();
		}

		interface Developer {
			String name();

			String email();

			String organization();

			String organizationUrl();
		}

		interface WebComponent {
			String name();

			String version();
		}

		interface WebResolution {
			String name();

			String version();
		}

		interface WebArtifact {
			String name();

			default String identifier() {
				return groupId() + ":" + artifactId() + ":" + version();
			}

			String groupId();

			String artifactId();

			String version();
		}

		interface Plugin extends Library {
			enum Phase {
				Export, PostCompilation, PrePackage, PostPackage, PostDistribution
			}

			default String identifier() {
				return groupId() + ":" + artifactId() + ":" + version();
			}

			default String name() {
				return "Intino: " + identifier();
			}


			default Phase phase() {
				return Phase.PrePackage;
			}
		}

		interface Package {
			LinuxService linuxService();

			enum Mode {
				ModulesAndLibrariesExtracted, LibrariesLinkedByManifest, ModulesAndLibrariesLinkedByManifest
			}

			Mode mode();

			boolean isRunnable();

			boolean createMavenPom();

			boolean attachSources();

			List<String> mavenPlugins();

			boolean attachDoc();

			boolean includeTests();

			boolean signArtifactWithGpg();

			String classpathPrefix();

			String finalName();

			String defaultJVMOptions();

			String mainClass();

			MacOs macOsConfiguration();

			Windows windowsConfiguration();

			interface MacOs {
				String icon();

				String resourceDirectory();
			}

			interface Windows {
				String icon();
			}

			interface LinuxService {
				String user();

				RunConfiguration runConfiguration();

				boolean restartOnFailure();

				int managementPort();
			}
		}

		interface Scm {
			String url();

			String connection();

			String developerConnection();

			String tag();
		}
	}

	interface Distribution {
		Repository release();

		Repository snapshot();

		BitBucketDistribution onBitbucket();

		boolean distributeLanguage();

		interface BitBucketDistribution {
			String owner();

			String slugName();
		}
	}

	interface Deployment {
		Server server();

		RunConfiguration runConfiguration();

		List<String> bugTrackingUsers();

		Requirements requirements();


		interface Requirements {
			int minHdd();

			int minMemory();

			int maxMemory();

			String jvmVersion();

			Sync sync();

			interface Sync {
				Map<String, String> moduleServer();
			}
		}
	}

	interface WebDependency {

	}

	interface RunConfiguration extends ConfigurationNode {
		String name();

		String mainClass();

		String vmOptions();

		List<Argument> arguments();

		default Map<String, String> finalArguments() {
			final Map<String, String> arguments = new HashMap<>();
			arguments().forEach(a -> arguments.put(a.name(), a.value()));
			final Artifact artifact = root().artifact();
			artifact.parameters().stream()
					.filter(p -> !arguments.containsKey(p.name()))
					.filter(p -> p.value() != null)
					.forEach(p -> arguments.put(p.name(), p.value()));
			return arguments;
		}

		interface Argument extends Parameter {
			String name();

			String value();

			default String description() {
				return "";
			}
		}
	}

	interface Repository extends ConfigurationNode {
		String identifier();

		String url();

		String user();

		String password();

		UpdatePolicy updatePolicy();

		enum UpdatePolicy {Always, Daily, Never}

		interface Release extends Repository {
		}

		interface Snapshot extends Repository {
		}
	}

	interface Server {
		enum Type {
			Dev, Pre, Pro, Demo
		}

		String name();

		Type type();
	}

	interface ConfigurationNode {
		Configuration root();

		ConfigurationNode owner();
	}

	interface Parameter extends ConfigurationNode {
		String name();

		String value();

		String description();
	}

	interface Library extends ConfigurationNode {
		String groupId();

		String artifactId();

		String version();

		void version(String version);
	}
}