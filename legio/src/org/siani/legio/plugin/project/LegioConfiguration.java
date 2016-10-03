package org.siani.legio.plugin.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.siani.legio.LegioApplication;
import org.siani.legio.Project;
import org.siani.legio.Project.Repositories.Language;
import org.siani.legio.Project.Repositories.Release;
import org.siani.legio.Project.Repositories.Repository;
import org.siani.legio.Project.Repositories.Snapshot;
import org.siani.legio.plugin.dependencyresolution.DependencyResolver;
import org.siani.legio.plugin.dependencyresolution.LanguageResolver;
import org.siani.legio.plugin.dependencyresolution.LibraryManager;
import tara.StashBuilder;
import tara.intellij.lang.psi.TaraModel;
import tara.intellij.project.configuration.Configuration;
import tara.lang.model.Node;
import tara.lang.model.Parameter;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LegioConfiguration implements Configuration {

	private static final String CONFIGURATION_LEGIO = "configuration.legio";
	private final Module module;
	private TaraModel legioConf;
	private LegioApplication legio;

	public LegioConfiguration(Module module) {
		this.module = module;
	}

	@Override
	public Configuration init() {
		legioConf = (TaraModel) new LegioModuleCreator(module).create();
		reloadInfo(null);
		return this;
	}

	@Override
	public boolean isSuitable() {
		return new File(new File(module.getModuleFilePath()).getParentFile(), CONFIGURATION_LEGIO).exists();
	}

	@Override
	public void reload() {
		final NotificationGroup balloon = NotificationGroup.toolWindowGroup("Tara Language", "Balloon");
		balloon.createNotification("Configuration has changed", "<a href=\"#\">Reload Configuration</a>", NotificationType.INFORMATION, (n, e) -> reloadInfo(n)).setImportant(true).notify(module.getProject());
	}

	private void reloadInfo(Notification notification) {
		reloadGraph();
		reloadDependencies();
		if (notification != null) {
			notification.expire();
			notification.hideBalloon();
		}
	}

	private void reloadGraph() {
		legio = GraphLoader.loadGraph(module, new StashBuilder(new File(legioConf.getVirtualFile().getPath()), "Legio", "1.0.0", module.getName()).build());
	}

	private void reloadDependencies() {
		final List<Library> newLibraries = new DependencyResolver(module, legio.project().repositories(), legio.project().dependencies()).resolve();
		newLibraries.addAll(new LanguageResolver(module, legio.project().repositories(), legio.project().factory()).resolve());
		LibraryManager.removeOldLibraries(module, newLibraries);
	}

	@Override
	public ModuleType type() {
		if (legio == null) return null;
		final Project.Factory factory = legio.project().factory();
		if (factory == null) return null;
		final String level = factory.node().conceptList().stream().filter(c -> c.id().contains("#")).map(c -> c.id().split("#")[0]).findFirst().orElse("Platform");
		return ModuleType.valueOf(level);
	}

	@Override
	public String artifactId() {
		return safe(() -> legio.project().name());
	}

	@Override
	public String groupId() {
		return safe(() -> legio.project().groupId());
	}

	@Override
	public String workingPackage() {
		return safe(() -> legio.project().factory().generationPackage(), dsl());
	}

	@Override
	public List<String> repositories() {
		return legio.project().repositories().repositoryList().stream().
				map(Repository::url).collect(Collectors.toList());
	}

	public List<String> releaseRepositories() {
		return legio.project().repositories().releaseList().stream().
				map(Release::url).collect(Collectors.toList());
	}

	@Override
	public List<String> snapshotRepositories() {
		return legio.project().repositories().snapshotList().stream().
				map(Snapshot::url).collect(Collectors.toList());
	}

	@Override
	public String languageRepository() {
		return legio.project().repositories().languageList().stream().
				map(Language::url).findFirst().orElse(null);

	}

	@Override
	public String dsl() {
		return safe(() -> legio.project().factory().modeling().language());
	}

	@Override
	public String outDSL() {
		return safe(() -> legio.project().name());
	}

	@Override
	public String dslVersion() {
		return safe(() -> legio.project().factory().modeling().version());
	}

	@Override
	public void dslVersion(String version) {
		new WriteCommandAction(legioConf.getProject(), legioConf) {
			@Override
			protected void run(@NotNull Result result) throws Throwable {
				legio.project().factory().modeling().version(version);
				final Node factory = legioConf.components().get(0).components().stream().filter(f -> f.type().equals("Project.Factory")).findFirst().orElse(null);
				if (factory == null) return;
				final Node modeling = factory.components().stream().filter(f -> f.type().equals(f.type())).findFirst().orElse(null);
				if (modeling == null) return;
				final Parameter versionParameter = modeling.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
				versionParameter.substituteValues(Collections.singletonList(version));
			}
		}.execute();
		reload();
	}

	@Override
	public String modelVersion() {
		return safe(() -> legio.project().version());
	}

	@Override
	public void modelVersion(String s) {

		reload();
	}

	@Override
	public int refactorId() {
		return 0;
	}

	@Override
	public void refactorId(int i) {

		reload();
	}

	@Override
	public boolean isPersistent() {
		return safe(() -> legio.project().factory().persistent());
	}

	private String safe(StringWrapper wrapper) {
		return safe(wrapper, "");
	}

	private boolean safe(BooleanWrapper wrapper) {
		try {
			return wrapper.value();
		} catch (NullPointerException e) {
			return false;
		}
	}

	private String safe(StringWrapper wrapper, String defaultValue) {
		try {
			return wrapper.value();
		} catch (NullPointerException e) {
			return defaultValue;
		}
	}

	public List<Project.Dependencies.Compile> dependencies() {
		return legio.project().dependencies().compileList();
	}

	public List<Repository> legioRepositories() {
		return legio.project().repositories().repositoryList();
	}

	private interface StringWrapper {
		String value();
	}

	private interface BooleanWrapper {
		boolean value();
	}
}
