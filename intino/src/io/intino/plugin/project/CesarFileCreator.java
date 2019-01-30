package io.intino.plugin.project;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Unknown;
import io.intino.cesar.box.CesarRestAccessor;
import io.intino.cesar.box.schemas.*;
import io.intino.plugin.file.cesar.CesarFileType;
import org.siani.itrules.model.Frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CesarFileCreator {
	private static final Logger LOG = Logger.getInstance(CesarAccessor.class.getName());

	private Project project;

	public CesarFileCreator(Project project) {
		this.project = project;
	}

	public void writeCesarConfiguration(ProjectInfo info) {
		if (info == null) return;
		Path file = getProjectFile();
		try {
			Files.write(file, loadText(info).getBytes());
			final VirtualFile ioFile = VfsUtil.findFileByIoFile(file.toFile(), true);
			if (ioFile != null) {
				final Application application = ApplicationManager.getApplication();
				if (application.isDispatchThread()) setReadOnly(ioFile);
				else application.invokeLater(() -> setReadOnly(ioFile));
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private String loadText(ProjectInfo project) {
		final CesarRestAccessor accessor = new CesarAccessor(this.project).accessor();
		return textFrom(project, accessor);
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
			if (status.bootTime() == null) return;
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


	private void setReadOnly(VirtualFile ioFile) {
		FileDocumentManager.getInstance().reloadFiles(ioFile);
		final Document document = FileDocumentManager.getInstance().getDocument(ioFile);
		if (document != null) document.setReadOnly(true);
	}
}
