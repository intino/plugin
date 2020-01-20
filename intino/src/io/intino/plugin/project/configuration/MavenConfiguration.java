package io.intino.plugin.project.configuration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import io.intino.plugin.project.configuration.maven.MavenHelper;
import io.intino.plugin.project.configuration.maven.ModuleMavenCreator;
import io.intino.tara.compiler.shared.Configuration;
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
		StartupManager.getInstance(project).registerPostStartupActivity(mavenizer::mavenize);
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
		return null;
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
		return null;
	}

//	@Override
//	public Model model() {
//		return new Model() {
//			@Override
//			public Model.Language language() {
//				return new Model.Language() {
//					@Override
//					public String name() {
//						return maven.getProperties().getProperty(MavenTags.DSL);
//					}
//
//					@Override
//					public String version() {
//						return maven == null ? "" : maven.getProperties().getProperty(MavenTags.DSL_VERSION);
//					}
//
//					@Override
//					public String effectiveVersion() {
//						return maven == null ? "" : maven.getProperties().getProperty(MavenTags.DSL_VERSION);
//					}
//
//					@Override
//					public void version(String version) {
//						new MavenHelper(module).dslVersion(mavenHelper.dslMavenId(module, name()), version);//TODO
//					}
//
//					@Override
//					public String generationPackage() {
//						return name();
//					}
//				};
//			}
//
//			@Override
//			public String outLanguage() {
//				final String outDSL = maven.getProperties().getProperty(MavenTags.OUT_DSL);
//				return outDSL != null ? outDSL : "";
//			}
//
//			@Override
//			public String outLanguageVersion() {
//				return version();
//			}
//
//			@Override
//			public Level level() {
//				final String property = maven.getProperties().getProperty(MavenTags.LEVEL);
//				return property == null ? Level.Platform : Level.valueOf(property);
//			}
//		};
//	}
//
//	@Override
//	public Box box() {
//		return new Box() {
//			@Override
//			public String language() {
//				return maven.getProperties().getProperty(MavenTags.INTERFACE_NAME);
//			}
//
//			@Override
//			public String version() {
//				return maven.getProperties().getProperty(MavenTags.INTERFACE_VERSION);
//			}
//
//			@Override
//			public String effectiveVersion() {
//				return version();
//			}
//
//			@Override
//			public String targetPackage() {
//				return "box";
//			}
//		};
//	}
//
//	@Override
//	public Map<String, String> releaseRepositories() {
//		return maven.getRemoteRepositories().stream().
//				filter(repository -> repository.getSnapshotsPolicy() == null).
//				collect(Collectors.toMap(MavenRemoteRepository::getUrl, MavenRemoteRepository::getId));
//	}
//
//	@Override
//	public String snapshotRepository() {
//		return maven.getRemoteRepositories().stream().
//				filter(repository -> repository.getSnapshotsPolicy() != null).
//				map(MavenRemoteRepository::getUrl).findFirst().orElse("");
//	}
//
//	@Override
//	public Map<String, String> languageRepositories() {
//		return maven.getRemoteRepositories().stream().
//				filter(repository -> repository.getSnapshotsPolicy() == null).
//				collect(Collectors.toMap(MavenRemoteRepository::getUrl, MavenRemoteRepository::getId));
//	}
//
//
//	@Override
//	public String version() {
//		return maven.getMavenId().getVersion();
//	}
//
//	@Override
//	public void version(String newVersion) {
//		mavenHelper.version(newVersion);
//	}

}
