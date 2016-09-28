package org.siani.legio.plugin.project;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import org.siani.legio.LegioApplication;
import org.siani.legio.Repository;
import org.siani.legio.SnapshotRepository;
import tara.StashBuilder;
import tara.intellij.project.configuration.Configuration;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static tara.intellij.lang.LanguageManager.reloadLanguage;

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
		balloon.createNotification("Configuration has changed", "<a href=\"#\">Reload Configuration</a>", NotificationType.INFORMATION, (n, e) -> {
			reloadInfo();
		}).setImportant(true).notify(module.getProject());
	}

	private void reloadInfo() {
		reloadGraph();
		reloadLanguage(this.module.getProject(), legio.project().dSL().name());
		reloadDependencies();
	}

	private void reloadGraph() {
		legio = GraphLoader.loadGraph(module, new StashBuilder(new File(legioConf.getVirtualFile().getPath()), "Legio", module.getName()).build());
	}

	private void reloadDependencies() {
		new DependencyResolver(module, legio.project().dependenciesList()).resolve();
	}

	@Override
	public ModuleType type() {
		if (legio == null) return null;
		final String level = legio.project().node().conceptList().stream().filter(c -> c.id().contains("#")).map(c -> c.id().split("#")[0]).findFirst().orElse("Platform");
		return ModuleType.valueOf(level);
	}

	@Override
	public String workingPackage() {
		return safe(() -> legio.project().generation().workingPackage(), dsl());
	}

	@Override
	public List<String> repository() {
		return legio.project().repositoryList().stream().
			filter(repository -> !repository.is(SnapshotRepository.class)).
			map(Repository::url).collect(Collectors.toList());
	}

	@Override
	public List<String> snapshotRepository() {
		return legio.project().repositoryList().stream().
			filter(repository -> repository.is(SnapshotRepository.class)).
			map(Repository::url).collect(Collectors.toList());
	}

	@Override
	public String dsl() {
		return safe(() -> legio.project().dSL().name());
	}

	@Override
	public boolean isImportedDsl() {
		return false;
	}

	@Override
	public String outDSL() {
		return safe(() -> legio.project().generation().outDSL().name(), dsl());
	}

	@Override
	public String dslVersion() {
		return safe(() -> legio.project().dSL().version());
	}

	@Override
	public void dslVersion(String version) {
		reload();
	}

	@Override
	public String modelVersion() {
		return safe(() -> legio.project().generation().outDSL().version());
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
		return false;
	}

	private String safe(StringWrapper wrapper) {
		return safe(wrapper, "");
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
