package org.siani.legio.plugin.project;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import org.siani.legio.LegioApplication;
import org.siani.legio.Project;
import org.siani.legio.Project.Repositories.Release;
import org.siani.legio.Project.Repositories.Snapshot;
import org.siani.legio.plugin.dependencyresolution.DependencyResolver;
import org.siani.legio.plugin.dependencyresolution.LanguageResolver;
import tara.StashBuilder;
import tara.intellij.project.configuration.Configuration;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class LegioConfiguration implements Configuration {

	private static final String CONFIGURATION_LEGIO = "configuration.legio";
	private final Module module;
	private PsiFile legioConf;
	private LegioApplication legio;

	public LegioConfiguration(Module module) {
		this.module = module;
	}

	@Override
	public Configuration init() {
		legioConf = new LegioModuleCreator(module).create();
		reloadInfo();
		return this;
	}

	@Override
	public boolean isSuitable() {
		return new File(new File(module.getModuleFilePath()).getParentFile(), CONFIGURATION_LEGIO).exists();
	}

	@Override
	public void reload() {
		final NotificationGroup balloon = NotificationGroup.toolWindowGroup("Tara Language", "Balloon");
		balloon.createNotification("Configuration has changed", "<a href=\"#\">Reload Configuration</a>", NotificationType.INFORMATION, (n, e) -> reloadInfo()).setImportant(true).notify(module.getProject());
	}

	private void reloadInfo() {
		reloadGraph();
		reloadDependencies();
	}

	private void reloadGraph() {
		legio = GraphLoader.loadGraph(module, new StashBuilder(new File(legioConf.getVirtualFile().getPath()), "Legio", "1.0.0", module.getName()).build());
	}

	private void reloadDependencies() {
		new DependencyResolver(module, legio.project().repositories(), legio.project().dependencies()).resolve();
		new LanguageResolver(module, legio.project().repositories(), legio.project().factory()).resolve();
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
	public List<String> repository() {
		return legio.project().repositories().releaseList().stream().
				map(Release::url).collect(Collectors.toList());
	}

	@Override
	public List<String> snapshotRepository() {
		return legio.project().repositories().snapshotList().stream().
				map(Snapshot::url).collect(Collectors.toList());
	}

	@Override
	public String dsl() {
		return safe(() -> legio.project().factory().modeling().language());
	}

	public boolean isImportedDsl() {
		return false;
	}

	@Override
	public String outDSL() {
		return safe(() -> legio.project().factory().modeling().language(), dsl());
	}

	@Override
	public String dslVersion() {
		return safe(() -> legio.project().factory().modeling().version());
	}

	@Override
	public void dslVersion(String version) {
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

	private interface StringWrapper {
		String value();
	}

	private interface BooleanWrapper {
		boolean value();
	}
}
