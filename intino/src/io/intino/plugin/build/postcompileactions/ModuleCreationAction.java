package io.intino.plugin.build.postcompileactions;

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import io.intino.Configuration;
import io.intino.Configuration.Repository;
import io.intino.alexandria.logger.Logger;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.ConfigurationManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static io.intino.plugin.archetype.Formatters.camelCaseToSnakeCase;
import static io.intino.plugin.project.configuration.ConfigurationManager.newExternalProvider;
import static java.util.Arrays.stream;


public class ModuleCreationAction extends PostCompileAction {
	private static final String LegioArtifact = "artifact.legio";

	private final String webModule;
	private final String uiVersion;
	private final ArtifactLegioConfiguration configuration;

	public ModuleCreationAction(Module module, List<String> parameters) {
		this(module, parameters.get(1), parameters.size() > 2 ? parameters.get(2) : "LATEST");
	}

	public ModuleCreationAction(Module module, String webModule, String uiVersion) {
		super(module);
		this.configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf(module);
		this.webModule = webModule;
		this.uiVersion = uiVersion;
	}

	@Override
	public FinishStatus execute() {
		Module webPsiModule = webModule();
		final boolean[] reload = {false};
		Application application = ApplicationManager.getApplication();
		application.invokeAndWait(() -> application.runWriteAction(() -> {
			if (webPsiModule != null) {
				Configuration webConf = IntinoUtil.configurationOf(webPsiModule);
				checkVersion(webConf);
				addWebDependency(webConf);
				reload[0] = true;
				return;
			}
			final ModuleManager manager = ModuleManager.getInstance(module.getProject());
			Module webModule = manager.newModule(moduleImlFilename(), WebModuleType.WEB_MODULE);
			final ModifiableRootModel model = ModuleRootManager.getInstance(webModule).getModifiableModel();
			final File moduleRoot = IntinoUtil.moduleRoot(webModule);
			moduleRoot.mkdirs();
			VirtualFile file = VfsUtil.findFile(moduleRoot.toPath(), true);
			if (file != null) model.addContentEntry(file);
			model.commit();
			boolean created = false;
			try {
				created = createConfigurationFile(moduleRoot);
			} catch (IOException e) {
				Logger.error(e);
			}
			if (created) addWebDependency(ConfigurationManager.register(webModule, newExternalProvider(webModule)));
		}));
		return reload[0] ? FinishStatus.RequiresReload : FinishStatus.NothingDone;
	}

	private void checkVersion(Configuration webConf) {
		String version = configuration.artifact().version();
		if (!webConf.artifact().version().equals(version)) webConf.artifact().version(version);
	}

	private boolean createConfigurationFile(File moduleRoot) throws IOException {
		Configuration.Artifact artifact = configuration.artifact();
		FrameBuilder builder = new FrameBuilder("artifact", "legio");
		builder.add("groupId", artifact.groupId());
		builder.add("artifactId", camelCaseToSnakeCase().format(webModule).toString());
		builder.add("version", artifact.version());
		builder.add("uiversion", uiVersion);
		final List<Repository> repositories = configuration.repositories().stream().filter(r -> r instanceof Repository.Release).toList();
		for (Repository repository : repositories)
			builder.add("repository", new FrameBuilder("repository", "release").add("id", repository.identifier()).add("url", repository.url()));
		File file = new File(moduleRoot, LegioArtifact);
		if (!file.exists()) {
			Files.write(file.toPath(), new ArtifactTemplate().render(builder).getBytes());
			return true;
		}
		return false;
	}

	private void addWebDependency(Configuration webConf) {
		for (Configuration.Artifact.Dependency.Web dep : configuration.artifact().webDependencies())
			if (dep.groupId().equals(webConf.artifact().groupId()) && dep.artifactId().equals(webConf.artifact().name())) {
				if (!dep.version().equals(webConf.artifact().version())) dep.version(webConf.artifact().version());
				return;
			}
		configuration.artifact().addDependencies(webDependency());
		ToolWindowManager.getInstance(module.getProject()).getToolWindow("Intino Console").show(null);
		webConf.reload();
	}

	@NotNull
	private Configuration.Artifact.Dependency.Web webDependency() {
		return new Configuration.Artifact.Dependency.Web() {

			@Override
			public String groupId() {
				return configuration.artifact().groupId();
			}

			@Override
			public String artifactId() {
				return camelCaseToSnakeCase().format(webModule).toString();
			}

			@Override
			public String version() {
				return configuration.artifact().version();
			}

			@Override
			public void version(String newVersion) {

			}

			@Override
			public String scope() {
				return "Web";
			}

			@Override
			public boolean toModule() {
				return true;
			}

			@Override
			public List<Exclude> excludes() {
				return Collections.emptyList();
			}

			@Override
			public String effectiveVersion() {
				return null;
			}

			@Override
			public boolean transitive() {
				return false;
			}

			@Override
			public Configuration root() {
				return null;
			}

			@Override
			public Configuration.ConfigurationNode owner() {
				return null;
			}

			@Override
			public void effectiveVersion(String s) {

			}

			@Override
			public void toModule(boolean b) {

			}
		};
	}

	private Module webModule() {
		return ApplicationManager.getApplication().
				runReadAction((Computable<Module>) () ->
						stream(ModuleManager.getInstance(module.getProject()).getModules()).filter(m -> m.getName().equals(toSnakeCase(webModule)) && IntinoUtil.configurationOf(m) != null).findFirst().orElse(null));
	}

	private String moduleImlFilename() {
		return module.getProject().getBasePath() + File.separator + toSnakeCase(webModule) + File.separator + toSnakeCase(webModule) + ModuleFileType.DOT_DEFAULT_EXTENSION;
	}

	private String toSnakeCase(String name) {
		return (String) camelCaseToSnakeCase().format(name);
	}
}
