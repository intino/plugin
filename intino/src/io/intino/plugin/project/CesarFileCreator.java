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
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.file.cesar.CesarFileType;

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
		return new CesarFileTemplate().render(new FrameBuilder("project").add("name", project.name()).
				add("servers", project.serverInfos().size()).
				add("devices", project.deviceInfos().size()).
				add("server", toServersFrames(project.serverInfos(), accessor)).
				add("device", toDevicesFrames(project.deviceInfos())).
				add("process", toSystemsFrames(project.processInfos())).toFrame());
	}

	private Frame[] toServersFrames(List<ServerInfo> serverInfos, CesarRestAccessor accessor) {
		return serverInfos.stream().map(s -> {
			final FrameBuilder builder = new FrameBuilder("server").
					add("name", s.id()).
					add("id", s.id()).
					add("status", s.active()).
					add("architecture", s.architecture()).
					add("cores", s.cores()).
					add("jvm", s.jvm()).
					add("os", s.os()).
					add("ip", s.ip());
			fillStatusServer(builder, s, accessor);
			return builder.toFrame();
		}).toArray(Frame[]::new);
	}

	private void fillStatusServer(FrameBuilder builder, ServerInfo server, CesarRestAccessor accessor) {
		try {
			final ServerStatus status = accessor.getServerStatus(server.id());
			if (status.bootTime() == null) return;
			builder.add("boot", status.bootTime());
			builder.add("serverCpu", new FrameBuilder().add("usage", status.cpu()).add("size", server.diskSize()).toFrame());
			builder.add("serverMemory", new FrameBuilder().add("used", status.memory()).add("size", server.memorySize()).toFrame());
			builder.add("fileSystem", new FrameBuilder().add("size", server.diskSize()).add("used", status.hdd()).toFrame());
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
