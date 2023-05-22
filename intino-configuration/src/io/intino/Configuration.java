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

		String description();

		String url();

		void version(String newVersion);

		Artifact.Code code();

		Artifact.Model model();

		Artifact.Box box();

		Artifact.Dependency.DataHub datahub();

		Artifact.Dependency.Archetype archetype();

		List<Artifact.Dependency> dependencies();

		List<Artifact.WebComponent> webComponents();

		List<Artifact.WebResolution> webResolutions();

		List<Artifact.WebArtifact> webArtifacts();

		List<Plugin> plugins();

		License license();

		Artifact.Scm scm();

		List<Developer> developers();

		Artifact.QualityAnalytics qualityAnalytics();

		List<Parameter> parameters();

		Package packageConfiguration();

		Distribution distribution();

		default List<Deployment> deployments() {
			return Collections.emptyList();
		}

		interface QualityAnalytics {
			String url();

			String token();
		}

		interface Model extends ConfigurationNode {

			enum Level {
				Solution, Product, Platform;

				public int compareLevelWith(Level type) {
					return type.ordinal() - this.ordinal();
				}

				public boolean is(Level type, int level) {
					return type.ordinal() == level;
				}

				public boolean isSolution() {
					return Solution.equals(this);
				}

				public boolean isProduct() {
					return Product.equals(this);
				}

				public boolean isPlatform() {
					return Platform.equals(this);
				}
			}

			enum ExcludedPhases {
				ExcludeFrameworkCode, ExcludeLanguageCode
			}

			Language language();

			String outLanguage();

			String outLanguageVersion();

			Level level();

			String sdkVersion();

			String sdk();

			List<ExcludedPhases> excludedPhases();

			interface Language {

				String name();

				String version();

				String effectiveVersion();

				void effectiveVersion(String version);

				void version(String version);

				String generationPackage();
			}
		}

		interface Code extends ConfigurationNode {
			String generationPackage();

			default String webDirectory() {
				return "lib";
			}

			default String modelPackage() {
				return "model";
			}

			default String boxPackage() {
				return "box";
			}

			String nativeLanguage();
		}

		interface Box extends ConfigurationNode {
			String language();

			String version();

			String effectiveVersion();

			void effectiveVersion(String version);

			String targetPackage();
		}

		interface Dependency {
			default String identifier() {
				return groupId() + ":" + artifactId() + ":" + version();
			}

			default String name() {
				return "Intino: " + identifier();
			}

			String groupId();

			String artifactId();

			String version();

			void version(String newVersion);

			String scope();

			List<Exclude> excludes();

			String effectiveVersion();

			void effectiveVersion(String version);

			boolean transitive();

			boolean resolved();

			void resolved(boolean resolved);

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

		interface Plugin {
			enum Phase {
				Export, PostCompilation, PrePackage, PostPackage, PostDistribution
			}

			String artifact();

			String pluginClass();

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

}