package io.intino.plugin.project;

import com.intellij.execution.RunManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.cesar.CesarRestAccessor;
import io.intino.cesar.schemas.*;
import io.intino.konos.alexandria.exceptions.BadRequest;
import io.intino.konos.alexandria.exceptions.Unknown;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.LegioGraph;
import io.intino.legio.graph.Repository;
import io.intino.legio.graph.RunConfiguration;
import io.intino.legio.graph.level.LevelArtifact;
import io.intino.plugin.IntinoException;
import io.intino.plugin.dependencyresolution.JavaDependencyResolver;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.dependencyresolution.LibraryManager;
import io.intino.plugin.dependencyresolution.WebDependencyResolver;
import io.intino.plugin.file.cesar.CesarFileType;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.plugin.project.run.IntinoRunConfiguration;
import org.siani.itrules.model.Frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static io.intino.plugin.project.LegioConfiguration.parametersOf;
import static io.intino.plugin.project.LibraryConflictResolver.libraryOf;
import static io.intino.plugin.project.LibraryConflictResolver.mustAdd;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static io.intino.plugin.settings.IntinoSettings.getSafeInstance;

public class ConfigurationReloader {
	private static final Logger LOG = Logger.getInstance(GulpExecutor.class.getName());

	private Module module;
	private final LegioGraph graph;

	public ConfigurationReloader(Module module, LegioGraph graph) {
		this.module = module;
		this.graph = graph;
	}

	void reloadInterfaceBuilder() {
		final Artifact.Box boxing = safe(() -> graph.artifact().box());
		if (boxing != null) new InterfaceBuilderManager().reload(module.getProject(), boxing.sdk());
	}

	void reloadRunConfigurations() {
		@SuppressWarnings("Convert2MethodRef") final List<RunConfiguration> runConfigurations = safeList(() -> graph.runConfigurationList());
		for (RunConfiguration runConfiguration : runConfigurations) {
			ApplicationConfiguration configuration = findRunConfiguration(runConfiguration.name$());
			if (configuration != null) configuration.setProgramParameters(parametersOf(runConfiguration));
		}
	}

	void reloadArtifactoriesMetaData() {
		new ArtifactorySensor(repositories()).update();
	}

	void reloadDependencies() {
		if (graph == null || graph.artifact() == null) return;
		resolveJavaDependencies();
		if (WebModuleType.isWebModule(module) && graph.artifact().webImports() != null)
			new WebDependencyResolver(module, graph.artifact(), repositories()).resolve();
	}

	private void resolveJavaDependencies() {
		if (safeList(() -> graph.artifact().imports().dependencyList()) == null) return;
		final JavaDependencyResolver resolver = new JavaDependencyResolver(module, repositories(), safeList(() -> graph.artifact().imports().dependencyList()));
		final List<Library> newLibraries = resolver.resolve();
		final List<Library> languageLibraries = resolveLanguages();
		for (Library languageLibrary : languageLibraries)
			if (mustAdd(languageLibrary, newLibraries)) replace(newLibraries, languageLibrary);
		LibraryManager.clean(module, newLibraries);
	}

	private void replace(List<Library> newLibraries, Library languageLibrary) {
		newLibraries.remove(libraryOf(newLibraries, languageLibrary.getName()));
		newLibraries.add(languageLibrary);
	}

	List<Library> resolveLanguages() {
		List<Library> libraries = new ArrayList<>();
		LevelArtifact.Model model = safe(() -> graph.artifact().asLevel().model());
		if (model == null) return libraries;
		final String effectiveVersion = model.effectiveVersion();
		String version = effectiveVersion == null || effectiveVersion.isEmpty() ? model.version() : effectiveVersion;
		libraries.addAll(new LanguageResolver(module, repositories(), model, version).resolve());
		return libraries;
	}

	private ApplicationConfiguration findRunConfiguration(String name) {
		final List<com.intellij.execution.configurations.RunConfiguration> list = RunManager.getInstance(module.getProject()).
				getAllConfigurationsList().stream().filter(r -> r instanceof IntinoRunConfiguration).collect(Collectors.toList());
		return (ApplicationConfiguration) list.stream().filter(r -> (r.getName()).equalsIgnoreCase(graph.artifact().name$().toLowerCase() + "-" + name)).findFirst().orElse(null);
	}

	public List<Repository.Type> repositories() {
		List<Repository.Type> repos = new ArrayList<>();
		if (graph == null) return Collections.emptyList();
		safeList(graph::repositoryList).stream().map(Repository::typeList).forEach(repos::addAll);
		return repos;
	}

	void reloadCesar() {
		try {
			if (graph == null || graph.serverList() == null) return;
			if (graph.serverList().isEmpty() || accessor() == null || !projectExists()) return;
			String text = loadText();
			writeCesarConfiguration(text);
		} catch (IOException ignored) {
		}
	}

	private void writeCesarConfiguration(String text) throws IOException {
		Path file = getProjectFile();
		Files.write(file, text.getBytes());
		final VirtualFile ioFile = VfsUtil.findFileByIoFile(file.toFile(), true);
		if (ioFile != null) FileDocumentManager.getInstance().reloadFiles(ioFile);
	}

	private String loadText() {
		try {
			final CesarRestAccessor accessor = accessor();

			ProjectInfo project = accessor.getProject(module.getProject().getName());
			if (project == null)
				return "Impossible to retrieve information from cesar. Check your configuration or internet connection";
			return textFrom(project, accessor);
		} catch (Unknown | BadRequest e) {
			return "Impossible to retrieve information from cesar. Check your configuration or internet connection";
		}
	}

	private boolean projectExists() {
		try {
			final CesarRestAccessor accessor = accessor();
			accessor.getProject(this.module.getProject().getName());
		} catch (BadRequest | Unknown e) {
			return false;
		}
		return false;
	}

	private String textFrom(ProjectInfo project, CesarRestAccessor accessor) {
		return CesarFileTemplate.create().format(new Frame("project").addSlot("name", project.name()).
				addSlot("servers", project.serverInfoList().size()).
				addSlot("devices", project.deviceInfoList().size()).
				addSlot("server", toServersFrames(project.serverInfoList(), accessor)).
				addSlot("device", toDevicesFrames(project.deviceInfoList())).
				addSlot("process", toSystemsFrames(project.processInfoList())));
	}

	private Frame[] toServersFrames(List<ServerInfo> serverInfos, CesarRestAccessor accessor) {
		return serverInfos.stream().map(s -> {
			final Frame frame = new Frame("server").
					addSlot("name", s.id()).
					addSlot("id", s.id()).
					addSlot("status", s.active()).
					addSlot("architecture", s.architecture()).
					addSlot("cores", s.cores()).
					addSlot("jvm", s.jvm()).
					addSlot("os", s.os()).
					addSlot("ip", s.ip());
			fillStatusServer(frame, s, accessor);
			return frame;
		}).toArray(Frame[]::new);
	}

	private void fillStatusServer(Frame frame, ServerInfo server, CesarRestAccessor accessor) {
		try {
			final ServerStatus status = accessor.getServerStatus(server.id());
			frame.addSlot("boot", status.bootTime());
			frame.addSlot("serverCpu", new Frame().addSlot("usage", status.cpu()).addSlot("size", server.hddSize()));
			frame.addSlot("serverMemory", new Frame().addSlot("used", status.memory()).addSlot("size", server.memorySize()));
			frame.addSlot("fileSystem", new Frame().addSlot("size", server.hddSize()).addSlot("used", status.hdd()));
		} catch (BadRequest | Unknown badRequest) {
			LOG.error(badRequest.getMessage());
		}
	}

	private Frame[] toDevicesFrames(List<DeviceInfo> deviceInfos) {
		return new Frame[0];
	}

	private Frame[] toSystemsFrames(List<ProcessInfo> processInfos) {
		return new Frame[0];
	}

	private Path getProjectFile() {
		return new File(module.getProject().getBasePath(), CesarFileType.CESAR_FILE).toPath();
	}

	private CesarRestAccessor accessor() {
		try {
			final Map.Entry<String, String> cesar = getSafeInstance(module.getProject()).cesar();
			return new CesarRestAccessor(urlOf(cesar.getKey()), cesar.getValue());
		} catch (IntinoException e) {
			return null;
		}
	}
}