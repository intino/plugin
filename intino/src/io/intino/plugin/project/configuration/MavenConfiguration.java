package io.intino.plugin.project.configuration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import io.intino.Configuration;
import io.intino.plugin.project.configuration.maven.MavenHelper;
import io.intino.plugin.project.configuration.maven.MavenTags;
import io.intino.plugin.project.configuration.maven.ModuleMavenCreator;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.util.Collections;
import java.util.List;

public class MavenConfiguration implements Configuration {
	private final Module module;
	private final MavenProject maven;
	private final MavenHelper mavenHelper;

	public MavenConfiguration(Module module) {
		this.module = module;
		maven = MavenProjectsManager.getInstance(module.getProject()).findProject(module);
		this.mavenHelper = new MavenHelper(module);
	}

	@Override
	public Configuration init() {
		ModuleMavenCreator mavenizer = new ModuleMavenCreator(module);
		if (module.getProject().isInitialized()) mavenizer.mavenize();
		else startWithMaven(mavenizer, module.getProject());
		return this;
	}

	private void startWithMaven(final ModuleMavenCreator mavenizer, Project project) {
		StartupManager.getInstance(project).runAfterOpened(mavenizer::mavenize);
	}

	@Override
	public boolean isSuitable() {
		return MavenProjectsManager.getInstance(module.getProject()).findProject(module) != null;
	}

	@Override
	public void reload() {
		final MavenProjectsManager manager = MavenProjectsManager.getInstance(module.getProject());
		final MavenProject project = manager.findProject(module);
		manager.forceUpdateProjects(Collections.singletonList(project));
	}

	@Override
	public Artifact artifact() {
		return new Artifact() {
			@Override
			public String groupId() {
				return null;
			}

			@Override
			public String name() {
				return null;
			}

			@Override
			public void name(String newName) {

			}

			@Override
			public String version() {
				return null;
			}

			@Override
			public String description() {
				return null;
			}

			@Override
			public String url() {
				return null;
			}

			@Override
			public void version(String newVersion) {

			}

			@Override
			public Code code() {
				return new Code() {
					@Override
					public String generationPackage() {
						return maven.getProperties().getProperty(MavenTags.INTINO_GENERATION_PACKAGE);
					}

					@Override
					public String nativeLanguage() {
						return null;
					}

					@Override
					public Configuration root() {
						return null;
					}

					@Override
					public ConfigurationNode owner() {
						return null;
					}
				};
			}

			public Artifact.Model model() {
				return new Artifact.Model() {
					@Override
					public Configuration root() {
						return null;
					}

					@Override
					public ConfigurationNode owner() {
						return null;
					}

					@Override
					public Artifact.Model.Language language() {
						return new Artifact.Model.Language() {
							@Override
							public String name() {
								return maven == null ? null : maven.getProperties().getProperty(MavenTags.DSL);
							}

							@Override
							public String version() {
								return maven == null ? "" : maven.getProperties().getProperty(MavenTags.DSL_VERSION);
							}

							@Override
							public String effectiveVersion() {
								return maven == null ? "" : maven.getProperties().getProperty(MavenTags.DSL_VERSION);
							}

							@Override
							public void effectiveVersion(String version) {

							}

							@Override
							public void version(String version) {
								new MavenHelper(module).dslVersion(mavenHelper.dslMavenId(module, name()), version);//TODO
							}

							@Override
							public String generationPackage() {
								return name();
							}
						};
					}

					@Override
					public String outLanguage() {
						if (maven == null) return null;
						final String outDSL = maven.getProperties().getProperty(MavenTags.OUT_DSL);
						return outDSL != null ? outDSL : "";
					}

					@Override
					public String outLanguageVersion() {
						if (maven == null) return null;
						final String outDSL = maven.getProperties().getProperty(MavenTags.OUT_DSL_VERSION);
						return outDSL != null ? outDSL : "";
					}

					@Override
					public Level level() {
						if (maven == null) return null;
						final String property = maven.getProperties().getProperty(MavenTags.LEVEL);
						return property == null ? Level.Platform : Level.valueOf(property);
					}

					@Override
					public String sdkVersion() {
						return null;
					}

					@Override
					public String sdk() {
						return null;
					}

					@Override
					public List<ExcludedPhases> excludedPhases() {
						return List.of();
					}
				};
			}

			@Override
			public Box box() {
				return null;
			}

			@Override
			public Dependency.DataHub datahub() {
				return null;
			}

			@Override
			public Dependency.Archetype archetype() {
				return null;
			}

			@Override
			public List<Dependency> dependencies() {
				return null;
			}

			@Override
			public List<WebComponent> webComponents() {
				return null;
			}

			@Override
			public List<WebResolution> webResolutions() {
				return null;
			}

			@Override
			public List<WebArtifact> webArtifacts() {
				return null;
			}

			@Override
			public List<Plugin> plugins() {
				return null;
			}

			@Override
			public License license() {
				return null;
			}

			@Override
			public Scm scm() {
				return null;
			}

			@Override
			public List<Developer> developers() {
				return Collections.emptyList();
			}

			@Override
			public QualityAnalytics qualityAnalytics() {
				return null;
			}

			@Override
			public List<Parameter> parameters() {
				return null;
			}

			@Override
			public Package packageConfiguration() {
				return null;
			}

			@Override
			public Distribution distribution() {
				return null;
			}

			@Override
			public Configuration root() {
				return null;
			}

			@Override
			public ConfigurationNode owner() {
				return null;
			}
		};
	}

	@Override
	public List<Server> servers() {
		return null;
	}

	@Override
	public List<RunConfiguration> runConfigurations() {
		return null;
	}

	@Override
	public List<Repository> repositories() {
		return Collections.emptyList();
	}
}
