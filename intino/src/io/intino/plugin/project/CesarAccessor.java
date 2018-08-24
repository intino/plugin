package io.intino.plugin.project;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.cesar.box.CesarRestAccessor;
import io.intino.cesar.box.schemas.*;
import io.intino.konos.alexandria.exceptions.BadRequest;
import io.intino.konos.alexandria.exceptions.Unknown;
import io.intino.plugin.IntinoException;
import io.intino.plugin.file.cesar.CesarFileType;
import org.siani.itrules.model.Frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static io.intino.plugin.settings.IntinoSettings.getSafeInstance;

public class CesarAccessor {
	private static final Logger LOG = Logger.getInstance(CesarAccessor.class.getName());
	private final Project project;

	public CesarAccessor(Project project) {
		this.project = project;
	}

	public ProjectInfo projectInfo() {
		try {
			ProjectInfo info = null;
			if (accessor() == null || (info = projectExists()) == null) return info;
			String text = loadText(info);
			writeCesarConfiguration(text);
			return info;
		} catch (IOException ignored) {
			return null;
		}
	}

	private void writeCesarConfiguration(String text) throws IOException {
		Path file = getProjectFile();
		Files.write(file, text.getBytes());
		final VirtualFile ioFile = VfsUtil.findFileByIoFile(file.toFile(), true);
		if (ioFile != null) {
			final Application application = ApplicationManager.getApplication();
			if (application.isDispatchThread()) setReadOnly(ioFile);
			else application.invokeLater(() -> setReadOnly(ioFile));
		}
	}

	private void setReadOnly(VirtualFile ioFile) {
		FileDocumentManager.getInstance().reloadFiles(ioFile);
		final Document document = FileDocumentManager.getInstance().getDocument(ioFile);
		if (document != null) document.setReadOnly(true);
	}

	private String loadText(ProjectInfo project) {
		final CesarRestAccessor accessor = accessor();
		return textFrom(project, accessor);
	}

	private ProjectInfo projectExists() {
		try {
			final CesarRestAccessor accessor = accessor();
			if (accessor == null) return null;
			return accessor.getProject(this.project.getName());
		} catch (BadRequest | Unknown e) {
			return null;
		}
	}

	private String textFrom(ProjectInfo project, CesarRestAccessor accessor) {
		return CesarFileTemplate.create().format(new Frame("project").addSlot("name", project.name()).
				addSlot("servers", project.serverInfos().size()).
				addSlot("devices", project.deviceInfos().size()).
				addSlot("server", toServersFrames(project.serverInfos(), accessor)).
				addSlot("device", toDevicesFrames(project.deviceInfos())).
				addSlot("process", toSystemsFrames(project.processInfos())));
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
			frame.addSlot("serverCpu", new Frame().addSlot("usage", status.cpu()).addSlot("size", server.diskSize()));
			frame.addSlot("serverMemory", new Frame().addSlot("used", status.memory()).addSlot("size", server.memorySize()));
			frame.addSlot("fileSystem", new Frame().addSlot("size", server.diskSize()).addSlot("used", status.hdd()));
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
		return new File(this.project.getBasePath(), CesarFileType.CESAR_FILE).toPath();
	}

	public CesarRestAccessor accessor() {
		try {
			final Map.Entry<String, String> cesar = getSafeInstance(this.project).cesar();
			return new CesarRestAccessor(urlOf(cesar.getKey()), cesar.getValue());
		} catch (IntinoException e) {
			return null;
		}
	}
}
